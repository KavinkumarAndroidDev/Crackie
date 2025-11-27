package com.kkdev.crackie.ui.home

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import com.kkdev.crackie.db.Fortune
import com.kkdev.crackie.db.FortuneDao
import com.kkdev.crackie.db.FortuneDatabase
import com.kkdev.crackie.db.FortuneRarity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class HomeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockApplication: Application
    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var db: FortuneDatabase
    private lateinit var fortuneDao: FortuneDao
    private lateinit var mockWorkManager: WorkManager

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockApplication = ApplicationProvider.getApplicationContext<Application>()
        mockContext = mockApplication.applicationContext
        mockPrefs = mockContext.getSharedPreferences("fortune_prefs", Context.MODE_PRIVATE)
        mockEditor = mockPrefs.edit()

        db = Room.inMemoryDatabaseBuilder(
            mockContext, FortuneDatabase::class.java
        ).allowMainThreadQueries().build()
        fortuneDao = db.fortuneDao()

        mockWorkManager = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial state is loading`() = runTest {
        coEvery { mockPrefs.getBoolean("is_first_launch", true) } returns true

        viewModel = HomeViewModel(mockApplication)

        assertEquals(HomeUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `test should show intro on first launch`() = runTest {
        coEvery { mockPrefs.getBoolean("is_first_launch", true) } returns true

        viewModel = HomeViewModel(mockApplication)
        advanceUntilIdle()

        assertEquals(true, viewModel.shouldShowIntro.value)
    }

    @Test
    fun `test cooldown state is triggered correctly`() = runTest {
        val twentyFourHours = 24 * 60 * 60 * 1000L
        val justNow = System.currentTimeMillis()
        coEvery { mockPrefs.getLong("last_crack_time", 0) } returns justNow
        coEvery { mockPrefs.getBoolean("is_first_launch", true) } returns false

        viewModel = HomeViewModel(mockApplication)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is HomeUiState.Cooldown)
    }

    @Test
    fun `test ready to crack state is set after cooldown`() = runTest {
        val twentyFourHours = 24 * 60 * 60 * 1000L
        val aDayAgo = System.currentTimeMillis() - twentyFourHours
        coEvery { mockPrefs.getLong("last_crack_time", 0) } returns aDayAgo
        coEvery { mockPrefs.getBoolean("is_first_launch", true) } returns false
        val fortune = Fortune(text = "Test", rarity = FortuneRarity.COMMON)
        fortuneDao.insertAll(listOf(fortune))

        viewModel = HomeViewModel(mockApplication)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is HomeUiState.ReadyToCrack)
    }
}
