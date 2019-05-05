package cse.cuhk.smartalbum.photodetails.fragments;

import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.chip.Chip;
import com.hootsuite.nachos.chip.ChipInfo;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import cse.cuhk.smartalbum.MainActivity;
import cse.cuhk.smartalbum.R;
import cse.cuhk.smartalbum.utils.GlideApp;
import cse.cuhk.smartalbum.utils.Photo;
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
                Log.d("aboutToDeletedTag", tag.get(0).name);
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
                    ArrayList<Long> rowIDs = db.insertTag(newChip.getText().toString(), true);
                    db.insertTagToPhoto(rowIDs.get(0), photoid);
                    if(rowIDs.size()==2){
                        boolean result = db.insertPhotoToAlbum(photoid, rowIDs.get(1).intValue());
                        if (result) {
                            db.updateAlbumCoverPhoto(photoid, rowIDs.get(1).intValue());
                            ((MainActivity) getActivity()).reloadFragment();
                        }
                    }
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.photo_details_fragment_bottom, container, false);
//        final NachoTextView nachoView = view.findViewById(R.id.photo_details_bottom_nacho_text_view);
//        new analyzeImage(photoid){
//            @Override
//            protected void onPostExecute(ArrayList<Tag> data) {
//                super.onPostExecute(data);
//                ArrayList<ChipInfo> chips = new ArrayList<>();
//                for(Tag tag: data){
//                    Log.d("TAG",tag.name);
//                    chips.add(new ChipInfo(tag.name, tag.id));
//                }
//                nachoView.setTextWithChips(chips);
//                final Set<Chip> oldChips = new HashSet<>(nachoView.getAllChips());
//
//                nachoView.setSaveEnabled(false);
//                nachoView.addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
//                nachoView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                    @Override
//                    public void onFocusChange(View view, boolean hasFocus) {
//                        if (!hasFocus) {
//                            Log.d("Focus","FOCUS CHANGE");
//                            new analyzeImage(new HashSet<>(nachoView.getAllChips()), oldChips).execute("UPDATE_TAGS");
//                        }
//                    }
//                });
//
//
//            }
//        }.execute("LOAD_TAG_FROM_DB");
        TextView exifInfo = view.findViewById(R.id.photo_details_activity_exif);
        Cursor res = db.getData(photoid, DBHelper.PHOTOS_TABLE_NAME);
        res.moveToFirst();
        Photo photo = DBHelper.convertCursorToPhoto(res);
        StringBuilder exifData = new StringBuilder();

        if (photo != null) {
            exifData.append("File path: " + photo.name);

            double latitude = -1, longitude = -1;
            try {
                ExifInterface exifInterface = new ExifInterface(photo.path);

                String length, width;
                length = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
                width = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
                if (length != null && width != null) {
                    if (!width.equals("0") && !length.equals("0")) {
                        exifData.append("\nDimensions: " + width + " x " + length);
                    }
                }
                String temp = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                if (temp != null) {
                    exifData.append("\nDate: " + temp);
                }
                temp = exifInterface.getAttribute(ExifInterface.TAG_COLOR_SPACE);
                if (temp != null) {
                    exifData.append("\nColor space: " + temp);
                }
                temp = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
                if (temp != null) {
                    exifData.append("\nDevice make: " + temp);
                }
                temp = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
                if (temp != null) {
                    exifData.append("\nDevice model: " + temp);
                }
                temp = exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
                if (temp != null) {
                    exifData.append("\nWhite balance: " + temp);
                }
                temp = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
                if (temp != null) {
                    exifData.append("\nFocal length: " + temp);
                }
                temp = exifInterface.getAttribute(ExifInterface.TAG_FLASH);
                if (temp != null) {
                    exifData.append("\nFlash: " + temp);
                }
                /*
                temp = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                if (temp != null) {
                    exifData.append("\nLatitude: " + temp);
                }
                temp = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
                if (temp != null) {
                    exifData.append("\nLongitude: " + temp);
                }*/

                float[] temp2 = new float[2];
                if (exifInterface.getLatLong(temp2)) {
                    latitude = temp2[0];
                    longitude = temp2[1];
                    Pair<String, String> countryCity = getCountryCityName(latitude, longitude);
                    exifData.append("\nLocation: " + countryCity.second + ", " + countryCity.first);
                }

                exifData.append("\nSmart description: " + photo.des);
            } catch (Exception e) {
                e.printStackTrace();
            }

            exifInfo.setText(exifData.toString());
        }
        return view;
    }
    public Pair<String, String> getCountryCityName(double lat, double lng) {
        Geocoder geocoder = new Geocoder(this.getContext(), Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address obj = addresses.get(0);
        return new Pair<>(obj.getCountryName(), obj.getAdminArea());
    }

//    private class analyzeImage extends AsyncTask<String, String, ArrayList<Tag>> {
//        // Store error message
//        private Exception e = null;
//        private int photoId;
//        private Set<Chip> newChips;
//        private Set<Chip> oldChips;
//
//        public analyzeImage(int photoId) {
//            this.photoId = photoId;
//        }
//        public analyzeImage(Set<Chip> newChips, Set<Chip> oldChips){
//            this.newChips = newChips;
//            this.oldChips = oldChips;
//        }
//        @Override
//        protected ArrayList<Tag> doInBackground(String... strings) {
//            Log.d("DOBACK", strings[0]);
//            if(strings[0].equals("LOAD_TAG_FROM_DB")){
//                ArrayList<Tag> tags = db.getTagsFromPhoto(this.photoId);
//                return tags;
//            }else{
//                Log.d("newchips", String.valueOf(newChips));
//                Log.d("oldchips", String.valueOf(oldChips));
//                updateTags(newChips, oldChips);
//                return new ArrayList<Tag>();
//            }
//
//        }
//    }
}