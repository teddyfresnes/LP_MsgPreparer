package com.teddyfresnes.msgpreparer;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private Context context;
    private static List<Contact> contactList;

    public ContactAdapter(Context context, List<Contact> contactList) {
        this.context = context;
        ContactAdapter.contactList = contactList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.bind(contact);

        SharedPreferences sharedPreferences = context.getSharedPreferences("checkbox_states", Context.MODE_PRIVATE);
        boolean isChecked = sharedPreferences.getBoolean("checkbox_" + contact.getPhoneNumber(), false);
        holder.checkBoxSelect.setChecked(isChecked);
    }


    @Override
    public int getItemCount() {
        return contactList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewProfile;
        TextView textViewName, textViewPhoneNumber;
        CheckBox checkBoxSelect;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfile = itemView.findViewById(R.id.image_view_profile);
            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewPhoneNumber = itemView.findViewById(R.id.text_view_phone_number);
            checkBoxSelect = itemView.findViewById(R.id.check_box_select);

            checkBoxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Contact contact = contactList.get(position);
                    contact.setSelected(isChecked);
                    saveCheckboxState(contact.getPhoneNumber(), isChecked);
                }
            });

        }

        void bind(Contact contact) {
            textViewName.setText(contact.getName());
            textViewPhoneNumber.setText(contact.getPhoneNumber());
            checkBoxSelect.setChecked(contact.isSelected());
        }

        private void saveCheckboxState(String phoneNumber, boolean isChecked) {
            SharedPreferences sharedPreferences = itemView.getContext().getSharedPreferences("checkbox_states", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("checkbox_" + phoneNumber, isChecked);
            editor.apply();
        }


    }
}
