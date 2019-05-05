package cse.cuhk.smartalbum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
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
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.chip.Chip;
import com.hootsuite.nachos.chip.ChipInfo;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cse.cuhk.smartalbum.photodetails.InfoActivity;
import cse.cuhk.smartalbum.photodetails.fragments.FragmentBottom;
import cse.cuhk.smartalbum.utils.GlideApp;
import cse.cuhk.smartalbum.utils.Photo;
import cse.cuhk.smartalbum.utils.Tag;
import cse.cuhk.smartalbum.utils.TagSuggestion;
import cse.cuhk.smartalbum.utils.database.DBHelper;

import static androidx.core.content.ContextCompat.getSystemService;

public class FullPhotoFragment extends Fragment {
    int photoID;
    int position;
    DBHelper db;
    ArrayList<Integer> photosIDs;

    public FullPhotoFragment(int photoID, ArrayList<Integer> photosIDs, int position){
        this.photoID = photoID;
        this.photosIDs = photosIDs;
        this.position = position;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = DBHelper.getInstance(this.getContext());
    }
    private void startInfoActivity(View view, Photo photo) {
        final Intent intent = new Intent(getActivity(), cse.cuhk.smartalbum.photodetails.PhotoDetailsActivity.class);
        intent.putExtra(cse.cuhk.smartalbum.photodetails.PhotoDetailsActivity.PHOTOS_ARRAY, photosIDs);
        intent.putExtra(cse.cuhk.smartalbum.photodetails.PhotoDetailsActivity.PHOTO_ID, position);
        Activity activity = this.getActivity();
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(activity, new Pair<>(view, getString(R.string.transition_image)));
        ActivityCompat.startActivity(activity,
                intent,
                options.toBundle());
        activity.finish();
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
                view.findViewById(R.id.fullphoto_image_nacho_text).setVisibility(View.VISIBLE);
                InputMethodManager imm = (InputMethodManager) getContext().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);

            }
        });
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startInfoActivity(view, photo);
                return false;
            }
        });
        final NachoTextView nachoView = view.findViewById(R.id.fullphoto_image_nacho_text);
        nachoView.setVisibility(View.GONE);
        nachoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startInfoActivity(view, photo);
                return false;
            }
        });
        new getChip(photoID){
            @Override
            protected void onPostExecute(ArrayList<Tag> data) {
                super.onPostExecute(data);
                ArrayList<ChipInfo> chips = new ArrayList<>();
                for(Tag tag: data){
                    Log.d("TAG",tag.name);
                    chips.add(new ChipInfo(tag.name, tag.id));
                }
                nachoView.setTextWithChips(chips);
                final Set<Chip> oldChips = new HashSet<>(nachoView.getAllChips());

                nachoView.setSaveEnabled(false);
                nachoView.addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
                nachoView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        if (!hasFocus) {
                            Log.d("Focus","FOCUS CHANGE");
                            new getChip(new HashSet<>(nachoView.getAllChips()), oldChips).execute("UPDATE_TAGS");
                        }
                    }
                });
            }
        }.execute("LOAD_TAG_FROM_DB");
        return view;
    }
    private void updateTags( Set<Chip> newChips, Set<Chip> oldChips){
        ArrayList<Chip> pendingRemoveChip = new ArrayList<>();
        for(Chip oldChip: oldChips){
            if(!newChips.contains(oldChip)){
                pendingRemoveChip.add(oldChip);
                ArrayList<Tag> tag = db.searchTagsByName(oldChip.getText().toString(), true);
                Log.d("aboutToDeletedTag", tag.get(0).name);
                db.removeTagFromPhoto(tag.get(0).id, photoID);
            }
        }
        oldChips.removeAll(pendingRemoveChip);

        for(Chip newChip: newChips){
            if(!oldChips.contains(newChip)){
                oldChips.add(newChip);
                ArrayList<Tag> tag = db.searchTagsByName(newChip.getText().toString(), true);
                if(tag != null){
                    db.insertTagToPhoto(tag.get(0).id, photoID);
                    db.updateTagCount(tag.get(0).id, tag.get(0).count+1);
                }else{
                    ArrayList<Long> rowIDs = db.insertTag(newChip.getText().toString(), true);
                    db.insertTagToPhoto(rowIDs.get(0), photoID);
                    if(rowIDs.size()==2){
                        boolean result = db.insertPhotoToAlbum(photoID, rowIDs.get(1).intValue());
                        if (result) {
                            db.updateAlbumCoverPhoto(photoID, rowIDs.get(1).intValue());
                        }
                    }
                }
            }
        }
    }
    private class getChip extends AsyncTask<String, String, ArrayList<Tag>> {
        // Store error message
        private Exception e = null;
        private int photoId;
        private Set<Chip> newChips;
        private Set<Chip> oldChips;

        public getChip(int photoId) {
            this.photoId = photoId;
        }
        public getChip(Set<Chip> newChips, Set<Chip> oldChips){
            this.newChips = newChips;
            this.oldChips = oldChips;
        }
        @Override
        protected ArrayList<Tag> doInBackground(String... strings) {
            Log.d("DOBACK", strings[0]);
            if(strings[0].equals("LOAD_TAG_FROM_DB")){
                ArrayList<Tag> tags = db.getTagsFromPhoto(this.photoId);
                return tags;
            }else{
                Log.d("newchips", String.valueOf(newChips));
                Log.d("oldchips", String.valueOf(oldChips));
                updateTags(newChips, oldChips);
                return new ArrayList<Tag>();
            }

        }
    }
}
