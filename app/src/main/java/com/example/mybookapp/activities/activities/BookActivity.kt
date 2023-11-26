package com.example.mybookapp.activities.activities

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mybookapp.activities.adapters.BooksAdapter
import com.example.mybookapp.activities.dialogs.AddBookDialog
import com.example.mybookapp.activities.models.Books
import com.example.mybookapp.activities.realm.BookRealm
import com.example.mybookapp.activities.realm.RealmDatabase
import com.example.mybookapp.databinding.ActivityBookBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookActivity : AppCompatActivity(), BooksAdapter.BooksAdapterInterface,
    AddBookDialog.RefreshDataInterface {
    private lateinit var binding: ActivityBookBinding
    private lateinit var booksList: ArrayList<Books>
    private lateinit var adapter: BooksAdapter
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
            val books = adapter.getBooksId(position)

            AlertDialog.Builder(this@BookActivity)
                .setTitle("Archive")
                .setMessage("Do you want to archive this book?")
                .setPositiveButton("Archive") { _, _ ->
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



    override fun refreshData(){
        getBooks()
    }

    override fun onResume() {
        super.onResume()
        //TODO: REALM DISCUSSION HERE
        getBooks()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManger = LinearLayoutManager(this)
        binding.rvBooks.layoutManager = layoutManger

        booksList = arrayListOf()
        adapter = BooksAdapter(booksList,this, this)
        binding.rvBooks.adapter = adapter

        itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvBooks)

        getBooks()

        binding.fab.setOnClickListener{
            val addBookDialog = AddBookDialog()
            addBookDialog.refreshDataCallback = this
            addBookDialog.show(supportFragmentManager, "AddBookDialog")
        }

//        getOwners()

//        itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
//        itemTouchHelper.attachToRecyclerView(binding.rvBooks)



    }

    private fun mapBooks(books: BookRealm): Books {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
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
        val scope = CoroutineScope(coroutineContext + CoroutineName("LoadAllBooks"))

        scope.launch(Dispatchers.IO) {
            val books = database.getAllBooks()
            val booksList = arrayListOf<Books>()
            booksList.addAll(
                books.map {
                    mapBooks(it)
                }
            )
            withContext(Dispatchers.Main) {
                adapter.updateBookList(booksList)
                adapter.notifyDataSetChanged()
                binding.empty.text = if (booksList.isEmpty()) "\nNo Books Added in this List" else ""
            }
        }
    }

    override fun archiveBook(bookId: ObjectId, position: Int) {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("archiveBook"))
        scope.launch(Dispatchers.IO) {
            val book = booksList[position]
            database.archiveBook(book)
            withContext(Dispatchers.Main){
                booksList.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.updateBookList(database.getAllBooks().map {mapBooks(it)} as ArrayList<Books>)
                Snackbar.make(binding.root, "Book Archived Successfully", Snackbar.LENGTH_LONG).show()
            }

        }
    }
}