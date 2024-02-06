package com.deliveryn.orderlist;

public class OrderInfoModel {
    private String menu;
    private String option;
    private String price;
    private String nickname;

    public OrderInfoModel() { }

    public String getMenu() { return menu; }

    public void setMenu(String menu) { this.menu = menu; }

    public String getOption() { return option; }

    public void setOption(String option) { this.option = option; }

    public String getPrice() { return price; }

    public void setPrice(String price) { this.price = price; }

    public String getNickname() { return nickname; }

    public void setNickname(String nickname) { this.nickname = nickname; }
}
