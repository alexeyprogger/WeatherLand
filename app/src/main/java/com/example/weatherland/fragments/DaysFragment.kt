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
import com.example.weatherland.R
import com.example.weatherland.databinding.FragmentDaysBinding


class DaysFragment : Fragment(), WeatherAdapter.Listener {
    private lateinit var adapter: WeatherAdapter
    private lateinit var binding: FragmentDaysBinding
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDaysBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        model.liveDataList.observe(viewLifecycleOwner){
            adapter.submitList(it.subList(0, it.size))
        }
    }

    private fun init() = with(binding){
        adapter = WeatherAdapter(this@DaysFragment )
        rcView.layoutManager = LinearLayoutManager(activity)
        rcView.adapter = adapter
    }
    companion object {

        @JvmStatic
        fun newInstance() = DaysFragment()
    }
    override fun onClick(item: DayItem) {
        model.liveDataCurrent.value = item
    }
}