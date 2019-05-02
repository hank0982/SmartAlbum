package cse.cuhk.smartalbum.utils.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import cse.cuhk.smartalbum.utils.Album;
import cse.cuhk.smartalbum.utils.Photo;

// SQLite Open Helper is thread safe
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "SmartAlbum.db";

    // ALBUMS TABLE
    public static final String ALBUMS_TABLE_NAME = "ALBUMS";
    public static final String ALBUMS_COLUMN_ID = "albumid";
    public static final String ALBUMS_COLUMN_NAME = "albumname";
    public static final String ALBUMS_COLUMN_COVERPHOTO = "albumcoverphoto";
    public static final String ALBUMS_COLUMN_TYPE = "albumtype";


    // ALBUMPHOTOS TABLE
    public static final String ALBUMPHOTOS_TABLE_NAME = "ALBUMPHOTOS";
    public static final String ALBUMPHOTOS_COLUMN_ALBUMPHOTOID = "albumphotoid";
    public static final String ALBUMPHOTOS_COLUMN_PHOTOID = "photoid";
    public static final String ALBUMPHOTOS_COLUMN_ALBUMID = "albumid";

    // PHOTOS TABLE
    public static final String PHOTOS_TABLE_NAME = "PHOTOS";
    public static final String PHOTOS_COLUMN_PHOTOID = "photoid";
    public static final String PHOTOS_COLUMN_NAME = "photoname";
    public static final String PHOTOS_COLUMN_PATH = "path";
    public static final String PHOTOS_COLUMN_DES = "description";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table ALBUMS " +
                        "(albumid integer primary key, albumname text,albumcoverphoto text, albumtype text)"
        );
        db.execSQL(
                "create table PHOTOS " +
                        "(photoid integer primary key, photoname text,path text,description text)"
        );
        db.execSQL(
                "create table ALBUMPHOTOS " +
                        "(albumphotoid integer primary key, photoid integer,albumid integer)"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS ALBUMS");
        db.execSQL("DROP TABLE IF EXISTS PHOTOS");
        db.execSQL("DROP TABLE IF EXISTS ALBUMPHOTOS");
        onCreate(db);
    }

    public boolean insertAlbum(String name, String coverPhotoPath, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ALBUMS_COLUMN_NAME, name);
        contentValues.put(ALBUMS_COLUMN_COVERPHOTO, coverPhotoPath);
        contentValues.put(ALBUMS_COLUMN_TYPE, type);
        db.insert(ALBUMS_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean insertPhotoToAlbum(int photoID, int albumID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("photoid", photoID);
        contentValues.put("albumid", albumID);
        db.insert(ALBUMPHOTOS_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean insertPhoto(String photoName, String path, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("photoname", photoName);
        contentValues.put("path", path);
        contentValues.put("description", description);
        db.insert("PHOTOS", null, contentValues);
        return true;
    }
    public Photo getPhotoByPath(String path){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from PHOTOS where path=" + path + "", null);
        res.moveToFirst();
        int id = res.getInt(res.getColumnIndex(PHOTOS_COLUMN_PHOTOID));
        String name = res.getString(res.getColumnIndex(PHOTOS_COLUMN_NAME));
        String imgpath = res.getString(res.getColumnIndex(PHOTOS_COLUMN_PATH));
        String des = res.getString(res.getColumnIndex(PHOTOS_COLUMN_DES));
        Photo newPhoto = new Photo(id, name, imgpath, des);
        return newPhoto;
    }
    public Cursor getData(int id, String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + tableName + " where id=" + id + "", null);
        return res;
    }

    public Integer deleteData(Integer id, String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(tableName,
                "id = ? ",
                new String[]{Integer.toString(id)});
    }

    public int numberOfRows(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, tableName);
        return numRows;
    }

    public ArrayList<Photo> getAllPhotos() {
        ArrayList<Photo> array_list = new ArrayList<Photo>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from PHOTOS", null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            int id = res.getInt(res.getColumnIndex(PHOTOS_COLUMN_PHOTOID));
            String name = res.getString(res.getColumnIndex(PHOTOS_COLUMN_NAME));
            String path = res.getString(res.getColumnIndex(PHOTOS_COLUMN_PATH));
            String des = res.getString(res.getColumnIndex(PHOTOS_COLUMN_DES));
            Photo newPhoto = new Photo(id, name, path, des);
            array_list.add(newPhoto);
            res.moveToNext();
        }
        return array_list;
    }

    public ArrayList<Album> getAllAlbums() {
        ArrayList<Album> array_list = new ArrayList<Album>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from ALBUMS", null);
        res.moveToFirst();
        while (res.isAfterLast() == false) {
            int id = res.getInt(res.getColumnIndex(ALBUMS_COLUMN_ID));
            String coverPhotoPath = res.getString(res.getColumnIndex(ALBUMS_COLUMN_COVERPHOTO));
            String name = res.getString(res.getColumnIndex(ALBUMS_COLUMN_NAME));
            String type = res.getString(res.getColumnIndex(ALBUMS_COLUMN_TYPE));
            Album newAlbum = new Album(id, name, coverPhotoPath, type);
            array_list.add(newAlbum);
            res.moveToNext();
        }
        return array_list;
    }
}
