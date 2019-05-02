package cse.cuhk.smartalbum;

import android.animation.ObjectAnimator;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.transition.Transition;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import cse.cuhk.smartalbum.utils.Album;
import cse.cuhk.smartalbum.utils.DecodeBitmapTask;
import cse.cuhk.smartalbum.utils.database.DBHelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

public class DetailsActivity extends AppCompatActivity {

    static final String BUNDLE_IMAGE_ID = "BUNDLE_IMAGE_ID";


    private DBHelper db;
    private Album album;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DBHelper(this.getBaseContext());
        setContentView(R.layout.activity_details);

        final int albumID = getIntent().getIntExtra(BUNDLE_IMAGE_ID, -999);
        if (albumID == -999) {
            finish();
            return;
        }
        Cursor res = db.getData(albumID, DBHelper.ALBUMS_TABLE_NAME);
        res.moveToFirst();
        album = DBHelper.convertCursorToAlbum(res);
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        if(album.name.equals(Album.ALL_PHOTOS_ALBUM_NAME)){
            trans.add(R.id.photo_view_fragment_container, new AllPhotosFragment());
        }else{

        }
        trans.commit();
    }
}
