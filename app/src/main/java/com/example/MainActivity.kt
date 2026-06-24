package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.CigaretteRepository
import com.example.ui.CigaretteTrackerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CigaretteViewModel
import com.example.ui.viewmodel.CigaretteViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database & Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = CigaretteRepository(database.cigaretteDao())

        // Create ViewModel using factory
        val viewModel: CigaretteViewModel by viewModels {
            CigaretteViewModelFactory(application, repository)
        }

        setContent {
            MyApplicationTheme {
                CigaretteTrackerScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
