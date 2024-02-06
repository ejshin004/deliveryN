package com.deliveryn.orderlist;

/**
    사용자 계정 정보 모델 클래스
**/
public class UserModel {
    private String uid; // Firebase Uid(고유 토큰 정보)
    private String emailId;
    private String password;
    private String nickname;

    public UserModel() { }

    public String getUid() { return uid; }

    public void setUid(String uid) { this.uid = uid; }

    public String getEmailId() { return emailId; }

    public void setEmailId(String emailId) { this.emailId = emailId; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public String getNickname() { return nickname; }

    public void setNickname(String nickname) { this.nickname = nickname; }
}
