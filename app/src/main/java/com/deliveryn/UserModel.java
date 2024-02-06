package com.deliveryn;

/**
    사용자 계정 정보 모델 클래스
**/
public class UserModel {
    private String uid; // Firebase Uid(고유 토큰 정보)
    private String emailId;
    private String password;
    private String nickname;
    private int discounted_delivery_fee;

    public UserModel() { }

    public String getUid() { return uid; }

    public void setUid(String uid) { this.uid = uid; }

    public String getEmailId() { return emailId; }

    public void setEmailId(String emailId) { this.emailId = emailId; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }

    public void setNickname(String nickname) { this.nickname = nickname; }

    public int getDiscounted_delivery_fee() { return discounted_delivery_fee; }

    public void setDiscounted_delivery_fee(int discounted_delivery_fee) { this.discounted_delivery_fee = discounted_delivery_fee; }
}
