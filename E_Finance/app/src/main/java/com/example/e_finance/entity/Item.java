package com.example.e_finance;

public class Item {
    private String name,ledgerid;
    public Item(String name) {
        this.name = name;
    }
    public Item(String name,String ledgerid){
        this.name = name;
        this.ledgerid = ledgerid;
    }
    public String getLedgerid() {
        return ledgerid;
    }

    public String getName() {
        return name;
    }
}
