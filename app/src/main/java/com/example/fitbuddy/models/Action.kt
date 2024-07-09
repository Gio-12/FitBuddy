package com.example.fitbuddy.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.fitbuddy.utils.DateConverter

@Entity(
    tableName = "actions",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["username"],
        childColumns = ["user_username"],
        onDelete = ForeignKey.CASCADE
    )]
)
@TypeConverters(DateConverter::class)
data class Action(
    @ColumnInfo(name = "user_username")
    var userUsername: String,
    @ColumnInfo(name = "action_type")
    var actionType: String,
    @ColumnInfo(name = "steps")
    var steps: Int,
    @ColumnInfo(name = "start_time")
    var startTime: Long,
    @ColumnInfo(name = "end_time")
    var endTime: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    constructor() : this("", "", 0, 0L, 0L)
}
