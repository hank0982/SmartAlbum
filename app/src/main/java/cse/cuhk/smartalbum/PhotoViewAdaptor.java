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

import java.lang.reflect.Array;
import java.util.ArrayList;

import cse.cuhk.smartalbum.utils.Photo;
import cse.cuhk.smartalbum.utils.database.DBHelper;

public class PhotoViewAdaptor extends RecyclerView.Adapter<PhotoViewAdaptor.PhotoViewHolder> implements GreedoLayoutSizeCalculator.SizeCalculatorDelegate {
    private ArrayList<Double> mImageAspectRatios = new ArrayList<>();
    private Context mContext;
    private ArrayList<Photo> photos;
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
//
//    private void calculateImageAspectRatios() {
//
//
//
//        for (int i = 0; i < photos.size(); i++) {
//
//            Log.d("Aspect", String.valueOf(options.outWidth / (double) options.outHeight));
//        }
//
//    }

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
        Glide.with(mContext).load(photos.get(position).path).placeholder(new ColorDrawable(Color.BLUE)).centerCrop().into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }
    private int getLoopedIndex(int index) {
        return index % photos.size(); // wrap around
    }
}

