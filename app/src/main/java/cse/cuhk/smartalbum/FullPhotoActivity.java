package cse.cuhk.smartalbum;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Array;
import java.util.ArrayList;

import cse.cuhk.smartalbum.photodetails.PhotoDetailsActivity;
import cse.cuhk.smartalbum.utils.GlideApp;
import cse.cuhk.smartalbum.utils.Photo;

public class FullPhotoActivity extends AppCompatActivity {
    FragmentPagerAdapter adapterViewPager;
    ViewPager vpPager;
    ArrayList<Integer> photos;
    public static final String PHOTOS_ARRAY = "PHOTOS_ARRAY";
    public static final String PHOTO_POSITION = "PHOTO_POSITION";
    private class MyPagerAdapter extends FragmentPagerAdapter {
        private int itemNum;
        private ArrayList<Integer> photos;
        private int position;
        public MyPagerAdapter(FragmentManager fragmentManager, ArrayList<Integer> photos) {
            super(fragmentManager);
            this.photos = photos;
            this.itemNum = photos.size();
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return itemNum;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            return new FullPhotoFragment(photos.get(position));
        }



    }

//    public static Intent newInstance(Context context, Photo photo) {
//        Intent intent = new Intent(context, cse.cuhk.smartalbum.photodetails.InfoActivity.class);
//        intent.putExtra(EXTRA_PHOTO, photo);
//        return intent;
//    }
    public void onBackPressed(){
        final Intent intent = new Intent(this, PhotoDetailsActivity.class);
        intent.putExtra(PhotoDetailsActivity.PHOTOS_ARRAY, photos);
        intent.putExtra(PhotoDetailsActivity.PHOTO_ID, vpPager.getCurrentItem());
        startActivity(intent);
        vpPager = null;
        photos = null;
        this.finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Created", "Created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_photo_layout);
        vpPager = (ViewPager) findViewById(R.id.vpPager);
        photos = getIntent().getIntegerArrayListExtra(PHOTOS_ARRAY);
        int position = getIntent().getIntExtra(PHOTO_POSITION, -999);
        Log.d("POSITION", String.valueOf(position));
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(), photos);
        vpPager.setAdapter(adapterViewPager);
        vpPager.setCurrentItem(position);
    }
}