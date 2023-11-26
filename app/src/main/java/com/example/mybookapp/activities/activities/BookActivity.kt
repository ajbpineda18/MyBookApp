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
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookActivity : AppCompatActivity(), BooksAdapter.BooksAdapterInterface,
    AddBookDialog.RefreshDataInterface {
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var binding: ActivityBookBinding
    private lateinit var booksList: ArrayList<Books>
    private var database = RealmDatabase()
    private lateinit var adapter: BooksAdapter

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
            val booksId = adapter.getBooksId(position)

            AlertDialog.Builder(this@BookActivity)
                .setTitle("Delete")
                .setMessage("Are you sure you want to Archive this?")
                .setPositiveButton("Archive") { _, _ ->
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

    override fun onResume() {
        super.onResume()
        getBooks()
    }
    override fun refreshData() {
        getBooks()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this)
        binding.rvBooks.layoutManager = layoutManager

        booksList = arrayListOf()
        adapter = BooksAdapter(booksList, this, this)
        binding.rvBooks.adapter = adapter

        itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvBooks)

        getBooks()

        binding.fab.setOnClickListener {
            val addBookDialog = AddBookDialog()
            addBookDialog.refreshDataCallback = this
            addBookDialog.show(supportFragmentManager, "AddBookDialog")
        }
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

    private suspend fun loadAllBooks() {
        val books = database.getAllBooks()
        val booksList = ArrayList(books.map { mapBooks(it) })
        withContext(Dispatchers.Main) {
            adapter.updateBookList(booksList)
            adapter.notifyDataSetChanged()
            binding.empty.text = if (booksList.isEmpty()) "No Books Yet..." else ""
        }
    }

    private fun getBooks() {
        val coroutineContext = Job() + Dispatchers.IO
        val scope = CoroutineScope(coroutineContext + CoroutineName("LoadAllBooks"))
        scope.launch { loadAllBooks() }
    }
}
