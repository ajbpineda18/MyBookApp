package com.example.mybookapp.activities.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mybookapp.R
import com.example.mybookapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBookList.setOnClickListener(this)
        binding.btnFavoritesList.setOnClickListener(this)
        binding.btnArchivedList.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        view?.let {
            when (it.id) {
                R.id.btn_book_list -> {
                    startActivity(Intent(this, BookActivity::class.java))
                }
                R.id.btn_favorites_list -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                }
                R.id.btn_archived_list -> {
                    startActivity(Intent(this, ArchivedActivity::class.java))
                }
            }
        }
    }
}
