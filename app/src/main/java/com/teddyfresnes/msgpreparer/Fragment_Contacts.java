package com.teddyfresnes.msgpreparer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Comparator;

public class Fragment_Contacts extends Fragment {

    private static final int REQUEST_READ_CONTACTS = 1;

    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;
    private static List<Contact> contactList;

    public Fragment_Contacts() {
        // empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view_contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        contactList = new ArrayList<>();
        contactAdapter = new ContactAdapter(requireContext(), contactList);
        recyclerView.setAdapter(contactAdapter);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        } else {
            loadContacts();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
    }

    // on gere les perms dans le mainactivity
    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    private void loadContacts() {
        Cursor cursor = requireContext().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            HashMap<String, HashSet<String>> contactMap = new HashMap<>();

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

            // convert into unique contact list
            ArrayList<Contact> uniqueContacts = new ArrayList<>();
            for (Map.Entry<String, HashSet<String>> entry : contactMap.entrySet()) {
                String contactName = entry.getKey();
                HashSet<String> phoneNumbers = entry.getValue();
                for (String phoneNumber : phoneNumbers) {
                    uniqueContacts.add(new Contact(contactName, phoneNumber));
                }
            }

            // Sort contact name
            Collections.sort(uniqueContacts, new Comparator<Contact>() {
                @Override
                public int compare(Contact c1, Contact c2) {
                    return c1.getName().compareToIgnoreCase(c2.getName());
                }
            });

            contactList.clear();
            contactList.addAll(uniqueContacts);
            contactAdapter.notifyDataSetChanged();
        }
    }






}
