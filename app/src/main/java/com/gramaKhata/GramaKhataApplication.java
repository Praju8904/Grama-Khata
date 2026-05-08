package com.gramaKhata;

import android.app.Application;

import com.gramaKhata.data.repository.GramaKhataRepository;

public class GramaKhataApplication extends Application {

    private static GramaKhataRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        repository = new GramaKhataRepository(this);
    }

    public static GramaKhataRepository getRepository() {
        return repository;
    }
}
