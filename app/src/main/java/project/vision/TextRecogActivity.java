package project.vision;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TextRecogActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    final List<String> resultStrings = new ArrayList<String>();

    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_recog);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Text Recognition");
        toolbar.setTitleTextAppearance(this, R.style.customfontstyle);
        setSupportActionBar(toolbar);

        ProgressBar progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);

        CardView cardView = findViewById(R.id.second_card_view);
        cardView.setVisibility(View.INVISIBLE);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.UK);
                }
            }
        });

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        FloatingActionButton ttsButton = findViewById(R.id.tts);
        ttsButton.setVisibility(View.INVISIBLE);
        ttsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                tts.speak(resultStrings.get(0), TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            TextView textView = findViewById(R.id.info_text);
            textView.setVisibility(View.INVISIBLE);

            Bundle extras = data.getExtras();
            final Bitmap imageBitmap = (Bitmap) extras.get("data");
            final ImageView imageView = findViewById(R.id.image_view);
            imageView.setImageBitmap(imageBitmap);

            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                    .getCloudTextRecognizer();


            ProgressBar progressBar = findViewById(R.id.progressbar);
            progressBar.setVisibility(View.VISIBLE);

            Task<FirebaseVisionText> result =
                    detector.processImage(image)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    resultStrings.clear();

                                    final ListView listView = findViewById(R.id.list_view);

                                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                            getApplicationContext(),
                                            android.R.layout.simple_list_item_1,
                                            resultStrings );
                                    listView.setAdapter(arrayAdapter);
                                    resultStrings.add(firebaseVisionText.getText());
                                    ProgressBar progressBar = findViewById(R.id.progressbar);
                                    progressBar.setVisibility(View.INVISIBLE);

                                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position,
                                                                long id) {
                                            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                            intent.putExtra(SearchManager.QUERY, resultStrings.get(position));
                                            startActivity(intent);
                                        }

                                    });

                                    CardView cardView = findViewById(R.id.second_card_view);
                                    cardView.setVisibility(View.VISIBLE);

                                    FloatingActionButton ttsButton = findViewById(R.id.tts);
                                    ttsButton.setVisibility(View.VISIBLE);

                                    Snackbar.make(findViewById(R.id.text_recog_activity), "Click on the results for more information", Snackbar.LENGTH_LONG)
                                            .show();
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            ProgressBar progressBar = findViewById(R.id.progressbar);
                                            progressBar.setVisibility(View.INVISIBLE);

                                            TextView textView = findViewById(R.id.info_text);
                                            textView.setText(R.string.error_msg);
                                            textView.setVisibility(View.INVISIBLE);
                                        }
                                    });
        }
    }

}
