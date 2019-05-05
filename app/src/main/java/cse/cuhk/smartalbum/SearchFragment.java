package cse.cuhk.smartalbum;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import java.util.ArrayList;
import java.util.List;

import cse.cuhk.smartalbum.utils.Photo;
import cse.cuhk.smartalbum.utils.Tag;
import cse.cuhk.smartalbum.utils.TagSuggestion;
import cse.cuhk.smartalbum.utils.database.DBHelper;

public class SearchFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private String mLastQuery = "";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final DBHelper db = DBHelper.getInstance(this.getActivity().getApplicationContext());
        View view = inflater.inflate(R.layout.search_fragment, container, false);
        TextView title = view.findViewById(R.id.search_title);
        title.setX(getResources().getDimensionPixelSize(R.dimen.left_offset));
        title.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "open-sans-extrabold.ttf"));
        final FloatingSearchView mSearchView = view.findViewById(R.id.floating_search_view);
        mSearchView.setBackgroundColor(Color.parseColor("#787878"));
        mSearchView.setViewTextColor(Color.parseColor("#e9e9e9"));
        mSearchView.setHintTextColor(Color.parseColor("#e9e9e9"));
        mSearchView.setActionMenuOverflowColor(Color.parseColor("#e9e9e9"));
        mSearchView.setMenuItemIconColor(Color.parseColor("#e9e9e9"));
        mSearchView.setLeftActionIconColor(Color.parseColor("#e9e9e9"));
        mSearchView.setClearBtnColor(Color.parseColor("#e9e9e9"));
        mSearchView.setDividerColor(Color.parseColor("#BEBEBE"));
        mSearchView.setLeftActionIconColor(Color.parseColor("#e9e9e9"));
        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    mSearchView.clearSuggestions();
                    return;
                }
                Log.d("newQuery", newQuery);
                //get suggestions based on newQuery
                ArrayList<Tag> tags = db.searchTagsByName(newQuery, false);
                List<TagSuggestion> suggestionList = new ArrayList<>();
                if(tags != null){
                    for (Tag tag : tags) {
                        suggestionList.add(new TagSuggestion(tag.name));
                    }
                }
                ArrayList<Integer> photos = db.getPhotoIDsByTags(tags);
                Log.d("photoids", String.valueOf(photos));
                //pass them on to the search view
                mSearchView.swapSuggestions(suggestionList);
            }
        });
        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final SearchSuggestion searchSuggestion) {

                mLastQuery = searchSuggestion.getBody();
                mSearchView.clearQuery();;
                mSearchView.clearSuggestions();
                mSearchView.clearSearchFocus();
            }

            @Override
            public void onSearchAction(String query) {
                mLastQuery = query;
                mSearchView.clearQuery();;
                mSearchView.clearSuggestions();
                mSearchView.clearSearchFocus();
            }
        });
        return view;
    }
}
