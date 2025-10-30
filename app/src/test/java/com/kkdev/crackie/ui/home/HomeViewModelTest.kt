package com.kkdev.crackie.ui.home

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkManager
import com.kkdev.crackie.db.Fortune
import com.kkdev.crackie.db.FortuneDao
import com.kkdev.crackie.db.FortuneDatabase
import com.kkdev.crackie.db.FortuneRarity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
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

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val mockApplication: Application = mockk(relaxed = true)
    private val mockContext: Context = mockk(relaxed = true)
    private val mockPrefs: SharedPreferences = mockk(relaxed = true)
    private val mockEditor: SharedPreferences.Editor = mockk(relaxed = true)
    private val mockDatabase: FortuneDatabase = mockk(relaxed = true)
    private val mockFortuneDao: FortuneDao = mockk(relaxed = true)
    private val mockWorkManager: WorkManager = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { mockApplication.applicationContext } returns mockContext
        every { mockContext.getSharedPreferences(any(), any()) } returns mockPrefs
        every { mockPrefs.edit() } returns mockEditor

        // --- FIX: Correctly mock the static methods ---
        mockkStatic(FortuneDatabase::class)
        every { FortuneDatabase.getDatabase(any()) } returns mockDatabase
        every { mockDatabase.fortuneDao() } returns mockFortuneDao

        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns mockWorkManager
    }

    @After
    fun tearDown() {
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
        coEvery { mockFortuneDao.getUnseenFortune() } returns Fortune(text = "Test", rarity = FortuneRarity.COMMON)

        viewModel = HomeViewModel(mockApplication)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is HomeUiState.ReadyToCrack)
    }
}
