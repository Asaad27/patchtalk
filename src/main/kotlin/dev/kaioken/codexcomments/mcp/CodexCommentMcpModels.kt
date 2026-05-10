package dev.kaioken.codexcomments.mcp

import kotlinx.serialization.Serializable

@Serializable
data class McpCommentMessageDto(
    val id: String,
    val author: String,
    val body: String,
    val createdAt: String,
)

@Serializable
data class McpCommentThreadDto(
    val id: String,
    val path: String,
    val line: Int? = null,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val messages: List<McpCommentMessageDto>,
)

@Serializable
data class McpCommentThreadListDto(
    val items: List<McpCommentThreadDto>,
)
