package cse.cuhk.smartalbum;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class AllPhotosFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_photos_fragment, container, false);
        TextView title = view.findViewById(R.id.all_photos_title);
        title.setX(getResources().getDimensionPixelSize(R.dimen.left_offset));
        title.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "open-sans-extrabold.ttf"));

        return view;
    }
}
