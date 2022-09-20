package eu.tutorials.futureweatherforecast.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "City_data_table")
data class ManageCities (

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "city_name")
    var id : Int,

    @ColumnInfo(name = "city_id")
    var CityName : String
)