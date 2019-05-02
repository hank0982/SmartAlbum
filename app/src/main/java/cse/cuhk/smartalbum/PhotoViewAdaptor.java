package cse.cuhk.smartalbum;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fivehundredpx.greedolayout.GreedoLayoutSizeCalculator;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

import cse.cuhk.smartalbum.utils.Photo;
import cse.cuhk.smartalbum.utils.database.DBHelper;

public class PhotoViewAdaptor extends RecyclerView.Adapter<PhotoViewAdaptor.PhotoViewHolder> implements GreedoLayoutSizeCalculator.SizeCalculatorDelegate {
    private ArrayList<Double> mImageAspectRatios = new ArrayList<>();
    private Context mContext;
    private ArrayList<Photo> photos;
    private DBHelper db;
    @Override
    public double aspectRatioForIndex(int index) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photos.get(index).path, options);
        return options.outWidth / (double) options.outHeight;
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        public PhotoViewHolder(ImageView imageView) {
            super(imageView);
            mImageView = imageView;
        }
    }

    public PhotoViewAdaptor(Context context, ArrayList<Photo> photos) {
        this.photos = photos;
        mContext = context;
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        return new PhotoViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder holder, int position) {
        File file = new File(photos.get(position).path);
        if(file.exists())
            Glide.with(mContext).load(photos.get(position).path).placeholder(new ColorDrawable(Color.BLUE)).centerCrop().into(holder.mImageView);
        else{
            Photo pendingDeletePhoto = db.getPhotoByPath(photos.get(position).path);
            db.deleteData(pendingDeletePhoto.id, DBHelper.PHOTOS_TABLE_NAME);
        }
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }
    private int getLoopedIndex(int index) {
        return index % photos.size(); // wrap around
    }
}

