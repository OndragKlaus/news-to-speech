package com.example.amarsaljic.newstospeech;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.json.*;


import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class MainActivity extends AppCompatActivity implements ISpeechRecognitionServerEvents, RecognitionListener {

    MicrophoneRecognitionClient micClient = null;
    TextView checkIntent;
    SpeechRecognizer recognizer;
    Button startSpeechRecognitionButton;
    final String KEYPHRASE = "hey newton";
    final String KWS_SEARCH = "wakeup";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.checkIntent = findViewById(R.id.check_intent);
        this.startSpeechRecognitionButton = findViewById(R.id.start_speech_recognition_button);
        startSpeechRecognitionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSpeechRecognition();
            }
        });
        new SetupTask(this).execute();
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
            JSONObject intentResultAsJSON = new JSONObject(s.toString());
            JSONArray recognizedIntents = (JSONArray) intentResultAsJSON.get("intents");
            JSONObject highestRankedRecognizedIntent = recognizedIntents.getJSONObject(0);
            String intentName = highestRankedRecognizedIntent.getString("intent");
            //TODO: Only trigger function if precision > 0.9, otherwise tell user to repeat his request
            this.checkIntent.setText(intentName);
            //TODO: Trigger function based on intent
            this.micClient.endMicAndRecognition();
            this.startSpeechRecognitionButton.setVisibility(View.VISIBLE);
            new SetupTask(this).execute();
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

    // Functions for implementing the RecognizesListener Functions of pocketsphinx for hotword detection
    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<MainActivity> activityReference;
        SetupTask(MainActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }
        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                activityReference.get().setupRecognizer(assetDir);
            } catch (IOException e) {
                return e;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Exception result) {
        }
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        this.recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .setKeywordThreshold((float)1e-18)
                .getRecognizer();
        this.recognizer.addListener(this);

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        this.recognizer.addKeyphraseSearch(this.KWS_SEARCH, this.KEYPHRASE);
        this.recognizer.startListening(this.KWS_SEARCH, 5000);
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {
        this.recognizer.stop();
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {

    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if(hypothesis != null){
            if (hypothesis.getHypstr().toLowerCase().contains(this.KEYPHRASE.toLowerCase())){
                this.startSpeechRecognitionButton.setVisibility(View.GONE);
                this.recognizer.shutdown();
                startSpeechRecognition();
                return;
            }
        }
        this.recognizer.startListening(this.KWS_SEARCH, 5000);
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onTimeout() {
    }
}
