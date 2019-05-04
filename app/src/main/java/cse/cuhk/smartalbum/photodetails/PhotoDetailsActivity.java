package cse.cuhk.smartalbum.photodetails;

import android.annotation.TargetApi;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;

import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.viewpager.widget.ViewPager;

import com.qslll.library.ExpandingPagerFactory;
import com.qslll.library.fragments.ExpandingFragment;

import java.util.ArrayList;
import java.util.Currency;
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
    private int[] colors = {1694446387, 1694472499, 1694498611, 1687813939, 1681129267, 1681129369, 1681129471, 1681103359, 1681077247, 1687761919, 1694446591, 1694446489};
    private ViewGroup back;
    private ArrayList<Integer> photos;
    private DBHelper db;
    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db =  DBHelper.getInstance(getBaseContext());
        setContentView(R.layout.photo_details_main);
        viewPager = findViewById(R.id.photo_details_viewPager);
        back = findViewById(R.id.photo_details_back);
        setupWindowAnimations();
//        photos = db.getAllPhotos();
        photos = getIntent().getIntegerArrayListExtra(PHOTOS_ARRAY);
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
                GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[] {manipulateColor(colors[position%12], 0.3f),manipulateColor(colors[(position+1)%12], 0.3f)});

                back.setBackground(gradient);
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
        Cursor res = db.getData(photos.get(viewPager.getCurrentItem()), DBHelper.PHOTOS_TABLE_NAME);
        res.moveToFirst();
        Photo photo = DBHelper.convertCursorToPhoto(res);
        startInfoActivity(view,photo);
    }
}
