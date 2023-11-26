package com.example.mybookapp.activities.activities

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mybookapp.activities.dialogs.AddBookDialog
import com.example.mybookapp.activities.models.Books
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ph.edu.rv_realm_quiz.adapters.BooksAdapter
import ph.edu.rv_realm_quiz.realm.BookRealm
import ph.edu.rv_realm_quiz.realm.RealmDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookActivity: AppCompatActivity(), BooksAdapter.BooksAdapterInterface, AddBookDialog.RefreshDataInterface {
    private lateinit var binding: BookActivity
    private lateinit var adapter: BooksAdapter
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
            val books = adapter.getBooksId(position)

            AlertDialog.Builder(this@BookActivity)
                .setTitle("Delete")
                .setMessage("Are you sure you want to archive this?")
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
        binding = BookActivity.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManger = LinearLayoutManager(this)
        binding.rvBooks.layoutManager = layoutManger

        booksList = arrayListOf()
        adapter = BooksAdapter(booksList, this, this)
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
                binding.empty.text = if (booksList.isEmpty()) "No Books Yet..." else ""
            }
        }
    }

//    override fun archiveBooks(ownerId: String, position: Int) {
//        val coroutineContext = Job() + Dispatchers.IO
//        val scope = CoroutineScope(coroutineContext + CoroutineName("archiveBook"))
//        scope.launch(Dispatchers.IO) {
//
//        }
//    }

}