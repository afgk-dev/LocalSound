package dev.afgk.localsound.data.helpers

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class SaveToInternalStorageFailure : Throwable()

suspend fun saveToInternalStorage(
    internalPath: Pair<String, String>,
    externalUri: Uri,
    contentResolver: ContentResolver,
): Result<File> = withContext(Dispatchers.IO) {
    try {
        val inputStream = contentResolver.openInputStream(externalUri)

        val file = File(internalPath.first, internalPath.second)
        val outputStream = FileOutputStream(file)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return@withContext Result.success(file)
    } catch (e: Exception) {
        e.printStackTrace()

        return@withContext Result.failure(SaveToInternalStorageFailure())
    }
}