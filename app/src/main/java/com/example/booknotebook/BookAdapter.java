package com.example.booknotebook;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booknotebook.databinding.BookListRecyclerBinding;

import java.util.ArrayList;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookHolder> {

    ArrayList<Book> bookArrayList;

    public BookAdapter(ArrayList<Book> bookArrayList){
        this.bookArrayList = bookArrayList;
    }

    @NonNull
    @Override
    public BookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        BookListRecyclerBinding bookListRecyclerBinding = BookListRecyclerBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);

        return new BookHolder(bookListRecyclerBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookHolder holder, int position) {

        Bitmap bitmapImg= BitmapFactory.decodeByteArray(bookArrayList.get(position).image, 0 , bookArrayList.get(position).image.length);
        holder.binding.rcyclerBookImage.setImageBitmap(bitmapImg);
        holder.binding.bookListNameRecycler.setText(bookArrayList.get(position).name);
        holder.binding.bookListAuthorRecycler.setText(bookArrayList.get(position).author);
        holder.binding.bookListDateRecycler.setText(bookArrayList.get(position).readingDate);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(),BookActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("bookId",bookArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return bookArrayList.size();
    }

    public class BookHolder extends RecyclerView.ViewHolder {

        private BookListRecyclerBinding binding;

        public BookHolder(BookListRecyclerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}