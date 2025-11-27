package com.kkdev.crackie.ui.home

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kkdev.crackie.db.Fortune
import com.kkdev.crackie.db.FortuneDatabase
import com.kkdev.crackie.worker.NotificationWorker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data object Cooldown : HomeUiState()
    data class ReadyToCrack(val fortune: Fortune) : HomeUiState()
}

enum class SortBy {
    DATE,
    RARITY
}

enum class SortOrder {
    ASC,
    DESC
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FortuneDatabase.getDatabase(application)
    private val prefs = application.getSharedPreferences("fortune_prefs", Context.MODE_PRIVATE)
    private val workManager = WorkManager.getInstance(application)

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _cooldownTimer = MutableStateFlow(0L)
    val cooldownTimer = _cooldownTimer.asStateFlow()
    private var timerJob: Job? = null

    private val _shouldShowIntro = MutableStateFlow(false)
    val shouldShowIntro = _shouldShowIntro.asStateFlow()

    private val _sortBy = MutableStateFlow(SortBy.DATE)
    val sortBy = _sortBy.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DESC)
    val sortOrder = _sortOrder.asStateFlow()

    val favoriteFortunes = _sortBy.flatMapLatest { sortBy ->
        _sortOrder.flatMapLatest { sortOrder ->
            when (sortBy) {
                SortBy.DATE -> if (sortOrder == SortOrder.DESC) db.fortuneDao().getFavoriteFortunesByDateDesc() else db.fortuneDao().getFavoriteFortunesByDateAsc()
                SortBy.RARITY -> if (sortOrder == SortOrder.DESC) db.fortuneDao().getFavoriteFortunesByRarityDesc() else db.fortuneDao().getFavoriteFortunesByRarityAsc()
            }
        }
    }

    init {
        checkFirstLaunch()
        checkUserState()
    }

    fun setSortBy(sortBy: SortBy) {
        _sortBy.value = sortBy
    }

    fun setSortOrder(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
    }

    private fun checkFirstLaunch() {
        val isFirstLaunch = prefs.getBoolean("is_first_launch", true)
        if (isFirstLaunch) {
            _shouldShowIntro.value = true
        }
    }

    fun dismissIntro() {
        _shouldShowIntro.value = false
        prefs.edit().putBoolean("is_first_launch", false).apply()
    }

    private fun checkUserState() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            delay(400)

            val lastCrackTime = prefs.getLong("last_crack_time", 0)
            val twentyFourHours = 24 * 60 * 60 * 1000L
            val timeSinceLastCrack = System.currentTimeMillis() - lastCrackTime

            if (timeSinceLastCrack < twentyFourHours) {
                _uiState.value = HomeUiState.Cooldown
                startCooldownTimer(twentyFourHours - timeSinceLastCrack)
            } else {
                timerJob?.cancel()
                var newFortune = db.fortuneDao().getUnseenFortune()
                if (newFortune == null) {
                    db.fortuneDao().resetSeenFortunes()
                    newFortune = db.fortuneDao().getUnseenFortune()
                }

                if (newFortune != null) {
                    _uiState.value = HomeUiState.ReadyToCrack(newFortune)
                } else {
                    _uiState.value = HomeUiState.Cooldown
                    startCooldownTimer(twentyFourHours)
                }
            }
        }
    }

    private fun startCooldownTimer(timeMillis: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var remainingTime = timeMillis
            _cooldownTimer.value = remainingTime
            while (remainingTime > 0) {
                delay(1000L)
                remainingTime -= 1000L
                _cooldownTimer.value = remainingTime
            }
            checkUserState()
        }
    }

    fun onCookieCracked(fortune: Fortune) {
        viewModelScope.launch {
            fortune.wasSeen = true
            db.fortuneDao().updateFortune(fortune)
            prefs.edit().putLong("last_crack_time", System.currentTimeMillis()).apply()
            scheduleNotification()
        }
    }

    private fun scheduleNotification() {
        val notificationWorkRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(24, TimeUnit.HOURS)
            .build()
        workManager.enqueue(notificationWorkRequest)
    }

    fun toggleFavorite(fortune: Fortune) {
        viewModelScope.launch {
            val updatedFortune = fortune.copy(
                isFavorite = !fortune.isFavorite,
                dateAdded = if (!fortune.isFavorite) System.currentTimeMillis() else fortune.dateAdded
            )
            db.fortuneDao().updateFortune(updatedFortune)
            _uiState.value = HomeUiState.ReadyToCrack(updatedFortune)
        }
    }

    fun completeCycle() {
        checkUserState()
    }
}
