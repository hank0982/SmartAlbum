package cse.cuhk.smartalbum.photodetails.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.qslll.library.fragments.ExpandingFragment;

import cse.cuhk.smartalbum.photodetails.model.Travel;
import cse.cuhk.smartalbum.utils.Photo;

/**
 * this is control fragment , Top and Bottom is child in it.
 *
 * Created by florentchampigny on 21/06/2016.
 */
public class TravelExpandingFragment extends ExpandingFragment {

    static final String ARG_PHOTO = "ARG_PHOTO";
    int photoid;

    public static TravelExpandingFragment newInstance(Integer photoid){
        TravelExpandingFragment fragment = new TravelExpandingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PHOTO, photoid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(args != null) {
            photoid = args.getInt(ARG_PHOTO);
        }
    }

    /**
     * include TopFragment
     * @return
     */
    @Override
    public Fragment getFragmentTop() {
        return FragmentTop.newInstance(photoid);
    }

    /**
     * include BottomFragment
     * @return
     */
    @Override
    public Fragment getFragmentBottom() {
        return FragmentBottom.newInstance(photoid);
    }
}
