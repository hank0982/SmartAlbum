package cse.cuhk.smartalbum.photodetails;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import cse.cuhk.smartalbum.R;
import cse.cuhk.smartalbum.photodetails.model.Travel;

public class InfoActivity extends AppCompatActivity {

    private static final String EXTRA_TRAVEL = "EXTRA_TRAVEL";
    private ImageView image;
    private TextView title;

    public static Intent newInstance(Context context, Travel travel) {
        Intent intent = new Intent(context, InfoActivity.class);
        intent.putExtra(EXTRA_TRAVEL, travel);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_details_activity_info);
        image = findViewById(R.id.photo_details_sharedImage);
        title = findViewById(R.id.photo_details_activity_title);
        Travel travel = getIntent().getParcelableExtra(EXTRA_TRAVEL);
        if (travel != null) {
            image.setImageResource(travel.getImage());
            title.setText(travel.getName());
        }
    }
}
