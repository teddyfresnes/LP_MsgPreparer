package com.teddyfresnes.msgpreparer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

public class ScheduledMessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String phoneNumber = intent.getStringExtra("phone_number");
        String messageText = intent.getStringExtra("message_text");

        if (phoneNumber != null && messageText != null) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, messageText, null, null);
            } catch (Exception e) {
                Log.e("ScheduledMessageReceiver", "Erreur lors de l'envoi du SMS : " + e.getMessage());
            }
        }
    }
}
