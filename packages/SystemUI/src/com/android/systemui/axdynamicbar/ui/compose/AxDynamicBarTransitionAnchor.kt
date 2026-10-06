package com.android.systemui.axdynamicbar.ui.compose

import android.content.ComponentName
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewRootImpl
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.platform.LocalView
import com.android.internal.jank.InteractionJankMonitor
import com.android.systemui.animation.ActivityTransitionAnimator
import com.android.systemui.animation.DialogCuj
import com.android.systemui.animation.DialogTransitionAnimator
import com.android.systemui.animation.Expandable
import com.android.systemui.animation.TransitionAnimator
import com.android.systemui.animation.TransitionSource
import kotlin.math.roundToInt

/**
 * An [Expandable] that animates a dialog out of the given on-screen [bounds]. The Compose host view
 * is not a LaunchableView, so Expandable.fromView() cannot be used here.
 */
@Composable
internal fun rememberBoundsExpandable(bounds: Rect?): Expandable {
    val view = LocalView.current
    val currentBounds = rememberUpdatedState(bounds)
    return remember(view) {
        Expandable(BoundsTransitionSource(view) { currentBounds.value })
    }
}

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

private class BoundsTransitionSource(
    private val sourceView: View,
    private val boundsProvider: () -> Rect?,
) : TransitionSource {
    private val identity = Any()

    override var isSourceVisible: Boolean = true
    override var onSourceVisibilityChanged: (Boolean) -> Unit = {}

    override fun dialogTransitionController(cuj: DialogCuj?): DialogTransitionAnimator.Controller? {
        val bounds = boundsProvider() ?: return null
        if (bounds.isEmpty) return null
        return BoundsDialogTransitionController(sourceView, identity, boundsProvider, cuj)
    }

    override fun activityTransitionController(
        launchCujType: Int?,
        cookie: ActivityTransitionAnimator.TransitionCookie?,
        component: ComponentName?,
        returnCujType: Int?,
        isEphemeral: Boolean,
    ): ActivityTransitionAnimator.Controller? = null
}

private class BoundsDialogTransitionController(
    private val sourceView: View,
    override val dialogIdentity: Any,
    private val boundsProvider: () -> Rect?,
    override val cuj: DialogCuj?,
) : DialogTransitionAnimator.Controller {
    override val viewRoot: ViewRootImpl?
        get() = sourceView.viewRootImpl

    override fun startDrawingInOverlayOf(viewGroup: ViewGroup) {}

    override fun stopDrawingInOverlay() {}

    override fun createTransitionController(): TransitionAnimator.Controller =
        BoundsTransitionController(sourceView, boundsProvider)

    override fun createExitController(): TransitionAnimator.Controller =
        BoundsTransitionController(sourceView, boundsProvider)

    override fun shouldAnimateExit(): Boolean =
        sourceView.isAttachedToWindow && sourceView.isShown && boundsProvider()?.isEmpty == false

    override fun onExitAnimationCancelled() {}

    override fun jankConfigurationBuilder(): InteractionJankMonitor.Configuration.Builder? {
        val type = cuj?.cujType ?: return null
        return InteractionJankMonitor.Configuration.Builder.withView(type, sourceView)
    }
}

private class BoundsTransitionController(
    sourceView: View,
    private val boundsProvider: () -> Rect?,
) : TransitionAnimator.Controller {
    override var transitionContainer: ViewGroup =
        (sourceView.rootView as? ViewGroup) ?: sourceView.parent as ViewGroup

    override val isLaunching: Boolean = true

    override fun createAnimatorState(): TransitionAnimator.State {
        val bounds = boundsProvider()
        if (bounds == null || bounds.isEmpty) return TransitionAnimator.State()
        val radius = bounds.height() / 2f
        return TransitionAnimator.State(
            top = bounds.top,
            bottom = bounds.bottom,
            left = bounds.left,
            right = bounds.right,
            topCornerRadius = radius,
            bottomCornerRadius = radius,
        )
    }
}
