package com.example.amarsaljic.newstospeech;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import org.json.*;


import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;

public class MainActivity extends AppCompatActivity implements ISpeechRecognitionServerEvents {

    MicrophoneRecognitionClient micClient = null;
    TextView checkIntent;
    MenuItem startSpeechRecognitionItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.checkIntent = findViewById(R.id.check_intent);
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
            //TODO: Only trigger function if precision > 0.9, otherwise tell user to repeat his request
            this.checkIntent.setText(intentName);
            //TODO: Trigger function based on intent
            this.micClient.endMicAndRecognition();
            this.startSpeechRecognitionItem.setEnabled(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
