package com.example.mybookapp.activities.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mybookapp.activities.models.Books
import com.example.mybookapp.activities.realm.RealmDatabase
import com.example.mybookapp.databinding.ContentFavBooksRvBinding
import com.example.mybookapp.activities.adapters.ItemTouchHelperAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FavBooksAdapter(
    private var booksList: ArrayList<Books>,
    private val context: Context,
    private val bookAdapterCallback: FavBooksAdapterInterface,
) : RecyclerView.Adapter<FavBooksAdapter.BookViewHolder>(), ItemTouchHelperAdapter {

    private lateinit var book: Books
    private var database = RealmDatabase()

    interface FavBooksAdapterInterface {
        fun unFavBook(bookId: String, position: Int)

        //        fun archiveOwner(ownerId: String, position: Int)
//        fun deleteOwnerAndTransferPets(ownerId: String, position: Int)
        fun refreshData()
    }

    inner class BookViewHolder(private val binding: ContentFavBooksRvBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(book: Books) {
            with(binding) {
                txtBookName.text = String.format("Book: %s", book.bookName)
                txtAuthor.text = String.format("Author: %s", book.author)
                txtPublished.text =
                    String.format("Date Published: %s", formatDate(book.dateBookPublished))
                txtAdded.text = String.format("Date Added: %s", formatDate(book.dateBookAdded))
                txtModified.text =
                    String.format("Date Modified: %s", formatDate(book.dateBookModified))
            }
        }
    }
    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        return formatter.format(date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavBooksAdapter.BookViewHolder {
        val binding =
            ContentFavBooksRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavBooksAdapter.BookViewHolder, position: Int) {
        val bookData = booksList[position]
        holder.bind(bookData)
        holder.itemView.tag = position
    }

    override fun getItemCount(): Int {
        return booksList.size
    }

    fun updateBookList(booksList: ArrayList<Books>) {
        this.booksList.clear()
        this.booksList.addAll(booksList)
        notifyDataSetChanged()
    }

    fun getBooksId(position: Int): String? {
        if (position in 0 until booksList.size) {
            return booksList[position].id
        }
        return null
    }

    override fun onItemDismiss(position: Int) {
        if (position in 0 until booksList.size) {
            val bookId = booksList[position].id
            bookAdapterCallback.unFavBook(bookId, position)
            //ownerAdapterCallback.archiveOwner(ownerId, position)
        }
    }

}