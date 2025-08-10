package com.github.zieiony.base.app.data

import java.io.Serializable

data class User(
    val id: Int,
    var name: String
) : Serializable
