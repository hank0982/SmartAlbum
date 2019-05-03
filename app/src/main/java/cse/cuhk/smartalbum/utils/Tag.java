package cse.cuhk.smartalbum.utils;

public class Tag {

    public int id;
    public String name;
    public int count;
    public boolean manuallyCreated;
    public Tag(int id, String name, int count, boolean manuallyCreated){
        this.id = id;
        this.name = name;
        this.count = count;
        this.manuallyCreated = manuallyCreated;
    }
}
