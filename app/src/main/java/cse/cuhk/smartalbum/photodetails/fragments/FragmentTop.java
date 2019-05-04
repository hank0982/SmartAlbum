package cse.cuhk.smartalbum.photodetails.fragments;

import android.database.Cursor;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import cse.cuhk.smartalbum.R;
import cse.cuhk.smartalbum.photodetails.InfoActivity;
import cse.cuhk.smartalbum.photodetails.model.Travel;
import cse.cuhk.smartalbum.utils.GlideApp;
import cse.cuhk.smartalbum.utils.Photo;
import cse.cuhk.smartalbum.utils.database.DBHelper;

public class FragmentTop extends Fragment {

    static final String ARG_PHOTO = "ARG_PHOTO";
    int photoid;

    private ImageView image;
    // private TextView title;

    public static FragmentTop newInstance(int photoid) {
        Bundle args = new Bundle();
        FragmentTop fragmentTop = new FragmentTop();
        args.putInt(ARG_PHOTO, photoid);
        fragmentTop.setArguments(args);
        return fragmentTop;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            photoid = args.getInt(ARG_PHOTO, -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.photo_details_fragment_front, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.image = view.findViewById(R.id.photo_details_sharedImage);
        // this.title = view.findViewById(R.id.photo_details_fragment_front_title);
        DBHelper db = DBHelper.getInstance(view.getContext());
        if (photoid != -1) {
            Cursor res = db.getData(photoid, DBHelper.PHOTOS_TABLE_NAME);
            res.moveToFirst();
            Photo photo = DBHelper.convertCursorToPhoto(res);
            GlideApp.with(view).load(photo.path).into(this.image);
            // title.setText(photo.name);
        }

    }
    private void startInfoActivity(View view, Photo photo) {
        FragmentActivity activity = getActivity();
        ActivityCompat.startActivity(activity,
            InfoActivity.newInstance(activity, photo),
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                new Pair<>(view, getString(R.string.transition_image)))
                .toBundle());
    }
}
