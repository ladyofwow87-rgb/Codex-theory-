package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.DoodleRepository
import com.example.ui.DoodleDashboard
import com.example.ui.DoodleViewModel
import com.example.ui.DoodleViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = AppDatabase.getDatabase(applicationContext)
    val repository = DoodleRepository(database.doodleDao())
    val factory = DoodleViewModelFactory(repository)
    val viewModel = ViewModelProvider(this, factory)[DoodleViewModel::class.java]

    setContent {
      MyApplicationTheme {
        DoodleDashboard(viewModel = viewModel)
      }
    }
  }
}
