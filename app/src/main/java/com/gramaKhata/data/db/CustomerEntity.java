package com.gramaKhata.data.db;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "customers")
public class CustomerEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    @Nullable
    private String photoUri;
    private String phone;
    private long createdAt = System.currentTimeMillis();

    public CustomerEntity() {
    }

    public CustomerEntity(String name, @Nullable String photoUri, String phone) {
        this.name = name;
        this.photoUri = photoUri;
        this.phone = phone;
        this.createdAt = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(@Nullable String photoUri) {
        this.photoUri = photoUri;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
