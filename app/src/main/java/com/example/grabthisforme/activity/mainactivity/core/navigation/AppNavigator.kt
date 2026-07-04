package com.example.grabthisforme.activity.mainactivity.core.navigation

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.Navigator.Name
import androidx.navigation.fragment.FragmentNavigator

@Name("keep_state_fragment")
class AppNavigator(
    private val context: Context,
    private val manager: FragmentManager,
    private val containerId: Int
) : FragmentNavigator(context, manager, containerId) {
    private val fragmentCache = HashMap<String, Fragment>()
    private val cachedFragments = setOf(
        "com.example.grabthisforme.activity.communityFragment.view.FragmentCommunity",
        "com.example.grabthisforme.activity.informationFragment.view.FragmentInformation",
        "com.example.grabthisforme.activity.myfragment.view.FragmentMy",
        "com.example.grabthisforme.activity.homeFragment.view.FragmentHomeContainer",
        "com.example.grabthisforme.activity.homeFragment.view.FragmentHome",
        "com.example.grabthisforme.activity.homeFragment.view.FragmentHome1"
    )

    private fun createFragment(className: String, args: Bundle?): Fragment {
        if (className in cachedFragments) {
            fragmentCache[className]?.let { cached ->
                cached.arguments = args
                return cached
            }
        }

        val fragment = manager.fragmentFactory.instantiate(context.classLoader, className).apply {
            arguments = args
        }
        if (className in cachedFragments) {
            fragmentCache[className] = fragment
        }
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
            fragment = createFragment(destination.className, entry.arguments)
            manager.beginTransaction().add(containerId, fragment, tag).commitNow()
        }

        manager.beginTransaction().apply {
            manager.fragments.forEach { frag ->
                val isInCachedList = frag.javaClass.name in cachedFragments
                if (isInCachedList && frag != fragment) {
                    hide(frag)
                }
            }
            show(fragment)
        }.commitNow()

        state.push(entry)
    }

    override fun popBackStack(popUpTo: NavBackStackEntry, savedState: Boolean) {
        super.popBackStack(popUpTo, savedState)
        val visible = manager.fragments.lastOrNull { it.isAdded && !it.isHidden } ?: run {
            manager.fragments.lastOrNull() ?: return
        }

        if (visible !in fragmentCache.values) {
            manager.beginTransaction().remove(visible).commitNow()
        }

        manager.beginTransaction().apply {
            manager.fragments.forEach { frag ->
                val isInCachedList = frag.javaClass.name in cachedFragments
                if (isInCachedList && frag != visible) {
                    hide(frag)
                }
            }
            show(visible)
        }.commitNow()
    }

    fun clearCache() {
        fragmentCache.clear()
    }
}
