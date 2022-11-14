package fi.tuni.tyoaikalaskuri;

/*
Class to represent a user
 */
public class User {
    private int id;
    private String name;
    private int currentWorkId = 0;

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public User() {

    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCurrentWorkId() {
        return currentWorkId;
    }

    public void setCurrentWorkId(int currentWorkId) {
        this.currentWorkId = currentWorkId;
    }
}
