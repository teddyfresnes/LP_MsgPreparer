package com.teddyfresnes.msgpreparer;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;
import android.telephony.SmsManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Fragment_Actions extends Fragment {

    private Spinner contactSpinner;
    private Button spamButton;
    private Button scheduleButton;
    private CheckBox autoReplyCheckBox;
    private List<String> contactNames;
    private List<Message> messageList;

    public Fragment_Actions() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_actions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        contactSpinner = view.findViewById(R.id.spinner_contacts);
        spamButton = view.findViewById(R.id.button_spam_contact);
        scheduleButton = view.findViewById(R.id.button_schedule_message);
        autoReplyCheckBox = view.findViewById(R.id.check_box_auto_reply);

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("checkbox_states", Context.MODE_PRIVATE);
        autoReplyCheckBox.setChecked(sharedPreferences.getBoolean("auto_reply_checked", false));

        loadContactsIntoSpinner();

        autoReplyCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("auto_reply_checked", isChecked);
            editor.apply();
        });

        spamButton.setOnClickListener(v -> {
            loadMessages();
            int selectedPosition = contactSpinner.getSelectedItemPosition();
            if (selectedPosition != AdapterView.INVALID_POSITION && selectedPosition != 0) {
                String selectedContactName = contactNames.get(selectedPosition);
                String phoneNumber = extractPhoneNumber(selectedContactName);
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    Message messageSpam = getSpamMessage();
                    if (messageSpam != null) {
                        sendSMS(phoneNumber, messageSpam.getText());
                        Toast.makeText(requireContext(), messageSpam.getPreview() + " envoyé au numéro : " + phoneNumber, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Aucun message spam trouvé.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Impossible de trouver le numéro de téléphone.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Veuillez sélectionner un contact valide.", Toast.LENGTH_SHORT).show();
            }
        });

        scheduleButton.setOnClickListener(v -> {
            loadMessages();
            int selectedPosition = contactSpinner.getSelectedItemPosition();
            if (selectedPosition != AdapterView.INVALID_POSITION && selectedPosition != 0) {
                String selectedContactName = contactNames.get(selectedPosition);
                String phoneNumber = extractPhoneNumber(selectedContactName);
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    Message messageSpam = getSpamMessage();
                    if (messageSpam != null) {
                        scheduleMessage(phoneNumber, messageSpam.getText());
                    } else {
                        Toast.makeText(requireContext(), "Aucun message spam trouvé.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Impossible de trouver le numéro de téléphone.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "Veuillez sélectionner un contact valide.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String extractPhoneNumber(String contactText) {
        int startIndex = contactText.indexOf("(");
        int endIndex = contactText.lastIndexOf(")");
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return contactText.substring(startIndex + 1, endIndex);
        }
        return null;
    }

    private void loadContactsIntoSpinner() {
        contactNames = new ArrayList<>();
        contactNames.add("Choisir un utilisateur");

        TreeMap<String, HashSet<String>> contactMap = new TreeMap<>();

        Cursor cursor = requireActivity().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int phoneNumberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                if (nameIndex >= 0 && phoneNumberIndex >= 0) {
                    String name = cursor.getString(nameIndex);
                    String phoneNumber = cursor.getString(phoneNumberIndex);

                    String normalizedPhoneNumber = phoneNumber.replaceAll("\\s+", "");

                    if (contactMap.containsKey(name)) {
                        contactMap.get(name).add(normalizedPhoneNumber);
                    } else {
                        HashSet<String> phoneNumbers = new HashSet<>();
                        phoneNumbers.add(normalizedPhoneNumber);
                        contactMap.put(name, phoneNumbers);
                    }
                }
            }
            cursor.close();

            for (Map.Entry<String, HashSet<String>> entry : contactMap.entrySet()) {
                String contactName = entry.getKey();
                HashSet<String> phoneNumbers = entry.getValue();
                for (String phoneNumber : phoneNumbers) {
                    contactNames.add(contactName + " (" + phoneNumber + ")");
                }
            }

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, contactNames);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            contactSpinner.setAdapter(spinnerAdapter);
        }
    }

    private void loadMessages() {
        messageList = new ArrayList<>();
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("message_preferences", Context.MODE_PRIVATE);
        SharedPreferences checkboxStates = requireContext().getSharedPreferences("checkbox_states", Context.MODE_PRIVATE);
        int messageCount = sharedPreferences.getInt("message_count", 0);

        int autoReplyCheckedPosition = checkboxStates.getInt("auto_reply_checked_position", 0);
        int spamCheckedPosition = checkboxStates.getInt("spam_checked_position", 1);

        for (int i = 0; i < messageCount; i++) {
            String messageText = sharedPreferences.getString("message_" + i, "");
            if (!messageText.isEmpty()) {
                boolean isAutoReply = i == autoReplyCheckedPosition;
                boolean isSpam = i == spamCheckedPosition;
                messageList.add(new Message(messageText, isAutoReply, isSpam));
            }
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
        return null;
    }

    public void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }

    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!requireContext().getSystemService(AlarmManager.class).canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }


    private void scheduleMessage(String phoneNumber, String message) {
        Calendar currentCalendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (timeView, hourOfDay, minute) -> {
                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDate.set(Calendar.MINUTE, minute);
                selectedDate.set(Calendar.SECOND, 0);

                long delay = selectedDate.getTimeInMillis() - System.currentTimeMillis();
                if (delay > 0) {
                    scheduleMessageWithDelay(phoneNumber, message, delay);
                } else {
                    Toast.makeText(requireContext(), "La date et l'heure sélectionnées sont déjà passées.", Toast.LENGTH_SHORT).show();
                }

            }, currentCalendar.get(Calendar.HOUR_OF_DAY), currentCalendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();

        }, currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void scheduleMessageWithDelay(String phoneNumber, String message, long delay) {
        if (requireActivity() != null) {
            requestExactAlarmPermission();
            AlarmManager alarmManager = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(requireContext(), ScheduledMessageReceiver.class);
            intent.putExtra("phone_number", phoneNumber);
            intent.putExtra("message_text", message);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pendingIntent);
                Toast.makeText(requireContext(), "Message programmé avec succès.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Erreur lors de la programmation du message.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}