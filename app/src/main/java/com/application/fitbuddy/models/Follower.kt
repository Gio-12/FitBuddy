package com.application.fitbuddy.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.application.fitbuddy.utils.DateConverter

@Entity(
    tableName = "follower",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["username"],
            childColumns = ["userFK"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["username"],
            childColumns = ["followerFK"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userFK"]),
        Index(value = ["followerFK"])
    ]
)
@TypeConverters(DateConverter::class)
data class Follower(
    @ColumnInfo(name = "userFK")
    var userFK: String,
    @ColumnInfo(name = "followerFK")
    var followerFK: String,
    @ColumnInfo(name = "followedDate")
    var followedDate: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    constructor() : this("", "", 0)
}
