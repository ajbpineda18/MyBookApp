package com.example.mybookapp.activities.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.mybookapp.R
import com.example.mybookapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(getLayoutInflater())
        setContentView(binding.root)

        binding.btnBookList.setOnClickListener(this)
        binding.btnFavoritesList.setOnClickListener(this)
        binding.btnArchivedList.setOnClickListener(this)

    }

    override fun onClick(p0: View?) {
        when(p0!!.id){
            R.id.btn_book_list -> {
                val intent = Intent(this, BookActivity::class.java)
                startActivity(intent)
            }
            R.id.btn_favorites_list -> {
                val intent = Intent(this, FavoritesActivity::class.java)
                startActivity(intent)
            }
            R.id.btn_archived_list -> {
                val intent = Intent(this, ArchivedActivity::class.java)
                startActivity(intent)
            }
        }
    }
}