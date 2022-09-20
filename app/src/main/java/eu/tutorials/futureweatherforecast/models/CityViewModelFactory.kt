package eu.tutorials.futureweatherforecast.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import eu.tutorials.futureweatherforecast.db.CityRepository

class CityViewModelFactory(private val repository: CityRepository):ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CityViewModel::class.java)) {
            return CityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown View Model class")
    }
}



