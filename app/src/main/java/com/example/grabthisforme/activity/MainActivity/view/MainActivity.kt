package com.example.grabthisforme.activity.MainActivity.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.DirectAction
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat

import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import androidx.navigation.NavGraphNavigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.MainActivity.adapter.RVRecentStoreAdapter
import com.example.grabthisforme.activity.MainActivity.adapter.RVRecentlyUserAdapter
import com.example.grabthisforme.activity.MainActivity.core.navigation.AppNavigator
import com.example.grabthisforme.activity.MainActivity.viewModel.MainViewModel
import com.example.grabthisforme.activity.fragment_misc.all_executor.view.OrderExecutorFragmentArgs
import com.example.grabthisforme.activity.fragment_misc.default_entry.view.BlankFragment
import com.example.grabthisforme.activity.fragment_misc.default_entry.view.BlankFragmentDirections
import com.example.grabthisforme.databinding.ActivityMainBinding
import com.example.grabthisforme.model.store.Store
import com.example.grabthisforme.model.user.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import kotlin.math.log

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private var isTouching = false
    private var originalImgHeight = 0
    private var isViewInit = false
    private val EXPAND_RATIO = 0.4f
    private val MAX_PULL_DISTANCE = 800f
    private var startTouchY = 0f
    private val targetScale = 0.97f
    private var currentPullDistance = 0f
    private var lastBackPressTime = 0L
    private var isOrderBottomSheetFragment = false
    private var isOpenNewFragment = false

    private lateinit var backCallback: OnBackPressedCallback
    private lateinit var navHostFragment : NavHostFragment
    private lateinit var navNewFragment : NavHostFragment
    private val cachedFragments = setOf(
        "com.example.grabthisforme.activity.communityFragment.view.FragmentCommunity",
        "com.example.grabthisforme.activity.informationFragment.view.FragmentInformation",
        "com.example.grabthisforme.activity.myFragment.FragmentMy",
        "com.example.grabthisforme.activity.homeFragment.view.FragmentHomeContainer"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        initNavigationBottom()
        drawerAnimation()
        viewModelObserve()
        waitViewDrawComplete()
        bottomUiAlpha(binding.llHome)
        initRvUser()
        initRvStore()
        nestedScrollviewTouchListener()
    }
    fun viewModelObserve(){
        viewModel.drawerOpenState.observe(this){ openState ->
            if (openState){
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        viewModel.page.observe(this){value ->
            if (value == 0){
                binding.llMenuPeopleBack.background = ContextCompat.
                getDrawable(this, R.drawable.bg_arc_gradient)
                binding.llMenuShopBack.background = ContextCompat.
                getDrawable(this,R.drawable.bg_arc_gradient)
                binding.llMenuToolBack.background = ContextCompat.
                getDrawable(this,R.drawable.bg_arc_gradient)
                binding.llMenuBack1.background = ContextCompat.
                getDrawable(this,R.drawable.bg_arc_gradient)
            }else{
                binding.llMenuPeopleBack.background = ContextCompat.
                getDrawable(this, R.drawable.bg_arc_gradient_green)
                binding.llMenuShopBack.background = ContextCompat.
                getDrawable(this,R.drawable.bg_arc_gradient_green)
                binding.llMenuToolBack.background = ContextCompat.
                getDrawable(this,R.drawable.bg_arc_gradient_green)
                binding.llMenuBack1.background = ContextCompat.
                getDrawable(this,R.drawable.bg_arc_gradient_green)
            }
        }
        viewModel.openNewFragment.observe(this){value ->
            if (value){
                isOpenNewFragment = true
            }else{
                isOpenNewFragment = false
            }
        }

    }
    fun initNavigationBottom(){
        navHostFragment = supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment
        navHostFragment.navController.navigatorProvider.addNavigator(
            AppNavigator(
                this,
                supportFragmentManager,
                binding.navHostFragment.id
            )
        )
        navHostFragment.navController.setGraph(R.navigation.nav_graph)
        binding.llCommunity.setOnClickListener {
            navHostFragment.navController.navigate(R.id.fragmentCommunity)
            bottomUiAlpha(binding.llCommunity)
        }
        binding.llHome.setOnClickListener {
            navHostFragment.navController.navigate(R.id.fragmentHomeContainer)
            bottomUiAlpha(binding.llHome)
        }
        binding.llInformation.setOnClickListener {
            navHostFragment.navController.navigate(R.id.fragmentInformation)
            bottomUiAlpha(binding.llInformation)
        }
        binding.llMy.setOnClickListener {
            navHostFragment.navController.navigate(R.id.fragmentMy)
            bottomUiAlpha(binding.llMy)
        }

        //零散片段
        navNewFragment = supportFragmentManager.findFragmentById(binding.navNewFragment.id) as NavHostFragment
        navNewFragment.navController.setGraph(R.navigation.nav_new)

        val navController = navNewFragment.navController
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (BlankFragment::class.java.name.contains(destination.label.toString())) {
                isOrderBottomSheetFragment = true
            } else {
                isOrderBottomSheetFragment = false
            }
        }
            backCallback = object : OnBackPressedCallback(true){
                override fun handleOnBackPressed() {

                    if (isOrderBottomSheetFragment && isOpenNewFragment){
                        showBottomBar()
                    }else if (isOpenNewFragment){
                        isEnabled = false
                        onBackPressed()
                        lifecycleScope.launch {
                            delay(200)
                            isEnabled = true
                        }
                    } else{
                        val currentFragmentClass = navHostFragment.navController.currentDestination?.let { destination ->
                            if (destination is FragmentNavigator.Destination) {
                                destination.className
                            } else {
                                null
                            }
                        }

                        if (currentFragmentClass in cachedFragments) {
                            Log.d("test11", "initNavigationBottom: ${currentFragmentClass in cachedFragments}")
                            val startId = navHostFragment.navController.graph.startDestinationId
                            val currentId = navHostFragment.navController.currentDestination?.id

                            val now = System.currentTimeMillis()
                            if (currentId != null && currentId != startId) {
                                navHostFragment.navController.navigate(startId)
                                bottomUiAlpha(binding.llHome)
                            } else if (now - lastBackPressTime < 2000) {
                                isEnabled = false
                                onBackPressed()
                                lifecycleScope.launch {
                                    delay(200)
                                    isEnabled = true
                                }

                            }else{
                                lastBackPressTime = now
                                Toast.makeText(this@MainActivity, "再次返回退出", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            isEnabled = false
                        }
                    }
                }
            }
            onBackPressedDispatcher.addCallback(this,backCallback)


    }

    fun intentToMiscFragment(id : Int){
        val navController = navNewFragment.navController
        navController.navigate(id)
    }
    fun intentToMiscFragment_ac(ac : Int){
        val navController = navNewFragment.navController
        val action = BlankFragmentDirections.actionBlankFragmentToOrderExecutorFragment(ac)
        navController.navigate(action)
    }
    fun bottomUiAlpha(targetView : View){
        binding.llMy.alpha = 0.5f
        binding.llHome.alpha = 0.5f
        binding.llCommunity.alpha = 0.5f
        binding.llInformation.alpha = 0.5f
        targetView.alpha = 1f
    }
    fun innerBottomBar(){
        viewModel.openNewFragment_ture()
        binding.navNewFragment.visibility = View.VISIBLE
        binding.bottomBar.visibility = View.GONE
        binding.navHostFragment.visibility = View.GONE
    }
    fun showBottomBar(){
        viewModel.openNewFragment_false()
        binding.navNewFragment.visibility = View.GONE
        binding.bottomBar.visibility = View.VISIBLE
        binding.navHostFragment.visibility = View.VISIBLE
    }

    fun initRvUser(){
        val adapter1 = RVRecentlyUserAdapter(){}
        val gridlayoutManager = GridLayoutManager(this,3)
        binding.rvUser.apply {
            layoutManager = gridlayoutManager
            adapter = adapter1
        }
        val templateUser = User(name = "李华", id = 1, headPic = "")
        val recentUserList = User.createVirtualUsers(templateUser,10)
        adapter1.submitList(recentUserList)
    }
    fun initRvStore(){
        val adapter2 = RVRecentStoreAdapter(){}
        val gridLayoutManager = GridLayoutManager(this,3)
        binding.rvStore.apply{
            layoutManager = gridLayoutManager
            adapter = adapter2
        }
        val templateStore = Store.createVirtualStores()
        adapter2.submitList(templateStore)
    }
    fun drawerAnimation(){
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener(){
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                val mainContent = binding.drawerLayout.getChildAt(0) // 主视图（第一个子布局）
                val currentScale = 1 - (1 - targetScale) * slideOffset

                mainContent.scaleX = currentScale
                mainContent.scaleY = currentScale

            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                val mainContent = binding.drawerLayout.getChildAt(0)
                mainContent.scaleX = 1f
                mainContent.scaleY = 1f
                mainContent.translationX = 0f
                viewModel.drawerOpenStateToClose()
            }
        })

    }
    @SuppressLint("ClickableViewAccessibility")
    fun nestedScrollviewTouchListener(){
        binding.nestedScrollViewMenu.setOnTouchListener { _, event ->
            if (!isViewInit) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (binding.nestedScrollViewMenu.scrollY <= 0) {
                        isTouching = true
                        startTouchY = event.rawY
                    }
                    false
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!isTouching) return@setOnTouchListener false

                    val touchOffset = event.rawY - startTouchY
                    currentPullDistance = Math.min(touchOffset * EXPAND_RATIO, MAX_PULL_DISTANCE)
                    updateImageViewSize(currentPullDistance)
                    false
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isTouching && currentPullDistance > 0) {
                        isTouching = false
                        resetImgWithAnimation()
                    }
                    isTouching = false
                    false
                }

                else -> false
            }
        }
    }
    private fun waitViewDrawComplete() {
        binding.ivBackCharactor.post {
            originalImgHeight = binding.ivBackCharactor.height
            isViewInit = true
        }
    }
    private fun updateImageViewSize(pullDistance: Float) {
        if (pullDistance <= 0 || originalImgHeight == 0) return
        val newHeight = originalImgHeight + pullDistance.toInt()
        val layoutParams = binding.ivBackCharactor.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.height = newHeight
        binding.ivBackCharactor.layoutParams = layoutParams
        binding.ivBackCharactor.requestLayout()
    }
    private fun resetImgWithAnimation() {
        val startDistance = currentPullDistance
        if (startDistance <= 0) return

        ValueAnimator.ofFloat(startDistance, 0f).apply {
            duration = 300
            interpolator = android.view.animation.DecelerateInterpolator()

            addUpdateListener { animation ->
                val currentDistance = animation.animatedValue as Float
                updateImageViewSize(currentDistance)
                currentPullDistance = currentDistance
            }

            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    currentPullDistance = 0f
                }
            })

            start()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
    }
}