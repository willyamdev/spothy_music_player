package com.bomboverk.spothy;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

import com.bomboverk.spothy.MusicAdapter.Music;

import java.io.IOException;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

public class MediaPlayerManager {

    private MainActivity maActivity;

    private MediaPlayer mediaPlayer;
    private Context context;
    private int length = 0;

    private PlayerBar playerBarManager;
    private SeekBar positionBar;

    private boolean randomEnabled = false;
    private boolean first = true;
    private boolean initPlugged = false;
    private boolean unplugged = false;

    public MediaPlayerManager(Context context, PlayerBar playerBar, MainActivity maActivity) {
        this.context = context;
        this.maActivity = maActivity;
        this.playerBarManager = playerBar;
        this.positionBar = playerBar.getPositionBar();
        mediaPlayer = new MediaPlayer();

        if (isHeadphonesPlugged()){
            initPlugged = true;
        }

        //seekBarControler();
    }

    public void startMusic(final Music music) {

        if (mediaPlayer != null) {
            stopSystem();
        }

        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(context, Uri.parse(music.getUrl()));
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (first){seekBarControler(); first=false;}
                    mp.start();
                    playerBarManager.changePlayerBarInfo(music);
                    playerBarManager.onStartMusic();
                    playerBarManager.finalDuration(mp.getDuration());
                    positionBar.setMax(mp.getDuration());

                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {

                            if (randomEnabled) {
                                Random random = new Random();
                                int randomMusic = random.nextInt(maActivity.getActualPlaylist().size());
                                startMusic(maActivity.getActualPlaylist().get(randomMusic));
                            } else {
                                maActivity.nextMusic(null);
                            }
                        }
                    });
                }
            });

            SharedPreferences.Editor editor = context.getSharedPreferences("st_lastmusic", MODE_PRIVATE).edit();
            editor.putLong("music", music.getMusicID());
            editor.apply();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resumeMusic(Music music) {
        if (length != 0) {
            mediaPlayer.start();
            playerBarManager.onStartMusic();
        } else {
            startMusic(music);
        }
    }

    public void pauseMusic() {
        length = mediaPlayer.getCurrentPosition();
        mediaPlayer.pause();
        playerBarManager.onPausedMusic();
    }

    public boolean isPlayng() {
        return mediaPlayer.isPlaying();
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setLooping() {
        if (mediaPlayer.isLooping()) {
            mediaPlayer.setLooping(false);
        } else {
            mediaPlayer.setLooping(true);
        }
        playerBarManager.onLoop(mediaPlayer.isLooping());
    }

    public void setRandom() {
        randomEnabled = !randomEnabled;
        playerBarManager.onRandom(randomEnabled);
    }

    public void stopSystem(){
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private boolean isHeadphonesPlugged(){
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        AudioDeviceInfo[] audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_ALL);
        for(AudioDeviceInfo deviceInfo : audioDevices){
            if(deviceInfo.getType()==AudioDeviceInfo.TYPE_WIRED_HEADPHONES || deviceInfo.getType()==AudioDeviceInfo.TYPE_WIRED_HEADSET){
                return true;
            }
        }
        return false;
    }

    public void seekBarControler() {
        positionBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    positionBar.setProgress(progress);
                    playerBarManager.changeCurrentTime(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration());
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {

                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

                while (mediaPlayer != null) {

                    if (initPlugged && !isHeadphonesPlugged() && mediaPlayer.isPlaying() && !unplugged) {
                        unplugged = true;
                        pauseMusic();
                    }else if (!initPlugged && isHeadphonesPlugged() && mediaPlayer.isPlaying() && !unplugged){
                        initPlugged = true;
                    }else if (unplugged && isHeadphonesPlugged()){
                        unplugged = false;
                    }

                    try {
                        Message msg = new Message();
                        msg.what = mediaPlayer.getCurrentPosition();
                        handler.sendMessage(msg);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            int currentPosition = msg.what;

            positionBar.setProgress(currentPosition);

            playerBarManager.changeCurrentTime(currentPosition, mediaPlayer.getDuration());
        }
    };
}