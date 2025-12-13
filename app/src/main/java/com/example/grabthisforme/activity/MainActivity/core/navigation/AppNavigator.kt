package com.example.grabthisforme.activity.MainActivity.core.navigation

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.Navigator.Name
import androidx.navigation.fragment.FragmentNavigator


@Name("keep_state_fragment")
class AppNavigator(private val context: Context,
    private val manager: FragmentManager,
    private val containerId : Int) : FragmentNavigator(context,manager,containerId) {
    private val fragmentCache = HashMap<String, Fragment>()
    override fun instantiateFragment(
        context: Context,
        fragmentManager: FragmentManager,
        className: String,
        args: Bundle?
    ): Fragment {
        val cached = fragmentCache[className]
        if (cached != null){
            cached.arguments = args
            return cached
        }
        val fragment = super.instantiateFragment(context, fragmentManager, className, args)
        fragmentCache[className] = fragment
        Log.d("test1", "instantiateFragment: $className")
        return fragment
    }

    override fun navigate(
        entries: List<NavBackStackEntry>,
        navOptions: NavOptions?,
        navigatorExtras: Navigator.Extras?
    ) {
        val entry = entries.last()
        val destination = entry.destination as Destination
        val tag = destination.id.toString()

        var fragment = manager.findFragmentByTag(tag)
        if (fragment == null) {
            fragment = instantiateFragment(context, manager,
                destination.className, entry.arguments)
            manager.beginTransaction().add(containerId, fragment, tag).commitNow()
        }

        manager.beginTransaction().apply {
            manager.fragments.forEach { hide(it) }
            show(fragment)
        }.commitNow()
        state.push(entry)
    }
    override fun popBackStack(popUpTo: NavBackStackEntry, savedState: Boolean) {
        super.popBackStack(popUpTo, savedState)
        val visible = manager.fragments.lastOrNull { it.isAdded && !it.isHidden } ?: run {
            val last = manager.fragments.lastOrNull()
            last ?: return
        }

        manager.beginTransaction().apply {
            manager.fragments.forEach { hide(it) }
            show(visible)
        }.commitNow()
    }

    fun clearCache() {
        fragmentCache.clear()
    }
}