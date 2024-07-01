package com.example.e_finance;

public class Billitem {
    private String Project,ItemLogo,Num,color,note,date,id,typeid;

    public Billitem(String Project, String ItemLogo,String Num,String color,String note,String date,String id,String typeid) {
        this.Project = Project;
        this.Num=Num;
        this.ItemLogo = ItemLogo;
        this.color = color;
        this.note = note;
        this.date = date;
        this.id=id;
        this.typeid=typeid;
    }
    public String getProject() {
        return Project;
    }
    public String getItemLogo() {
        return ItemLogo;
    }
    public String getNum() {
        return Num;
    }
    public String getColor() {
        return color;
    }
    public String getNote() {
        return note;
    }

    public String getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    public String getTypeid() {
        return typeid;
    }
}