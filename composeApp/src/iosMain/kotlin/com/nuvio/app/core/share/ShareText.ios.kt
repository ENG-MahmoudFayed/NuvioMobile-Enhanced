package com.nuvio.app.core.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

@Composable
internal actual fun rememberShareText(): (String) -> Unit = remember {
    { text ->
        val presenter = topmostViewController()
        if (presenter != null) {
            val sheet = UIActivityViewController(
                activityItems = listOf(text),
                applicationActivities = null,
            )
            // iPad presents the sheet as a popover, which needs an anchor.
            sheet.popoverPresentationController?.sourceView = presenter.view
            presenter.presentViewController(sheet, animated = true, completion = null)
        }
    }
}

private fun topmostViewController(): UIViewController? {
    var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}
