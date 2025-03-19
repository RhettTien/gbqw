package com.rhett.gbqw.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库帮助类，负责创建和管理SQLite数据库
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    // 数据库基本信息
    private static final String DATABASE_NAME = "standards.db";
    private static final int DATABASE_VERSION = 1;

    // 收藏表相关常量
    public static final String TABLE_FAVORITES = "favorites";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_STANDARD_NO = "standard_no";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_CREATE_TIME = "create_time";

    // 创建收藏表的SQL语句
    private static final String CREATE_TABLE_FAVORITES = 
            "CREATE TABLE " + TABLE_FAVORITES + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TITLE + " TEXT NOT NULL, " +
            COLUMN_STANDARD_NO + " TEXT NOT NULL, " +
            COLUMN_URL + " TEXT NOT NULL, " +
            COLUMN_CREATE_TIME + " INTEGER NOT NULL);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * 创建数据库表
     * @param db 数据库对象
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_FAVORITES);
    }

    /**
     * 数据库升级时的操作
     * @param db 数据库对象
     * @param oldVersion 旧版本号
     * @param newVersion 新版本号
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 数据库升级时删除旧表并创建新表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }
} 