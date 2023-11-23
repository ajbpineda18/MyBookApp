package com.example.mybookapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOwnerList.setOnClickListener(this)
        binding.btnPetList.setOnClickListener(this)
        binding.btnArchived.setOnClickListener(this)
        binding.btnConsigned.setOnClickListener(this)

    }

    override fun onClick(p0: View?) {
        when(p0!!.id){
            R.id.btn_book_list -> {
                val intent = Intent(this, OwnersActivity::class.java)
                startActivity(intent)
            }
            R.id.btn_favorites_list -> {
                val intent = Intent(this, ArchivedActivity::class.java)
                startActivity(intent)
            }

        }
    }
}