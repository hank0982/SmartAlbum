package cse.cuhk.smartalbum.photodetails.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.chip.Chip;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import com.hootsuite.nachos.validator.ChipifyingNachoValidator;

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
    Set<Chip> tagChips = new HashSet<>();
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
        db = new DBHelper(this.getActivity());
        Bundle args = getArguments();
        if (args != null) {
            photoid = args.getInt(ARG_PHOTO, -1);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.photo_details_fragment_bottom, container, false);
        final NachoTextView nachoView = view.findViewById(R.id.photo_details_bottom_nacho_text_view);
        nachoView.addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
        nachoView.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                Set<Chip>newChips = new HashSet<>(nachoView.getAllChips());
                for(Chip oldChip: tagChips){
                    if(!newChips.contains(oldChip)){
                        tagChips.remove(oldChip);
                        ArrayList<Tag> tag = db.searchTagsByName(oldChip.getText().toString(), true);
                        db.removeTagFromPhoto(tag.get(0).id, photoid);
                    }
                }
                for(Chip newChip: newChips){
                    if(!tagChips.contains(newChip)){
                        tagChips.add(newChip);
                        ArrayList<Tag> tag = db.searchTagsByName(newChip.getText().toString(), true);
                        if(tag != null){
                            db.insertTagToPhoto(tag.get(0).id, photoid);
                            db.updateTagCount(tag.get(0).id, tag.get(0).count+1);
                        }else{
                            long rowID = db.insertTag(newChip.getText().toString());
                            db.insertTagToPhoto((int) rowID, photoid);
                        }
                    }
                }
            }
        });
        return view;
    }

}
