package com.yandex.practicum.middle_homework_4.data.setting_repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.yandex.practicum.middle_homework_4.ui.contract.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SettingsRepository {
    private val REFRESH_PERIOD_KEY = longPreferencesKey("REFRESH_PERIOD")
    private val FIRST_LAUNCH_DELAY_KEY = longPreferencesKey("FIRST_LAUNCH_DELAY")
    private val _state = MutableStateFlow(SettingContainer.initial)
    override val state = _state.asStateFlow()

    init {
        CoroutineScope(Job() + dispatcher).launch {
            readSetting()
        }
    }

    override suspend fun saveSetting(periodic: Long, delayed: Long) {
        withContext(dispatcher) {
            dataStore.edit { mutablePreferences ->
                mutablePreferences[REFRESH_PERIOD_KEY] = periodic
                mutablePreferences[FIRST_LAUNCH_DELAY_KEY] = delayed
            }

            updateState(periodic, delayed)
        }
    }


    override suspend fun readSetting() {
        withContext(dispatcher) {
            val preferences = dataStore.data.first()
            val periodic = preferences[REFRESH_PERIOD_KEY] ?: SettingContainer.DEFAULT_REFRESH_PERIOD
            val delayed = preferences[FIRST_LAUNCH_DELAY_KEY] ?: SettingContainer.FIST_LAUNCH_DELAY

            updateState(periodic, delayed)
        }
    }

    private fun updateState(periodic: Long, delayed: Long) {
        _state.update { current ->
            current.copy(
                periodic = periodic,
                delayed = delayed
            )
        }
    }
}