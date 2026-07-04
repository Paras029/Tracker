package com.parasgarg.tracker.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri

fun renderPdfFirstPage(context: Context, uri: Uri): Bitmap? = runCatching {
    context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
        PdfRenderer(pfd).use { renderer ->
            renderer.openPage(0).use { page ->
                val scale = 1280f / maxOf(page.width, page.height)
                val bmp = Bitmap.createBitmap(
                    (page.width * scale).toInt(),
                    (page.height * scale).toInt(),
                    Bitmap.Config.ARGB_8888,
                )
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bmp
            }
        }
    }
}.getOrNull()
