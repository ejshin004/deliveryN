package com.deliveryn.orderlist;

public class ReportModel {
    public String reporter_nickname;
    public Object datetime;
    public String reported_user_type;
    public String reported_nickname;
    public String report_type;
    public String specific;

    public ReportModel() { }

    public ReportModel(String reporter, Object datetime, String reported_type, String reported_nickname, String report_type, String specific){
        this.reporter_nickname = reporter;
        this.datetime = datetime;
        this.reported_user_type = reported_type;
        this.reported_nickname = reported_nickname;
        this.report_type = report_type;
        this.specific = specific;
    }

    public ReportModel(String reporter, Object datetime, String reported_type, String reported_nickname, String report_type){
        this.reporter_nickname = reporter;
        this.datetime = datetime;
        this.reported_user_type = reported_type;
        this.reported_nickname = reported_nickname;
        this.report_type = report_type;
    }
}
