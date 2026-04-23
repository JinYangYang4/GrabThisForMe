package com.example.grabthisforme.activity.mainactivity.core.navigation

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
class AppNavigator(
    private val context: Context,
    private val manager: FragmentManager,
    private val containerId: Int
) : FragmentNavigator(context, manager, containerId) {
    private val fragmentCache = HashMap<String, Fragment>()
    private val cachedFragments = setOf(
        "com.example.grabthisforme.activity.communityFragment.view.FragmentCommunity",
        "com.example.grabthisforme.activity.informationFragment.view.FragmentInformation",
        "com.example.grabthisforme.activity.myFragment.FragmentMy",
        "com.example.grabthisforme.activity.homeFragment.view.FragmentHomeContainer",
        "com.example.grabthisforme.activity.homeFragment.view.FragmentHome",
        "com.example.grabthisforme.activity.homeFragment.view.FragmentHome1"
    )

    override fun instantiateFragment(
        context: Context,
        fragmentManager: FragmentManager,
        className: String,
        args: Bundle?
    ): Fragment {
        // 打印 classname
        Log.d("AppNavigator", "instantiateFragment called for class: $className")

        // 如果是需要缓存的片段
        if (className in cachedFragments) {
            val cached = fragmentCache[className]
            if (cached != null) {
                cached.arguments = args
                return cached
            }
        }

        // 对于不需要缓存的片段，使用默认的方式实例化
        val fragment = super.instantiateFragment(context, fragmentManager, className, args)
        Log.d("AppNavigator", "instantiateFragment: 没有缓存")

        // 需要缓存的片段加入缓存
        if (className in cachedFragments) {
            fragmentCache[className] = fragment
            Log.i("AppNavigator", "Instantiating and caching: $className")
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
            fragment = instantiateFragment(context, manager,
                destination.className, entry.arguments)
            manager.beginTransaction().add(containerId, fragment, tag).commitNow()
        }

        // 核心修改：只隐藏缓存列表中的Fragment，而非所有Fragment
        manager.beginTransaction().apply {
            // 遍历所有Fragment，只隐藏缓存列表内的非目标Fragment
            manager.fragments.forEach { frag ->
                // 判断当前Fragment是否在缓存列表中
                val isInCachedList = frag.javaClass.name in cachedFragments
                // 只隐藏缓存列表中的、且不是当前要显示的Fragment
                if (isInCachedList && frag != fragment) {
                    hide(frag)
                    Log.d("AppNavigator", "隐藏缓存Fragment: ${frag.javaClass.simpleName}")
                }
            }
            // 显示目标Fragment
            show(fragment)
            Log.d("AppNavigator", "显示目标Fragment: ${fragment.javaClass.simpleName}")
        }.commitNow()

        state.push(entry)
    }

    override fun popBackStack(popUpTo: NavBackStackEntry, savedState: Boolean) {
        super.popBackStack(popUpTo, savedState)
        Log.d("AppNavigator", "Removed non-cached fragment:")

        val visible = manager.fragments.lastOrNull { it.isAdded && !it.isHidden } ?: run {
            val last = manager.fragments.lastOrNull()
            last ?: return
        }

        if (visible !in fragmentCache.values) {
            manager.beginTransaction().remove(visible).commitNow()
            Log.d("AppNavigator", "Removed non-cached fragment: ${visible.javaClass.simpleName}")
        }

        // 同样修改popBackStack中的隐藏逻辑，只处理缓存Fragment
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