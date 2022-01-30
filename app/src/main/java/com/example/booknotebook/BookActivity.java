package com.example.booknotebook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.example.booknotebook.databinding.ActivityBookBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class BookActivity extends AppCompatActivity {

    private ActivityBookBinding binding;

    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;

    Bitmap selectedBookImage;

    SQLiteDatabase bookable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();

        bookable = openOrCreateDatabase("Books", MODE_PRIVATE,null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if(info.equals("new")){
            //adding new book

            binding.bookname.setText("");
            binding.authorname.setText("");
            binding.readingdate.setText("");
            binding.savebt.setVisibility(View.VISIBLE);
            binding.bookImage.setImageResource(R.drawable.selectbookimage3);

        } else{

            int bookId = intent.getIntExtra("bookId",0);
            binding.savebt.setVisibility(View.INVISIBLE);

            try {

                Cursor cursor = bookable.rawQuery("SELECT * FROM books WHERE id = ?",new String[]{String.valueOf(bookId)});
                int bookNameIndex = cursor.getColumnIndex("bookName");
                int authorNameIndex = cursor.getColumnIndex("authorName");
                int readingDateIndex = cursor.getColumnIndex("readingDate");
                int imageIndex = cursor.getColumnIndex("image");

                while(cursor.moveToNext()){

                    binding.bookname.setText(cursor.getString(bookNameIndex));
                    binding.authorname.setText(cursor.getString(authorNameIndex));
                    binding.readingdate.setText(cursor.getString(readingDateIndex));

                    byte[] bytes = cursor.getBlob(imageIndex);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.bookImage.setImageBitmap(bitmap);
                }
                cursor.close();


            }catch (Exception exception){
                exception.printStackTrace();
            }

        }

    }

    public void save(View view){

        String bookName = binding.bookname.getText().toString();
        String authorName = binding.authorname.getText().toString();
        String readingDate = binding.readingdate.getText().toString();

        Bitmap smallSizeImage = makeImageSmallSize(selectedBookImage,350);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallSizeImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        byte[] bytes = outputStream.toByteArray();

        try {


            bookable.execSQL("CREATE TABLE IF NOT EXISTS books(id INTEGER PRIMARY KEY, bookName VARCHAR, authorName VARCHAR, readingDate VARCHAR, image BLOB)");

            String SqlString = "INSERT INTO books(bookName, authorName, readingDate, image) VALUES(?, ?, ?, ?)";

            SQLiteStatement sqLiteStatement = bookable.compileStatement(SqlString);

            sqLiteStatement.bindString(1,bookName);
            sqLiteStatement.bindString(2,authorName);
            sqLiteStatement.bindString(3,readingDate);
            sqLiteStatement.bindBlob(4,bytes);
            sqLiteStatement.execute();

        }catch (Exception exception){
            exception.printStackTrace();
        }

        Intent intent = new Intent(BookActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    public Bitmap makeImageSmallSize(Bitmap image, int maxSize){

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float)height;

        if (bitmapRatio > 1){
            //for landscape images

            width = maxSize;
            height = (int)(width / bitmapRatio);

        }else{
            //for portrait images

            height = maxSize;
            width = (int) (height * bitmapRatio);

        }

        return image.createScaledBitmap(image, width, height, true);
    }

    public void selectBookImage(View view){
        
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){

                Snackbar.make(view,"Permission needed!",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

                    }
                }).show();

            } else {

                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

            }

        } else {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(galleryIntent);

        }

    }

    private void registerLauncher(){

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if(result.getResultCode() == RESULT_OK){
                    //user have chosen photo from gallery
                    Intent fromResult = result.getData();

                    if(fromResult != null){
                        Uri bookImageData = fromResult.getData(); //like a path (uri)
                        //binding.imageView.setImageURI(bookImageData); --> but we will also save in db so not enough

                        try {

                            if(Build.VERSION.SDK_INT >= 28){
                                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(),bookImageData);
                                selectedBookImage = ImageDecoder.decodeBitmap(source);
                                binding.bookImage.setImageBitmap(selectedBookImage);
                            }else{
                                selectedBookImage = MediaStore.Images.Media.getBitmap(getContentResolver(),bookImageData);
                                binding.bookImage.setImageBitmap(selectedBookImage);
                            }

                        }catch (Exception exception){
                            exception.printStackTrace();
                        }
                    }
                }

            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {

                if(result){
                    //permission granted
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(galleryIntent);
                }else{
                    //permission denied
                    Toast.makeText(BookActivity.this,"Permission needed!",Toast.LENGTH_LONG).show();
                }

            }
        });

    }
}