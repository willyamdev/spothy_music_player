package com.bomboverk.spothy.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bomboverk.spothy.MainActivity;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int play = intent.getIntExtra("action", 0);

        switch (play){
            case 1:
                MainActivity.getInstace().startLastMusic(null);
                break;
            case 2:
                MainActivity.getInstace().nextMusic(null);
                break;
            case 3:
                MainActivity.getInstace().backMusic(null);
                break;
            case 4:
                MainActivity.getInstace().removeNotification();
                break;
        }
    }
}
