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

                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // User clicked Cancel, dismiss the dialog
                    adapter.notifyItemChanged(position)
                    dialog.dismiss()
                }
                .show()
            getBooks()

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

        binding

        getBooks()

    }

    override fun unArchiveBook(bookId: String, position: Int) {
//        val coroutineContext = Job() + Dispatchers.IO
//        val scope = CoroutineScope(coroutineContext + CoroutineName("unarchiveBook"))
//        scope.launch(Dispatchers.IO) {
//            val book = booksList[position]
//            database.unArchiveBook(book)
//            withContext(Dispatchers.Main){
//                booksList.removeAt(position)
//                adapter.notifyItemRemoved(position)
//                adapter.updateBookList(database.getFavoriteBooks().map {mapBooks(it)} as ArrayList<Books>)
//                Snackbar.make(binding.root, "Book Unarchived Successfully", Snackbar.LENGTH_LONG).show()
//            }
//        }
    }

    override fun deleteBook(bookId: String, position: Int) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("deleteBook"))
        scope.launch(Dispatchers.IO) {
            val book = booksList[position]
            val bookDelete = BsonObjectId(book.id)
            database.deleteBook(bookDelete)
            withContext(Dispatchers.Main){
                booksList.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.updateBookList(database.getArchivedBooks().map {mapBooks(it)} as ArrayList<Books>)
                Snackbar.make(binding.root, "Book Deleted Successfully", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun refreshData(){
        getBooks()
    }



    private fun mapBooks(books: BookRealm): Books {
        return Books(
            id = books.id.toHexString(),
            bookName = books.name,
            author = books.author,
            dateBookAdded = Date(books.dateBookAdded),
            dateBookModified = Date(books.dateBookModified),
            dateBookPublished = Date(books.dateBookPublished)

        )
    }

    fun getBooks() {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("LoadArchivedBooks"))

        scope.launch(Dispatchers.IO) {
            val books = database.getArchivedBooks()
            val booksList = arrayListOf<Books>()
            booksList.addAll(
                books.map {
                    mapBooks(it)
                }
            )
            withContext(Dispatchers.Main) {
                adapter.updateBookList(booksList)
                adapter.notifyDataSetChanged()
                binding.empty.text = if (booksList.isEmpty()) "No Books Archived in this List" else ""
            }
        }
    }
}