package com.bomboverk.spothy.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bomboverk.spothy.MusicAdapter.Music;
import com.bomboverk.spothy.R;

public class NotificationService extends Service {

    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    private static final String CHANNEL_ID = "spothychannelmusic";
    private static final int ID = 19;
    private static NotificationService notificationService;

    final MyBinder binder = new MyBinder();

    RemoteViews collapsedView;

    private NotificationManagerCompat notificationManager;
    Notification notification;

    public void createNotification(){

        notificationService = this;

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Music Player", NotificationManager.IMPORTANCE_LOW);
            mNotificationManager = ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE));
            mNotificationManager.createNotificationChannel(channel);
        }else{
            notificationManager = NotificationManagerCompat.from(this);
        }

        collapsedView = new RemoteViews(this.getPackageName(), R.layout.notification_music);

        //PLAY BUTTON
        Intent btnPlay = new Intent(this, NotificationReceiver.class);
        btnPlay.putExtra("action", 1);

        Intent btnNext = new Intent(this, NotificationReceiver.class);
        btnNext.putExtra("action", 2);

        Intent btnBack = new Intent(this, NotificationReceiver.class);
        btnBack.putExtra("action", 3);

        Intent closeNotify = new Intent(this, NotificationReceiver.class);
        closeNotify.putExtra("action", 4);

        PendingIntent clickPlay = PendingIntent.getBroadcast(this, 0, btnPlay, PendingIntent.FLAG_UPDATE_CURRENT);
        collapsedView.setOnClickPendingIntent(R.id.noty_btn_play, clickPlay);

        PendingIntent clickNext = PendingIntent.getBroadcast(this, 1, btnNext, PendingIntent.FLAG_UPDATE_CURRENT);
        collapsedView.setOnClickPendingIntent(R.id.noty_btn_next, clickNext);

        PendingIntent clickBack = PendingIntent.getBroadcast(this, 2, btnBack, PendingIntent.FLAG_UPDATE_CURRENT);
        collapsedView.setOnClickPendingIntent(R.id.noty_btn_back, clickBack);

        PendingIntent clickClose = PendingIntent.getBroadcast(this, 3, closeNotify, PendingIntent.FLAG_UPDATE_CURRENT);
        collapsedView.setOnClickPendingIntent(R.id.noty_btn_close, clickClose);



        if (Build.VERSION.SDK_INT >= 26) {
            mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notificationicon)
                    .setCustomBigContentView(collapsedView)
                    .setCustomContentView(collapsedView)
                    .setAutoCancel(true)
                    .setOngoing(true);

            startForeground(ID, mBuilder.build());
        }else{
            notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notificationicon)
                    .setCustomBigContentView(collapsedView)
                    .setCustomContentView(collapsedView)
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .build();

            startForeground(ID, notification);
        }

    }

    public static NotificationService getInstance(){
        return notificationService;
    }

    public void updateNotifycation(Music music, boolean isPaused){
        if (!isPaused){
            collapsedView.setImageViewResource(R.id.noty_btn_play, R.drawable.ic_pbe_pause);
        }else{
            collapsedView.setImageViewResource(R.id.noty_btn_play, R.drawable.ic_pbe_playbutton);
        }

        collapsedView.setTextViewText(R.id.noty_text_nameMusic, music.getNome());
        collapsedView.setTextViewText(R.id.noty_text_nameAutor, music.getArtista());

        if (Build.VERSION.SDK_INT >= 26) {
            mNotificationManager.notify(ID, mBuilder.build());
        }else{
            notificationManager.notify(ID, notification);
        }
    }

    public NotificationCompat.Builder getNotification() {
        return mBuilder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopForeground(true);
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void cancelNotification(){
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MyBinder extends Binder {
        public NotificationService getServiceSystem() {
            return NotificationService.this;
        }
    }
}
