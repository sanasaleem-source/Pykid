package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val code: String,
    val lastModified: Long = System.currentTimeMillis(),
    val isTutorial: Boolean = false
)
