package com.example.algokotlinapp.model

data class Node(
    val x: Int,
    val y: Int,
    val g: Int = 0,
    val h: Int = 0,
    val parent: Node? = null
) {
    val f: Int get() = g + h
}
