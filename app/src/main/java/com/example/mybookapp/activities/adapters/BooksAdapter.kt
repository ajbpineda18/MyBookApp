package com.example.mybookapp.activities.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mybookapp.activities.models.Books
import com.example.mybookapp.activities.realm.RealmDatabase
import com.example.mybookapp.databinding.ContentBooksRvBinding
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class BooksAdapter(
    private var booksList: ArrayList<Books>,
    private val context: Context,
    private val bookAdapterCallback: BooksAdapterInterface,
) : RecyclerView.Adapter<BooksAdapter.BookViewHolder>(), ItemTouchHelperAdapter {

    private lateinit var book: Books
    private var database = RealmDatabase()

    interface BooksAdapterInterface {
        fun archiveBook(bookId: ObjectId, position: Int)
        fun refreshData()
    }
    fun setBook(book: Books) {
        this.book = book
    }
    inner class BookViewHolder(private val binding: ContentBooksRvBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: Books) {
            with(binding) {
                txtBookName.text = String.format("Title: %s", book.bookName)
                txtAuthor.text = String.format("Author: %s", book.author)
                txtPublished.text =
                    String.format("Date Published: %s", formatDate(book.dateBookPublished))
                txtAdded.text = String.format("Date Added: %s", formatDate(book.dateBookAdded))
                txtModified.text =
                    String.format("Date Modified: %s", formatDate(book.dateBookModified))


                btnToFav.setOnClickListener {
                    val coroutineContext = Job() + Dispatchers.IO
                    val scope = CoroutineScope(coroutineContext + CoroutineName("favBook"))
                    scope.launch(Dispatchers.IO) {
                        database.favBook(book)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Book is added to Favorites List!", Toast.LENGTH_LONG).show()
                            bookAdapterCallback.refreshData()
                        }
                    }
                }
            }
        }
    }
    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        return formatter.format(date)
    }
    fun LocalDate.formatted(): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
        return format(formatter)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding =
            ContentBooksRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }
    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
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
            val bookId = BsonObjectId(booksList[position].id)
            bookAdapterCallback.archiveBook(bookId, position)
        } else {
            //Log.d("OwnerAdapter", "Error: Position out of bounds")
        }
    }


}