package cse.cuhk.smartalbum.photodetails.adapter;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.qslll.library.ExpandingViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import cse.cuhk.smartalbum.photodetails.fragments.TravelExpandingFragment;
import cse.cuhk.smartalbum.photodetails.model.Travel;
import cse.cuhk.smartalbum.utils.Photo;

/**
 * Created by Qs on 16/5/30.
 */
public class TravelViewPagerAdapter extends ExpandingViewPagerAdapter {

    List<Integer> photos;

    public TravelViewPagerAdapter(FragmentManager fm) {
        super(fm);
        photos = new ArrayList<>();
    }

    public void addAll(List<Integer> photos){
        this.photos.addAll(photos);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        int photoid = photos.get(position);
        return TravelExpandingFragment.newInstance(photoid);
    }

    @Override
    public int getCount() {
        return photos.size();
    }

}
