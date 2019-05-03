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
import cse.cuhk.smartalbum.utils.Tag;

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

    // TAGS TABLE
    public static final String TAGS_TABLE_NAME = "TAGS";
    public static final String TAGS_COLUMN_TAGID = "tagid";
    public static final String TAGS_COLUMN_TIMES = "tagtimes";
    public static final String TAGS_COLUMN_NAME = "tagname";

    // PHOTOTAGS TABLE
    public static final String PHOTOTAGS_TABLE_NAME = "PHOTOTAGS";
    public static final String PHOTOTAGS_COLUMN_PHOTOTAGSID = "phototagid";
    public static final String PHOTOTAGS_COLUMN_PHOTOID = "photoid";
    public static final String PHOTOTAGS_COLUMN_TAGID = "tagid";
    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 5);
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
        db.execSQL(
                "create table TAGS " +
                        "(tagid integer primary key, tagtimes integer,tagname text)"
        );
        db.execSQL(
                "create table PHOTOTAGS " +
                        "(phototagid integer primary key, photoid integer,tagid integer)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS ALBUMS");
        db.execSQL("DROP TABLE IF EXISTS PHOTOS");
        db.execSQL("DROP TABLE IF EXISTS ALBUMPHOTOS");
        db.execSQL("DROP TABLE IF EXISTS TAGS");
        db.execSQL("DROP TABLE IF EXISTS PHOTOTAGS");
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

    public boolean insertTagToPhoto(int tagID, int photoID){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("tagid", tagID);
        contentValues.put("photoid", photoID);
        db.insert(PHOTOTAGS_TABLE_NAME, null, contentValues);
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

    public ArrayList<Photo> getPhotosInAlbum(int albumID){
        ArrayList<Photo> array_list = new ArrayList<Photo>();
        SQLiteDatabase db = this.getReadableDatabase();
        String albumsTableJoinAlbumPhotoTableSql = "(SELECT * FROM " + ALBUMS_TABLE_NAME + " NATURAL JOIN " + ALBUMPHOTOS_TABLE_NAME +" WHERE "+ALBUMS_COLUMN_ID + "="+albumID+")";
        String sql = "SELECT * FROM " + PHOTOS_TABLE_NAME + " NATURAL JOIN " + albumsTableJoinAlbumPhotoTableSql;
        Cursor res = db.rawQuery(sql, null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            Photo newPhoto = convertCursorToPhoto(res);
            array_list.add(newPhoto);
            res.moveToNext();
        }
        return array_list;
    }

    public ArrayList<Tag> getTagsFromPhoto(int photoID){
        ArrayList<Tag> array_list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String photoTableJoinPhotoTagTableSql = "(SELECT * FROM " + PHOTOS_TABLE_NAME + " NATURAL JOIN " + PHOTOTAGS_TABLE_NAME +" WHERE "+PHOTOS_COLUMN_PHOTOID + "="+photoID+")";
        String sql = "SELECT * FROM " + TAGS_TABLE_NAME + " NATURAL JOIN " + photoTableJoinPhotoTagTableSql;
        Cursor res = db.rawQuery(sql, null);
        res.moveToFirst();

        while (res.isAfterLast() == false) {
            Tag newTag = convertCursorToTag(res);
            array_list.add(newTag);
            res.moveToNext();
        }
        return array_list;
    }

    private String getIdNameFromTableName(String tableName){
        String idName;
        if(tableName.equals(ALBUMPHOTOS_TABLE_NAME)){
            idName = ALBUMPHOTOS_COLUMN_ALBUMPHOTOID;
        }else if(tableName.equals(ALBUMS_TABLE_NAME)){
            idName = ALBUMS_COLUMN_ID;
        }else if(tableName.equals(TAGS_TABLE_NAME)){
            idName = TAGS_COLUMN_TAGID;
        }else if(tableName.equals(PHOTOTAGS_TABLE_NAME)){
            idName = PHOTOTAGS_COLUMN_PHOTOTAGSID;
        }else{
            idName = PHOTOS_COLUMN_PHOTOID;
        }
        return idName;
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
        Cursor res = db.rawQuery("select * from PHOTOS where path='" + path + "'", null);
        res.moveToFirst();
        if(res.getCount()>0){
            int id = res.getInt(res.getColumnIndex(PHOTOS_COLUMN_PHOTOID));
            String name = res.getString(res.getColumnIndex(PHOTOS_COLUMN_NAME));
            String imgpath = res.getString(res.getColumnIndex(PHOTOS_COLUMN_PATH));
            String des = res.getString(res.getColumnIndex(PHOTOS_COLUMN_DES));
            Photo newPhoto = new Photo(id, name, imgpath, des);
            return newPhoto;
        }else{
            return null;
        }
    }
    public Cursor getData(int id, String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String idName = getIdNameFromTableName(tableName);

        Cursor res = db.rawQuery("select * from " + tableName + " where " + idName + "=" + id + "", null);
        return res;
    }
    static public Tag convertCursorToTag(Cursor res){
        int id = res.getInt(res.getColumnIndex(TAGS_COLUMN_TAGID));
        String tagName = res.getString(res.getColumnIndex(TAGS_COLUMN_NAME));
        int times = res.getInt(res.getColumnIndex((TAGS_COLUMN_TIMES)));
        Tag newTag = new Tag(id, tagName, times);
        return newTag;
    }
    static public Album convertCursorToAlbum(Cursor res){
        int id = res.getInt(res.getColumnIndex(ALBUMS_COLUMN_ID));
        String coverPhotoPath = res.getString(res.getColumnIndex(ALBUMS_COLUMN_COVERPHOTO));
        String name = res.getString(res.getColumnIndex(ALBUMS_COLUMN_NAME));
        String type = res.getString(res.getColumnIndex(ALBUMS_COLUMN_TYPE));
        Album newAlbum = new Album(id, name, coverPhotoPath, type);
        return newAlbum;
    }

    static public Photo convertCursorToPhoto(Cursor res){
        int id = res.getInt(res.getColumnIndex(PHOTOS_COLUMN_PHOTOID));
        String name = res.getString(res.getColumnIndex(PHOTOS_COLUMN_NAME));
        String path = res.getString(res.getColumnIndex(PHOTOS_COLUMN_PATH));
        String des = res.getString(res.getColumnIndex(PHOTOS_COLUMN_DES));
        Photo newPhoto = new Photo(id, name, path, des);
        return newPhoto;
    }

    public Integer deleteData(Integer id, String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String idName = getIdNameFromTableName(tableName);
        return db.delete(tableName,
                idName+" = ? ",
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
            Photo newPhoto = convertCursorToPhoto(res);
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
            Album newAlbum = convertCursorToAlbum(res);
            array_list.add(newAlbum);
            res.moveToNext();
        }
        return array_list;
    }
}
