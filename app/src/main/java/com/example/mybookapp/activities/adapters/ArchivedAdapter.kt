package com.example.mybookapp.activities.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mybookapp.activities.models.Books
import com.example.mybookapp.activities.realm.RealmDatabase
import com.example.mybookapp.databinding.ContentArchivedBooksRvBinding
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ArchivedAdapter (
    private var booksList: ArrayList<Books>,
    private val context: Context,
    private val bookAdapterCallback: ArchivedBooksAdapterInterface,
) : RecyclerView.Adapter<ArchivedAdapter.BookViewHolder>(), ItemTouchHelperAdapter{

    private lateinit var book: Books
    private var database = RealmDatabase()

    interface ArchivedBooksAdapterInterface {
        fun unArchiveBook(bookId: String, position: Int)
        fun deleteBook(bookId: String, position: Int)
        fun refreshData()
    }
    inner class BookViewHolder(private val binding: ContentArchivedBooksRvBinding) :
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

                btnUnarchive.setOnClickListener {
                    val coroutineContext = Job() + Dispatchers.IO
                    val scope = CoroutineScope(coroutineContext + CoroutineName("unarchiveBook"))
                    scope.launch(Dispatchers.IO) {
                        database.unArchiveBook(book)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Book Unarchived!", Toast.LENGTH_LONG).show()
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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchivedAdapter.BookViewHolder {
        val binding = ContentArchivedBooksRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ArchivedAdapter.BookViewHolder, position: Int) {
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
            bookAdapterCallback.deleteBook(bookId, position)
        }
    }
}