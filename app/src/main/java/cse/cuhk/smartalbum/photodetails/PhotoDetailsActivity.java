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

public class PhotoDetailsActivity extends AppCompatActivity implements ExpandingFragment.OnExpandingClickListener{

    private ViewPager viewPager;
    private ViewGroup back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_details_main);
        viewPager = findViewById(R.id.photo_details_viewPager);
        back = findViewById(R.id.photo_details_back);
        setupWindowAnimations();

        TravelViewPagerAdapter adapter = new TravelViewPagerAdapter(getSupportFragmentManager());
        adapter.addAll(generateTravelList());
        viewPager.setAdapter(adapter);


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

    private List<Travel> generateTravelList(){
        List<Travel> travels = new ArrayList<>();
        for(int i=0;i<5;++i){
            travels.add(new Travel("Seychelles", R.drawable.seychelles));
            travels.add(new Travel("Shang Hai", R.drawable.p2));
            travels.add(new Travel("New York", R.drawable.p3));
            travels.add(new Travel("castle", R.drawable.p1));
        }
        return travels;
    }
    @SuppressWarnings("unchecked")
    private void startInfoActivity(View view, Travel travel) {
        Activity activity = this;
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(activity, new Pair<>(view, getString(R.string.transition_image)));
        Log.d("STARTINFOACTIVITY", String.valueOf(options.toBundle()));
        ActivityCompat.startActivity(activity,
                InfoActivity.newInstance(activity, travel),
                options.toBundle());
    }

    @Override
    public void onExpandingClick(View v) {
        //v is expandingfragment layout
        View view = v.findViewById(R.id.photo_details_sharedImage);
        Log.d("TEST", "onExpandingClick: "+view);
        Travel travel = generateTravelList().get(viewPager.getCurrentItem());
        startInfoActivity(view,travel);
    }
}
