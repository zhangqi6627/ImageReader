package com.china.reader.imagereader.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class ImageBean {
    @Id(autoincrement = true)
    private Long id;
    private int webIndex;
    private int pageIndex;
    @Unique
    private String imageUrl;
    private int width;
    private int height;
    private boolean isFavorite;
    @Keep
    public ImageBean(int webIndex,int pageIndex, String imageUrl){
        this.webIndex = webIndex;
        this.pageIndex = pageIndex;
        this.imageUrl = imageUrl;
    }
    @Generated(hash = 1250200731)
    public ImageBean(Long id, int webIndex, int pageIndex, String imageUrl,
            int width, int height, boolean isFavorite) {
        this.id = id;
        this.webIndex = webIndex;
        this.pageIndex = pageIndex;
        this.imageUrl = imageUrl;
        this.width = width;
        this.height = height;
        this.isFavorite = isFavorite;
    }
    @Generated(hash = 645668394)
    public ImageBean() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public int getWebIndex() {
        return this.webIndex;
    }
    public void setWebIndex(int webIndex) {
        this.webIndex = webIndex;
    }
    public int getPageIndex() {
        return this.pageIndex;
    }
    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }
    public String getImageUrl() {
        return this.imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public int getWidth() {
        return this.width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
    public int getHeight() {
        return this.height;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public boolean getIsFavorite() {
        return this.isFavorite;
    }
    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

}
