package com.example.amarsaljic.newstospeech;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

public class PlayActivity extends AppCompatActivity {

    private Article article;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        DefaultArticles da = DefaultArticles.getInstance(this);
        this.article = da.articleList.get(0);

        final MediaPlayer article_audio = MediaPlayer.create(PlayActivity.this,
                this.article.audio_file_id);

        ImageButton play = (ImageButton) findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if (article_audio.isPlaying()) {
                    article_audio.pause();
                } else {
                    article_audio.start();
                }
            }
        });
    }

}
