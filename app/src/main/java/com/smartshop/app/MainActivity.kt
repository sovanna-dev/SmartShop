package com.smartshop.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.smartshop.app.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

// @AndroidEntryPoint tells Hilt this Activity
// can receive injected dependencies
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding — no findViewById() ever
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}