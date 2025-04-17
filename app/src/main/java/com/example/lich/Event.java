package com.example.lich;

public class Event {
    private int id;
    private String title;
    private String MieuTa;
    private String date;
    private String time;
    private String DuongHoacAm;
    private String location;


    public Event(int id, String title, String MieuTa, String date, String time, String duongHoacAm, String location) {
        this.id = id;
        this.title = title;
        this.MieuTa = MieuTa;
        this.date = date;
        this.time = time;
        this.DuongHoacAm = duongHoacAm;
        this.location = location;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getLocation() { return location; }
    public String getCalendarType() { return DuongHoacAm;}
    public String getDescription() { return MieuTa; }
}