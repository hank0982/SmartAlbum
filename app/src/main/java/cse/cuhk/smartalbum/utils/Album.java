package cse.cuhk.smartalbum.utils;

public class Album {
    public int id;
    public String name;
    public String coverPhotoPath;
    public Album(int id, String name, String coverPhotoPath){
        this.id = id;
        this.name = name;
        this.coverPhotoPath = coverPhotoPath;
    }
}
