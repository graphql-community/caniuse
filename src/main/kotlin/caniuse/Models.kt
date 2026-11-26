package caniuse

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val name: String,
    val types: List<String>,
    val description: String,
    val url: String,
    val features: Map<String, SupportInfo?>,
)

@Serializable
data class Feature(
    val name: String,
    val description: String,
    val url: String? = null,
    val tags: List<String> = emptyList(),
)

@Serializable
data class SupportInfo(
    val since: String? = null,
    val note: String? = null,
)
