package cse.cuhk.smartalbum.utils;

import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

public class TagSuggestion implements SearchSuggestion {
    private String tag;
    private boolean mIsHistory = false;

    public TagSuggestion(String suggestion) {
        this.tag = suggestion.toLowerCase();
    }

    public TagSuggestion(Parcel source) {
        this.tag = source.readString();
        this.mIsHistory = source.readInt() != 0;
    }

    public void TagSuggestion(boolean isHistory) {
        this.mIsHistory = isHistory;
    }

    public boolean getIsHistory() {
        return this.mIsHistory;
    }

    @Override
    public String getBody() {
        return tag;
    }

    public static final Creator<TagSuggestion> CREATOR = new Creator<TagSuggestion>() {
        @Override
        public TagSuggestion createFromParcel(Parcel in) {
            return new TagSuggestion(in);
        }

        @Override
        public TagSuggestion[] newArray(int size) {
            return new TagSuggestion[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tag);
        dest.writeInt(mIsHistory ? 1 : 0);
    }
}