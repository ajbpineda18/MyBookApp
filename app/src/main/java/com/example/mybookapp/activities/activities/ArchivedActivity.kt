package com.example.mybookapp.activities.activities

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mybookapp.activities.adapters.ArchivedAdapter
import com.example.mybookapp.activities.models.Books
import com.example.mybookapp.activities.realm.BookRealm
import com.example.mybookapp.activities.realm.RealmDatabase
import com.example.mybookapp.databinding.ActivityArchivedBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.BsonObjectId
import java.util.Date

class ArchivedActivity: AppCompatActivity(), ArchivedAdapter.ArchivedBooksAdapterInterface {
    private lateinit var binding: ActivityArchivedBinding
    private lateinit var adapter: ArchivedAdapter
    private lateinit var booksList: ArrayList<Books>
    private lateinit var itemTouchHelper: ItemTouchHelper
    private var database = RealmDatabase()

    private val swipeToDeleteCallback = object : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val bookId = adapter.getBooksId(position)

            AlertDialog.Builder(this@ArchivedActivity)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this permanently?")
                .setPositiveButton("Delete") { _, _ ->
                    adapter.onItemDismiss(position)
                    getBooks()

                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    adapter.notifyItemChanged(position)
                    dialog.dismiss()
                }
                .show()

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArchivedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManger = LinearLayoutManager(this)
        binding.rvArchivedBooks.layoutManager = layoutManger

        booksList = arrayListOf()
        adapter = ArchivedAdapter(booksList, this, this)
        binding.rvArchivedBooks.adapter = adapter

        itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvArchivedBooks)

        getBooks()
    }
    override fun unArchiveBook(bookId: String, position: Int) {
    }
    override fun deleteBook(bookId: String, position: Int) {
        val coroutineScope = CoroutineScope(Job() + Dispatchers.IO + CoroutineName("deleteBook"))

        coroutineScope.launch {
            val book = booksList[position]
            val bookDelete = BsonObjectId(book.id)

            withContext(Dispatchers.IO) {
                database.deleteBook(bookDelete)
            }

            withContext(Dispatchers.Main) {
                booksList.removeAt(position)
                adapter.notifyItemRemoved(position)
                val updatedBooks = database.getArchivedBooks().map { mapBooks(it) } as ArrayList<Books>
                adapter.updateBookList(updatedBooks)

                Snackbar.make(binding.root, "Book Deleted Successfully", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun refreshData(){
        getBooks()
    }
    private fun mapBooks(books: BookRealm): Books = Books(
        id = books.id.toHexString(),
        bookName = books.name,
        author = books.author,
        dateBookAdded = Date(books.dateBookAdded),
        dateBookModified = Date(books.dateBookModified),
        dateBookPublished = Date(books.dateBookPublished)
    )

    fun getBooks() {
        val coroutineScope = CoroutineScope(Job() + Dispatchers.IO + CoroutineName("LoadArchivedBooks"))

        coroutineScope.launch {
            val books = database.getArchivedBooks()
            val booksList = ArrayList(books.map { mapBooks(it) })

            withContext(Dispatchers.Main) {
                adapter.updateBookList(booksList)
                adapter.notifyDataSetChanged()
                binding.empty.text = if (booksList.isEmpty()) "\nNo Books Are Archived" else ""
            }
        }
    }

}