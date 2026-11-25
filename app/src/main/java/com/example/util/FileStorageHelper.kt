package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.example.data.model.DocumentFileType
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object FileStorageHelper {

    fun createTempCameraImageUri(context: Context): Pair<Uri, File> {
        val dir = File(context.cacheDir, "camera_temp").apply { mkdirs() }
        val file = File(dir, "cam_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return uri to file
    }

    data class CopiedFileResult(
        val fileName: String,
        val localPath: String,
        val mimeType: String,
        val sizeBytes: Long,
        val fileType: DocumentFileType
    )

    fun copyUriToInternalStorage(context: Context, uri: Uri, caseId: Long): CopiedFileResult? {
        return try {
            val resolver = context.contentResolver
            val mimeType = resolver.getType(uri) ?: "application/octet-stream"
            val originalName = queryDisplayName(context, uri) ?: "document_${System.currentTimeMillis()}"
            val extension = originalName.substringAfterLast('.', "").lowercase()
            val fileType = detectFileType(mimeType, extension)

            val caseDir = File(context.filesDir, "case_docs/$caseId").apply { mkdirs() }
            val safeExt = if (extension.isNotBlank()) ".$extension" else defaultExtFor(fileType)
            val destFile = File(caseDir, "${UUID.randomUUID()}$safeExt")

            resolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            CopiedFileResult(
                fileName = originalName,
                localPath = destFile.absolutePath,
                mimeType = mimeType,
                sizeBytes = destFile.length(),
                fileType = fileType
            )
        } catch (_: Throwable) {
            null
        }
    }

    fun copyFileToInternalStorage(context: Context, sourceFile: File, caseId: Long): CopiedFileResult? {
        return try {
            if (!sourceFile.exists()) return null
            val caseDir = File(context.filesDir, "case_docs/$caseId").apply { mkdirs() }
            val destFile = File(caseDir, "${UUID.randomUUID()}.jpg")
            sourceFile.copyTo(destFile, overwrite = true)
            CopiedFileResult(
                fileName = "تصویر دوربین_${PersianDateUtils.formatShamsiShort(System.currentTimeMillis())}.jpg",
                localPath = destFile.absolutePath,
                mimeType = "image/jpeg",
                sizeBytes = destFile.length(),
                fileType = DocumentFileType.IMAGE
            )
        } catch (_: Throwable) {
            null
        }
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        if (uri.scheme == "file") return uri.lastPathSegment
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
        }
    }

    fun detectFileType(mimeType: String, extension: String): DocumentFileType {
        val m = mimeType.lowercase()
        val e = extension.lowercase()
        return when {
            m.contains("pdf") || e == "pdf" -> DocumentFileType.PDF
            m.startsWith("image/") || e in listOf("jpg", "jpeg", "png", "webp") -> DocumentFileType.IMAGE
            m.contains("word") || m.contains("msword") || e in listOf("doc", "docx") -> DocumentFileType.WORD
            m.contains("excel") || m.contains("spreadsheet") || e in listOf("xls", "xlsx", "csv") -> DocumentFileType.EXCEL
            else -> DocumentFileType.OTHER
        }
    }

    private fun defaultExtFor(type: DocumentFileType): String = when (type) {
        DocumentFileType.PDF -> ".pdf"
        DocumentFileType.IMAGE -> ".jpg"
        DocumentFileType.WORD -> ".docx"
        DocumentFileType.EXCEL -> ".xlsx"
        DocumentFileType.OTHER -> ".bin"
    }

    fun formatFileSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val text = when {
            mb >= 1.0 -> String.format(java.util.Locale.US, "%.1f مگابایت", mb)
            kb >= 1.0 -> String.format(java.util.Locale.US, "%.0f کیلوبایت", kb)
            else -> "$bytes بایت"
        }
        return PersianDateUtils.toPersianDigits(text)
    }

    fun openExternalFile(context: Context, localPath: String, mimeType: String): Boolean {
        return try {
            val file = File(localPath)
            if (!file.exists()) return false
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType.ifBlank { "*/*" })
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "باز کردن فایل با..."))
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun shareFile(context: Context, localPath: String, mimeType: String, title: String): Boolean {
        return try {
            val file = File(localPath)
            if (!file.exists()) return false
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType.ifBlank { "application/pdf" }
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "ارسال فایل از طریق..."))
            true
        } catch (_: Throwable) {
            false
        }
    }
}
