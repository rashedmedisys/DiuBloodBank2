package com.diu.bloodbank;

/**
 * Created by user on 11/26/2017.
 */

public class Results {

    public String area;
    public String name;
    public String photoUri;
    public String bloodGroup;
    public String lastDonate;

    public Results() {
    }

    public Results(String area, String name, String photoUri, String bloodGroup, String lastDonate) {
        this.area = area;
        this.name = name;
        this.photoUri = photoUri;
        this.bloodGroup = bloodGroup;
        this.lastDonate = lastDonate;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getLastDonate() {
        return lastDonate;
    }

    public void setLastDonate(String lastDonate) {
        this.lastDonate = lastDonate;
    }
}
