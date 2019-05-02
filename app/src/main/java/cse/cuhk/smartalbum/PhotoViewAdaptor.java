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
    private int [] redcolorpllate = {255, 255, 255, 153, 51, 51, 51,51,51,153,255,255};
    private int [] greencolorpllate = {51, 153, 255, 255, 255, 255, 255,153,51,51,51,51};
    private int [] bluecolorpllate = {51, 51, 51, 51, 51, 153, 255,255,255,255,255,153};

    @Override
    public double aspectRatioForIndex(int index) {
        File file = new File(photos.get(index).path);
        BitmapFactory.Options options = new BitmapFactory.Options();
        if(file.exists()) {
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(photos.get(index).path, options);
            return options.outWidth / (double) options.outHeight;

        }else{
            Photo pendingDeletePhoto = db.getPhotoByPath(photos.get(index).path);
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
        File file = new File(photos.get(position).path);
        int A = 100;

        int R = redcolorpllate[position%12];
        int G = greencolorpllate[position%12];
        int B = bluecolorpllate[position%12];
        int color = (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);
        R = redcolorpllate[(position+1)%12];
        G = greencolorpllate[(position+1)%12];
        B = bluecolorpllate[(position+1)%12];
        int nextColor = (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | (B & 0xff);

        GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[] {color,nextColor});

        if(file.exists())
            Glide.with(mContext).load(photos.get(position).path).placeholder(gradient).centerCrop().into(holder.mImageView);
        else{
            Photo pendingDeletePhoto = db.getPhotoByPath(photos.get(position).path);
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

