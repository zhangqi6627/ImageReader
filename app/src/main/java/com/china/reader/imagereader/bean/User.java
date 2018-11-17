package com.china.reader.imagereader.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class User
{
    @Id(autoincrement = true)
    private Long id;
    private String username;
    private String password;
    private int points;
    @Keep
    public User(String username, String password){
        this.username = username;
        this.password = password;
    }
    @Keep
    public User(String username, String password, int points) {
        this.username = username;
        this.password = password;
        this.points = points;
    }
    @Generated(hash = 586692638)
    public User() {
    }
    @Generated(hash = 1688032590)
    public User(Long id, String username, String password, int points) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.points = points;
    }
    public Long getId() {
        return this.id;
    }
    public String getUsername() {
        return this.username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public int getPoints() {
        return this.points;
    }
    public void setPoints(int points) {
        this.points = points;
    }
    public void setId(Long id) {
        this.id = id;
    }

}
