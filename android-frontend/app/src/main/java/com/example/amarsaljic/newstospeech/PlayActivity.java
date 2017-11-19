package com.example.amarsaljic.newstospeech;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class PlayActivity extends AppCompatActivity  implements ISpeechRecognitionServerEvents{

    private Article article;
    private boolean isStared;
    private MediaPlayer article_audio;
    private final Integer skipLength = 10000;
    private final Integer previousReplaysLimit = 5000;
    private final Integer moveBackAfterPause = 2500;
    private Handler mHandler = new Handler();

    MicrophoneRecognitionClient micClient = null;
    MenuItem startSpeechRecognitionItem;
    private int index = 16;
    DefaultArticles da = DefaultArticles.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        // Intent intent = getIntent();
        // int index = intent.getIntExtra("index", 0);

        isStared = false;
        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);


        this.article = da.articleList.get(index);
        article_audio = MediaPlayer.create(PlayActivity.this, this.article.audio_file_id);
        seekBar.setMax(article_audio.getDuration() / 1000);

        //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        //          .setAction("Action", null).show();

        final ImageButton replay = (ImageButton) findViewById(R.id.replay);
        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seekBackIfPossible(skipLength);
            }
        });

        this.initArticleDescription();

        final ImageButton previous = (ImageButton) findViewById(R.id.previous);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (article_audio.getCurrentPosition() > previousReplaysLimit
                        || index == 0) {
                    article_audio.seekTo(0);
                } else {
                    index -= 1;
                    playArticle();
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
                    seekBackIfPossible(moveBackAfterPause);
                    article_audio.start();
                    play.setImageResource(R.drawable.ic_pause_circle_filled_black_48dp);
                }
            }
        });

        final ImageButton next = (ImageButton) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (index == da.articleList.size() - 1){
                    article_audio.pause();
                } else {
                    index += 1;
                    playArticle();
                }
                // if list index == list.length - 1 then stop else next
                // seekBar.setMax(article_audio.getDuration() / 1000);
            }
        });

        final ImageButton star = (ImageButton) findViewById(R.id.star);
        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isStared) {
                    star.setImageResource(R.drawable.ic_star_black_48dp);
                    star.setColorFilter(getResources().getColor(R.color.colorAccent));
                } else {
                    star.setImageResource(R.drawable.ic_star_border_black_48dp);
                    star.setColorFilter(getResources().getColor(R.color.grey));
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


        article_audio.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (index == da.articleList.size() - 1) {
                    article_audio.pause();
                } else {
                    index += 1;
                    playArticle();
                }
            }
        });

        article_audio.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                article_audio.start();
                seekBar.setMax(article_audio.getDuration() / 1000);
                final ImageButton play = (ImageButton) findViewById(R.id.play);
                play.setImageResource(R.drawable.ic_pause_circle_filled_black_48dp);
            }
        });


    }

    private void playArticle() {
        article_audio.stop();
        article_audio.reset();
        article = da.articleList.get(index);
        String ghjk = "android.resource://" + getPackageName() + "/raw/a";
        try {
            article_audio.setDataSource(PlayActivity.this, Uri.parse(ghjk + article.article_id));
            article_audio.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        article_audio.start();
        final ImageButton play = (ImageButton) findViewById(R.id.play);
        play.setImageResource(R.drawable.ic_pause_circle_filled_black_48dp);
    }

    private void initArticleDescription() {
        ImageView articleCoverIV = findViewById(R.id.article_cover);
        articleCoverIV.setImageResource(R.drawable.newtospeech2);

        TextView articleTitleTV = findViewById(R.id.article_title);
        articleTitleTV.setText(article.title);
        articleTitleTV.setTextSize(20);

        TextView articleProviderTV = findViewById(R.id.article_provider);
        articleProviderTV.setText(article.provider_name);
        articleProviderTV.setTextSize(17);

        TextView articleDateTV = findViewById(R.id.article_date);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        articleDateTV.setText(df.format(article.date_published));
        articleDateTV.setTextSize(17);

    }

    @Override
    protected void onStop() {
        super.onStop();
        article_audio.pause();
        final ImageButton play = (ImageButton) findViewById(R.id.play);
        play.setImageResource(R.drawable.ic_play_circle_filled_black_48dp);
    }

    private void seekBackIfPossible(Integer seekBackTime) {
        if (article_audio != null) {
            Integer currentPosition = article_audio.getCurrentPosition();
            if (currentPosition > seekBackTime) {
                article_audio.seekTo(currentPosition - seekBackTime);
            } else {
                article_audio.seekTo(0);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.nts_menu, menu);
        this.startSpeechRecognitionItem = menu.findItem(R.id.action_speech_recognition);
        this.startSpeechRecognitionItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.ding);
                mp.start();
                startSpeechRecognition();
                menuItem.setEnabled(false);
                return true;
            }
        });
        return true;
    }


    // Functions relevant for Bing Speech API
    /**
     * Gets the default locale.
     * @return The default locale.
     */
    private String getDefaultLocale() {
        return "en-us";
    }

    /**
     * Gets the primary subscription key
     */
    public String getPrimaryKey() {
        return this.getString(R.string.primaryKey);
    }

    /**
     * Gets the LUIS application identifier.
     * @return The LUIS application identifier.
     */
    private String getLuisAppId() {
        return this.getString(R.string.luisAppID);
    }

    /**
     * Gets the LUIS subscription identifier.
     * @return The LUIS subscription identifier.
     */
    private String getLuisSubscriptionID() {
        return this.getString(R.string.luisSubscriptionID);
    }

    /**
     * Gets the Cognitive Service Authentication Uri.
     * @return The Cognitive Service Authentication Uri.  Empty if the global default is to be used.
     */
    private String getAuthenticationUri() {
        return this.getString(R.string.authenticationUri);
    }

    /**
     * Starts recognizing the user's spoken input
     */
    private void startSpeechRecognition(){
        this.micClient = SpeechRecognitionServiceFactory.createMicrophoneClientWithIntent(
                this,
                this.getDefaultLocale(),
                this,
                this.getPrimaryKey(),
                this.getLuisAppId(),
                this.getLuisSubscriptionID());

        this.micClient.setAuthenticationUri(this.getAuthenticationUri());

        this.micClient.startMicAndRecognition();
    }

    // NOTE: ISpeechRecognitionServerEvents methods
    @Override
    public void onPartialResponseReceived(String s) {

    }

    @Override
    public void onFinalResponseReceived(RecognitionResult recognitionResult) {

    }

    @Override
    public void onIntentReceived(String s) {
        this.micClient.endMicAndRecognition();
        try {
            MediaPlayer mp = MediaPlayer.create(this, R.raw.dong);
            mp.start();
            JSONObject intentResultAsJSON = new JSONObject(s.toString());
            JSONArray recognizedIntents = (JSONArray) intentResultAsJSON.get("intents");
            JSONObject highestRankedRecognizedIntent = recognizedIntents.getJSONObject(0);
            String intentName = highestRankedRecognizedIntent.getString("intent");
            //TODO: Trigger function based on intent
            Log.i("Intent: ", intentName);
            switch (intentName) {
                case "Next_article":
                    toastIt("Playing the next article.");
                    break;
                case "Previous_article":
                    toastIt("Playing the previous article.");
                    break;
                case "repeat_article":
                    toastIt("Repeat the article.");
                    break;
                default:
                    break;
            }
            this.micClient.endMicAndRecognition();
            this.startSpeechRecognitionItem.setEnabled(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void toastIt(String toastMessage){
        Toast toast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void onError(int i, String s) {

    }

    @Override
    public void onAudioEvent(boolean b) {

    }

    // Functions which are triggered by voice commands
    //TODO: Implement all of these!
    private void readNextArticle(){

    }

    private void readPreviousArticle(){

    }

    private void repeatArticle(){

    }

}
