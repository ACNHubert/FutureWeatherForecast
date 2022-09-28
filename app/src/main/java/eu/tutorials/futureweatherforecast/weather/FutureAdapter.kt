package eu.tutorials.futureweatherforecast.weather

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import eu.tutorials.futureweatherforecast.R

class FutureAdapter() : RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.list_item,parent,false)
        return MyViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
       return 5
    }

}
class MyViewHolder(var view : View): RecyclerView.ViewHolder(view){
    val temperature = view.findViewById<TextView>(R.id.textView_temperature)
    val condition = view.findViewById<TextView>(R.id.textView_condition)
    val futureDate = view.findViewById<TextView>(R.id.textView_date)
    val conditionImage = view.findViewById<ImageView>(R.id.conditionImage)
}