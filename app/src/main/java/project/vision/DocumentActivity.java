package project.vision;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class DocumentActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    final List<String> resultStrings = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.customfontstyle);

        CardView cardView = findViewById(R.id.second_card_view);
        cardView.setVisibility(View.INVISIBLE);

        ProgressBar progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);

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

        FloatingActionButton sendEmail = findViewById(R.id.send_email);
        sendEmail.setVisibility(View.INVISIBLE);
        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchSendEmailIntent();
            }
        });
    }

    private void dispatchSendEmailIntent(){
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, "");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Document Input");
        emailIntent.putExtra(Intent.EXTRA_TEXT, resultStrings.get(0));

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
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
            FirebaseVisionDocumentTextRecognizer detector = FirebaseVision.getInstance()
                    .getCloudDocumentTextRecognizer();
            ProgressBar progressBar = findViewById(R.id.progressbar);
            progressBar.setVisibility(View.VISIBLE);

            Task<FirebaseVisionDocumentText> result =
                    detector.processImage(image)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionDocumentText>() {
                                @Override
                                public void onSuccess(FirebaseVisionDocumentText result) {
                                    resultStrings.clear();

                                    final ListView listView = findViewById(R.id.list_view);

                                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                            getApplicationContext(),
                                            android.R.layout.simple_list_item_1,
                                            resultStrings );

                                    listView.setAdapter(arrayAdapter);

                                    if(result != null)
                                        resultStrings.add(result.getText());

                                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position,
                                                                long id) {
                                            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                            intent.putExtra(SearchManager.QUERY, resultStrings.get(position));
                                            startActivity(intent);
                                        }

                                    });

                                    ProgressBar progressBar = findViewById(R.id.progressbar);
                                    progressBar.setVisibility(View.INVISIBLE);

                                    CardView cardView = findViewById(R.id.second_card_view);
                                    cardView.setVisibility(View.VISIBLE);

                                    FloatingActionButton sendEmail = findViewById(R.id.send_email);
                                    sendEmail.setVisibility(View.VISIBLE);

                                    //Snackbar.make(findViewById(R.id.document_activity), "Click on the results for more information", Snackbar.LENGTH_LONG)
                                    //        .show();
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

}
