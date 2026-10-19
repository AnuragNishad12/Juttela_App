package com.example.juttela.Utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.policy.UploadPolicy
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

sealed class UploadResult {
    data class Success(val url: String) : UploadResult()
    data class Failure(val reason: String) : UploadResult()
}

object CloudinaryUploader {

    private const val TAG = "Cloudinary"
    private const val CLOUD_NAME = "dnnmvzdnd"
    private const val PROFILE_PRESET = "jutella_profile_uploads"
    private const val VERIFICATION_PRESET = "verification_uploads"
    private const val UPLOAD_TIMEOUT_MS = 60_000L

    // ── Init (safe to call many times, from any thread) ─────────────
    @Synchronized
    private fun ensureInitialized(context: Context) {
        try {
            MediaManager.get()   // throws IllegalStateException if not initialized yet
        } catch (e: IllegalStateException) {
            MediaManager.init(
                context.applicationContext,
                mapOf("cloud_name" to CLOUD_NAME, "secure" to "true")
            )
        }
    }

    // ── Core: one function that every upload goes through ───────────
    private suspend fun upload(
        context: Context,
        uri: Uri,
        preset: String,
        folder: String,
        publicId: String? = null
    ): UploadResult {
        ensureInitialized(context)

        val result = withTimeoutOrNull(UPLOAD_TIMEOUT_MS) {
            suspendCancellableCoroutine<UploadResult> { cont ->

                fun finish(r: UploadResult) {
                    if (cont.isActive) cont.resume(r)
                }

                val request = MediaManager.get()
                    .upload(uri)
                    .unsigned(preset)
                    .option("folder", folder)
                    .policy(
                        UploadPolicy.Builder()
                            .maxRetries(0)   // fail fast: no silent background retries
                            .build()
                    )

                if (publicId != null) request.option("public_id", publicId)

                val requestId = request
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String?) {
                            Log.d(TAG, "Started: $requestId")
                        }

                        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                        override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                            val url = resultData?.get("secure_url") as? String
                            Log.d(TAG, "Success: $url")
                            finish(
                                if (url != null) UploadResult.Success(url)
                                else UploadResult.Failure("Server returned no URL")
                            )
                        }

                        override fun onError(requestId: String?, error: ErrorInfo?) {
                            Log.e(TAG, "Error ${error?.code}: ${error?.description}")
                            finish(UploadResult.Failure(error?.description ?: "Upload failed"))
                        }

                        // THE FIX: this used to just log, so the coroutine never finished
                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                            Log.e(TAG, "Rescheduled ${error?.code}: ${error?.description}")
                            requestId?.let { MediaManager.get().cancelRequest(it) } // no ghost retries
                            finish(UploadResult.Failure(error?.description ?: "Network problem"))
                        }
                    })
                    .dispatch()

                cont.invokeOnCancellation { MediaManager.get().cancelRequest(requestId) }
            }
        }

        return result ?: UploadResult.Failure("Upload timed out").also {
            Log.e(TAG, "Timed out for $uri")
        }
    }


    suspend fun uploadImage(context: Context, imageUri: Uri): String? =
        (upload(context, imageUri, PROFILE_PRESET, "juttela/profile_images") as? UploadResult.Success)?.url

    suspend fun uploadVerificationImage(
        context: Context,
        imageUri: Uri,
        userId: String,
        imageName: String = "image_${System.currentTimeMillis()}"
    ): String? = uploadVerificationImageResult(context, imageUri, userId, imageName)
        .let { (it as? UploadResult.Success)?.url }


    suspend fun uploadVerificationImageResult(
        context: Context,
        imageUri: Uri,
        userId: String,
        imageName: String = "image_${System.currentTimeMillis()}"
    ): UploadResult = upload(
        context = context,
        uri = imageUri,
        preset = VERIFICATION_PRESET,
        folder = "VerificationImage/users/$userId",
        publicId = imageName
    )
}