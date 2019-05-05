package cse.cuhk.smartalbum;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.chip.Chip;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import com.ramotion.cardslider.CardSliderLayoutManager;
import com.ramotion.cardslider.CardSnapHelper;

import java.util.ArrayList;
import java.util.List;

import cse.cuhk.smartalbum.cards.SliderAdapter;
import cse.cuhk.smartalbum.utils.Album;
import cse.cuhk.smartalbum.utils.Photo;
import cse.cuhk.smartalbum.utils.Tag;
import cse.cuhk.smartalbum.utils.database.DBHelper;

public class AlbumFragment extends Fragment {
    private DBHelper db;
    private ArrayList<Album> manulAlbums = new ArrayList<>();
    private ArrayList<Album> autoAlbums = new ArrayList<>();

    private SliderAdapter sliderAdapter;
    private SliderAdapter sliderAdapter_two;

    private CardSliderLayoutManager layoutManger;
    private RecyclerView firstRecyclerView;
    private RecyclerView secondRecyclerView;

    private TextView firstTitle1;
    private TextView firstTitle2;

    private TextView secondTitle1;
    private TextView secondTitle2;

    private int titleOffSet1;
    private int titleOffSet2;

    private long titleAnimationDuration;

    private Activity getActivityFromContext(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.album_fragment, container, false);
        db =  DBHelper.getInstance(getActivity());
        ArrayList<Album> albums = db.getAllAlbums();
        for(Album album: albums) {

            if(album.type.equals(Album.MANUAL_ALBUM)){
                manulAlbums.add(album);
            }else{
                autoAlbums.add(album);
            }

        }
        if(manulAlbums.isEmpty()){
            if(db.getAllPhotos() != null && db.getAllPhotos().size() != 0){
                db.insertAlbum(Album.ALL_PHOTOS_ALBUM_NAME,db.getAllPhotos().get(0).path, Album.MANUAL_ALBUM);
            }else{
                db.insertAlbum(Album.ALL_PHOTOS_ALBUM_NAME, Photo.getAllShownImagesPath(getActivityFromContext(container.getContext())).get(0),  Album.MANUAL_ALBUM);
            }
            albums = db.getAllAlbums();
            manulAlbums.clear();
            autoAlbums.clear();
            for(Album album: albums) {
                if(album.type.equals(Album.MANUAL_ALBUM)){
                    manulAlbums.add(album);
                }else{
                    autoAlbums.add(album);
                }

            }
        }
        sliderAdapter = new SliderAdapter(manulAlbums, manulAlbums.size(), new AlbumFragment.OnCardClickListener(1), new AlbumFragment.OnCardLongClickListener(1));
        sliderAdapter_two = new SliderAdapter(autoAlbums, autoAlbums.size(), new AlbumFragment.OnCardClickListener(2), new AlbumFragment.OnCardLongClickListener(2));
        initRecyclerView(view);
        initTitleText(view);

        final ImageButton addAlbumButton = view.findViewById(R.id.add_album_button);
        addAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Add a new album");
                View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_album, (ViewGroup)getView(), false);
                final EditText albumInput = (EditText) viewInflated.findViewById(R.id.album_title_input);
                final NachoTextView tagInput = (NachoTextView) viewInflated.findViewById(R.id.album_tags_input);
                tagInput.addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
                builder.setView(viewInflated);

                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (TextUtils.isEmpty(albumInput.getText())) {
                            albumInput.setError("Album name is required");
                        }
                        else {
                            String albumName = albumInput.getText().toString();
                            ArrayList<Tag> tags = new ArrayList<>();

                            for (Chip chip : tagInput.getAllChips()) {
                                ArrayList<Tag> temp = db.searchTagsByName(chip.getText().toString().toLowerCase(), true);
                                if (temp == null) {
                                    tags.add(db.insertNewTag(chip.getText().toString().toLowerCase()));
                                }
                                else {
                                    tags.add(temp.get(0));
                                }
                            }

                            ArrayList<Integer> photoIDList = db.getPhotoIDsByTags(tags);
                            if (photoIDList == null) {
                                return;
                            }
                            Cursor res = db.getData(photoIDList.get(0), db.PHOTOS_TABLE_NAME);
                            res.moveToFirst();
                            Photo photo = db.convertCursorToPhoto(res);
                            long albumID = db.insertAlbum(albumName, photo.path, Album.MANUAL_ALBUM);
                            Log.d("album name and id", albumName + " " + albumID);
                            for (int photoid: photoIDList) {
                                db.insertPhotoToAlbum(photoid, (int)albumID);
                            }
                        }
                        ((MainActivity) getActivity()).reloadFragment();
                    }
                });

                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                final Button button = builder.show().getButton(AlertDialog.BUTTON_POSITIVE);
                button.setEnabled(false);
                albumInput.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (albumInput.getText().length() > 0) {
                            button.setEnabled(true);
                        }
                        else {
                            button.setEnabled(false);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        });

        return view;

    }

    private void initRecyclerView(View view) {
        firstRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        firstRecyclerView.setAdapter(sliderAdapter);
        firstRecyclerView.setHasFixedSize(true);

        firstRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    onActiveCardChange();
                }
            }
        });

        layoutManger = (CardSliderLayoutManager) firstRecyclerView.getLayoutManager();

        new CardSnapHelper().attachToRecyclerView(firstRecyclerView);

        secondRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view2);
        secondRecyclerView.setAdapter(sliderAdapter_two);
        secondRecyclerView.setHasFixedSize(true);

        secondRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    onActiveCardChange();
                }
            }
        });

        layoutManger = (CardSliderLayoutManager) secondRecyclerView.getLayoutManager();

        new CardSnapHelper().attachToRecyclerView(secondRecyclerView);
    }

    private void initTitleText(View view) {
        titleAnimationDuration = getResources().getInteger(R.integer.labels_animation_duration);
        titleOffSet1 = getResources().getDimensionPixelSize(R.dimen.left_offset);
        titleOffSet2 = getResources().getDimensionPixelSize(R.dimen.card_width);
        firstTitle1 = (TextView) view.findViewById(R.id.tv_country_1);
        firstTitle2 = (TextView) view.findViewById(R.id.tv_country_2);

        firstTitle1.setX(titleOffSet1);
        firstTitle2.setX(titleOffSet2);
        firstTitle1.setText("My Albums");
        firstTitle2.setAlpha(0f);

        firstTitle1.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "open-sans-extrabold.ttf"));
        firstTitle2.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "open-sans-extrabold.ttf"));

        secondTitle1 = (TextView) view.findViewById(R.id.row_two_title);
        secondTitle2 = (TextView) view.findViewById(R.id.row_two_title_buffer);

        secondTitle1.setX(titleOffSet1);
        secondTitle2.setX(titleOffSet2);
        secondTitle1.setText("Auto Albums");
        secondTitle2.setAlpha(0f);

        secondTitle1.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "open-sans-extrabold.ttf"));
        secondTitle2.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "open-sans-extrabold.ttf"));
    }



    private class OnCardClickListener implements View.OnClickListener {
        private int row;
        public OnCardClickListener(int row){
            super();
            this.row = row;

        }
        public void onClick(View view) {
            CardSliderLayoutManager lm;
            if(row == 1){
                lm =  (CardSliderLayoutManager) firstRecyclerView.getLayoutManager();
            }else{
                lm =  (CardSliderLayoutManager) secondRecyclerView.getLayoutManager();
            }

            if (lm.isSmoothScrolling()) {
                return;
            }

            final int activeCardPosition = lm.getActiveCardPosition();
            if (activeCardPosition == RecyclerView.NO_POSITION) {
                return;
            }
            int clickedPosition;
            if(row == 1){
                clickedPosition = firstRecyclerView.getChildAdapterPosition(view);
            }else{
                clickedPosition = secondRecyclerView.getChildAdapterPosition(view);
            }
            if (clickedPosition == activeCardPosition) {
                final Intent intent = new Intent(getActivity(), AlbumDetailsActivity.class);
                if(row == 1){
                    intent.putExtra(AlbumDetailsActivity.BUNDLE_IMAGE_ID, manulAlbums.get(activeCardPosition % manulAlbums.size()).id);
                }else{
                    intent.putExtra(AlbumDetailsActivity.BUNDLE_IMAGE_ID, autoAlbums.get(activeCardPosition % autoAlbums.size()).id);
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(intent);
                } else {
                    final CardView cardView = (CardView) view;
                    final View sharedView = cardView.getChildAt(cardView.getChildCount() - 1);
                    final ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation(getActivity(), sharedView, "shared");
                    startActivity(intent, options.toBundle());
                }
            } else if (clickedPosition > activeCardPosition) {

                if(row == 1){
                    firstRecyclerView.smoothScrollToPosition(clickedPosition);
                }else{
                    secondRecyclerView.smoothScrollToPosition(clickedPosition);
                }
                final Intent intent = new Intent(getActivity(), AlbumDetailsActivity.class);
                if(row == 1){
                    intent.putExtra(AlbumDetailsActivity.BUNDLE_IMAGE_ID, manulAlbums.get(clickedPosition % manulAlbums.size()).id);
                }else{
                    intent.putExtra(AlbumDetailsActivity.BUNDLE_IMAGE_ID, autoAlbums.get(clickedPosition % autoAlbums.size()).id);
                }
                startActivity(intent);
            }
        }
    }

    private class OnCardLongClickListener implements View.OnLongClickListener {

        private int row;
        public OnCardLongClickListener(int row){
            super();
            this.row = row;

        }

        public void showDialog(String albumName, final int albumID) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Are you sure you want to delete the " + albumName + " album?").setTitle("Delete album?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    db.deleteAlbum(albumID);
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.create().show();
        }

        @Override
        public boolean onLongClick(View v) {
            CardSliderLayoutManager lm;
            if(row == 1){
                lm =  (CardSliderLayoutManager) firstRecyclerView.getLayoutManager();
            }else{
                lm =  (CardSliderLayoutManager) secondRecyclerView.getLayoutManager();
            }

            if (lm.isSmoothScrolling()) {
                return false;
            }

            final int activeCardPosition = lm.getActiveCardPosition();
            if (activeCardPosition == RecyclerView.NO_POSITION) {
                return false;
            }
            int clickedPosition;
            if(row == 1){
                clickedPosition = firstRecyclerView.getChildAdapterPosition(v);
            }else{
                clickedPosition = secondRecyclerView.getChildAdapterPosition(v);
            }
            if (clickedPosition == activeCardPosition) {
                if(row == 1){
                    final Album album = manulAlbums.get(activeCardPosition % manulAlbums.size());
                    if (album.name.equals(Album.ALL_PHOTOS_ALBUM_NAME)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("All Photos album cannot be deleted");
                        builder.create().show();
                    }
                    else {
                        showDialog(album.name, album.id);
                    }
                }else{
                    final Album album = autoAlbums.get(activeCardPosition % autoAlbums.size());
                    showDialog(album.name, album.id);
                }
            } else if (clickedPosition > activeCardPosition) {
                if(row == 1){
                    firstRecyclerView.smoothScrollToPosition(clickedPosition);
                }else{
                    secondRecyclerView.smoothScrollToPosition(clickedPosition);
                }
                if(row == 1){
                    final Album album = manulAlbums.get(activeCardPosition % manulAlbums.size());
                    if (album.name.equals(Album.ALL_PHOTOS_ALBUM_NAME)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("All Photos album cannot be deleted");
                        builder.create().show();
                    }
                    else {
                        showDialog(album.name, album.id);
                    }
                }else{
                    final Album album = autoAlbums.get(activeCardPosition % autoAlbums.size());
                    showDialog(album.name, album.id);
                }
            }
            return true;
        }
    }
}
