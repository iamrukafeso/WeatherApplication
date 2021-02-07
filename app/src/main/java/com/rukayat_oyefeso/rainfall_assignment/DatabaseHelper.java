package com.rukayat_oyefeso.rainfall_assignment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    public static final String DATABASE_NAME = "City";
    private static final String  TABLE_NAME = "list";
    public static final String COL1 = "ID";
    public static final String COL2 = "NAME";
    public static final String COL3 = "LIST";
    public static final String COL4 = "RAIN";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_NAME + "(" + COL1
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL2
            + " VARCHAR, " + COL3
            + " VARCHAR, " + COL4
            + " VARCHAR );";

    public DatabaseHelper(Context context) {
        super(context,  DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addData(String cityName,String days, String raintTyp){
        SQLiteDatabase db = this.getWritableDatabase();
        //Creates an empty set of values using the default initial size
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, cityName);
        contentValues.put(COL3, days);
        contentValues.put(COL4, raintTyp);

        db.insert(TABLE_NAME, null, contentValues);

    }

    public Cursor getData(){
        SQLiteDatabase db  = this.getWritableDatabase();

        Cursor data  = db.rawQuery(  "SELECT * FROM " + TABLE_NAME,null);
        return data;
    }

    public Cursor getLastData(){
        SQLiteDatabase db  = this.getWritableDatabase();
        String query =  "SELECT * FROM " + TABLE_NAME +" ORDER BY "+ COL1+" DESC LIMIT 1;";
        Cursor cursor = db.rawQuery(query,null);
        return  cursor;
    }

    public Cursor findById(String ID) {
        String query = "Select * FROM " + TABLE_NAME + " WHERE " + COL1 + " =  \"" + ID + "\"";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        return cursor;
    }
    public boolean editList(String id,String listName,String list) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL2, listName);
        values.put(COL3, list);
        db.update(TABLE_NAME,values, COL1 + " = ?", new String[] {id});
        db.close();
        return true;
    }

}
