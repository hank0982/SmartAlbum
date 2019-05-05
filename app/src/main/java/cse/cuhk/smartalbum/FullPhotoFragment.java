package cse.cuhk.smartalbum;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
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
import androidx.fragment.app.FragmentTransaction;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.bumptech.glide.Glide;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import cse.cuhk.smartalbum.photodetails.InfoActivity;
import cse.cuhk.smartalbum.utils.GlideApp;
import cse.cuhk.smartalbum.utils.Photo;
import cse.cuhk.smartalbum.utils.Tag;
import cse.cuhk.smartalbum.utils.TagSuggestion;
import cse.cuhk.smartalbum.utils.database.DBHelper;

public class FullPhotoFragment extends Fragment {
    int photoID;
    public FullPhotoFragment(int photoID){
        this.photoID = photoID;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    private void startInfoActivity(View view, Photo photo) {
        Activity activity = this.getActivity();
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(activity, new Pair<>(view, getString(R.string.transition_image)));
        ActivityCompat.startActivity(activity,
                InfoActivity.newInstance(activity, photo),
                options.toBundle());
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        DBHelper db = DBHelper.getInstance(this.getContext());
        final View view = inflater.inflate(R.layout.full_photo_fragment, container, false);
        Cursor res = db.getData(photoID, DBHelper.PHOTOS_TABLE_NAME);
        res.moveToFirst();
        final Photo photo = db.convertCursorToPhoto(res);
        ImageView imageView = (ImageView) view.findViewById(R.id.fullphoto_image);
        GlideApp.with(this.getContext()).load(photo.path).into(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startInfoActivity(view, photo);
            }
        });

        return view;
    }
}
