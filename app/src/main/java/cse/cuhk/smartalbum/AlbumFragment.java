package cse.cuhk.smartalbum;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.ramotion.cardslider.CardSliderLayoutManager;
import com.ramotion.cardslider.CardSnapHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;

import cse.cuhk.smartalbum.cards.SliderAdapter;
import cse.cuhk.smartalbum.utils.Album;
import cse.cuhk.smartalbum.utils.Photo;
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
        db = new DBHelper(this.getActivity());
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
        sliderAdapter = new SliderAdapter(manulAlbums, manulAlbums.size(), new AlbumFragment.OnCardClickListener(1));
        sliderAdapter_two = new SliderAdapter(autoAlbums, autoAlbums.size(), new AlbumFragment.OnCardClickListener(2));
        initRecyclerView(view);
        initTitleText(view);
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
                    intent.putExtra(AlbumDetailsActivity.BUNDLE_IMAGE_ID, manulAlbums.get(activeCardPosition % manulAlbums.size()).id);
                }else{
                    intent.putExtra(AlbumDetailsActivity.BUNDLE_IMAGE_ID, autoAlbums.get(activeCardPosition % autoAlbums.size()).id);
                }
                startActivity(intent);

            }
        }
    }

}
