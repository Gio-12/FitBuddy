package com.example.fitbuddy.models

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.fitbuddy.utils.DateConverter
import java.util.Date

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
    ]
)
@TypeConverters(DateConverter::class)
data class Follower(
    @ColumnInfo(name = "userId")
    var userFK: String,
    @ColumnInfo(name = "followerId")
    var followerFK: String,
    @ColumnInfo(name = "followedDate")
    var followedDate: Long
) {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    var id: Int = 0
    constructor() : this("", "", 0)
}
