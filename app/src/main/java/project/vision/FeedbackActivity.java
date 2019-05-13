package project.vision;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class FeedbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.customfontstyle);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Button submit = findViewById(R.id.submit_form);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchSendEmailIntent();
            }
        });
    }

    private void dispatchSendEmailIntent(){
        EditText name = findViewById(R.id.full_name);
        EditText phone = findViewById(R.id.phone_number);
        EditText message = findViewById(R.id.feedback_msg);

        String final_msg = name.getText().toString()+"\n"+phone.getText().toString()+"\n"+message.getText().toString()+"\n";
        System.out.println(final_msg);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"agrim.spv@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "App Feedback");
        emailIntent.putExtra(Intent.EXTRA_TEXT, final_msg);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

}
