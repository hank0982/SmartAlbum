package cse.cuhk.smartalbum.photodetails;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import cse.cuhk.smartalbum.R;
import cse.cuhk.smartalbum.photodetails.model.Travel;
import cse.cuhk.smartalbum.utils.GlideApp;
import cse.cuhk.smartalbum.utils.Photo;

public class InfoActivity extends AppCompatActivity {

    private static final String EXTRA_PHOTO = "EXTRA_PHOTO";
    private ImageView image;
    private TextView title;

    public static Intent newInstance(Context context, Photo photo) {
        Intent intent = new Intent(context, InfoActivity.class);
        intent.putExtra(EXTRA_PHOTO, photo);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_details_activity_info);
        image = findViewById(R.id.photo_details_sharedImage);
        title = findViewById(R.id.photo_details_activity_title);
        Photo photo = getIntent().getParcelableExtra(EXTRA_PHOTO);
        if (photo != null) {
            GlideApp.with(this).load(photo.path).into(this.image);
            title.setText(photo.name);
        }
    }
}
