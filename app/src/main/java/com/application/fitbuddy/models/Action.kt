package com.application.fitbuddy.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.application.fitbuddy.utils.DateConverter

@Entity(
    tableName = "actions",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["username"],
        childColumns = ["user_username"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["user_username"])]
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


    override fun toString(): String {
        return "'$userUsername', have been $actionType, with  $steps steps taken, started at ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(startTime))}, ended ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(endTime))})"
    }

    constructor() : this("", "", 0, 0L, 0L)
}
