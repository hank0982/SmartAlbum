package cse.cuhk.smartalbum.utils;

import android.app.Activity;
import android.app.Service;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import java.util.ArrayList;

public class Photo implements Parcelable {
    public int id;
    public String name;
    public String path;
    public String des;
    public Photo(int id, String name, String path, String des){
        this.id = id;
        this.name = name;
        this.path = path;
        this.des = des;
    }

    protected Photo(Parcel in) {
        id = in.readInt();
        name = in.readString();
        path = in.readString();
        des = in.readString();
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    public final static ArrayList<String> getAllShownImagesPath(Activity activity) {

        Uri uri;
        Cursor[] cursors = new Cursor[2];

        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursors[0] = activity.getContentResolver().query(uri, projection, null,
                null,MediaStore.Images.ImageColumns.DATE_TAKEN + " ASC");
        cursors[1] = activity.getContentResolver().query(
                MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                new String[]{
                        MediaStore.Images.Media.DATA,
                },
                null,
                null,
                MediaStore.Images.ImageColumns.DATE_TAKEN + " ASC"
        );

        Cursor cursor = new MergeCursor(cursors);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(path);
        dest.writeString(des);
    }
}
