package com.example.amarsaljic.newstospeech;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

public class PlayActivity extends AppCompatActivity {

    private Article article;
    private boolean isStared;
    private MediaPlayer article_audio;
    private final Integer skipLength = 10000;
    private final Integer previousReplaysLimit = 5000;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        isStared = false;
        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);

        DefaultArticles da = DefaultArticles.getInstance(this);
        this.article = da.articleList.get(0);
        article_audio = MediaPlayer.create(PlayActivity.this, this.article.audio_file_id);
        seekBar.setMax(article_audio.getDuration() / 1000);

        //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        //          .setAction("Action", null).show();

        final ImageButton replay = (ImageButton) findViewById(R.id.replay);
        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // or list index == 0
                Integer currentPosition = article_audio.getCurrentPosition();
                if (currentPosition >= skipLength) {
                    article_audio.seekTo(currentPosition - skipLength);
                } else {
                    article_audio.seekTo(0);
                }
            }
        });

        final ImageButton previous = (ImageButton) findViewById(R.id.previous);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // or list index == 0
                if (article_audio.getCurrentPosition() > previousReplaysLimit) {
                    article_audio.seekTo(0);
                } else {
                    // play previous
                }
            }
        });

        final ImageButton play = (ImageButton) findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (article_audio.isPlaying()) {
                    article_audio.pause();
                    //Integer l = article_audio.getCurrentPosition();
                    play.setImageResource(R.drawable.ic_play_circle_filled_black_48dp);
                } else {
                    article_audio.start();
                    play.setImageResource(R.drawable.ic_pause_circle_filled_black_48dp);
                }
            }
        });

        final ImageButton next = (ImageButton) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if list index == list.length - 1 then stop else next
            }
        });

        final ImageButton star = (ImageButton) findViewById(R.id.star);
        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStared) {
                    star.setImageResource(R.drawable.ic_star_black_48dp);
                } else {
                    star.setImageResource(R.drawable.ic_star_border_black_48dp);
                }
                isStared = !isStared;
            }
        });

        //Make sure you update Seekbar on UI thread
        PlayActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(article_audio != null){
                    int mCurrentPosition = article_audio.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                }
                mHandler.postDelayed(this, 100);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(article_audio != null && fromUser){
                    article_audio.seekTo(progress * 1000);
                }
            }
        });
    }

}
