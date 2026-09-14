package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.datastore.SettingsRepository
import com.example.data.datastore.dataStore
import com.example.data.db.AppDatabase
import com.example.data.repository.FavoriteRepository
import com.example.data.repository.HistoryRepository
import com.example.ui.MainViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Arw Hyper Toolkit", appName)
  }

  @Test
  fun `floating window state and position sync test`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
    val histRepo = HistoryRepository(db.historyDao())
    val favRepo = FavoriteRepository(db.favoriteDao())
    val setRepo = SettingsRepository(context.dataStore)
    val viewModel = MainViewModel(histRepo, favRepo, setRepo)

    // Gender selection synchronization
    viewModel.setGender("Female")
    assertEquals("Female", viewModel.selectedGender.first())

    viewModel.setGender("Male")
    assertEquals("Male", viewModel.selectedGender.first())

    viewModel.setGender("Unisex")
    assertEquals("Unisex", viewModel.selectedGender.first())

    // Active tool switching
    viewModel.setActiveTool("Username")
    assertEquals("Username", viewModel.activeTool.first())

    viewModel.setActiveTool("Password")
    assertEquals("Password", viewModel.activeTool.first())

    viewModel.setActiveTool("First & Middle")
    assertEquals("First & Middle", viewModel.activeTool.first())

    // Mode and country selection
    viewModel.setMode("First Name")
    assertEquals("First Name", viewModel.selectedMode.first())

    viewModel.setCountry("United States")
    assertEquals("United States", viewModel.selectedCountry.first())

    // Style and count
    viewModel.setStyle("Cute")
    assertEquals("Cute", viewModel.selectedStyle.first())

    viewModel.setCount(50)
    assertEquals(50, viewModel.selectedCount.first())

    db.close()
  }
}


