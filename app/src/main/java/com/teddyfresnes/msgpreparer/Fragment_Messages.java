package com.teddyfresnes.msgpreparer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Fragment_Messages extends Fragment {

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText editTextMessage;
    private Button buttonAddMessage;
    private TextView textViewEmptyList;

    public Fragment_Messages() {
        // Constructeur vide
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view_messages);
        textViewEmptyList = view.findViewById(R.id.text_view_empty_list);
        editTextMessage = view.findViewById(R.id.edit_text_message);
        buttonAddMessage = view.findViewById(R.id.button_add_message);

        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 1));

        messageList = new ArrayList<>();

        loadMessages();

        messageAdapter = new MessageAdapter(requireContext(), messageList);
        recyclerView.setAdapter(messageAdapter);

        buttonAddMessage.setOnClickListener(v -> {
            String messageText = editTextMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                Message newMessage = new Message(messageText, false, false);
                messageList.add(newMessage);
                messageAdapter.notifyDataSetChanged(); // Ajout de cette ligne pour mettre à jour la liste
                saveMessages();
                editTextMessage.setText("");
                updateEmptyListVisibility();
            }
        });

        updateEmptyListVisibility();
    }

    private void loadMessages() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("message_preferences", Context.MODE_PRIVATE);
        int messageCount = sharedPreferences.getInt("message_count", 0);
        for (int i = 0; i < messageCount; i++) {
            String messageText = sharedPreferences.getString("message_" + i, "");
            if (!messageText.isEmpty()) {
                messageList.add(new Message(messageText, false, false));
            }
        }
    }

    private void saveMessages() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("message_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // effacer ancienne sauvegarde
        editor.putInt("message_count", messageList.size());
        for (int i = 0; i < messageList.size(); i++) {
            editor.putString("message_" + i, messageList.get(i).getText());
        }
        editor.apply();
    }

    private void updateEmptyListVisibility() {
        if (messageList.isEmpty()) {
            textViewEmptyList.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            textViewEmptyList.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

}
