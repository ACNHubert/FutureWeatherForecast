package eu.tutorials.futureweatherforecast.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CityDAO {
    @Insert
    suspend fun insertCity(manageCities: ManageCities): Long

    //@Update
    //suspend fun updateCity(manageCities: ManageCities)

    @Delete
    suspend fun deleteCity(manageCities: ManageCities)

    @Query("DELETE FROM City_data_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM City_data_table")
    fun getAllCity(): LiveData<List<ManageCities>>
}