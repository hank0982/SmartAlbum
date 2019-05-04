package cse.cuhk.smartalbum.photodetails.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.chip.Chip;
import com.hootsuite.nachos.chip.ChipInfo;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cse.cuhk.smartalbum.R;
import cse.cuhk.smartalbum.utils.Tag;
import cse.cuhk.smartalbum.utils.database.DBHelper;


public class FragmentBottom extends Fragment {
    static final String ARG_PHOTO = "ARG_PHOTO";
    int photoid;
    private DBHelper db;
    public static FragmentBottom newInstance(int photoid) {
        Bundle args = new Bundle();
        FragmentBottom fragmentBottom = new FragmentBottom();
        args.putInt(ARG_PHOTO, photoid);
        fragmentBottom.setArguments(args);
        return fragmentBottom;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db =  DBHelper.getInstance(getActivity());
        Bundle args = getArguments();
        if (args != null) {
            photoid = args.getInt(ARG_PHOTO, -1);
        }
    }
    private void updateTags( Set<Chip> newChips, Set<Chip> oldChips){
        ArrayList<Chip> pendingRemoveChip = new ArrayList<>();
        for(Chip oldChip: oldChips){
            if(!newChips.contains(oldChip)){
                pendingRemoveChip.add(oldChip);
                ArrayList<Tag> tag = db.searchTagsByName(oldChip.getText().toString(), true);
                db.removeTagFromPhoto(tag.get(0).id, photoid);
            }
        }
        oldChips.removeAll(pendingRemoveChip);

        for(Chip newChip: newChips){
            if(!oldChips.contains(newChip)){
                oldChips.add(newChip);
                ArrayList<Tag> tag = db.searchTagsByName(newChip.getText().toString(), true);
                if(tag != null){
                    db.insertTagToPhoto(tag.get(0).id, photoid);
                    db.updateTagCount(tag.get(0).id, tag.get(0).count+1);
                }else{
                    long rowID = db.insertTag(newChip.getText().toString(), true);
                    db.insertTagToPhoto((int) rowID, photoid);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.photo_details_fragment_bottom, container, false);
        final NachoTextView nachoView = view.findViewById(R.id.photo_details_bottom_nacho_text_view);
        new analyzeImage(photoid){
            @Override
            protected void onPostExecute(ArrayList<Tag> data) {
                super.onPostExecute(data);
                ArrayList<ChipInfo> chips = new ArrayList<>();
                for(Tag tag: data){
                    Log.d("TAG",tag.name);
                    chips.add(new ChipInfo(tag.name, tag.id));
                }
                nachoView.setTextWithChips(chips);

                nachoView.setSaveEnabled(false);
                nachoView.addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
//                nachoView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                    @Override
//                    public void onFocusChange(View view, boolean hasFocus) {
//                        Log.d("Focus","FOCUS CHANGE");
//                        if (!hasFocus) {
//                            Set<Chip> oldChips = new HashSet<>(nachoView.getAllChips());
//                            new analyzeImage(new HashSet<>(nachoView.getAllChips()), oldChips).execute("UPDATE_TAGS");
//                        }
//                    }
//                });


            }
        }.execute("LOAD_TAG_FROM_DB");

        return view;
    }
    private class analyzeImage extends AsyncTask<String, String, ArrayList<Tag>> {
        // Store error message
        private Exception e = null;
        private int photoId;
        private Set<Chip> newChips;
        private Set<Chip> oldChips;

        private int sleepTime = 3000;

        public analyzeImage(int photoId) {
            this.photoId = photoId;
        }
        public analyzeImage(Set<Chip> newChips, Set<Chip> oldChips){
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
                updateTags(newChips, oldChips);
                return new ArrayList<Tag>();
            }

        }
    }
}