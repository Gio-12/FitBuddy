package com.application.fitbuddy.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "spot_logs",
    foreignKeys = [ForeignKey(
        entity = Spot::class,
        parentColumns = ["id"],
        childColumns = ["spot_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["spot_id"])]
)
data class SpotLog(
    @ColumnInfo(name = "spot_id")
    var spotId: Int,
    @ColumnInfo(name = "date")
    var date: Long,
    @ColumnInfo(name = "entry")
    var entry: Boolean

) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    constructor() : this(0, 0, false)

    override fun toString(): String {
        val entryType = if (entry) "Entry" else "Exit"
        return "$entryType at ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(date))}"
    }
}