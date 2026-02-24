package com.android.systemui.axdynamicbar.data.source

import com.android.systemui.axdynamicbar.model.IslandEvent
import com.android.systemui.dagger.SysUISingleton
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** QPR0 has no Quick Look provider; media now-playing is supplied by MediaIslandManager. */
@SysUISingleton
class SmartspaceIslandManager @Inject constructor() {
    private val _sportsEvents = MutableStateFlow<List<IslandEvent.Sports>>(emptyList())
    val sportsEvents: StateFlow<List<IslandEvent.Sports>> = _sportsEvents.asStateFlow()

    private val _nowPlayingEvent = MutableStateFlow<IslandEvent.NowPlaying?>(null)
    val nowPlayingEvent: StateFlow<IslandEvent.NowPlaying?> = _nowPlayingEvent.asStateFlow()

    fun startListening() = Unit

    fun stopListening() {
        _sportsEvents.value = emptyList()
        _nowPlayingEvent.value = null
    }

    fun clearSportsEvent(key: String) {
        _sportsEvents.value = _sportsEvents.value.filter { it.key != key }
    }
}
