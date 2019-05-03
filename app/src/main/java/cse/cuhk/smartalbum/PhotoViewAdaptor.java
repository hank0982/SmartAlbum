package cse.cuhk.smartalbum;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
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

    private int[] colors = {1694446387, 1694472499, 1694498611, 1687813939, 1681129267, 1681129369, 1681129471, 1681103359, 1681077247, 1687761919, 1694446591, 1694446489};
    @Override
    public double aspectRatioForIndex(int index) {
        File file = new File(photos.get(getLoopedIndex(index)).path);
        BitmapFactory.Options options = new BitmapFactory.Options();
        if(file.exists()) {
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(photos.get(index % photos.size()).path, options);

            return options.outWidth / (double) options.outHeight;

        }else{
            Photo pendingDeletePhoto = db.getPhotoByPath(photos.get(getLoopedIndex(index % photos.size())).path);
            if(pendingDeletePhoto!=null){
                db.deleteData(pendingDeletePhoto.id, DBHelper.PHOTOS_TABLE_NAME);
            }
            return 0;
        }

    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder{
        private ImageView mImageView;
        public PhotoViewHolder(ImageView imageView) {
            super(imageView);
            mImageView = imageView;
        }
    }

    public PhotoViewAdaptor(Context context, ArrayList<Photo> photos) {
        this.photos = photos;
        mContext = context;
        db = new DBHelper(context);
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
        int loopedPosition = getLoopedIndex(position);
        File file = new File(photos.get(loopedPosition).path);

        GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[] {colors[position%12],colors[(position+1)%12]});

        if(file.exists())
            Glide.with(mContext).load(photos.get(loopedPosition).path).placeholder(gradient).centerCrop().into(holder.mImageView);
        else{
            Photo pendingDeletePhoto = db.getPhotoByPath(photos.get(loopedPosition).path);
            if(pendingDeletePhoto!=null){
                db.deleteData(pendingDeletePhoto.id, DBHelper.PHOTOS_TABLE_NAME);
            }
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

