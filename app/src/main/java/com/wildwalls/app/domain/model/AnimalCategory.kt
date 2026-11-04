package com.wildwalls.app.domain.model

/**
 * A browsing category. [query] is a real Wikimedia Commons CirrusSearch expression, [hints] are
 * used to keep only results that are actually filed under the animal (Commons categories contain
 * lookalike files such as statues, streets and aircraft display teams).
 */
data class AnimalCategory(
    val id: String,
    val name: String,
    val subtitle: String,
    val query: String,
    val hints: List<String>
)
