package com.china.reader.imagereader.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;

@Entity
public class ImagePage implements Serializable{
    public final static long serialVersionUID = 11L;
    @Id(autoincrement = true)
    private Long id;
    private int webIndex;
    private int pageIndex;
    @Unique
    private String title;
    private String albumImage;
    private boolean isViewed = false;
    private boolean isFavorite = false;
    @Keep
    public ImagePage(int webIndex, int pageIndex, String title, String albumImage) {
        this.webIndex = webIndex;
        this.title = title;
        this.albumImage = albumImage;
        this.pageIndex = pageIndex;
    }
    @Keep
    public ImagePage(int webIndex, int pageIndex, String title) {
        this.webIndex = webIndex;
        this.title = title;
        this.pageIndex = pageIndex;
    }
    @Generated(hash = 71049392)
    public ImagePage(Long id, int webIndex, int pageIndex, String title,
            String albumImage, boolean isViewed, boolean isFavorite) {
        this.id = id;
        this.webIndex = webIndex;
        this.pageIndex = pageIndex;
        this.title = title;
        this.albumImage = albumImage;
        this.isViewed = isViewed;
        this.isFavorite = isFavorite;
    }
    @Generated(hash = 1560919835)
    public ImagePage() {
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
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getAlbumImage() {
        return this.albumImage;
    }
    public void setAlbumImage(String albumImage) {
        this.albumImage = albumImage;
    }
    public boolean getIsViewed() {
        return this.isViewed;
    }
    public void setIsViewed(boolean isViewed) {
        this.isViewed = isViewed;
    }
    public boolean getIsFavorite() {
        return this.isFavorite;
    }
    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }
}
