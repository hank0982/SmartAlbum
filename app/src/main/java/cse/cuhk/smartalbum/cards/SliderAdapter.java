package cse.cuhk.smartalbum.cards;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cse.cuhk.smartalbum.R;
import cse.cuhk.smartalbum.utils.Album;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class SliderAdapter extends RecyclerView.Adapter<SliderCard> {

    private final int count;
    private final ArrayList<Album> albums;
    private final View.OnClickListener clickListener;
    private final View.OnLongClickListener longClickListener;

    public SliderAdapter(ArrayList<Album> albums, int count, View.OnClickListener clickListener, View.OnLongClickListener longClickListener) {
        this.albums = albums;
        this.count = count;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @Override
    public SliderCard onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.layout_slider_card, parent, false);

        if (clickListener != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onClick(view);
                }
            });
        }

        if (longClickListener != null) {
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    longClickListener.onLongClick(view);
                    return false;
                }
            });
        }

        return new SliderCard(view);
    }

    @Override
    public void onBindViewHolder(SliderCard holder, int position) {
        if(albums.size() == 0){

        }else{
            Album album = albums.get(position%albums.size());
            Glide.with(holder.imageView.getContext()).load(album.coverPhotoPath).placeholder(new ColorDrawable(Color.BLUE)).centerCrop().into(holder.imageView);
            holder.title.setText(album.name);
        }
    }

//    @Override
//    public void onViewRecycled(SliderCard holder) {
//        holder.clearContent();
//    }

    @Override
    public int getItemCount() {
        return count;
    }

}
