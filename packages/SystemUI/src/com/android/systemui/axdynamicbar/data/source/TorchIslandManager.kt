package com.android.systemui.axdynamicbar.data.source

import com.android.systemui.axdynamicbar.model.IslandEvent
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.statusbar.policy.FlashlightController
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@SysUISingleton
class TorchIslandManager
@Inject
constructor(
    private val flashlightController: FlashlightController,
) {
    private val _torchEvent = MutableStateFlow<IslandEvent.Torch?>(null)
    val torchEvent: StateFlow<IslandEvent.Torch?> = _torchEvent.asStateFlow()

    private var listening = false

    private val listener =
        object : FlashlightController.FlashlightListener {
            override fun onFlashlightChanged(enabled: Boolean) {
                if (enabled) {
                    _torchEvent.value = IslandEvent.Torch()
                } else {
                    _torchEvent.value = null
                }
            }

            override fun onFlashlightError() {
                _torchEvent.value = null
            }

            override fun onFlashlightAvailabilityChanged(available: Boolean) {
                if (!available) {
                    _torchEvent.value = null
                }
            }
        }

    fun startListening() {
        if (listening) return
        listening = true
        flashlightController.addCallback(listener)
        if (flashlightController.isEnabled) {
            _torchEvent.value = IslandEvent.Torch()
        }
    }

    fun stopListening() {
        if (!listening) return
        listening = false
        flashlightController.removeCallback(listener)
        _torchEvent.value = null
    }

    fun clear() {
        _torchEvent.value = null
    }

    fun toggleTorch() {
        flashlightController.setFlashlight(!flashlightController.isEnabled)
    }

    fun setLevel(level: Int) = Unit

    fun setLevelTemporary(level: Int) = Unit
}
