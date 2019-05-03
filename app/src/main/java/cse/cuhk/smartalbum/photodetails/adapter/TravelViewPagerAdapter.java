package cse.cuhk.smartalbum.photodetails.adapter;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.qslll.library.ExpandingViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import cse.cuhk.smartalbum.photodetails.fragments.TravelExpandingFragment;
import cse.cuhk.smartalbum.photodetails.model.Travel;

/**
 * Created by Qs on 16/5/30.
 */
public class TravelViewPagerAdapter extends ExpandingViewPagerAdapter {

    List<Travel> travels;

    public TravelViewPagerAdapter(FragmentManager fm) {
        super(fm);
        travels = new ArrayList<>();
    }

    public void addAll(List<Travel> travels){
        this.travels.addAll(travels);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        Travel travel = travels.get(position);
        return TravelExpandingFragment.newInstance(travel);
    }

    @Override
    public int getCount() {
        return travels.size();
    }

}
