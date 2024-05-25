package com.teddyfresnes.msgpreparer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Fragment_Home extends Fragment {

    private MainActivity mainActivity;

    public Fragment_Home() {
        // constructeur vide
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // recuperation de l'activité
        mainActivity = (MainActivity) getActivity();
        // gestion des boutons pour changer les tabs
        view.findViewById(R.id.buttonContacts).setOnClickListener(v -> mainActivity.selectTab(1));
        view.findViewById(R.id.buttonMessages).setOnClickListener(v -> mainActivity.selectTab(2));
        view.findViewById(R.id.buttonActions).setOnClickListener(v -> mainActivity.selectTab(3));
    }
}
