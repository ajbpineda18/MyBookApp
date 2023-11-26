package com.example.mybookapp.activities.dialogs

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.mybookapp.databinding.DialogAddBookBinding
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ph.edu.rv_realm_quiz.realm.RealmDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddBookDialog : DialogFragment() {

    private lateinit var binding: DialogAddBookBinding
    private var database = RealmDatabase()
    private var isDateSelected = false
    //private var datePickerCallback: DatePickerCallback? = null
    lateinit var refreshDataCallback: RefreshDataInterface
    private var date: Date? = Calendar.getInstance().time
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

        with(binding) {
            btnDatePublished.setOnClickListener {
                showDatePicker()
            }
            btnAddBook.setOnClickListener {
                if (edtBookName.text.isNullOrEmpty()) {
                    edtBookName.error = "Required"
                    return@setOnClickListener
                }
                if (edtAuthor.text.isNullOrEmpty()) {
                    edtAuthor.error = "Required"
                    return@setOnClickListener
                }
                val bookName = edtBookName.text.toString()
                val bookAuthor = edtAuthor.text.toString()
                val bookPublished = date!!.time
                val currentDate = Calendar.getInstance().time.time

                if(bookPublished != null){
                    val coroutineContext = Job() + Dispatchers.IO
                    val scope = CoroutineScope(coroutineContext + CoroutineName("addBookToRealm"))
                    scope.launch(Dispatchers.IO) {
                        database.addBook(bookName, bookAuthor, bookPublished, currentDate, currentDate)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(activity, "Book has been added!", Toast.LENGTH_LONG).show()
                            refreshDataCallback.refreshData()
                            dialog?.dismiss()
                        }

                    }
                }

                else{
                    Toast.makeText(activity, "Error! $bookPublished is null!", Toast.LENGTH_LONG).show()
                }
                // ... rest of your logic
            }
        }
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        //val datePickerCallback = datePickerCallback
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Handle the selected date
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                val selectedDate = selectedCalendar.time
                date = selectedDate

                // Set the selected date to the TextView or any other UI element
                binding.tvDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate)

                // Notify the callback with the selected date
                //datePickerCallback?.onDateSelected(selectedDate)
                binding.btnAddBook.isEnabled = true
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

}