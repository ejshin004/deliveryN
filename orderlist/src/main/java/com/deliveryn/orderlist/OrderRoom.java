package com.deliveryn.orderlist;

public class OrderRoom {
    public String roomId;
    public String resName;
    public String deliverTime;
    public String deliverLocation;
    public String resCategory;
    public String deliverLink;
    public String orderState;
    public int orderNum;
    public String orderCompleted;
    public String boss;

    public OrderRoom() { // Default Constructor
    }

    public OrderRoom(String resName, String deliverTime, String deliverLocation, String resCategory, String deliverLink){
        this.resName = resName;
        this.deliverTime = deliverTime;
        this.deliverLocation = deliverLocation;
        this.resCategory = resCategory;
        this.deliverLink = deliverLink;
        this.orderState = "before";
        this.orderNum = 1;
        this.orderCompleted = "N";
    }
    public OrderRoom(String resName, String deliverTime, String deliverLocation, String orderState) {
        this.resName = resName;
        this.deliverTime = deliverTime;
        this.deliverLocation = deliverLocation;
        this.orderState = orderState;
    }
    public OrderRoom(String roomId, String resName, String resCategory, String deliverTime, String deliverLocation, String deliverLink){
        this.roomId = roomId;
        this.resName = resName;
        this.resCategory = resCategory;
        this.deliverTime = deliverTime;
        this.deliverLocation = deliverLocation;
        this.deliverLink = deliverLink;
    }

    public OrderRoom(String roomId, String resName, String resCategory, String deliverTime, String deliverLocation, String deliverLink, String orderState){
        this.roomId = roomId;
        this.resName = resName;
        this.resCategory = resCategory;
        this.deliverTime = deliverTime;
        this.deliverLocation = deliverLocation;
        this.deliverLink = deliverLink;
        this.orderState = orderState;
    }

    public void setId(String roomId){
        this.roomId = roomId;
    }

    public String getId() {
        return roomId;
    }

    public void setBoss(String boss) { this.boss = boss; }

    public String getBoss() {return boss;}

}
