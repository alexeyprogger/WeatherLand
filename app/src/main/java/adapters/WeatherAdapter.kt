package adapters

import android.location.GnssAntennaInfo.Listener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherland.DayItem
import com.example.weatherland.R
import com.example.weatherland.databinding.ListItemBinding
import com.squareup.picasso.Picasso
import org.json.JSONObject

class WeatherAdapter(val listener: Listener?) : ListAdapter<DayItem, WeatherAdapter.Holder>(Comparator()) {

    // Заполнение разметки
    class Holder(view: View, val listener: Listener?) : RecyclerView.ViewHolder(view) {
        val binding = ListItemBinding.bind(view)
        var itemTemp: DayItem? = null

        init {
            itemView.setOnClickListener {
                itemTemp?.let { it1 -> listener?.onClick(it1) }
            }
        }

        fun bind(item: DayItem) = with(binding){
            itemTemp = item
            tvDate.text = item.time
            tvCond.text = item.condition
            tvTemp.text = "${item.currentTemp.ifEmpty { "${item.maxTemp}° / ${item.minTemp}" }}°"
            Picasso.get().load("https:" + item.imageUrl).into(im)
        }
    }


        class Comparator : DiffUtil.ItemCallback<DayItem>() {
            override fun areItemsTheSame(oldItem: DayItem, newItem: DayItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: DayItem, newItem: DayItem): Boolean {
                return oldItem == newItem
            }

        }

        // Создание карточки (View-элемента) - шаблона list_item
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
            return Holder(view, listener)
        }

        // Заполнение карточки
        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.bind(getItem(position))
        }

        interface Listener {
            fun onClick(item: DayItem)
        }

    }

