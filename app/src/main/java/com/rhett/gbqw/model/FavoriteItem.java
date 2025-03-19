package com.rhett.gbqw.model;

/**
 * 收藏项数据模型类，用于存储标准收藏的相关信息
 */
public class FavoriteItem {
    private long id;              // 数据库主键
    private String title;         // 标准标题
    private String standardNo;    // 标准编号
    private String url;           // 标准URL
    private long createTime;      // 收藏时间

    /**
     * 默认构造函数
     */
    public FavoriteItem() {
    }

    /**
     * 带参数的构造函数
     * @param title 标准标题
     * @param standardNo 标准编号
     * @param url 标准URL
     */
    public FavoriteItem(String title, String standardNo, String url) {
        this.title = title;
        this.standardNo = standardNo;
        this.url = url;
        this.createTime = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStandardNo() {
        return standardNo;
    }

    public void setStandardNo(String standardNo) {
        this.standardNo = standardNo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
} 