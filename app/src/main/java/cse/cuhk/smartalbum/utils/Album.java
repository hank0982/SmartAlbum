package cse.cuhk.smartalbum.utils;

public class Album {
    public final static String AUTO_ALBUM = "cse.cuhk.smartalbum.utils.auto_album";
    public final static String MANUAL_ALBUM = "cse.cuhk.smartalbum.utils.manual_album";

    public int id;
    public String name;
    public String type;
    public String coverPhotoPath;
    public Album(int id, String name, String coverPhotoPath, String type){
        this.id = id;
        this.name = name;
        this.type = type;
        this.coverPhotoPath = coverPhotoPath;
    }
}
