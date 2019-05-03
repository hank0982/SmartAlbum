package cse.cuhk.smartalbum;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.gigamole.navigationtabstrip.NavigationTabStrip;
import com.ramotion.cardslider.CardSliderLayoutManager;
import com.ramotion.cardslider.CardSnapHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;

import cse.cuhk.smartalbum.cards.SliderAdapter;


public class MainActivity extends AppCompatActivity {
    final int REQUEST_PERMISSIONS = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE))) {
                showExplanation("Permission Needed", "Please Allow The Storage Permission For Accessing Your Photos", REQUEST_PERMISSIONS);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);

            }
        }else{
            sendImageListToService();
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            trans.add(R.id.fragment_container, new AlbumFragment());
            trans.commit();
            initMenu();
        }
    }
    public void sendImageListToService(){
        Intent i=new Intent(getBaseContext(),  InitUpdateService.class);
        startService(i);
    }
    public void startUpdateService(View view) {
        startService(new Intent(getBaseContext(), InitUpdateService.class));
    }

    // Method to stop the service
    public void stopUpdateService(View view) {
        stopService(new Intent(getBaseContext(), InitUpdateService.class));
    }
    private void switchFragment(int index){
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();

        switch (index){
            case 1:
                trans.replace(R.id.fragment_container, new AlbumFragment());
                break;
            case 0:
                trans.replace(R.id.fragment_container, new SearchFragment());
                break;
            case 2:
                trans.replace(R.id.fragment_container, new AllPhotosFragment());
                break;
        }
        trans.commit();
    }

    private void showExplanation(String title,
                                 String message,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                                permissionRequestCode);                    }
                });
        builder.create().show();
    }

    private void initMenu(){
        NavigationTabStrip menu = (NavigationTabStrip) findViewById(R.id.main_slide_menu);
        menu.setTabIndex(1);
        menu.setOnTabStripSelectedIndexListener(new NavigationTabStrip.OnTabStripSelectedIndexListener() {

            @Override
            public void onStartTabSelected(String title, int index) {
                switchFragment(index);
            }

            @Override
            public void onEndTabSelected(String title, int index) {

            }
        });
    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }



}
