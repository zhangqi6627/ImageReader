package com.james.imagereader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    public final static String TABLE_NAME = "albums";
    private Context mContext;
    public DatabaseHelper(Context context) {
        super(context, "assets", null, 1);
        mContext = context;
    }
    public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists albums(id INTEGER PRIMARY KEY AUTOINCREMENT, packageName varchar(80) unique, displayName varchar(120), packageSize varchar(10), imageCount integer, progress integer, offset integer, favorite boolean);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
