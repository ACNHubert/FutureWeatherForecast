package eu.tutorials.futureweatherforecast.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Switch
import android.widget.Toast
import android.widget.VideoView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog

import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import eu.tutorials.futureweatherforecast.R
import eu.tutorials.futureweatherforecast.databinding.ActivityMainBinding
import eu.tutorials.futureweatherforecast.currentForecastModels.WeatherResponse
import eu.tutorials.futureweatherforecast.network.WeatherService
import eu.tutorials.futureweatherforecast.utils.Constants
import retrofit.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mProgressDialog: Dialog? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var bindind: ActivityMainBinding
    internal lateinit var mySwitch : Switch

    companion object {
        var LOCATION = ""
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindind = DataBindingUtil.setContentView(this, R.layout.activity_main)

        bindind.futureForecast.setOnClickListener{
           Constants.LOCATION = bindind.tvName.text.toString()
             val intent = Intent(this@MainActivity, fiveDaysForecast::class.java)
            startActivity(intent)
        }
        bindind.manageCity.setOnClickListener{
            val intent = Intent(this@MainActivity, ManageCities::class.java)
            startActivity(intent)
        }

        bindind.searchButton.setOnClickListener{
            Constants.LOCATION = bindind.inputLocation.text.toString()
            var locationinput : String = bindind.inputLocation.text.toString()
            if (locationinput == ""){
                getLocationWeatherDetails()
            } else {
                getLocationWeather()
            }

        }

        mySwitch = bindind.nightMode as Switch
        mySwitch.setOnClickListener{
            if (mySwitch.isChecked){
                bindind.MainBG.setBackgroundResource(R.drawable.bgcloudynight)
                bindind.nightMode.setTextColor(Color.WHITE)
                bindind.tempText.setTextColor(Color.WHITE)
                Toast.makeText(this,"Night mode is on",Toast.LENGTH_SHORT).show()
            } else {
                bindind.MainBG.setBackgroundResource(R.drawable.bgmorning)
                bindind.nightMode.setTextColor(Color.BLACK)
                bindind.tempText.setTextColor(Color.BLACK)
                Toast.makeText(this,"Night mode is off",Toast.LENGTH_SHORT).show()
            }
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE)
        //setupUI()

        if (!isLocationEnabled()) {
            Toast.makeText(
                this,
                "Your location provider is turned off. Please turn it on.",
                Toast.LENGTH_SHORT
            ).show()
            // This will redirect you to settings from where you need to turn on the location provider.
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            //Ask the location permission on runtime.)

            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            requestLocationData()
                     
                        }

                        if (report.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(
                                this@MainActivity,
                                "You have denied location permission. Please allow it is mandatory.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread()
                .check()

        }

    }




    private fun isLocationEnabled(): Boolean {
        // This provides access to the system location services.
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER

        )
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It Looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
            .setPositiveButton(
                "GO TO SETTINGS"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()

    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private fun getLocationWeatherDetails() {
        if (Constants.isNetworkAvailable(this@MainActivity)) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: WeatherService =
                retrofit.create<WeatherService>(WeatherService::class.java)
            val listCall: Call<WeatherResponse> = service.getWeather(
                mLatitude, mLongitude, Constants.METRIC_UNIT,Constants.APP_ID
            )
            showCustomProgressDialog()
            listCall.enqueue(object : Callback<WeatherResponse> {
                @RequiresApi(Build.VERSION_CODES.N)
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    response: Response<WeatherResponse>,
                    retrofit: Retrofit
                ) {

                    if (response.isSuccess) {
                        hideProgressDialog()

                        val weatherList: WeatherResponse = response.body()
                        Log.i("Response Result", "$weatherList")

                        val weatherResponseJsonString = Gson().toJson(weatherList)
                        val editor = mSharedPreferences.edit()

                        editor.putString(Constants.WEATHER_RESPONSE_DATA, weatherResponseJsonString)
                        editor.apply()
                        setupUI()
                    } else {
                        val sc = response.code()
                        hideProgressDialog()
                        when (sc) {
                            400 -> {
                                Log.e("Error 400", "Bad Request")
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                            }
                            else -> {
                                Log.e("Error", "Generic Error")
                            }
                        }
                    }
                }
                override fun onFailure(t: Throwable) {
                    Log.e("Errorrrrr", t.message.toString())
                    hideProgressDialog()
                }
            })
        } else {
            Toast.makeText(
                this@MainActivity,
                "No internet connection available.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val mLocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation

            mLatitude = mLastLocation!!.latitude
            Log.e("Current Latitude", "$mLatitude")
            mLongitude = mLastLocation.longitude
            Log.e("Current Longitude", "$mLongitude")
            if (LOCATION == ""){
                getLocationWeatherDetails()
            } else {
                Constants.LOCATION = LOCATION
                getLocationWeather()
            }
        }
    }

    //Method is used to show the Custom Progress Dialog.
    private fun showCustomProgressDialog() {
        mProgressDialog = Dialog(this)

        /*Set the screen content from a layout resource.
        The resource will be inflated, adding all top-level views to the screen.*/
        //mProgressDialog!!.setContentView(R.layout.)

        //Start the dialog and display it on screen.
        mProgressDialog!!.show()
    }


    //This function is used to dismiss the progress dialog if it is visible to user.
    private fun hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    private fun setupUI() {
        val weatherResponseJsonString =
            mSharedPreferences.getString(Constants.WEATHER_RESPONSE_DATA, "")

        if (!weatherResponseJsonString.isNullOrEmpty()) {

            val weatherList =
                Gson().fromJson(weatherResponseJsonString, WeatherResponse::class.java)
            // For loop to get the required data. And all are populated in the UI.
            for (z in weatherList.weather.indices) {
                Log.i("NAMEEEEEEEE", weatherList.weather[z].main)

                bindind.tvMain.text = weatherList.weather[z].main
                bindind.tvMainDescription.text = weatherList.weather[z].description
                bindind.tvTemp.text = weatherList.main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
                bindind.tvHumidity.text = weatherList.main.humidity.toString() + " per cent"
                bindind.tvMin.text = weatherList.main.temp_min.toString() + " min"
                bindind.tvMax.text = weatherList.main.temp_max.toString() + " max"
                bindind.tvSpeed.text = weatherList.wind.speed.toString()
                bindind.tvName.text = weatherList.name
                bindind.tvCountry.text = weatherList.sys.country
                bindind.tvSunriseTime.text = unixTime(weatherList.sys.sunrise.toLong()) + " AM"
                bindind.tvSunsetTime.text = unixTime(weatherList.sys.sunset.toLong()) + " PM"
                bindind.tempText.text = weatherList.main.temp.toString() + getUnit(application.resources.configuration.locales.toString())


                // Here we update the main icon
                when (weatherList.weather[z].icon) {
                    "01d" -> bindind.ivMain.setImageResource(R.drawable.sunny)
                    "02d" -> bindind.ivMain.setImageResource(R.drawable.cloud)
                    "03d" -> bindind.ivMain.setImageResource(R.drawable.cloud)
                    "04d" -> bindind.ivMain.setImageResource(R.drawable.cloud)
                    "04n" -> bindind.ivMain.setImageResource(R.drawable.cloud)
                    "10d" -> bindind.ivMain.setImageResource(R.drawable.rain)
                    "11d" -> bindind.ivMain.setImageResource(R.drawable.storm)
                    "13d" -> bindind.ivMain.setImageResource(R.drawable.snowflake)
                    "01n" -> bindind.ivMain.setImageResource(R.drawable.cloud)
                    "02n" -> bindind.ivMain.setImageResource(R.drawable.cloud)
                    "03n" -> bindind.ivMain.setImageResource(R.drawable.cloud)
                    "10n" -> bindind.ivMain.setImageResource(R.drawable.cloud)
                    "11n" -> bindind.ivMain.setImageResource(R.drawable.rain)
                    "13n" -> bindind.ivMain.setImageResource(R.drawable.snowflake)
                }
            }
        }
    }


    //Function is used to get the temperature unit value.

    private fun getUnit(value: String): String? {
        Log.i("unitttttt", value)
        var value = "°C"
        if ("US" == value || "LR" == value || "MM" == value) {
            value = "°F"
        }
        return value
    }


    //The function is used to get the formatted time based on the Format and the LOCALE we pass to it.

    @RequiresApi(Build.VERSION_CODES.N)
    private fun unixTime(timex: Long): String? {
        val date = Date(timex * 1000L)
        @SuppressLint("SimpleDateFormat") val sdf =
            SimpleDateFormat("HH:mm", Locale.CHINA)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }

     fun getLocationWeather() {
        if (Constants.isNetworkAvailable(this@MainActivity)) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: WeatherService =
                retrofit.create<WeatherService>(WeatherService::class.java)
            val listCall: Call<WeatherResponse> = service.getLocationWeather(
                mLatitude, mLongitude, Constants.METRIC_UNIT,Constants.LOCATION,Constants.APP_ID
            )
            showCustomProgressDialog()
            listCall.enqueue(object : Callback<WeatherResponse> {
                @RequiresApi(Build.VERSION_CODES.N)
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    response: Response<WeatherResponse>,
                    retrofit: Retrofit
                ) {

                    if (response.isSuccess) {
                        hideProgressDialog()

                        val weatherList: WeatherResponse = response.body()
                        Log.i("Response Result", "$weatherList")

                        val weatherResponseJsonString = Gson().toJson(weatherList)
                        val editor = mSharedPreferences.edit()

                        editor.putString(Constants.WEATHER_RESPONSE_DATA, weatherResponseJsonString)
                        editor.apply()

//                        var location : String = weatherList.sys.country
//                        if (location == "PH") {
                            setupUI()
//                        } else {
//                            Toast.makeText(
//                                this@MainActivity,
//                                "Invalid City",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }

                    } else {
                        val sc = response.code()
                        hideProgressDialog()
                        when (sc) {
                            400 -> {
                                Log.e("Error 400", "Bad Request")
                                Toast.makeText(
                                    this@MainActivity,
                                    "Invalid City",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                                Toast.makeText(
                                    this@MainActivity,
                                    "Invalid City",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            else -> {
                                Log.e("Error", "Generic Error")
                            }
                        }
                    }
                }
                override fun onFailure(t: Throwable) {
                    Log.e("Errorrrrr", t.message.toString())
                    hideProgressDialog()
                }
            })
        } else {
            Toast.makeText(
                this@MainActivity,
                "No internet connection available.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }



} //End


