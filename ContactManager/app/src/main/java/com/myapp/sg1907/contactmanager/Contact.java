package com.myapp.sg1907.contactmanager;

/**
 * Created by sg1907 on 17.04.2016.
 */
public class Contact {
    private String name;
    private String phoneNumber;
    private byte[] photo;

    public Contact(String name, String phoneNumber, byte[] photo) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }
}
