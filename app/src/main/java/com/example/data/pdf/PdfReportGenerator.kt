package com.example.data.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.ShopEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.BakiKhataRepository
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    /**
     * Generates a PDF statement for a customer ledger and returns the File.
     */
    fun generateCustomerStatementPdf(
        context: Context,
        shop: ShopEntity,
        customer: CustomerEntity,
        transactions: List<TransactionEntity>
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard size
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(10, 126, 68) // Brand Emerald Green
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(11, 25, 44) // Deep Navy
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.rgb(33, 33, 33)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(230, 244, 234)
            style = Paint.Style.FILL
        }

        val tableHeaderPaint = Paint().apply {
            color = Color.rgb(10, 126, 68)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.rgb(200, 200, 200)
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
        }

        val redPaint = Paint().apply {
            color = Color.rgb(211, 47, 47)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val greenPaint = Paint().apply {
            color = Color.rgb(46, 125, 50)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        var y = 45f

        // 1. Header Box
        canvas.drawText("BAKI KHATA - ডিজিটাল বাকি খাতা", 40f, y, titlePaint)
        y += 18f
        canvas.drawText(shop.name, 40f, y, subtitlePaint)
        y += 14f
        canvas.drawText("ঠিকানা: ${shop.address}, ${shop.upazila}, ${shop.district} | ফোন: ${shop.phone}", 40f, y, textPaint)
        y += 10f

        // Horizontal Line
        canvas.drawLine(40f, y, 555f, y, borderPaint)
        y += 20f

        // 2. Customer Statement Title & Details
        val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date())

        canvas.drawText("গ্রাহক খাতা স্টেটমেন্ট (Customer Statement)", 40f, y, subtitlePaint)
        canvas.drawText("তারিখ: $dateStr", 380f, y, textPaint)
        y += 16f

        canvas.drawText("গ্রাহকের নাম: ${customer.name}", 40f, y, textPaint)
        canvas.drawText("মোবাইল: ${customer.phone}", 380f, y, textPaint)
        y += 14f
        canvas.drawText("ঠিকানা: ${customer.address.ifBlank { "উল্লেখ নেই" }}", 40f, y, textPaint)
        y += 20f

        // 3. Outstanding Summary Card
        canvas.drawRoundRect(40f, y, 555f, y + 50f, 6f, 6f, headerPaint)
        val dueFormatted = BakiKhataRepository.formatCurrency(customer.totalDuePoisha)
        val paidFormatted = BakiKhataRepository.formatCurrency(customer.totalPaidPoisha)

        val summaryTextPaint = Paint().apply {
            color = Color.rgb(10, 126, 68)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val summaryRedPaint = Paint().apply {
            color = Color.rgb(211, 47, 47)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("মোট বাকি (Outstanding Due): $dueFormatted", 55f, y + 22f, summaryRedPaint)
        canvas.drawText("মোট আদায়/জমা (Total Paid): $paidFormatted", 55f, y + 40f, summaryTextPaint)
        canvas.drawText("হিসাব স্ট্যাটাস: ${if (customer.totalDuePoisha > 0) "বাকি আছে" else "পরিশোধিত"}", 380f, y + 30f, textPaint)
        y += 65f

        // 4. Transactions Table Header
        canvas.drawRect(40f, y, 555f, y + 22f, headerPaint)
        canvas.drawRect(40f, y, 555f, y + 22f, borderPaint)

        canvas.drawText("তারিখ", 45f, y + 15f, tableHeaderPaint)
        canvas.drawText("বিবরণ", 120f, y + 15f, tableHeaderPaint)
        canvas.drawText("বাকি (+)", 330f, y + 15f, tableHeaderPaint)
        canvas.drawText("জমা (-)", 410f, y + 15f, tableHeaderPaint)
        canvas.drawText("অবশিষ্ট বাকি", 480f, y + 15f, tableHeaderPaint)
        y += 22f

        // 5. Transaction Rows
        val rowDateFormat = SimpleDateFormat("dd-MM-yy", Locale.getDefault())
        val visibleTxs = transactions.take(25) // Fits safely on single page

        for (tx in visibleTxs) {
            val txDate = rowDateFormat.format(Date(tx.dateEpochMillis))
            val amountStr = BakiKhataRepository.formatCurrency(tx.amountPoisha)
            val balanceStr = BakiKhataRepository.formatCurrency(tx.balanceAfterPoisha)

            canvas.drawRect(40f, y, 555f, y + 20f, borderPaint)

            canvas.drawText(txDate, 45f, y + 14f, textPaint)
            val desc = if (tx.productName.length > 25) tx.productName.take(22) + "..." else tx.productName
            canvas.drawText(desc, 120f, y + 14f, textPaint)

            if (tx.type == "DUE") {
                canvas.drawText(amountStr, 330f, y + 14f, redPaint)
                canvas.drawText("-", 415f, y + 14f, textPaint)
            } else {
                canvas.drawText("-", 335f, y + 14f, textPaint)
                canvas.drawText(amountStr, 410f, y + 14f, greenPaint)
            }
            canvas.drawText(balanceStr, 480f, y + 14f, textPaint)

            y += 20f
        }

        // 6. Signatures and verification footer
        y += 40f
        canvas.drawLine(60f, y, 200f, y, borderPaint)
        canvas.drawLine(380f, y, 520f, y, borderPaint)
        y += 14f
        canvas.drawText("গ্রাহকের স্বাক্ষর", 100f, y, textPaint)
        canvas.drawText("দোকানদারের স্বাক্ষর ও সিল", 390f, y, textPaint)

        y += 30f
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 8.5f
            isAntiAlias = true
        }
        canvas.drawText("বাকি খাতা (Baki Khata) মোবাইল অ্যাপ দ্বারা প্রস্তুতকৃত ডিজিটাল স্টেটমেন্ট।", 130f, y, footerPaint)

        document.finishPage(page)

        return try {
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) reportsDir.mkdirs()
            val file = File(reportsDir, "Statement_${customer.phone}_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            document.writeTo(outputStream)
            outputStream.close()
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            null
        }
    }

    /**
     * Creates an Android Share Intent for the generated PDF.
     */
    fun createSharePdfIntent(context: Context, pdfFile: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "বাকি খাতা স্টেটমেন্ট")
            putExtra(Intent.EXTRA_TEXT, "বাকি খাতা অ্যাপ থেকে গ্রাহক স্টেটমেন্ট সংযুক্ত করা হলো।")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(shareIntent, "স্টেটমেন্ট পিডিএফ শেয়ার করুন")
    }
}
