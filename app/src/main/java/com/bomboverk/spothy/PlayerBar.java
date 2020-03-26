package com.bomboverk.spothy;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bomboverk.spothy.Datadase.DatabaseHelper;
import com.bomboverk.spothy.MusicAdapter.Music;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class PlayerBar {

    private ArrayList<Music> musicas;
    private FrameLayout playerBar;
    private View navHeader;
    private Context context;
    private MainActivity mActivity;

    //PLAYER BAR
    private TextView pbMusicName;
    private TextView pbMusicAutor;
    private ImageView pbPlayButton;
    private ImageView pbDiks;
    private Animation pbDiskAnim;

    //PLAYER BAR EXPANDED
    private TextView pbeMusicName;
    private TextView pbeMusicAutor;
    private TextView pbeCurrentDuration;
    private TextView pbeFinalDuration;
    private ImageView pbePlayButton;
    private SeekBar positionBar;
    private ImageView pbeLoopButton;
    private ImageView pbeRandomButton;
    private ImageView pbeDisk;
    private Animation pbeDiksAnim;
    private ImageButton favButton;
    private Animation favAnim;

    //PLAYER BAR NAVIEW
    private TextView navNomeMusic;
    private TextView navNomeAutor;

    private Music lastMusic;

    private DatabaseHelper databaseHelper;

    public PlayerBar(FrameLayout playerBar, FrameLayout playerBarExpanded, Context context, ArrayList<Music> music, View navHeader, MainActivity mActivity) {
        this.playerBar = playerBar;
        this.context = context;
        this.musicas = music;
        this.navHeader = navHeader;
        this.mActivity = mActivity;

        //PLAYER BAR
        pbMusicName = playerBar.findViewById(R.id.act_main_nomeMusicas);
        pbMusicAutor = playerBar.findViewById(R.id.act_main_nomeArtistas);
        pbPlayButton = playerBar.findViewById(R.id.act_main_playButton);
        pbDiks = playerBar.findViewById(R.id.pb_disk);

        //PLAYER BAR EXPANDED
        pbeMusicName = playerBarExpanded.findViewById(R.id.text_pbe_nomeMusica);
        pbeMusicAutor = playerBarExpanded.findViewById(R.id.text_pbe_nomeAutor);
        pbeCurrentDuration = playerBarExpanded.findViewById(R.id.text_pbe_currentDuration);
        pbeFinalDuration = playerBarExpanded.findViewById(R.id.text_pbe_finalDuration);
        pbePlayButton = playerBarExpanded.findViewById(R.id.btn_pbe_play);
        positionBar = playerBarExpanded.findViewById(R.id.seekbar_pbe_musicDuration);
        pbeLoopButton = playerBarExpanded.findViewById(R.id.btn_pbe_loopMusic);
        pbeRandomButton = playerBarExpanded.findViewById(R.id.btn_pbe_randomMusic);
        pbeDisk = playerBarExpanded.findViewById(R.id.pbe_disk);
        favButton = playerBarExpanded.findViewById(R.id.btn_pbe_fav);

        //NAV
        navNomeMusic = navHeader.findViewById(R.id.nav_header_musicNome);
        navNomeAutor = navHeader.findViewById(R.id.nav_header_musicAutor);

        databaseHelper = new DatabaseHelper(context);

        prepareAnimationDisks();
        loadLastMusic();
    }

    private void loadLastMusic() {
        SharedPreferences prefs = context.getSharedPreferences("st_lastmusic", MODE_PRIVATE);
        long lastmusicpb = prefs.getLong("music", 0);

        if (lastmusicpb != 0) {
            for (Music m : musicas) {
                if (m.getMusicID() == lastmusicpb) {
                    changePlayerBarInfo(m);
                    getLastDurationMusic(m);
                    break;
                }
            }
        }
    }

    public void changePlayerBarInfo(Music music) {

        lastMusic = music;

        pbMusicName.setText(music.getNome());
        pbMusicAutor.setText(music.getArtista());

        pbeMusicName.setText(music.getNome());
        pbeMusicAutor.setText(music.getArtista());

        navNomeMusic.setText(music.getNome());
        navNomeAutor.setText(music.getArtista());

        if (playerBar.getVisibility() == View.GONE) {
            playerBar.setVisibility(View.VISIBLE);
        }

        if (databaseHelper.verifyMusicFav(mActivity.playlistFav, music)) {
            onFav();
        } else {
            onUnfav();
        }
    }

    public Music getLastMusic() {
        return lastMusic;
    }

    public void onPausedMusic() {
        pbPlayButton.setImageResource(R.drawable.ic_playerbar_playbtn);
        pbePlayButton.setImageResource(R.drawable.ic_pbe_playbutton);
        animationDisks(false);

        if (mActivity.getNotificationService().getNotification() == null){
            mActivity.getNotificationService().createNotification();
        }
        mActivity.getNotificationService().updateNotifycation(lastMusic, true);
        //mActivity.getNotificationHelper().showNotification(lastMusic, true);
    }

    public void onStartMusic() {
        pbPlayButton.setImageResource(R.drawable.ic_playerbar_stopbtn);
        pbePlayButton.setImageResource(R.drawable.ic_pbe_pause);
        animationDisks(true);
        if (mActivity.getNotificationService().getNotification() == null){
            mActivity.getNotificationService().createNotification();
        }
        mActivity.getNotificationService().updateNotifycation(lastMusic, false);
        //mActivity.getNotificationHelper().showNotification(lastMusic, false);
    }

    public void onFav() {
        favButton.setImageResource(R.drawable.ic_pbe_fav);
        favButton.startAnimation(favAnim);
    }

    public void onUnfav() {
        favButton.setImageResource(R.drawable.ic_pbe_nofav);
    }

    private void animationDisks(boolean action) {
        if (action) {
            pbDiskAnim.setDuration(20000);
            pbDiks.startAnimation(pbDiskAnim);
            pbeDiksAnim.setDuration(20000);
            pbeDisk.startAnimation(pbeDiksAnim);

        } else {
            pbDiskAnim.cancel();
            pbeDiksAnim.cancel();
        }
    }

    private void prepareAnimationDisks() {
        pbDiskAnim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        pbDiskAnim.setRepeatCount(0);
        pbDiskAnim.setInterpolator(new LinearInterpolator());
        pbDiskAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if (mActivity.getIsPlayngMedia()) {
                    pbDiks.startAnimation(pbDiskAnim);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        pbeDiksAnim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        pbeDiksAnim.setRepeatCount(0);
        pbeDiksAnim.setInterpolator(new LinearInterpolator());
        pbeDiksAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mActivity.getIsPlayngMedia()) {
                    pbeDisk.startAnimation(pbeDiksAnim);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        favAnim = new ScaleAnimation(favButton.getScaleX(), 1.5f, favButton.getScaleY(), 1.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        favAnim.setDuration(300);
        final ScaleAnimation favAnimReturn = new ScaleAnimation(1.5f, 1, 1.5f, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        favAnimReturn.setDuration(300);
        favAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                favButton.startAnimation(favAnimReturn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
    }


    public SeekBar getPositionBar() {
        return positionBar;
    }

    public void onLoop(boolean looping) {

        if (looping) {
            pbeLoopButton.setImageResource(R.drawable.ic_pbe_repeat_on);
        } else {
            pbeLoopButton.setImageResource(R.drawable.ic_pbe_repeat);
        }
    }

    public void onRandom(boolean random) {
        if (random) {
            pbeRandomButton.setImageResource(R.drawable.ic_pbe_random_on);
        } else {
            pbeRandomButton.setImageResource(R.drawable.ic_pbe_random);
        }
    }

    public ArrayList<Music> getMusicas() {
        return musicas;
    }

    public void changeCurrentTime(int currentPosition, int totalTime) {
        String elapsedTime = createTimeLabel(currentPosition);
        pbeCurrentDuration.setText(elapsedTime);

        //String remainingTime = createTimeLabel(totalTime - currentPosition);
        //pbeFinalDuration.setText("-"+totalTime);
    }

    public void finalDuration(int finaldura) {
        String remainingTime = createTimeLabel(finaldura);
        pbeFinalDuration.setText("" + remainingTime);
    }

    private String createTimeLabel(int time) {
        String timeLabel = "";

        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }

    private void getLastDurationMusic(Music m) {
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(context, Uri.parse(m.getUrl()));

        String out = "";
        // get mp3 info

        // convert duration to minute:seconds
        String duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Log.v("time", duration);
        long dur = Long.parseLong(duration);
        String seconds = String.valueOf((dur % 60000) / 1000);

        Log.v("seconds", seconds);
        String minutes = String.valueOf(dur / 60000);
        out = minutes + ":" + seconds;
        if (seconds.length() == 1) {
            pbeFinalDuration.setText(minutes + ":0" + seconds);
        } else {
            pbeFinalDuration.setText(minutes + ":" + seconds);
        }
        Log.v("minutes", minutes);
        // close object
        metaRetriever.release();
    }


        /*View header = navigationView.getHeaderView(0);
        TextView text = (TextView) header.findViewById(R.id.nav_header_musicNome);
        TextView edw = header.findViewById(R.id.nav_header_musicAutor);

        text.setText("dededw");
        edw.setText("pppppppppp");

        MULTIPLE HEADERS
        navigationView.getHeaderCount()
        */
}
