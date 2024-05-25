package com.teddyfresnes.msgpreparer;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private List<Message> messageList;
    private int checkedAutoReplyPosition = -1;
    private int checkedSpamPosition = -1;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
        loadCheckedPositions(); // charger positions des cases cochées
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bind(message);

        holder.checkBoxAutoReply.setChecked(checkedAutoReplyPosition == position);
        holder.checkBoxSpam.setChecked(checkedSpamPosition == position);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewMessagePreview;
        ImageView imageViewDelete;
        CheckBox checkBoxAutoReply, checkBoxSpam;
        MessageAdapter messageAdapter;

        ViewHolder(@NonNull View itemView, MessageAdapter adapter) {
            super(itemView);
            textViewMessagePreview = itemView.findViewById(R.id.text_view_message_preview);
            imageViewDelete = itemView.findViewById(R.id.image_view_delete);
            checkBoxAutoReply = itemView.findViewById(R.id.check_box_auto_reply);
            checkBoxSpam = itemView.findViewById(R.id.check_box_spam);
            this.messageAdapter = adapter;
        }

        void bind(Message message) {
            textViewMessagePreview.setText(message.getPreview());

            // gestion case à cocher auto réponse
            checkBoxAutoReply.setOnCheckedChangeListener(null);
            checkBoxAutoReply.setChecked(getAdapterPosition() == messageAdapter.checkedAutoReplyPosition);

            checkBoxAutoReply.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && isChecked) {
                    // uncheck previous checked item
                    if (messageAdapter.checkedAutoReplyPosition != -1 && messageAdapter.checkedAutoReplyPosition != position) {
                        messageAdapter.notifyItemChanged(messageAdapter.checkedAutoReplyPosition);
                    }
                    messageAdapter.checkedAutoReplyPosition = position;
                    messageAdapter.notifyItemChanged(position);

                    SharedPreferences sharedPreferences = itemView.getContext().getSharedPreferences("checkbox_states", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("auto_reply_checked_position", position);
                    editor.apply();
                    Log.d("CheckBoxChange", "Auto Reply Checked position set to " + position);
                // si on décoche la case déjà coché
                } else if (!isChecked && messageAdapter.checkedAutoReplyPosition == position) {
                    messageAdapter.checkedAutoReplyPosition = -1;
                    SharedPreferences sharedPreferences = itemView.getContext().getSharedPreferences("checkbox_states", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove("auto_reply_checked_position");
                    editor.apply();
                    Log.d("CheckBoxChange", "Auto Reply Checked position removed");

                }
            });

            // gestion case à cocher spam
            checkBoxSpam.setOnCheckedChangeListener(null);
            checkBoxSpam.setChecked(getAdapterPosition() == messageAdapter.checkedSpamPosition);

            checkBoxSpam.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && isChecked) {
                    // uncheck previous checked item
                    if (messageAdapter.checkedSpamPosition != -1 && messageAdapter.checkedSpamPosition != position) {
                        messageAdapter.notifyItemChanged(messageAdapter.checkedSpamPosition);
                    }
                    messageAdapter.checkedSpamPosition = position;
                    messageAdapter.notifyItemChanged(position);

                    SharedPreferences sharedPreferences = itemView.getContext().getSharedPreferences("checkbox_states", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("spam_checked_position", position);
                    editor.apply();
                    Log.d("CheckBoxChange", "Spam Checked position set to " + position);
                } else if (!isChecked && messageAdapter.checkedSpamPosition == position) {
                    messageAdapter.checkedSpamPosition = -1;
                    SharedPreferences sharedPreferences = itemView.getContext().getSharedPreferences("checkbox_states", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove("spam_checked_position");
                    editor.apply();
                    Log.d("CheckBoxChange", "Spam Checked position removed");

                }
            });

            imageViewDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    messageAdapter.removeItem(position);
                }
            });
        }
    }

    public void removeItem(int position) {
        if (checkedAutoReplyPosition == position) {
            checkedAutoReplyPosition = -1;
        }
        if (checkedSpamPosition == position) {
            checkedSpamPosition = -1;
        }

        updateCheckedPositionsInSharedPreferences();

        messageList.remove(position);
        notifyItemRemoved(position);
        saveMessages(); // save message et positions cases
    }

    // maj des cases cochés dans la mémoire
    private void updateCheckedPositionsInSharedPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("checkbox_states", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("auto_reply_checked_position", checkedAutoReplyPosition);
        editor.putInt("spam_checked_position", checkedSpamPosition);
        editor.apply();
    }

    // charger les cases cochés depuis la mémoire
    private void loadCheckedPositions() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("checkbox_states", Context.MODE_PRIVATE);
        checkedAutoReplyPosition = sharedPreferences.getInt("auto_reply_checked_position", -1);
        checkedSpamPosition = sharedPreferences.getInt("spam_checked_position", -1);
    }

    // sauvegarde état des messages et des cases cochées après suppression!!
    private void saveMessages() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("message_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putInt("message_count", messageList.size());
        for (int i = 0; i < messageList.size(); i++) {
            editor.putString("message_" + i, messageList.get(i).getText());
        }

        // save positions cases
        editor.putInt("auto_reply_checked_position", checkedAutoReplyPosition);
        editor.putInt("spam_checked_position", checkedSpamPosition);

        editor.apply();
    }



}
