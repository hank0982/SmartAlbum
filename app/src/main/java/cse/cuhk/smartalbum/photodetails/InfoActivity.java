package cse.cuhk.smartalbum.photodetails;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cse.cuhk.smartalbum.MainActivity;
import cse.cuhk.smartalbum.R;
import cse.cuhk.smartalbum.photodetails.model.Travel;
import cse.cuhk.smartalbum.utils.GlideApp;
import cse.cuhk.smartalbum.utils.Photo;
import cse.cuhk.smartalbum.utils.database.DBHelper;

public class InfoActivity extends AppCompatActivity {

    private static final String EXTRA_PHOTO = "EXTRA_PHOTO";
    private DBHelper db;
    private ImageView image;
    private TextView exifInfo;

    public static Intent newInstance(Context context, Photo photo) {
        Intent intent = new Intent(context, InfoActivity.class);
        intent.putExtra(EXTRA_PHOTO, photo);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = DBHelper.getInstance(this);
        setContentView(R.layout.photo_details_activity_info);
        image = findViewById(R.id.photo_details_sharedImage);
        exifInfo = findViewById(R.id.photo_details_activity_exif);
        Photo photo = getIntent().getParcelableExtra(EXTRA_PHOTO);

        StringBuilder exifData = new StringBuilder();

        if (photo != null) {
            exifData.append("File path: " + photo.name);

            GlideApp.with(this).load(photo.path).into(this.image);
            double latitude = -1, longitude = -1;
            try {
                ExifInterface exifInterface = new ExifInterface(photo.path);

                String length, width;
                length = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
                width = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
                if (length != null && width != null) {
                    if (!width.equals("0") && !length.equals("0")) {
                        exifData.append("\nDimensions: " + width + " x " + length);
                    }
                }
                String temp = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                if (temp != null) {
                    exifData.append("\nDate: " + temp);
                }
                temp = exifInterface.getAttribute(ExifInterface.TAG_COLOR_SPACE);
                if (temp != null) {
                    exifData.append("\nColor space: " + temp);
                }
                temp = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
                if (temp != null) {
                    exifData.append("\nDevice make: " + temp);
                }
                temp = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
                if (temp != null) {
                    exifData.append("\nDevice model: " + temp);
                }
                temp = exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
                if (temp != null) {
                    exifData.append("\nWhite balance: " + temp);
                }
                temp = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
                if (temp != null) {
                    exifData.append("\nFocal length: " + temp);
                }
                temp = exifInterface.getAttribute(ExifInterface.TAG_FLASH);
                if (temp != null) {
                    exifData.append("\nFlash: " + temp);
                }
                /*
                temp = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                if (temp != null) {
                    exifData.append("\nLatitude: " + temp);
                }
                temp = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
                if (temp != null) {
                    exifData.append("\nLongitude: " + temp);
                }*/

                float[] temp2 = new float[2];
                if (exifInterface.getLatLong(temp2)) {
                    latitude = temp2[0];
                    longitude = temp2[1];
                    Pair<String, String> countryCity = getCountryCityName(latitude, longitude);
                    exifData.append("\nLocation: " + countryCity.second + ", " + countryCity.first);
                }

                exifData.append("\nSmart description: " + photo.des);
            } catch (Exception e) {
                e.printStackTrace();
            }

            exifInfo.setText(exifData.toString());
        }
    }


    public Pair<String, String> getCountryCityName(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address obj = addresses.get(0);
        return new Pair<>(obj.getCountryName(), obj.getAdminArea());
    }

}


