package com.example.weatherland.fragments

import adapters.WeatherAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherland.DayItem
import com.example.weatherland.MainViewModel
import com.example.weatherland.databinding.FragmentHoursBinding
import org.json.JSONArray
import org.json.JSONObject


class HoursFragment : Fragment() {
    private lateinit var binding: FragmentHoursBinding
    private lateinit var adapter : WeatherAdapter
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHoursBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView()
        model.liveDataCurrent.observe(viewLifecycleOwner) {
            adapter.submitList(getHoursList(it))
        }
    }

    private fun initRcView() = with(binding){
        rcView.layoutManager = LinearLayoutManager(activity) // показ по вертикали по умолчанию
        adapter = WeatherAdapter(null)
        rcView.adapter = adapter
    }

    private fun getHoursList(wItem: DayItem): List<DayItem>{
        val hoursArray = JSONArray(wItem.hours)
        val list = ArrayList<DayItem>()
        for(i in 0 until hoursArray.length()){
            val item = DayItem(
                wItem.city,
                (hoursArray[i] as JSONObject).getString("time"),
                (hoursArray[i] as JSONObject).getJSONObject("condition")
                    .getString("text"),
                (hoursArray[i] as JSONObject).getJSONObject("condition")
                    .getString("icon"),
                (hoursArray[i] as JSONObject).getString("temp_c").toFloat().toInt().toString(),
                "",
                "",
                "",
            "",
                "")
            list.add(item)
        }
        return list
    }

    companion object {

        @JvmStatic
        fun newInstance() = HoursFragment()

    }
}