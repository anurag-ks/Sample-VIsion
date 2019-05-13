package project.vision;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LandmarkActivity extends AppCompatActivity implements OnMapReadyCallback{
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int RESULT_LOAD_IMAGE = 2;
    final List<String> Landmark = new ArrayList<String>();

    String landmarkName = "";
    private GoogleMap mMap;

    double longitude = 0;
    double latitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.customfontstyle);

        ProgressBar progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);

        RelativeLayout relativeLayout = findViewById(R.id.result_card);
        relativeLayout.setVisibility(View.INVISIBLE);

        CardView mapCard = findViewById(R.id.second_card_view);
        mapCard.setVisibility(View.INVISIBLE);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        FloatingActionButton upload_fab = findViewById(R.id.upload_fab);
        upload_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchUploadPictureIntent();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney, Australia, and move the camera.
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    private void dispatchUploadPictureIntent(){
        Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        ProgressBar progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK){
            Uri selectedImage = data.getData();
            Bitmap bitmap;
            Bitmap resized_bitmap = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                resized_bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()/2, bitmap.getHeight()/2, false);

                ImageView imageView = findViewById(R.id.image_view);
                imageView.setImageBitmap(resized_bitmap);
                imageView.setRotation(0);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            CardView cardView = findViewById(R.id.card_view);
            cardView.setVisibility(View.INVISIBLE);

            RelativeLayout relativeLayout = findViewById(R.id.result_card);
            relativeLayout.setVisibility(View.VISIBLE);

            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(resized_bitmap);
            FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
                    .getVisionCloudLandmarkDetector();
            Task<List<FirebaseVisionCloudLandmark>> result = detector.detectInImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLandmark>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionCloudLandmark> firebaseVisionCloudLandmarks) {
                            for (FirebaseVisionCloudLandmark landmark: firebaseVisionCloudLandmarks) {
                                landmarkName = landmark.getLandmark();
                                System.out.println(landmarkName);
                                for (FirebaseVisionLatLng loc: landmark.getLocations()) {
                                    latitude = loc.getLatitude();
                                    longitude = loc.getLongitude();
                                }
                            }

                            if(landmarkName == ""){
                                TextView textView = findViewById(R.id.card_title);
                                textView.setText("Could not identify the location");
                            }
                            else{
                                TextView textView = findViewById(R.id.card_title);
                                textView.setText(landmarkName);

                                textView = findViewById(R.id.card_subtitle);
                                textView.setText("Click here for more information");

                                CardView mapCard = findViewById(R.id.second_card_view);
                                mapCard.setVisibility(View.VISIBLE);

                                LatLng latLng = new LatLng(latitude, longitude);
                                mMap.addMarker(new MarkerOptions().position(latLng).title(landmarkName));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                            }

                            ProgressBar progressBar = findViewById(R.id.progressbar);
                            progressBar.setVisibility(View.INVISIBLE);

                            RelativeLayout relativeLayout = findViewById(R.id.result_card);
                            relativeLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                    intent.putExtra(SearchManager.QUERY, landmarkName);
                                    startActivity(intent);
                                }
                            });


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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            final Bitmap imageBitmap = (Bitmap) extras.get("data");
            final ImageView imageView = findViewById(R.id.image_view);
            imageView.setImageBitmap(imageBitmap);

            CardView cardView = findViewById(R.id.card_view);
            cardView.setVisibility(View.INVISIBLE);

            RelativeLayout relativeLayout = findViewById(R.id.result_card);
            relativeLayout.setVisibility(View.VISIBLE);

            CardView mapCard = findViewById(R.id.second_card_view);
            mapCard.setVisibility(View.VISIBLE);

            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
            FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
                    .getVisionCloudLandmarkDetector();
            Task<List<FirebaseVisionCloudLandmark>> result = detector.detectInImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLandmark>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionCloudLandmark> firebaseVisionCloudLandmarks) {
                            for (FirebaseVisionCloudLandmark landmark: firebaseVisionCloudLandmarks) {
                                landmarkName = landmark.getLandmark();
                                System.out.println(landmarkName);
                                for (FirebaseVisionLatLng loc: landmark.getLocations()) {
                                    latitude = loc.getLatitude();
                                    longitude = loc.getLongitude();
                                }
                            }

                            TextView textView = findViewById(R.id.card_title);
                            textView.setText(landmarkName);

                            textView = findViewById(R.id.card_subtitle);
                            textView.setText("Click here for more information");

                            LatLng latLng = new LatLng(latitude, longitude);

                            mMap.addMarker(new MarkerOptions().position(latLng).title(landmarkName));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                            ProgressBar progressBar = findViewById(R.id.progressbar);
                            progressBar.setVisibility(View.INVISIBLE);

                            RelativeLayout relativeLayout = findViewById(R.id.result_card);
                            relativeLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                    intent.putExtra(SearchManager.QUERY, landmarkName);
                                    startActivity(intent);
                                }
                            });


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
