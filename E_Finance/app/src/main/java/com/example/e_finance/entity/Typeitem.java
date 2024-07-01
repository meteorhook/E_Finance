package com.example.e_finance.entity;

public class Typeitem {
    private  String typename,color,imageId,imageIdselect,typeid;
    private Boolean Tstate;
    public Typeitem(String typename, String color, String imageId, String imageIdselect,String typeid,Boolean tstate) {
        this.typename = typename;
        this.imageId = imageId;
        this.imageIdselect=imageIdselect;
        this.color=color;
        this.typeid = typeid;
        this.Tstate=tstate;
    }
    public  String getType() {
        return typename;
    }
    public  String getImageId() {
        return imageId;
    }
    public  String getimageIdselect() {return imageIdselect;}
    public  String getColor(){return color;}
    public String getTypeid() {
        return typeid;
    }

    public Boolean getTstate() {
        return Tstate;
    }
}
