package com.bomboverk.spothy.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bomboverk.spothy.MusicAdapter.Music;
import com.bomboverk.spothy.R;

public class NotificationHelper {

    private static final String CHANNEL_ID = "spothychannelmusic";
    private static final int ID = 19;

    private Context context;
    private NotificationManagerCompat notificationManager;
    RemoteViews collapsedView;

    public NotificationHelper(Context context) {
        this.context = context;
        notificationManager = NotificationManagerCompat.from(context);
    }

    public NotificationManagerCompat getNotificationManager() {
        return notificationManager;
    }

    public void showNotification(Music music, boolean paused) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Music Player", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }

        collapsedView = new RemoteViews(context.getPackageName(), R.layout.notification_music);

        //PLAY BUTTON
        Intent btnPlay = new Intent(context, NotificationReceiver.class);
        btnPlay.putExtra("action", 1);

        Intent btnNext = new Intent(context, NotificationReceiver.class);
        btnNext.putExtra("action", 2);

        Intent btnBack = new Intent(context, NotificationReceiver.class);
        btnBack.putExtra("action", 3);

        Intent closeNotify = new Intent(context, NotificationReceiver.class);
        closeNotify.putExtra("action", 4);

        PendingIntent clickPlay = PendingIntent.getBroadcast(context, 0, btnPlay, PendingIntent.FLAG_UPDATE_CURRENT);
        collapsedView.setOnClickPendingIntent(R.id.noty_btn_play, clickPlay);

        PendingIntent clickNext = PendingIntent.getBroadcast(context, 1, btnNext, PendingIntent.FLAG_UPDATE_CURRENT);
        collapsedView.setOnClickPendingIntent(R.id.noty_btn_next, clickNext);

        PendingIntent clickBack = PendingIntent.getBroadcast(context, 2, btnBack, PendingIntent.FLAG_UPDATE_CURRENT);
        collapsedView.setOnClickPendingIntent(R.id.noty_btn_back, clickBack);

        PendingIntent clickClose = PendingIntent.getBroadcast(context, 3, closeNotify, PendingIntent.FLAG_UPDATE_CURRENT);
        collapsedView.setOnClickPendingIntent(R.id.noty_btn_close, clickClose);

        if (!paused){
            collapsedView.setImageViewResource(R.id.noty_btn_play, R.drawable.ic_pbe_pause);
        }else{
            collapsedView.setImageViewResource(R.id.noty_btn_play, R.drawable.ic_pbe_playbutton);
        }

        updateInfoNotificationMusic(music);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notificationicon)
                .setCustomBigContentView(collapsedView)
                .setCustomContentView(collapsedView)
                .setAutoCancel(true)
                .setOngoing(true)
                .build();


        notificationManager.notify(ID, notification);

    }

    public void updateInfoNotificationMusic(Music music) {
        collapsedView.setTextViewText(R.id.noty_text_nameMusic, music.getNome());
        collapsedView.setTextViewText(R.id.noty_text_nameAutor, music.getArtista());
    }

    public void cancelNotification(){
        notificationManager.cancelAll();
    }

    //SET TEXT
    //collapsedView.setTextViewText(R.id.txtNamemusicnoty, "CU");

    //SET IMAGE
    //collapsedView.setImageViewResource(R.id.dewde, R.drawable.frefe);

}
