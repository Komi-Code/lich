package com.example.lich.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.lich.Event;

import java.util.ArrayList;
import java.util.List;

public class TaoDatabase extends SQLiteOpenHelper {
    public static String TB_TaiKhoan = "TaiKhoan";
    public static String TB_SuKien = "SuKien";
    public static String TB_Note = "Note";



    public static String TB_TaiKhoan_MaTK = "ID";
    public static String TB_TaiKhoan_TenTK = "TenTK";
    public static String TB_TaiKhoan_MatKhau = "MatKhau";
    public static String TB_TaiKhoan_Email = "Email";
    public static String TB_TaiKhoan_SDT = "SDT";

    public static String TB_SuKien_MaSK = "ID";
    public static String TB_SuKien_TenSK = "TenSK";
    public static String TB_SuKien_MieuTa = "MieuTa";
    public static String TB_SuKien_Ngay = "Ngay";
    public static String TB_SuKien_Gio = "Gio";
    public static String TB_SuKien_DiaDiem = "DiaDiem";
    public static String TB_SuKien_LichDuongHoacAm = "LichDuongHoacAm";

    public static String TB_Note_MaNote = "ID";
    public static String TB_Note_TieuDe = "TieuDe";
    public static String TB_Note_NoiDung = "NoiDung";
    public static String TB_Note_Ngay = "Ngay";
    public static String TB_Note_Gio = "Gio";

    public TaoDatabase(Context context) {
        super(context, "Lich", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String tbTaiKhoan = "CREATE TABLE " + TB_TaiKhoan + " (" +
                TB_TaiKhoan_MaTK + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TB_TaiKhoan_TenTK + " TEXT, " +
                TB_TaiKhoan_MatKhau + " TEXT, " +
                TB_TaiKhoan_Email + " TEXT, " +
                TB_TaiKhoan_SDT + " TEXT)";

        String tbSuKien = "CREATE TABLE " + TB_SuKien + " (" +
                TB_SuKien_MaSK + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TB_SuKien_TenSK + " TEXT, " +
                TB_SuKien_MieuTa + " TEXT, " +
                TB_SuKien_Ngay + " TEXT, " +
                TB_SuKien_Gio + " TEXT, " +
                TB_SuKien_LichDuongHoacAm + " TEXT, " +
                TB_SuKien_DiaDiem + " TEXT)";

        String tbNote = "CREATE TABLE " + TB_Note + " (" +
                TB_Note_MaNote + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TB_Note_TieuDe + " TEXT, " +
                TB_Note_NoiDung + " TEXT, " +
                TB_Note_Ngay + " TEXT, " +
                TB_Note_Gio + " TEXT)";

        db.execSQL(tbTaiKhoan);
        db.execSQL(tbSuKien);
        db.execSQL(tbNote);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean kiemTraDangNhap(String email, String matKhau) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {TB_TaiKhoan_MaTK};
        String selection = TB_TaiKhoan_Email + " = ? AND " + TB_TaiKhoan_MatKhau + " = ?";
        String[] selectionArgs = {email, matKhau};

        Cursor cursor = db.query(TB_TaiKhoan, columns, selection, selectionArgs, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean kiemTraEmailTonTai(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {TB_TaiKhoan_MaTK};
        String selection = TB_TaiKhoan_Email + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TB_TaiKhoan, columns, selection, selectionArgs, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    //Tạo tk
    public long themTaiKhoan(String tenTK, String matKhau, String email, String sdt) {
        ContentValues values = new ContentValues();
        values.put(TB_TaiKhoan_TenTK, tenTK);
        values.put(TB_TaiKhoan_MatKhau, matKhau);
        values.put(TB_TaiKhoan_Email, email);
        values.put(TB_TaiKhoan_SDT, sdt);

        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(TB_TaiKhoan, null, values);
    }

    //quên mk
    public void capNhatMatKhau(String email, String matKhauMoi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TB_TaiKhoan_MatKhau, matKhauMoi);

        db.update(TB_TaiKhoan, values, TB_TaiKhoan_Email + " = ?", new String[]{email});
    }

    //Thêm sự kiện
    public long insertEvent(String title, String note, String date, String time, String location, String calendarType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TB_SuKien_TenSK, title);
        values.put(TB_SuKien_MieuTa, note);
        values.put(TB_SuKien_Ngay, date);
        values.put(TB_SuKien_Gio, time);
        values.put(TB_SuKien_DiaDiem, location);
        values.put(TB_SuKien_LichDuongHoacAm, calendarType);
        return db.insert(TB_SuKien, null, values);
    }

    public List<Event> getEventsByDateAndCalendarType(String date, String calendarType) {
        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
                TB_SuKien_MaSK,
                TB_SuKien_TenSK,
                TB_SuKien_MieuTa,
                TB_SuKien_Ngay,
                TB_SuKien_Gio,
                TB_SuKien_LichDuongHoacAm,
                TB_SuKien_DiaDiem
        };
        String selection = TB_SuKien_Ngay + " = ? AND " + TB_SuKien_LichDuongHoacAm + " = ?";
        String[] selectionArgs = {date, calendarType};

        try (Cursor cursor = db.query(TB_SuKien, columns, selection, selectionArgs, null, null, null)) {
            if (cursor.moveToFirst()) {
                do {
                    events.add(new Event(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getString(5),
                            cursor.getString(6)
                    ));
                } while (cursor.moveToNext());
            }
        }
        return events;
    }

    //Kiểm tra sự kiện được thêm
    public boolean hasEventsOnDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = TB_SuKien_Ngay + " = ?";
        String[] selectionArgs = {date};

        Cursor cursor = db.query(TB_SuKien, new String[]{TB_SuKien_MaSK}, selection, selectionArgs, null, null, null);
        boolean hasEvents = cursor.getCount() > 0;
        cursor.close();
        return hasEvents;
    }

    // Phương thức xóa sự kiện theo ID
    public boolean deleteEvent(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TB_SuKien, TB_SuKien_MaSK + " = ?", new String[]{String.valueOf(eventId)}) > 0;
    }

    // Phương thức cập nhật sự kiện
    public boolean updateEvent(int eventId, String title, String note, String date, String time, String location, String calendarType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TB_SuKien_TenSK, title);
        values.put(TB_SuKien_MieuTa, note);
        values.put(TB_SuKien_Ngay, date);
        values.put(TB_SuKien_Gio, time);
        values.put(TB_SuKien_DiaDiem, location);
        values.put(TB_SuKien_LichDuongHoacAm, calendarType);
        return db.update(TB_SuKien, values, TB_SuKien_MaSK + " = ?", new String[]{String.valueOf(eventId)}) > 0;
    }


    // Thêm một ghi chú mới
    public long insertNote(String title, String content, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TB_Note_TieuDe, title);
        values.put(TB_Note_NoiDung, content);
        values.put(TB_Note_Ngay, date);
        values.put(TB_Note_Gio, time);
        return db.insert(TB_Note, null, values);
    }

    // Lấy ghi chú cho một ngày cụ thể
    public Cursor getNoteByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = TB_Note_Ngay + " = ?";
        String[] selectionArgs = {date};
        return db.query(TB_Note, null, selection, selectionArgs, null, null, null);
    }

//    // Lấy tất cả ghi chú
//    public Cursor getAllNotes() {
//        SQLiteDatabase db = this.getReadableDatabase();
//        return db.query(TB_Note, null, null, null, null, null, TB_Note_Ngay + " DESC");
//    }

    // Cập nhật ghi chú
    public boolean updateNote(int noteId, String title, String content, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TB_Note_TieuDe, title);
        values.put(TB_Note_NoiDung, content);
        values.put(TB_Note_Ngay, date);
        values.put(TB_Note_Gio, time);
        return db.update(TB_Note, values, TB_Note_MaNote + " = ?", new String[]{String.valueOf(noteId)}) > 0;
    }

//    // Xóa ghi chú theo ID
//    public boolean deleteNote(int noteId) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        return db.delete(TB_Note, TB_Note_MaNote + " = ?", new String[]{String.valueOf(noteId)}) > 0;
//    }

    // Xóa ghi chú theo ngày
    public boolean deleteNoteByDate(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TB_Note, TB_Note_Ngay + " = ?", new String[]{date}) > 0;
    }

//    // Kiểm tra xem có ghi chú cho một ngày cụ thể không
//    public boolean hasNoteOnDate(String date) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        String selection = TB_Note_Ngay + " = ?";
//        String[] selectionArgs = {date};
//        Cursor cursor = db.query(TB_Note, new String[]{TB_Note_MaNote}, selection, selectionArgs, null, null, null);
//        boolean hasNote = cursor.getCount() > 0;
//        cursor.close();
//        return hasNote;
//    }



    public SQLiteDatabase openDB() {
        return this.getWritableDatabase();
    }
}
