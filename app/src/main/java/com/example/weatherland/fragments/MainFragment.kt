package com.example.weatherland.fragments

import adapters.VpAdapter
import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherland.DayItem
import com.example.weatherland.DialogManager
import com.example.weatherland.MainViewModel
import com.example.weatherland.R
import com.example.weatherland.databinding.FragmentMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import org.json.JSONObject

const val API = "Ваш API-ключ"

class MainFragment : Fragment() {
    private lateinit var fLocationClient : FusedLocationProviderClient
    private val fList = listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance()
    )
    private val tList = listOf(
        "По часам",
        "На 3 дня"
    )
    private lateinit var binding: FragmentMainBinding
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Создание экземпляра привязки к FragmentMain
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        init()
        updateCurrentCard()
    }

    private fun checkPermission() {
        //местоположение (по GPS)
        if (!isPermissionGranted(ACCESS_FINE_LOCATION)) {
            permissionListener()
            pLauncher.launch(ACCESS_FINE_LOCATION)
        }
    }

    private fun init() = with(binding) {
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter = VpAdapter(activity as FragmentActivity, fList)
        vp.adapter = adapter
        TabLayoutMediator(tabLayout, vp) { tab, pos ->
          tab.text = tList[pos]
        }.attach()
        // С помощью attach() связываем ViewPager и TabLayout, чтобы при перелистывании
        // ViewPager менялся выделенный TabLayout
        ibSync.setOnClickListener {
            tabLayout.selectTab(tabLayout.getTabAt(0))
            checkLocation()
        }
        ibSearch.setOnClickListener {
            DialogManager.searchByNameDialog(requireContext(), object: DialogManager.Listener{
                override fun onClick(name: String?) {
                    name?.let { it1 -> requestWeatherData(it1) }
                }

            })
        }
    }

    private fun updateCurrentCard() = with(binding) {
        model.liveDataCurrent.observe(viewLifecycleOwner) {
            val maxMinTemp = "${it.maxTemp}°/${it.minTemp}°"
            tvData.text = it.time
            tvCity.text = it.city
            tvCurrentTemp.text = "${it.currentTemp.ifEmpty { "${it.maxTemp}°/${it.minTemp}"}}°"
            tvCondition.text = it.condition
            if (Regex("rain").containsMatchIn(tvCondition.text)) imageView.setImageResource(R.drawable.rain)
            if (Regex("drizzle").containsMatchIn(tvCondition.text)) imageView.setImageResource(R.drawable.rain)
            if (Regex("thunder").containsMatchIn(tvCondition.text)) imageView.setImageResource(R.drawable.thunder)
            if (tvCondition.text == "Sunny") imageView.setImageResource(R.drawable.sun)
            if (tvCondition.text == "Partly cloudy") imageView.setImageResource(R.drawable.partly)
            if (tvCondition.text in listOf("Mist", "Fog")) imageView.setImageResource(R.drawable.fog)
            if (tvCondition.text == "Clear") imageView.setImageResource(R.drawable.moon)
            if (tvCondition.text in listOf("Overcast", "Cloudy")) imageView.setImageResource(R.drawable.overcast)
            tvMaxMin.text = if(it.currentTemp.isEmpty()) "" else maxMinTemp
            Picasso.get().load("https:" + it.imageUrl).into(imWeather)
            textWarnings.text = if (it.events.isEmpty()) ""
            else (if (it.events == "Радуйтесь: нет штормовых предупреждений!")"   Радуйтесь: нет штормовых предупреждений!"
            else it.events)
        }
    }


    override fun onResume() {
        super.onResume()
        checkLocation()
    }


    private fun checkLocation(){
        if(isLocationEnabled()){
            getLocation()
        } else {
            DialogManager.locationSettingsDialog(requireContext(), object : DialogManager.Listener{
                override fun onClick(name: String?) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    }

    private fun isLocationEnabled(): Boolean{
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getLocation(){
        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token).addOnCompleteListener{
                requestWeatherData("${it.result.latitude},${it.result.longitude}")
            }
    }



    private fun permissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            if (it == true) Toast.makeText(
                activity,
                "Доступ успешно получен! Будут загружены метеорологичекие данные для вашего местоположения",
                Toast.LENGTH_LONG
            ).show()
            else Toast.makeText(
                activity,
                "Доступ к местоположению не получен. Вы можете изменить это в настройках или при следующем запуске приложения",
                Toast.LENGTH_LONG
            ).show()
        }
    }

   private fun requestWeatherData(city: String) {
        val url = "https://api.weatherapi.com/v1/forecast.json?key=" +
                API +
                "&q=$city" +
                "&days=3" +
                "&aqi=no" +
                "&alerts=yes"
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            { result -> parseWeatherData(result) },
            { error -> Log.d("MyOwnLog", "Error: $error")
                Toast.makeText(
                    activity,
                    "Такого города не существует! Вы ввели что-то не то! Попробуйте ещё раз",
                    Toast.LENGTH_LONG
                ).show()}
        )
        queue.add(request)
    }


    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject)
        parseCurrentData(mainObject, list[0])
    }

    private fun parseDays(mainObject: JSONObject): List<DayItem> {
        val list = ArrayList<DayItem>()
        val daysArray = mainObject.getJSONObject("forecast").getJSONArray("forecastday")
        val name = mainObject.getJSONObject("location").getString("name")
        for (i in 0 until daysArray.length()) {
            val day = daysArray[i] as JSONObject
            val item = DayItem(
                name,
                day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition").getString("text"),
                day.getJSONObject("day").getJSONObject("condition").getString("icon"),
                "",
                day.getJSONObject("day").getString("maxtemp_c").toFloat().toInt().toString(),
                day.getJSONObject("day").getString("mintemp_c").toFloat().toInt().toString(),
                day.getJSONArray("hour").toString(),
                "",
            "")
            list.add(item)
        }
        model.liveDataList.value = list
        return list
    }


    private fun parseCurrentData(mainObject: JSONObject, weatherItem: DayItem) {
        val jsonArray = mainObject.getJSONObject("alerts").getJSONArray("alert")
        if (jsonArray.length() > 0) {
            val array_for_not_repeat = Array(7,  {i -> 0})
            for(i in 0 until jsonArray.length()) {

                weatherItem.warnings =
                    mainObject.getJSONObject("alerts").getJSONArray("alert").getJSONObject(i)
                        .getString("category")
                if (weatherItem.warnings == "Wind") {
                    if (array_for_not_repeat[0] == 0) weatherItem.events += "ВНИМАНИЕ, ветер: ожидается сильный ветер. Лучше оставайтесь дома.${if (i!=jsonArray.length()-1) "\n" else ""}"
                    array_for_not_repeat[0] = 1
                }
                if (weatherItem.warnings == "Fire warning") {
                    if (array_for_not_repeat[1] == 0) weatherItem.events += "ВНИМАНИЕ, повышенная пожароопасность: будьте аккуратны с огнём на природе. Телефон экстренных служб: 101/112${if (i != jsonArray.length()-1) "\n" else ""}"
                    array_for_not_repeat[1] = 1
                }
                if (weatherItem.warnings == "Thunderstorm") {
                    if (array_for_not_repeat[2] == 0) weatherItem.events += "ВНИМАНИЕ, гроза: возможны опасные конвективные явления! Будьте осторожны!${if (i!=jsonArray.length()-1) "\n" else ""}"
                    array_for_not_repeat[2] = 1
                }
                if (weatherItem.warnings == "Extreme temperature value") {
                    if (array_for_not_repeat[3] == 0) weatherItem.events += "ВНИМАНИЕ, температурная аномалия: температура существенно отличается от средних метеорологических значений${if (i!=jsonArray.length()-1) "\n" else ""}"
                    array_for_not_repeat[3] = 1
                }
                if (weatherItem.warnings == "Met") {
                    if (array_for_not_repeat[4] == 0) weatherItem.events += "ВНИМАНИЕ, снегопады в горах: будьте аккуратны при путешествиях!${if (i!=jsonArray.length()-1) "\n" else ""}"
                    array_for_not_repeat[4] = 1
                }
                if (weatherItem.warnings == "Flood") {
                    if (array_for_not_repeat[5] == 0) weatherItem.events += "ВНИМАНИЕ, опасность наводнения/паводка: будьте осторожны!${if (i!=jsonArray.length()-1) "\n" else ""}"
                    array_for_not_repeat[5] = 1
                }
                if (weatherItem.warnings == "Avalanches") {
                    if (array_for_not_repeat[6] == 0) weatherItem.events += "ВНИМАНИЕ, лавинная опасность: будьте осторожны в горах!${if (i!=jsonArray.length()-1) "\n" else ""}"
                    array_for_not_repeat[6] = 1
                }

                with(binding) { textWarnings.setTextColor(Color.rgb(255, 255, 255)); }
            }
        } else {
            weatherItem.events = "Радуйтесь: нет штормовых предупреждений!"
            with(binding) { textWarnings.setTextColor(Color.rgb(255, 255, 255)); }
        }
        val item = DayItem(
            mainObject.getJSONObject("location").getString("name"),
            mainObject.getJSONObject("current").getString("last_updated"),
            mainObject.getJSONObject("current").getJSONObject("condition").getString("text"),
            mainObject.getJSONObject("current").getJSONObject("condition").getString("icon"),
            mainObject.getJSONObject("current").getString("temp_c"),
            weatherItem.maxTemp,
            weatherItem.minTemp,
            weatherItem.hours,
            weatherItem.warnings,
            weatherItem.events)
        model.liveDataCurrent.value = item
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()

    }
}