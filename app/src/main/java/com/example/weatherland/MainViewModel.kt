package com.example.weatherland

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel(){
    val liveDataCurrent = MutableLiveData<DayItem>()
    val liveDataList = MutableLiveData<List<DayItem>>()
}