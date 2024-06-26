package com.teddyfresnes.msgpreparer;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MyPagerAdapter extends FragmentStateAdapter {

    public MyPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new Fragment_Home();
            case 1:
                return new Fragment_Contacts();
            case 2:
                return new Fragment_Messages();
            case 3:
                return new Fragment_Actions();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 4; // nb pages
    }
}