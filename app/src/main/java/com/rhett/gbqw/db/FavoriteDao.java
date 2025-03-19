package com.rhett.gbqw.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.rhett.gbqw.model.FavoriteItem;
import java.util.ArrayList;
import java.util.List;

/**
 * 收藏数据访问对象，负责处理收藏相关的数据库操作
 */
public class FavoriteDao {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public FavoriteDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * 打开数据库连接
     */
    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    /**
     * 关闭数据库连接
     */
    public void close() {
        dbHelper.close();
    }

    /**
     * 添加收藏项
     * @param item 要添加的收藏项
     * @return 新插入行的ID
     */
    public long addFavorite(FavoriteItem item) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, item.getTitle());
        values.put(DatabaseHelper.COLUMN_STANDARD_NO, item.getStandardNo());
        values.put(DatabaseHelper.COLUMN_URL, item.getUrl());
        values.put(DatabaseHelper.COLUMN_CREATE_TIME, System.currentTimeMillis());
        return database.insert(DatabaseHelper.TABLE_FAVORITES, null, values);
    }

    /**
     * 根据ID删除收藏项
     * @param id 要删除的收藏项ID
     * @return 是否删除成功
     */
    public boolean deleteFavorite(long id) {
        return database.delete(DatabaseHelper.TABLE_FAVORITES,
                DatabaseHelper.COLUMN_ID + " = " + id, null) > 0;
    }

    /**
     * 检查URL是否已收藏
     * @param url 要检查的URL
     * @return 是否已收藏
     */
    public boolean isFavorited(String url) {
        Cursor cursor = database.query(DatabaseHelper.TABLE_FAVORITES,
                new String[]{DatabaseHelper.COLUMN_ID},
                DatabaseHelper.COLUMN_URL + " = ?",
                new String[]{url}, null, null, null);
        boolean isFavorited = cursor.getCount() > 0;
        cursor.close();
        return isFavorited;
    }

    /**
     * 分页获取收藏列表
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 收藏项列表
     */
    public List<FavoriteItem> getFavorites(int offset, int limit) {
        List<FavoriteItem> favorites = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.TABLE_FAVORITES,
                null, null, null, null, null,
                DatabaseHelper.COLUMN_CREATE_TIME + " DESC",
                offset + ", " + limit);

        while (cursor.moveToNext()) {
            FavoriteItem item = new FavoriteItem();
            item.setId(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID)));
            item.setTitle(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE)));
            item.setStandardNo(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_STANDARD_NO)));
            item.setUrl(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_URL)));
            item.setCreateTime(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.COLUMN_CREATE_TIME)));
            favorites.add(item);
        }
        cursor.close();
        return favorites;
    }

    /**
     * 获取收藏总数
     * @return 收藏项总数
     */
    public int getFavoritesCount() {
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_FAVORITES, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    /**
     * 根据URL删除收藏项
     * @param url 要删除的收藏项URL
     * @return 是否删除成功
     */
    public boolean deleteFavoriteByUrl(String url) {
        return database.delete(DatabaseHelper.TABLE_FAVORITES,
                DatabaseHelper.COLUMN_URL + " = ?",
                new String[]{url}) > 0;
    }
} 