package com.example.mybookapp.activities.activities

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mybookapp.activities.adapters.FavBooksAdapter
import com.example.mybookapp.activities.models.Books
import com.example.mybookapp.activities.realm.BookRealm
import com.example.mybookapp.activities.realm.RealmDatabase
import com.example.mybookapp.databinding.ActivityFavoritesBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FavoritesActivity : AppCompatActivity(), FavBooksAdapter.FavBooksAdapterInterface {
    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var adapter: FavBooksAdapter
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
        ): Boolean = true

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition

            AlertDialog.Builder(this@FavoritesActivity)
                .setTitle("Unfavorite")
                .setMessage("Are you sure you want to unfavorite this?")
                .setPositiveButton("Unfavorite") { _, _ ->
                    adapter.onItemDismiss(position)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    adapter.notifyItemChanged(position)
                    dialog.dismiss()
                }
                .show()

            getBooks()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = LinearLayoutManager(this)
        binding.rvFavBooks.layoutManager = layoutManager

        booksList = arrayListOf()
        adapter = FavBooksAdapter(booksList, this, this)
        binding.rvFavBooks.adapter = adapter

        itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvFavBooks)

        getBooks()
    }

    override fun unFavBook(bookId: String, position: Int) {
        val coroutineScope = CoroutineScope(Job() + Dispatchers.IO + CoroutineName("unFavBook"))

        coroutineScope.launch {
            val book = booksList[position]
            database.unFavBook(book)

            withContext(Dispatchers.Main) {
                booksList.removeAt(position)
                adapter.notifyItemRemoved(position)
                val updatedBooks = database.getFavoriteBooks().map { mapBooks(it) } as ArrayList<Books>
                adapter.updateBookList(updatedBooks)

                Snackbar.make(binding.root, "Book Unfavorited Successfully", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun refreshData() {
        getBooks()
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

    private fun getBooks() {
        val coroutineScope = CoroutineScope(Job() + Dispatchers.IO + CoroutineName("LoadFavoriteBooks"))

        coroutineScope.launch {
            val favoriteBooks = database.getFavoriteBooks()
            val updatedBooksList = ArrayList(favoriteBooks.map { mapBooks(it) })

            withContext(Dispatchers.Main) {
                adapter.updateBookList(updatedBooksList)
                adapter.notifyDataSetChanged()
                binding.empty.text = if (updatedBooksList.isEmpty()) "\nNo Favorite Books Yet" else ""
            }
        }
    }
}
