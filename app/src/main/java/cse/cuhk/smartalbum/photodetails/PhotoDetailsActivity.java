package cse.cuhk.smartalbum.photodetails;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.viewpager.widget.ViewPager;

import com.qslll.library.ExpandingPagerFactory;
import com.qslll.library.fragments.ExpandingFragment;

import java.util.ArrayList;
import java.util.List;

import cse.cuhk.smartalbum.R;
import cse.cuhk.smartalbum.photodetails.adapter.TravelViewPagerAdapter;
import cse.cuhk.smartalbum.photodetails.model.Travel;
import cse.cuhk.smartalbum.utils.Photo;
import cse.cuhk.smartalbum.utils.database.DBHelper;

public class PhotoDetailsActivity extends AppCompatActivity implements ExpandingFragment.OnExpandingClickListener{
    public static final String PHOTOS_ARRAY = "cse.cuhk.smartalbum.photo_array";
    public static final String PHOTO_ID = "cse.cuhk.smartalbum.photo_id";
    private ViewPager viewPager;
    private ViewGroup back;
    private ArrayList<Photo> photos;
    private DBHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Create","created");
        db = new DBHelper(this);
        setContentView(R.layout.photo_details_main);
        viewPager = findViewById(R.id.photo_details_viewPager);
        back = findViewById(R.id.photo_details_back);
        setupWindowAnimations();
//        photos = db.getAllPhotos();
        final ArrayList<Integer> photos = getIntent().getIntegerArrayListExtra(PHOTOS_ARRAY);
        final int position = getIntent().getIntExtra(PHOTO_ID, -999);
        TravelViewPagerAdapter adapter = new TravelViewPagerAdapter(getSupportFragmentManager());
        adapter.addAll(photos);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);

        ExpandingPagerFactory.setupViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                ExpandingFragment expandingFragment = ExpandingPagerFactory.getCurrentFragment(viewPager);
                if(expandingFragment != null && expandingFragment.isOpenend()){
                    expandingFragment.close();
                }
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if(!ExpandingPagerFactory.onBackPressed(viewPager)){
            super.onBackPressed();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
        Explode slideTransition = new Explode();
        getWindow().setReenterTransition(slideTransition);
        getWindow().setExitTransition(slideTransition);
    }

    @SuppressWarnings("unchecked")
    private void startInfoActivity(View view, Photo photo) {
        Activity activity = this;
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(activity, new Pair<>(view, getString(R.string.transition_image)));
        ActivityCompat.startActivity(activity,
                InfoActivity.newInstance(activity, photo),
                options.toBundle());
    }

    @Override
    public void onExpandingClick(View v) {
        //v is expandingfragment layout
        View view = v.findViewById(R.id.photo_details_sharedImage);
        Photo photo = photos.get(viewPager.getCurrentItem());
        startInfoActivity(view,photo);
    }
}
