package com.example.juttela.Utils



import android.content.Context

object ApprovalPrefs {
    const val PREFS = "approval_prefs"
    const val KEY_STATUS = "approval_status"

    fun saveStatus(context: Context, status: String?) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_STATUS, status)
            .apply()
    }

    fun isApproved(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_STATUS, null) == "approved"

    enum class VerificationStatus { NOT_VERIFIED, PENDING, APPROVED, REJECTED }


    fun getVerificationStatus(context: Context): VerificationStatus =
        when (context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_STATUS, null)) {
            "approved" -> VerificationStatus.APPROVED
            "pending"  -> VerificationStatus.PENDING
            "rejected" -> VerificationStatus.REJECTED
            else       -> VerificationStatus.NOT_VERIFIED   // null = never submitted
        }
}
