package com.teddyfresnes.msgpreparer;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class SMSService extends Service {

    private static final String TAG = "SMSService";
    private boolean active;
    private int countSMSReceived;
    private SMSReceiver smsReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        active = true;
        smsReceiver = new SMSReceiver();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, filter);
        Log.d(TAG, "SMSService created and SMSReceiver registered");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SMSService started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsReceiver);
        Log.d(TAG, "SMSReceiver unregistered");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void incrementSMSCount() {
        countSMSReceived++;
    }

    public boolean isActive() {
        return active;
    }
}
