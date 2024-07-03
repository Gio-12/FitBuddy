package com.example.fitbuddy.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "spot_logs",
    foreignKeys = [ForeignKey(
        entity = Spot::class,
        parentColumns = ["id"],
        childColumns = ["spot_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class SpotLog(
    @ColumnInfo(name = "spot_id")
    var locationId: Int,
    @ColumnInfo(name = "date")
    var date: Long,
    @ColumnInfo(name = "date")
    var entry: Boolean

) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    constructor() : this(0, 0, false)
}