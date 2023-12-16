package adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

// Создание адаптера под прогнозы погоды по часам и по дням
class VpAdapter(fa: FragmentActivity, private val list: List<Fragment>) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int {
       return list.size
    }

    override fun createFragment(position: Int): Fragment {
       return list[position]
    }
}