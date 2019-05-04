package cse.cuhk.smartalbum.utils.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Array;
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
    public static final String TAGS_COLUMN_COUNT = "tagcount";
    public static final String TAGS_COLUMN_NAME = "tagname";
    public static final String TAGS_COLUMN_MANUALLY_CREATED = "tagmanuallycreated";
    public static final String TAGS_COLUMN_AUTO_ALBUM_ID = "tagautoalbumid";

    // PHOTOTAGS TABLE
    public static final String PHOTOTAGS_TABLE_NAME = "PHOTOTAGS";
    public static final String PHOTOTAGS_COLUMN_PHOTOTAGSID = "phototagid";
    public static final String PHOTOTAGS_COLUMN_PHOTOID = "photoid";
    public static final String PHOTOTAGS_COLUMN_TAGID = "tagid";
    private static DBHelper single_instance = null;

    private static final int ALBUM_CREATE_NUM = 3;

    public static DBHelper getInstance(Context context)
    {
        if (single_instance == null)
            single_instance = new DBHelper(context);

        return single_instance;
    }

    private DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 26);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table ALBUMS " +
                        "(albumid integer primary key, albumname text unique,albumcoverphoto text, albumtype text)"
        );
        db.execSQL(
                "create table PHOTOS " +
                        "(photoid integer primary key, photoname text unique,path text,description text)"
        );
        db.execSQL(
                "create table ALBUMPHOTOS " +
                        "(albumphotoid integer primary key, photoid integer,albumid integer)"
        );
        db.execSQL(
                "create table TAGS " +
                        "(tagid integer primary key, tagcount integer,tagname text unique, tagmanuallycreated integer, tagautoalbumid integer)"
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

    public long insertAlbum(String name, String coverPhotoPath, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ALBUMS_COLUMN_NAME, name);
        contentValues.put(ALBUMS_COLUMN_COVERPHOTO, coverPhotoPath);
        contentValues.put(ALBUMS_COLUMN_TYPE, type);
        return db.insert(ALBUMS_TABLE_NAME, null, contentValues);
    }

    // just a function to insert a tag that has never been created before
    public Tag insertNewTag(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TAGS_COLUMN_NAME, name.toLowerCase());
        contentValues.put(TAGS_COLUMN_COUNT, 0);
        // always used when manually created (at least for now)
        contentValues.put(TAGS_COLUMN_MANUALLY_CREATED, 1);
        contentValues.put(TAGS_COLUMN_AUTO_ALBUM_ID, -1);
        return new Tag((int)db.insert(TAGS_TABLE_NAME, null, contentValues), name.toLowerCase(), 0, true, -1);
    }

    public ArrayList<Long> insertTag(String name, boolean manuallyCreated) {
        SQLiteDatabase db = this.getWritableDatabase();
        String nameLowerCase = name.toLowerCase();
        ArrayList<Long> idList = new ArrayList<>();
        ArrayList<Tag> tags = searchTagsByName(nameLowerCase, true);

        if (tags == null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(TAGS_COLUMN_NAME, nameLowerCase);
            contentValues.put(TAGS_COLUMN_COUNT, 1);
            contentValues.put(TAGS_COLUMN_MANUALLY_CREATED, manuallyCreated? 1:0);
            contentValues.put(TAGS_COLUMN_AUTO_ALBUM_ID, -1);
            idList.add(db.insert(TAGS_TABLE_NAME, null, contentValues));
            return idList;
        }
        else {
            Tag tag = tags.get(0);
            idList.add(Long.valueOf(tag.id));
            int newCount = tag.count + 1;

            if (newCount == ALBUM_CREATE_NUM) {
                ArrayList<Integer> photoIDList = getPhotoIDsByTags(tags);
                Cursor res = this.getData(photoIDList.get(0), PHOTOS_TABLE_NAME);
                res.moveToFirst();
                Photo photo = this.convertCursorToPhoto(res);
                long albumID = insertAlbum(tag.name, photo.path, Album.AUTO_ALBUM);
                updateTagAutoAlbumID(tag.id, (int)albumID);
                idList.add(albumID);
                for (int photoid: photoIDList) {
                    insertPhotoToAlbum(photoid, (int)albumID);
                }
            } else if (newCount > ALBUM_CREATE_NUM && tag.autoAlbumID != -1) {
                idList.add(Long.valueOf(tag.autoAlbumID));
            }

            updateTagCount(tag.id, newCount);
            if (tag.manuallyCreated == false && manuallyCreated == true) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(TAGS_COLUMN_MANUALLY_CREATED, true? 1:0);
                db.update(TAGS_TABLE_NAME, contentValues, "tagid = ? ", new String[] { Integer.toString(tag.id) } );
            }

            return idList;
        }
    }

    public boolean updateTagAutoAlbumID(int tagid, int autoAlbumID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TAGS_COLUMN_AUTO_ALBUM_ID, autoAlbumID);
        db.update(TAGS_TABLE_NAME, contentValues, "tagid = ? ", new String[] {Integer.toString(tagid)});
        return true;
    }

    public boolean updateTagCount(int tagid, int newCount){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TAGS_COLUMN_COUNT, newCount);
        db.update(TAGS_TABLE_NAME, contentValues, "tagid = ? ", new String[] { Integer.toString(tagid) } );
        return true;
    }
    public ArrayList<Tag> searchTagsByName(String name, boolean exactMatch){
        ArrayList<Tag> tags = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        if(exactMatch){
            Cursor res = db.rawQuery("select * from TAGS where tagname='" + name + "'", null);
            res.moveToFirst();
            if(res.getCount()>0) {
                int tagCount = res.getInt(res.getColumnIndex(TAGS_COLUMN_COUNT));
                int tagID = res.getInt(res.getColumnIndex(TAGS_COLUMN_TAGID));
                String tagName = res.getString(res.getColumnIndex(TAGS_COLUMN_NAME));
                boolean manuallyCreated = res.getInt(res.getColumnIndex(TAGS_COLUMN_MANUALLY_CREATED)) > 0;
                int autoAlbumID = res.getInt(res.getColumnIndex(TAGS_COLUMN_AUTO_ALBUM_ID));
                Tag newTag = new Tag(tagID, tagName, tagCount, manuallyCreated, autoAlbumID);
                tags.add(newTag);
                res.close();
                return tags;
            }else{
                res.close();
                return null;
            }
        }else{
                Cursor res = db.rawQuery("select * from TAGS where tagname like '%" + name + "%'", null);
                res.moveToFirst();
                if(res.getCount()>0) {
                    while (res.isAfterLast() == false) {
                        int tagCount = res.getInt(res.getColumnIndex(TAGS_COLUMN_COUNT));
                        int tagID = res.getInt(res.getColumnIndex(TAGS_COLUMN_TAGID));
                        String tagName = res.getString(res.getColumnIndex(TAGS_COLUMN_NAME));
                        boolean manuallyCreated = res.getInt(res.getColumnIndex(TAGS_COLUMN_MANUALLY_CREATED)) > 0;
                        int autoAlbumID = res.getInt(res.getColumnIndex(TAGS_COLUMN_AUTO_ALBUM_ID));
                        Tag newTag = new Tag(tagID, tagName, tagCount, manuallyCreated, autoAlbumID);
                        tags.add(newTag);
                        res.moveToNext();
                    }
                    res.close();
                    return tags;
                }else{
                    res.close();
                    return null;
                }
        }
    }
    public boolean removeTagFromPhoto(int tagID, int photoID){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select phototagid, tagcount, tagid from PHOTOTAGS NATURAL JOIN TAGS where photoid=" + photoID + " and tagid=" + tagID+"", null);
        res.moveToFirst();
        int id = res.getInt(res.getColumnIndex(PHOTOTAGS_COLUMN_PHOTOTAGSID));
        int tagid = res.getInt(res.getColumnIndex(TAGS_COLUMN_TAGID));
        int oldTagCount = res.getInt(res.getColumnIndex(TAGS_COLUMN_COUNT));
        res.close();
        int newTagCount = oldTagCount-1;
        this.deleteData(id, PHOTOTAGS_TABLE_NAME);
        db = this.getWritableDatabase();
        if(newTagCount != 0){
            ContentValues contentValues = new ContentValues();
            contentValues.put(TAGS_COLUMN_COUNT, newTagCount);
            db.update(TAGS_TABLE_NAME, contentValues, "tagid = ? ", new String[] { Integer.toString(tagid) } );
        }else{
            this.deleteData(tagid, TAGS_TABLE_NAME);
        }
        return true;
    }

    public boolean insertTagToPhoto(long tagID, long photoID){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("tagid", tagID);
        contentValues.put("photoid", photoID);
        db.insert(PHOTOTAGS_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean insertPhotoToAlbum(int photoID, int albumID) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + ALBUMPHOTOS_TABLE_NAME + " WHERE " + ALBUMPHOTOS_COLUMN_PHOTOID + "=" + photoID + " AND " + ALBUMPHOTOS_COLUMN_ALBUMID + "=" + albumID;
        Cursor res = db.rawQuery(query, null);
        if (res.getCount() > 0) {
            Log.d("insertPhotoToAlbum", "repeat insertion of photo " + photoID + " in album " + albumID);
            return false;
        }
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
    public long insertPhoto(String photoName, String path, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("photoname", photoName);
        contentValues.put("path", path);
        contentValues.put("description", description);
        return db.insert("PHOTOS", null, contentValues);
    }

    public boolean updatePhotoDescription(long photoID, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PHOTOS_COLUMN_DES, description);
        db.update(PHOTOS_TABLE_NAME, contentValues, "photoid = ? ", new String[] { Long.toString(photoID) } );
        return true;
    }



    public ArrayList<Integer> getPhotoIDsByTags(ArrayList<Tag> tags) {

        if (tags.size() == 0) {
            return null;
        }

        StringBuilder tagList = new StringBuilder();
        int tagLen = tags.size();
        for(int i = 0; i < tagLen - 1; i++){
            tagList.append("" + tags.get(i).id + ",");
        }
        tagList.append("" + tags.get(tagLen-1).id + "");

        ArrayList<Integer> photoIDs = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from PHOTOTAGS where tagid in (" + tagList.toString() +")";
        Log.d("SQL", sql);
        Cursor res = db.rawQuery(sql, null);
        if (res.getCount() > 0) {
            res.moveToFirst();
            while (res.isAfterLast() == false) {
                int id = res.getInt(res.getColumnIndex(PHOTOS_COLUMN_PHOTOID));

                photoIDs.add(id);
                res.moveToNext();
            }
            res.close();
            return photoIDs;
        }
        else {
            res.close();
            return null;
        }
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
        int times = res.getInt(res.getColumnIndex((TAGS_COLUMN_COUNT)));
        boolean manuallyCreated = res.getInt(res.getColumnIndex(TAGS_COLUMN_MANUALLY_CREATED)) > 0;
        int autoAlbumID = res.getInt(res.getColumnIndex(TAGS_COLUMN_AUTO_ALBUM_ID));
        Tag newTag = new Tag(id, tagName, times, manuallyCreated, autoAlbumID);
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

    public boolean deleteAlbum(int albumID) {
        deleteData(albumID, ALBUMS_TABLE_NAME);
        deleteData(albumID, ALBUMPHOTOS_TABLE_NAME);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TAGS_COLUMN_AUTO_ALBUM_ID, -1);
        db.update(TAGS_TABLE_NAME, contentValues, "tagautoalbumid = ? ", new String[] { Integer.toString(albumID) } );
        return true;
    }

    public int numberOfRows(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, tableName);
        return numRows;
    }

    public ArrayList<Photo> getAllPhotos() {
        ArrayList<Photo> array_list = new ArrayList<Photo>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from PHOTOS Order by photoid DESC", null);
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
