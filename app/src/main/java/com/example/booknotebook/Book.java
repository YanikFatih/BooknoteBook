package com.example.booknotebook;

import android.graphics.Bitmap;

import java.sql.Blob;

public class Book {

    int id;
    String name;
    String author;
    String readingDate;
    byte[] image;

    public Book(byte[] image, int id, String name, String author, String readingDate){
        this.id = id;
        this.name = name;
        this.author = author;
        this.readingDate = readingDate;
        this.image = image;
    }

}
