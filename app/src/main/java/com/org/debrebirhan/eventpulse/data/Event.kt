package com.org.debrebirhan.eventpulse.data

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val date: String = "",
    val price: String = "",
    val imageUrl: String = "",
    val capacity: String = "",
    val organizerId: String = "",
    val category: String = ""
)