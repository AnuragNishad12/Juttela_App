package com.example.juttela.Utils

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object CloudinaryUploader {

    private const val CLOUD_NAME = "dnnmvzdnd"
    private const val UPLOAD_PRESET = "jutella_profile_uploads"

    private var initialized = false

    private fun initialize(context: Context) {

        if (!initialized) {

            val config = hashMapOf<String, String>()

            config["cloud_name"] = CLOUD_NAME
            config["secure"] = "true"

            MediaManager.init(
                context.applicationContext,
                config
            )

            initialized = true
        }
    }

    suspend fun uploadImage(
        context: Context,
        imageUri: Uri
    ): String? {

        initialize(context)

        return suspendCancellableCoroutine { continuation ->

            MediaManager.get()
                .upload(imageUri)
                .unsigned(UPLOAD_PRESET)
                .option("folder", "juttela/profile_images")
                .callback(object : UploadCallback {

                    override fun onStart(requestId: String?) {
                        // Upload started
                    }

                    override fun onProgress(
                        requestId: String?,
                        bytes: Long,
                        totalBytes: Long
                    ) {
                        // Upload progress
                    }

                    override fun onSuccess(
                        requestId: String?,
                        resultData: Map<*, *>?
                    ) {

                        val secureUrl =
                            resultData?.get("secure_url") as? String

                        if (continuation.isActive) {
                            continuation.resume(secureUrl)
                        }
                    }

                    override fun onError(
                        requestId: String?,
                        error: ErrorInfo?
                    ) {

                        println(
                            "Cloudinary Error: ${error?.description}"
                        )

                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }

                    override fun onReschedule(
                        requestId: String?,
                        error: ErrorInfo?
                    ) {

                        println(
                            "Cloudinary Rescheduled: ${error?.description}"
                        )
                    }
                })
                .dispatch()
        }
    }
}