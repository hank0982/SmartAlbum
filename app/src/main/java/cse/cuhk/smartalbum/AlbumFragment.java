package cse.cuhk.smartalbum;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
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

import java.util.ArrayList;

import cse.cuhk.smartalbum.cards.SliderAdapter;

public class AlbumFragment extends Fragment {
    private final int[] pics = {R.drawable.p1, R.drawable.p2, R.drawable.p3};
    private final int[] pics2 = {R.drawable.people, R.drawable.p4, R.drawable.p5};

    private final SliderAdapter sliderAdapter = new SliderAdapter(pics, 20, new AlbumFragment.OnCardClickListener(1));
    private final SliderAdapter sliderAdapter_two = new SliderAdapter(pics2, 20, new AlbumFragment.OnCardClickListener(2));

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(AlbumFragment.this.getClass().getName(), "onCreateView");
        View view = inflater.inflate(R.layout.album_fragment, container, false);
        initRecyclerView(view);
        initTitleText(view);
        return view;

    }



    private ArrayList<String> getAllShownImagesPath(Activity activity) {

        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = activity.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;

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
                final Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.BUNDLE_IMAGE_ID, pics[activeCardPosition % pics.length]);

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
                final Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.BUNDLE_IMAGE_ID, pics[clickedPosition % pics.length]);

                startActivity(intent);

            }
        }
    }

}
