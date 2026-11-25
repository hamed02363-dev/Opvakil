package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.example.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object PdfExportHelper {

    private const val PAGE_WIDTH = 595   // A4 points
    private const val PAGE_HEIGHT = 842  // A4 points
    private const val MARGIN_X = 42f
    private const val MARGIN_TOP = 46f
    private const val MARGIN_BOTTOM = 44f

    fun generatePersianPdf(
        context: Context,
        fileNamePrefix: String,
        documentTitle: String,
        subtitle: String,
        sections: List<Pair<String, String>>
    ): File? {
        return try {
            val typeface: Typeface = ResourcesCompat.getFont(context, R.font.vazirmatn)
                ?: Typeface.DEFAULT

            val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.typeface = Typeface.create(typeface, Typeface.BOLD)
                color = Color.WHITE
                textSize = 15f
            }
            val subHeaderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.typeface = typeface
                color = Color.parseColor("#E8D6A7")
                textSize = 10.5f
            }
            val sectionTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.typeface = Typeface.create(typeface, Typeface.BOLD)
                color = Color.parseColor("#0F233B")
                textSize = 12.5f
            }
            val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.typeface = typeface
                color = Color.parseColor("#1D2430")
                textSize = 10.8f
            }
            val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.typeface = typeface
                color = Color.parseColor("#6B7280")
                textSize = 9f
            }

            val bannerPaint = Paint().apply {
                color = Color.parseColor("#0F233B")
                style = Paint.Style.FILL
            }
            val goldLinePaint = Paint().apply {
                color = Color.parseColor("#C59B43")
                strokeWidth = 2f
            }
            val cardBorderPaint = Paint().apply {
                color = Color.parseColor("#D5DCE6")
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }

            val pdf = PdfDocument()
            val contentWidth = (PAGE_WIDTH - 2 * MARGIN_X).toInt()
            var pageNumber = 1
            var page = pdf.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            var canvas = page.canvas

            fun drawHeader(c: Canvas, isFirstPage: Boolean) {
                if (isFirstPage) {
                    c.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 84f, bannerPaint)
                    c.drawLine(0f, 84f, PAGE_WIDTH.toFloat(), 84f, goldLinePaint)

                    val titleLayout = buildRtlLayout(documentTitle, headerPaint, contentWidth)
                    c.save()
                    c.translate(MARGIN_X, 18f)
                    titleLayout.draw(c)
                    c.restore()

                    val subLayout = buildRtlLayout(subtitle, subHeaderPaint, contentWidth)
                    c.save()
                    c.translate(MARGIN_X, 50f)
                    subLayout.draw(c)
                    c.restore()
                } else {
                    c.drawLine(MARGIN_X, 34f, PAGE_WIDTH - MARGIN_X, 34f, goldLinePaint)
                }
            }

            fun drawFooter(c: Canvas, pNum: Int) {
                val dateLabel = PersianDateUtils.formatShamsiShort(System.currentTimeMillis())
                val footerText = PersianDateUtils.toPersianDigits(
                    "ساخته‌شده در اپلیکیشن آفلاین وکیل‌یار | تاریخ: $dateLabel | صفحه $pNum"
                )
                val footerLayout = buildRtlLayout(footerText, footerPaint, contentWidth)
                c.save()
                c.translate(MARGIN_X, PAGE_HEIGHT - 28f)
                footerLayout.draw(c)
                c.restore()
            }

            drawHeader(canvas, true)
            var currentY = 104f

            fun startNextPage() {
                drawFooter(canvas, pageNumber)
                pdf.finishPage(page)
                pageNumber++
                page = pdf.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
                canvas = page.canvas
                drawHeader(canvas, false)
                currentY = MARGIN_TOP
            }

            for ((secTitle, secBody) in sections) {
                if (secTitle.isNotBlank()) {
                    val secTitleLayout = buildRtlLayout(secTitle, sectionTitlePaint, contentWidth)
                    if (currentY + secTitleLayout.height + 16f > PAGE_HEIGHT - MARGIN_BOTTOM) {
                        startNextPage()
                    }
                    canvas.save()
                    canvas.translate(MARGIN_X, currentY)
                    secTitleLayout.draw(canvas)
                    canvas.restore()
                    currentY += secTitleLayout.height + 6f
                    canvas.drawLine(MARGIN_X, currentY, PAGE_WIDTH - MARGIN_X, currentY, cardBorderPaint)
                    currentY += 8f
                }

                val paragraphs = secBody.split("\n")
                for (para in paragraphs) {
                    val cleanPara = if (para.isBlank()) " " else PersianDateUtils.toPersianDigits(para)
                    val paraLayout = buildRtlLayout(cleanPara, bodyPaint, contentWidth)
                    if (currentY + paraLayout.height + 6f > PAGE_HEIGHT - MARGIN_BOTTOM) {
                        startNextPage()
                    }
                    canvas.save()
                    canvas.translate(MARGIN_X, currentY)
                    paraLayout.draw(canvas)
                    canvas.restore()
                    currentY += paraLayout.height + 4f
                }
                currentY += 12f
            }

            drawFooter(canvas, pageNumber)
            pdf.finishPage(page)

            val outDir = File(context.filesDir, "exported_pdfs").apply { mkdirs() }
            val outFile = File(outDir, "${fileNamePrefix}_${System.currentTimeMillis()}.pdf")
            FileOutputStream(outFile).use { fos -> pdf.writeTo(fos) }
            pdf.close()
            outFile
        } catch (_: Throwable) {
            null
        }
    }

    private fun buildRtlLayout(text: String, paint: TextPaint, width: Int): StaticLayout {
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width.coerceAtLeast(100))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setLineSpacing(2f, 1.15f)
            .setIncludePad(false)
            .build()
    }

    fun printPdfFile(context: Context, pdfFile: File, jobName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder(pdfFile.name)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                try {
                    FileInputStream(pdfFile).use { input ->
                        FileOutputStream(destination?.fileDescriptor).use { output ->
                            input.copyTo(output)
                        }
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }
        printManager.print(jobName, adapter, PrintAttributes.Builder().build())
    }
}
