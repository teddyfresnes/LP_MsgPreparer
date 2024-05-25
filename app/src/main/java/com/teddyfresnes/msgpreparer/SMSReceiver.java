package com.teddyfresnes.msgpreparer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SMSReceiver extends BroadcastReceiver {

    private static final String TAG = "SMSReceiver";
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private List<Message> messageList;
    private static final long MESSAGE_EXPIRATION_TIME = 1000; // 5 sec
    private static final Map<String, Long> processedMessages = new HashMap<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive() called with action: " + intent.getAction());
        if (intent.getAction().equals(SMS_RECEIVED)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    Log.d(TAG, "PDUs not null, processing SMS");
                    StringBuilder fullMessage = new StringBuilder();
                    String sender = null;

                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        sender = smsMessage.getDisplayOriginatingAddress();
                        fullMessage.append(smsMessage.getMessageBody());
                    }

                    String messageBody = fullMessage.toString();
                    String messageId = sender + messageBody.hashCode(); // Unique identifier for the message using hash code
                    long currentTime = System.currentTimeMillis();

                    //  clean up old messages
                    cleanUpOldMessages(currentTime);

                    synchronized (processedMessages) {
                        if (processedMessages.containsKey(messageId)) {
                            Log.d(TAG, "Message already processed, skipping.");
                            return;
                        }

                        processedMessages.put(messageId, currentTime);
                    }

                    Log.d(TAG, "SMS reçu de " + sender + ": " + messageBody);
                    Toast.makeText(context, "SMS reçu de " + sender + ": " + messageBody, Toast.LENGTH_LONG).show();

                    loadMessages(context);

                    SharedPreferences sharedPreferences = context.getSharedPreferences("checkbox_states", Context.MODE_PRIVATE);
                    boolean isAutoReplyChecked = sharedPreferences.getBoolean("auto_reply_checked", false);
                    Log.d(TAG, "Auto-reply est coché: " + isAutoReplyChecked);

                    if (isAutoReplyChecked) {
                        if (isContactChecked(sharedPreferences, sender)) {
                            Log.d(TAG, "Contact " + sender + " est coché pour la réponse automatique");
                            Message autoReplyMessage = getAutoReplyMessage();
                            if (autoReplyMessage != null) {
                                Log.d(TAG, "Message de réponse automatique trouvé: " + autoReplyMessage.getText());
                                sendSMS(sender, autoReplyMessage.getText());
                                Log.d(TAG, "SMS de réponse automatique envoyé à " + sender);
                            } else {
                                Log.d(TAG, "Aucun message de réponse automatique trouvé.");
                            }
                        } else {
                            Log.d(TAG, "Contact " + sender + " non coché pour la réponse automatique.");
                        }
                    } else {
                        Log.d(TAG, "Auto-reply n'est pas coché");
                    }
                } else {
                    Log.d(TAG, "No PDUs found");
                }
            } else {
                Log.d(TAG, "Bundle is null");
            }
        } else {
            Log.d(TAG, "Received unexpected action: " + intent.getAction());
        }
    }

    private boolean isContactChecked(SharedPreferences sharedPreferences, String sender) {
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (String key : allEntries.keySet()) {
            if (key.startsWith("checkbox_")) {
                boolean isChecked = sharedPreferences.getBoolean(key, false);
                Log.d(TAG, "Clé: " + key + ", isChecked: " + isChecked);
                if (isChecked) {
                    String contactInfo = key.substring("checkbox_".length());
                    Log.d(TAG, "Vérification de la clé: " + key + ", contactInfo: " + contactInfo);
                    if (contactInfo.equals(sender)) {
                        Log.d(TAG, "Contact " + sender + " trouvé dans les préférences");
                        return true;
                    }
                }
            }
        }
        Log.d(TAG, "Contact " + sender + " non trouvé dans les préférences");
        return false;
    }


    private void loadMessages(Context context) {
        messageList = new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences("message_preferences", Context.MODE_PRIVATE);
        SharedPreferences checkboxStates = context.getSharedPreferences("checkbox_states", Context.MODE_PRIVATE);
        int messageCount = sharedPreferences.getInt("message_count", 0);

        Log.d("LoadMessages", "Message count: " + messageCount);

        int autoReplyCheckedPosition = checkboxStates.getInt("auto_reply_checked_position", 0);
        int spamCheckedPosition = checkboxStates.getInt("spam_checked_position", 1);

        Log.d("LoadMessages", "Auto Reply Checked Position: " + autoReplyCheckedPosition);
        Log.d("LoadMessages", "Spam Checked Position: " + spamCheckedPosition);

        for (int i = 0; i < messageCount; i++) {
            String messageText = sharedPreferences.getString("message_" + i, "");
            Log.d("LoadMessages", "Message " + i + ": " + messageText);
            if (!messageText.isEmpty()) {
                // Utiliser les positions des cases cochées pour déterminer si chaque message est marqué comme spam ou autoreply
                boolean isAutoReply = i == autoReplyCheckedPosition;
                boolean isSpam = i == spamCheckedPosition;
                messageList.add(new Message(messageText, isAutoReply, isSpam));
            }
        }

        if (messageList == null) {
            Log.e("LoadMessages", "messageList is null after loading messages.");
        }
    }

    private Message getSpamMessage() {
        if (messageList != null) {
            for (Message message : messageList) {
                if (message.isSpam()) {
                    return message;
                }
            }
        }
        return null; // Aucun message spam trouvé
    }

    private Message getAutoReplyMessage() {
        if (messageList != null) {
            for (Message message : messageList) {
                if (message.isAutoReply()) {
                    return message;
                }
            }
        }
        return null; // Aucun message autoreply trouvé
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }

    private void cleanUpOldMessages(long currentTime) {
        synchronized (processedMessages) {
            processedMessages.entrySet().removeIf(entry -> (currentTime - entry.getValue()) > MESSAGE_EXPIRATION_TIME);
        }
    }
}
