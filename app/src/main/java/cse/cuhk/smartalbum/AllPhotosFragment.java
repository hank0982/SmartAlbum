package cse.cuhk.smartalbum;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fivehundredpx.greedolayout.GreedoLayoutManager;
import com.fivehundredpx.greedolayout.GreedoSpacingItemDecoration;

import java.util.ArrayList;

import cse.cuhk.smartalbum.utils.MeasUtils;
import cse.cuhk.smartalbum.utils.Photo;
import cse.cuhk.smartalbum.utils.database.DBHelper;

public class AllPhotosFragment extends Fragment {
    private DBHelper db;
    private ArrayList<Photo> photos;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DBHelper(this.getActivity());
        photos = db.getAllPhotos();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        db = new DBHelper(this.getContext());
        View view = inflater.inflate(R.layout.all_photos_fragment, container, false);
        TextView title = view.findViewById(R.id.all_photos_title);
        title.setX(getResources().getDimensionPixelSize(R.dimen.left_offset));
        title.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "open-sans-extrabold.ttf"));
        PhotoViewAdaptor recyclerAdapter = new PhotoViewAdaptor(this.getActivity(), photos);
        GreedoLayoutManager layoutManager = new GreedoLayoutManager(recyclerAdapter);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.photo_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);
        layoutManager.setFixedHeight(true);

// Set the max row height in pixels
        layoutManager.setMaxRowHeight(600);

// If you would like to add spacing between items (Note, MeasUtils is in the sample project)
        int spacing = MeasUtils.dpToPx(4, this.getActivity());
        recyclerView.addItemDecoration(new GreedoSpacingItemDecoration(spacing));

        return view;
    }

}