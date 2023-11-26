package ph.edu.rv_realm_quiz.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ph.edu.rv_realm_quiz.databinding.ContentBooksRvBinding
import ph.edu.rv_realm_quiz.dialogs.AddBookDialog
import ph.edu.rv_realm_quiz.models.Books
import ph.edu.rv_realm_quiz.realm.RealmDatabase
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
    //lateinit var refreshDataCallback: AddBookDialog.RefreshDataInterface

    interface BooksAdapterInterface {

        //        fun archiveOwner(ownerId: String, position: Int)
//        fun deleteOwnerAndTransferPets(ownerId: String, position: Int)
        fun refreshData()
    }

    fun setBook(book: Books) {
        this.book = book
    }

    inner class BookViewHolder(private val binding: ContentBooksRvBinding) :
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


                btnToFav.setOnClickListener {
                    val coroutineContext = Job() + Dispatchers.IO
                    val scope = CoroutineScope(coroutineContext + CoroutineName("favBook"))
                    scope.launch(Dispatchers.IO) {
                        database.favBook(book)
                        withContext(Dispatchers.Main) {
                            // You can update the UI or show a message if needed
                            Toast.makeText(context, "Book Moved to Favorites!", Toast.LENGTH_LONG).show()
                            // Refresh data if necessary
                            bookAdapterCallback.refreshData()
                        }

                    }


//                btnEditOwner.isEnabled = itemData.name != "Lotus"
//
//                btnEditOwner.setOnClickListener {
//                    val editOwnerDialog = EditOwner()
//                    editOwnerDialog.refreshDataCallback = object : EditPet.RefreshDataInterface{
//                        override fun refreshData() {
//                            ownerAdapterCallback.refreshData()
//                        }
//                    }
//                    editOwnerDialog.bindOwnerData(itemData)
//                    editOwnerDialog.show(fragmentManager, null)
//                }
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
            val bookId = booksList[position].id

            //ownerAdapterCallback.archiveOwner(ownerId, position)
        } else {
            Log.d("OwnerAdapter", "Error: Position out of bounds")
        }
    }


}