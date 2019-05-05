package cse.cuhk.smartalbum;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fivehundredpx.greedolayout.GreedoLayoutManager;
import com.fivehundredpx.greedolayout.GreedoSpacingItemDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cse.cuhk.smartalbum.photodetails.PhotoDetailsActivity;
import cse.cuhk.smartalbum.utils.Album;
import cse.cuhk.smartalbum.utils.MeasUtils;
import cse.cuhk.smartalbum.utils.Photo;
import cse.cuhk.smartalbum.utils.RecyclerItemClickListener;
import cse.cuhk.smartalbum.utils.database.DBHelper;

public class AllPhotosFragment extends Fragment {
    private DBHelper db;
    private ArrayList<Photo> photos;
    private String title;
    private int albumID;
    public AllPhotosFragment(){
        db = DBHelper.getInstance(this.getContext());
        this.albumID = albumID;
        this.title = "All Photos";
        this.photos = db.getAllPhotos();
    }
    public AllPhotosFragment(int AlbumID, String title){
        db = DBHelper.getInstance(this.getContext());
        this.albumID = albumID;
        this.photos = db.getPhotosInAlbum(AlbumID);
        this.title = title;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("onCreateVIew","CreateView");
        View view = inflater.inflate(R.layout.all_photos_fragment, container, false);
        TextView title = view.findViewById(R.id.all_photos_title);
        title.setText(this.title);
        title.setX(getResources().getDimensionPixelSize(R.dimen.left_offset));
        title.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "open-sans-extrabold.ttf"));
        for(Photo photo: photos){
            Log.d("DES",photo.des);
            Log.d("NAME",photo.name);
            Log.d("ID", String.valueOf(photo.id));
            Log.d("DES",photo.path);

        }
        PhotoViewAdaptor recyclerAdapter = new PhotoViewAdaptor(this.getActivity(), photos);
        final GreedoLayoutManager layoutManager = new GreedoLayoutManager(recyclerAdapter);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.photo_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(container.getContext(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        final Intent intent = new Intent(getActivity(), PhotoDetailsActivity.class);
                        ArrayList<Integer> photoids = new ArrayList<>();
                        for (Photo photo: photos){
                            photoids.add(photo.id);
                        }
                        intent.putExtra(PhotoDetailsActivity.PHOTOS_ARRAY, photoids);
                        intent.putExtra(PhotoDetailsActivity.PHOTO_ID, position);
                        Log.d("test", "test");
                        startActivity(intent);
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        final int photoID = photos.get(position).id;
                        ArrayList<Album> albums = db.getAllAlbums();
                        final Map<Integer, Integer> map = new HashMap<Integer, Integer>();
                        final CharSequence[] albumCharSeq = new CharSequence[albums.size()];
                        int i = 0;
                        for (Album album: albums) {
                            albumCharSeq[i] = album.name;
                            map.put(i, album.id);
                            i++;
                        }
                        final ArrayList<Integer> selectedAlbums = new ArrayList<>();
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Add this photo to album(s)");
                        builder.setMultiChoiceItems(albumCharSeq, null, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    selectedAlbums.add(which);
                                }
                                else if (selectedAlbums.contains(which)) {
                                    selectedAlbums.remove(Integer.valueOf(which));
                                }
                            }
                        }).
                        setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (Integer index: selectedAlbums) {
                                    if (map == null) {
                                        Log.d("Map", "null");
                                        return;
                                    }
                                    Integer albumID = map.get(index);
                                    if (albumID == null) {
                                        Log.d("AlbumID", "null");
                                        return;
                                    }
                                    boolean result = db.insertPhotoToAlbum(photoID, albumID);
                                    if (result) {
                                        db.updateAlbumCoverPhoto(photoID, albumID);
                                    }
                                }
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.create().show();
                    }
                })
        );
        layoutManager.setFixedHeight(true);

// Set the max row height in pixels
        layoutManager.setMaxRowHeight(600);

// If you would like to add spacing between items (Note, MeasUtils is in the sample project)
        int spacing = MeasUtils.dpToPx(4, this.getActivity());
        recyclerView.addItemDecoration(new GreedoSpacingItemDecoration(spacing));

        return view;
    }

}