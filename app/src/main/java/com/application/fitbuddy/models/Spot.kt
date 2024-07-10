package com.application.fitbuddy.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "spots",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["username"],
        childColumns = ["user_username"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["user_username"])]
)
data class Spot(
    @ColumnInfo(name = "user_username")
    var userUsername: String,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "latitude")
    var latitude: Double,
    @ColumnInfo(name = "longitude")
    var longitude: Double
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    constructor() : this("","", 0.0, 0.0)
}