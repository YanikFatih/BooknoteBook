package com.example.booknotebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.booknotebook.databinding.ActivityMainBinding;

import java.sql.Blob;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    ArrayList<Book> bookArrayList;

    BookAdapter bookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        bookArrayList = new ArrayList<>();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookAdapter = new BookAdapter(bookArrayList);
        binding.recyclerView.setAdapter(bookAdapter);

        getDataFromDb();
    }

    private void getDataFromDb(){

        try {

            SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("Books", MODE_PRIVATE,null);

            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM books ORDER BY id desc",null);

            int imageIndex = cursor.getColumnIndex("image");
            int bookNameIndex = cursor.getColumnIndex("bookName");
            int authorNameIndex =  cursor.getColumnIndex("authorName");
            int readingDateIndex = cursor.getColumnIndex("readingDate");
            int idIndex = cursor.getColumnIndex("id");

            while (cursor.moveToNext()){

                byte[] image = cursor.getBlob(imageIndex);
                String name = cursor.getString(bookNameIndex);
                String author = cursor.getString(authorNameIndex);
                String date = cursor.getString(readingDateIndex);
                int id = cursor.getInt(idIndex);

                Book book = new Book(image, id, name, author, date);

                bookArrayList.add(book);

            }

            bookAdapter.notifyDataSetChanged();
            cursor.close();

        }catch (Exception exception){

            exception.printStackTrace();

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.addbook_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.add_Book){
            Intent intent = new Intent(this,BookActivity.class);
            intent.putExtra("info", "new");
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}