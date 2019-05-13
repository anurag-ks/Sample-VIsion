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
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    TextToSpeech tts;
    final List<String> imageLabels = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Image Labeling");
        toolbar.setTitleTextAppearance(this, R.style.customfontstyle);
        setSupportActionBar(toolbar);
        ProgressBar progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);

        ImageView imageView = findViewById(R.id.image_view);
        imageView.setVisibility(View.INVISIBLE);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.UK);
                }
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
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
            Bundle extras = data.getExtras();
            final Bitmap imageBitmap = (Bitmap) extras.get("data");
            final ImageView imageView = findViewById(R.id.image_view);

            TextView textView = findViewById(R.id.info_text);
            textView.setVisibility(View.INVISIBLE);

            imageView.setImageBitmap(imageBitmap);
            imageView.setVisibility(View.VISIBLE);

            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
            FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
                    .getCloudImageLabeler();
            ProgressBar progressBar = findViewById(R.id.progressbar);

            progressBar.setVisibility(View.VISIBLE);
            labeler.processImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                            String result = "";
                            imageLabels.clear();

                            final ListView listView = findViewById(R.id.list_view);

                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                    getApplicationContext(),
                                    android.R.layout.simple_list_item_1,
                                    imageLabels );

                            listView.setAdapter(arrayAdapter);

                            for (FirebaseVisionImageLabel label: labels) {
                                String text = label.getText();
                                float confidence = label.getConfidence();
                                if (confidence > 0.7)
                                    imageLabels.add(text);
                            }

                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position,
                                                        long id) {
                                    tts.speak(imageLabels.get(position), TextToSpeech.QUEUE_FLUSH, null);

                                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                    intent.putExtra(SearchManager.QUERY, imageLabels.get(position));
                                    startActivity(intent);
                                }

                            });


                            ProgressBar progressBar = findViewById(R.id.progressbar);
                            progressBar.setVisibility(View.INVISIBLE);

                            Snackbar.make(findViewById(R.id.main_activity), "Click on the results for more information", Snackbar.LENGTH_LONG)
                                    .show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    void openTextRecogActivity(){
        Intent intent = new Intent(this, TextRecogActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_text_recognition) {
            openTextRecogActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
