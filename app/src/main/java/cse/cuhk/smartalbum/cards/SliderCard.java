package cse.cuhk.smartalbum.cards;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import cse.cuhk.smartalbum.R;

import androidx.annotation.DrawableRes;
import androidx.recyclerview.widget.RecyclerView;

public class SliderCard extends RecyclerView.ViewHolder {

    public final ImageView imageView;
    public final TextView title;

    public SliderCard(View itemView) {
        super(itemView);
        imageView = (ImageView) itemView.findViewById(R.id.image);
        title = (TextView) itemView.findViewById(R.id.album_title);
    }

}