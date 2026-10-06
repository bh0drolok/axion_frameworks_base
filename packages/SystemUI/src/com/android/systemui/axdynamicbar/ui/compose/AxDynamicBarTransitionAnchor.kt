package com.android.systemui.axdynamicbar.ui.compose

import android.graphics.Rect
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.platform.LocalView
import com.android.systemui.animation.Expandable
import kotlin.math.roundToInt

@Composable
// The Compose host view is not a LaunchableView, so Expandable.fromView() throws in this tree.
// Returning null makes the expanded card open without the zoom transition instead of crashing.
@Suppress("UNUSED_PARAMETER")
internal fun rememberBoundsExpandable(bounds: Rect?): Expandable? = null

internal fun LayoutCoordinates.screenBounds(view: View): Rect {
    val bounds = boundsInRoot()
    val location = IntArray(2)
    view.getLocationOnScreen(location)
    return Rect(
        (location[0] + bounds.left).roundToInt(),
        (location[1] + bounds.top).roundToInt(),
        (location[0] + bounds.right).roundToInt(),
        (location[1] + bounds.bottom).roundToInt(),
    )
}
