package eu.tutorials.futureweatherforecast.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import eu.tutorials.futureweatherforecast.R
import eu.tutorials.futureweatherforecast.databinding.ActivityManageCitiesBinding
import eu.tutorials.futureweatherforecast.db.CityDatabase
import eu.tutorials.futureweatherforecast.db.CityRepository
import eu.tutorials.futureweatherforecast.db.ManageCities
import eu.tutorials.futureweatherforecast.models.CityViewModel
import eu.tutorials.futureweatherforecast.models.CityViewModelFactory
import eu.tutorials.futureweatherforecast.utils.CityAdapter
import eu.tutorials.futureweatherforecast.utils.Constants

class ManageCities : AppCompatActivity() {
    private lateinit var binding: ActivityManageCitiesBinding
    private lateinit var cityViewModel: CityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_manage_cities)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_cities)
        val dao = CityDatabase.getInstance(application).cityDAO
        val repository = CityRepository(dao)
        val factory = CityViewModelFactory(repository)
        cityViewModel = ViewModelProvider(this, factory)[CityViewModel::class.java]
        binding.myViewModel = cityViewModel
        binding.lifecycleOwner = this
        initRecyclerView()

<<<<<<< HEAD
        binding.viewData.setOnClickListener() {
            MainActivity.LOCATION = binding.nameText.text.toString()
=======
        binding.viewData.setOnClickListener(){
            Constants.LOCATION = binding.nameText.text.toString()

>>>>>>> 584cea831f73f803de916498d486698d845a136e
            val intent = Intent(this@ManageCities, MainActivity::class.java)
            startActivity(intent)
        }

    }

<<<<<<< HEAD

    private fun initRecyclerView() {
=======
    private fun initRecyclerView(){
>>>>>>> 584cea831f73f803de916498d486698d845a136e
        binding.cityRecyclerView.layoutManager = LinearLayoutManager(this)
        displayCityList()
    }


    private fun displayCityList() {
        cityViewModel.cities.observe(this, Observer {
            Log.i("MYTAG", it.toString())
            binding.cityRecyclerView.adapter =
                CityAdapter(it, { selectedItem: ManageCities -> listItemClicked(selectedItem) })
        })
    }

    private fun listItemClicked(manageCities: eu.tutorials.futureweatherforecast.db.ManageCities) {
        cityViewModel.initUpdateAndDelete(manageCities)
    }
}