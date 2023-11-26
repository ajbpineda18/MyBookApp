package com.example.mybookapp.activities.dialogs

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.mybookapp.activities.realm.RealmDatabase
import com.example.mybookapp.databinding.DialogAddBookBinding
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddBookDialog : DialogFragment() {

    private lateinit var binding: DialogAddBookBinding
    lateinit var refreshDataCallback: RefreshDataInterface
    private var date: Date? = Calendar.getInstance().time
    private var database = RealmDatabase()
    private var isDateSelected = false
    interface DatePickerCallback {
        fun onDateSelected(selectedDate: Date)
    }
    interface RefreshDataInterface{
        fun refreshData()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogAddBookBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnDatePublished.setOnClickListener {
                showDatePicker()
            }

            btnAddBook.setOnClickListener {
                val bookName = edtBookName.text.toString()
                val bookAuthor = edtAuthor.text.toString()

                if (bookName.isBlank()) {
                    edtBookName.error = "Required"
                    return@setOnClickListener
                }

                if (bookAuthor.isBlank()) {
                    edtAuthor.error = "Required"
                    return@setOnClickListener
                }

                val bookPublished = date?.time ?: run {
                    Toast.makeText(activity, "Error! Book Published date is null!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val currentDate = Calendar.getInstance().time.time

                val coroutineContext = Job() + Dispatchers.IO
                val scope = CoroutineScope(coroutineContext + CoroutineName("addBookToRealm"))
                scope.launch(Dispatchers.IO) {
                    try {
                        database.addBook(bookName, bookAuthor, bookPublished, currentDate, currentDate)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(activity, "This Book has been Added!", Toast.LENGTH_LONG).show()
                            refreshDataCallback.refreshData()
                            dialog?.dismiss()
                        }
                    } catch (e: IllegalStateException) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun showDatePicker() {
        val currentDate = Calendar.getInstance()
        val year = currentDate.get(Calendar.YEAR)
        val month = currentDate.get(Calendar.MONTH)
        val day = currentDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                val selectedDate = selectedCalendar.time

                updateSelectedDate(selectedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }
    private fun updateSelectedDate(selectedDate: Date) {
        date = selectedDate
        binding.tvDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate)
        binding.btnAddBook.isEnabled = true
    }
}