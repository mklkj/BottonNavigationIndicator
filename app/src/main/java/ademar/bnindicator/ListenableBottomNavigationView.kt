package ademar.bnindicator

import android.content.Context
import android.util.AttributeSet
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener

class ListenableBottomNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : BottomNavigationView(context, attrs), OnItemSelectedListener {

    private val onNavigationItemSelectedListeners = mutableListOf<OnItemSelectedListener>()

    init {
        super.setOnItemSelectedListener(this)
    }

    override fun setOnItemSelectedListener(listener: OnItemSelectedListener?) {
        if (listener != null) addOnNavigationItemSelectedListener(listener)
    }

    fun addOnNavigationItemSelectedListener(listener: (Int) -> Unit) {
        addOnNavigationItemSelectedListener(OnItemSelectedListener {
            for (i in 0 until menu.size()) if (menu.getItem(i) == it) listener(i)
            false
        })
    }

    fun addOnNavigationItemSelectedListener(listener: OnItemSelectedListener) {
        onNavigationItemSelectedListeners.add(listener)
    }

    override fun onNavigationItemSelected(item: MenuItem) = onNavigationItemSelectedListeners
        .map { it.onNavigationItemSelected(item) }
        .fold(false) { acc, it -> acc || it }

}
