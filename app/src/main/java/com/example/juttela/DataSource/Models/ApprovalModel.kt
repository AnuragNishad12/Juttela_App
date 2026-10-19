package com.example.juttela.DataSource.Models

data class ApprovalModel(
    val userId: String,
    val approvalStatus: String,
    val approvedAt: String? = null,
    val rejectedReason: String? = null
)