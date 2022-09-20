package eu.tutorials.futureweatherforecast.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.location.Location
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import android.widget.VideoView
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.*
import com.google.gson.Gson
import eu.tutorials.futureweatherforecast.R
import eu.tutorials.futureweatherforecast.currentForecastModels.WeatherResponse
import eu.tutorials.futureweatherforecast.databinding.ActivityFiveDaysForecastBinding
import eu.tutorials.futureweatherforecast.databinding.ActivityMainBinding
import eu.tutorials.futureweatherforecast.futureForecastModel.FutureWeather
import eu.tutorials.futureweatherforecast.network.WeatherService
import eu.tutorials.futureweatherforecast.utils.Constants
import retrofit.*
import java.util.*


class fiveDaysForecast : AppCompatActivity() {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mSharedPreferences: SharedPreferences
    private var mProgressDialog: Dialog? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    private lateinit var bindind: ActivityFiveDaysForecastBinding





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_five_days_forecast)
        bindind = DataBindingUtil.setContentView(this, R.layout.activity_five_days_forecast)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE)

        requestLocationData()
    }


    private fun getFutureWeatherDetails() {
        if (Constants.isNetworkAvailable(this@fiveDaysForecast )) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val service: WeatherService =
                retrofit.create<WeatherService>(WeatherService::class.java)
            val listCall: Call<FutureWeather> = service.getFutureWeather(
                mLatitude, mLongitude,  Constants.METRIC_UNIT,Constants.LOCATION,Constants.APP_ID
            )

            showCustomProgressDialog()

            listCall.enqueue(object : Callback<FutureWeather> {
                @RequiresApi(Build.VERSION_CODES.N)
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    response: Response<FutureWeather>,
                    retrofit: Retrofit
                ) {

                    if (response.isSuccess) {
                        hideProgressDialog()
                        /** The de-serialized response body of a successful response. */
                        val weatherList: FutureWeather = response.body()
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
                this@fiveDaysForecast,
                "No internet connection available.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    private fun setupUI() {
        val weatherResponseJsonString =
            mSharedPreferences.getString(Constants.WEATHER_RESPONSE_DATA, "")

        if (!weatherResponseJsonString.isNullOrEmpty()) {

            val weatherList =
                Gson().fromJson(weatherResponseJsonString, eu.tutorials.futureweatherforecast.futureForecastModel.FutureWeather::class.java)
            // For loop to get the required data. And all are populated in the UI.
            for (z in weatherList.list.indices) {
                bindind.textLocation.text = weatherList.city.name
                bindind.tempText1.text = weatherList.list[0].main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
                bindind.tempText2.text = weatherList.list[8].main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
                bindind.tempText3.text = weatherList.list[16].main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
                bindind.tempText4.text = weatherList.list[24].main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
                bindind.tempText5.text = weatherList.list[32].main.temp.toString() + getUnit(application.resources.configuration.locales.toString())

                bindind.tempDate1.text = weatherList.list[0].dt_txt
                bindind.tempDate2.text = weatherList.list[8].dt_txt
                bindind.tempDate3.text = weatherList.list[16].dt_txt
                bindind.tempDate4.text = weatherList.list[24].dt_txt
                bindind.tempDate5.text = weatherList.list[32].dt_txt

                bindind.status1.text = weatherList.list[0].weather[0].description
              bindind.status2.text = weatherList.list[8].weather[0].description
                bindind.status3.text = weatherList.list[16].weather[0].description
                bindind.status4.text = weatherList.list[24].weather[0].description
                bindind.status5.text = weatherList.list[32].weather[0].description


                when (weatherList.list[0].weather[0].icon) {
                    "01d" -> bindind.TempImage1.setImageResource(R.drawable.sunny)
                    "02d" -> bindind.TempImage1.setImageResource(R.drawable.cloud)
                    "03d" -> bindind.TempImage1.setImageResource(R.drawable.cloud)
                    "04d" -> bindind.TempImage1.setImageResource(R.drawable.cloud)
                    "04n" -> bindind.TempImage1.setImageResource(R.drawable.cloud)
                    "10d" -> bindind.TempImage1.setImageResource(R.drawable.rain)
                    "11d" -> bindind.TempImage1.setImageResource(R.drawable.storm)
                    "13d" -> bindind.TempImage1.setImageResource(R.drawable.snowflake)
                    "01n" -> bindind.TempImage1.setImageResource(R.drawable.cloud)
                    "02n" -> bindind.TempImage1.setImageResource(R.drawable.cloud)
                    "03n" -> bindind.TempImage1.setImageResource(R.drawable.cloud)
                    "10n" -> bindind.TempImage1.setImageResource(R.drawable.cloud)
                    "11n" -> bindind.TempImage1.setImageResource(R.drawable.rain)
                    "13n" -> bindind.TempImage1.setImageResource(R.drawable.snowflake)
                }
                when (weatherList.list[8].weather[0].icon) {
                    "01d" -> bindind.TempImage2.setImageResource(R.drawable.sunny)
                    "02d" -> bindind.TempImage2.setImageResource(R.drawable.cloud)
                    "03d" -> bindind.TempImage2.setImageResource(R.drawable.cloud)
                    "04d" -> bindind.TempImage2.setImageResource(R.drawable.cloud)
                    "04n" -> bindind.TempImage2.setImageResource(R.drawable.cloud)
                    "10d" -> bindind.TempImage2.setImageResource(R.drawable.rain)
                    "11d" -> bindind.TempImage2.setImageResource(R.drawable.storm)
                    "13d" -> bindind.TempImage2.setImageResource(R.drawable.snowflake)
                    "01n" -> bindind.TempImage2.setImageResource(R.drawable.cloud)
                    "02n" -> bindind.TempImage2.setImageResource(R.drawable.cloud)
                    "03n" -> bindind.TempImage2.setImageResource(R.drawable.cloud)
                    "10n" -> bindind.TempImage2.setImageResource(R.drawable.cloud)
                    "11n" -> bindind.TempImage2.setImageResource(R.drawable.rain)
                    "13n" -> bindind.TempImage2.setImageResource(R.drawable.snowflake)
                }
                when (weatherList.list[16].weather[0].icon) {
                    "01d" -> bindind.TempImage3.setImageResource(R.drawable.sunny)
                    "02d" -> bindind.TempImage3.setImageResource(R.drawable.cloud)
                    "03d" -> bindind.TempImage3.setImageResource(R.drawable.cloud)
                    "04d" -> bindind.TempImage3.setImageResource(R.drawable.cloud)
                    "04n" -> bindind.TempImage3.setImageResource(R.drawable.cloud)
                    "10d" -> bindind.TempImage3.setImageResource(R.drawable.rain)
                    "11d" -> bindind.TempImage3.setImageResource(R.drawable.storm)
                    "13d" -> bindind.TempImage3.setImageResource(R.drawable.snowflake)
                    "01n" -> bindind.TempImage3.setImageResource(R.drawable.cloud)
                    "02n" -> bindind.TempImage3.setImageResource(R.drawable.cloud)
                    "03n" -> bindind.TempImage3.setImageResource(R.drawable.cloud)
                    "10n" -> bindind.TempImage3.setImageResource(R.drawable.cloud)
                    "11n" -> bindind.TempImage3.setImageResource(R.drawable.rain)
                    "13n" -> bindind.TempImage3.setImageResource(R.drawable.snowflake)
                }
                when (weatherList.list[24].weather[0].icon) {
                    "01d" -> bindind.TempImage4.setImageResource(R.drawable.sunny)
                    "02d" -> bindind.TempImage4.setImageResource(R.drawable.cloud)
                    "03d" -> bindind.TempImage4.setImageResource(R.drawable.cloud)
                    "04d" -> bindind.TempImage4.setImageResource(R.drawable.cloud)
                    "04n" -> bindind.TempImage4.setImageResource(R.drawable.cloud)
                    "10d" -> bindind.TempImage4.setImageResource(R.drawable.rain)
                    "11d" -> bindind.TempImage4.setImageResource(R.drawable.storm)
                    "13d" -> bindind.TempImage4.setImageResource(R.drawable.snowflake)
                    "01n" -> bindind.TempImage4.setImageResource(R.drawable.cloud)
                    "02n" -> bindind.TempImage4.setImageResource(R.drawable.cloud)
                    "03n" -> bindind.TempImage4.setImageResource(R.drawable.cloud)
                    "10n" -> bindind.TempImage4.setImageResource(R.drawable.cloud)
                    "11n" -> bindind.TempImage4.setImageResource(R.drawable.rain)
                    "13n" -> bindind.TempImage4.setImageResource(R.drawable.snowflake)
                }
                when (weatherList.list[32].weather[0].icon) {
                    "01d" -> bindind.TempImage5.setImageResource(R.drawable.sunny)
                    "02d" -> bindind.TempImage5.setImageResource(R.drawable.cloud)
                    "03d" -> bindind.TempImage5.setImageResource(R.drawable.cloud)
                    "04d" -> bindind.TempImage5.setImageResource(R.drawable.cloud)
                    "04n" -> bindind.TempImage5.setImageResource(R.drawable.cloud)
                    "10d" -> bindind.TempImage5.setImageResource(R.drawable.rain)
                    "11d" -> bindind.TempImage5.setImageResource(R.drawable.storm)
                    "13d" -> bindind.TempImage5.setImageResource(R.drawable.snowflake)
                    "01n" -> bindind.TempImage5.setImageResource(R.drawable.cloud)
                    "02n" -> bindind.TempImage5.setImageResource(R.drawable.cloud)
                    "03n" -> bindind.TempImage5.setImageResource(R.drawable.cloud)
                    "10n" -> bindind.TempImage5.setImageResource(R.drawable.cloud)
                    "11n" -> bindind.TempImage5.setImageResource(R.drawable.rain)
                    "13n" -> bindind.TempImage5.setImageResource(R.drawable.snowflake)
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

    private fun showCustomProgressDialog() {
        mProgressDialog = Dialog(this)
        mProgressDialog!!.show()
    }

    private fun hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    private val mLocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation

            mLatitude = mLastLocation!!.latitude
            Log.e("Current Latitude", "$mLatitude")
            mLongitude = mLastLocation.longitude
            Log.e("Current Longitude", "$mLongitude")

            getFutureWeatherDetails()
        }
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
}