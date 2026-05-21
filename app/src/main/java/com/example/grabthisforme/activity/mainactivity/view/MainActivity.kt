package com.example.grabthisforme.activity.mainactivity.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat

import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.mainactivity.adapter.RVRecentStoreAdapter
import com.example.grabthisforme.activity.mainactivity.adapter.RVRecentlyUserAdapter
import com.example.grabthisforme.activity.mainactivity.core.navigation.AppNavigator
import com.example.grabthisforme.activity.mainactivity.viewmodel.MainViewModel
import com.example.grabthisforme.activity.fragment_misc.default_entry.view.BlankFragment
import com.example.grabthisforme.activity.fragment_misc.default_entry.view.BlankFragmentDirections
import com.example.grabthisforme.databinding.ActivityMainBinding
import com.example.grabthisforme.model.store.domain.Store
import com.example.grabthisforme.model.user.domain.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
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
        "com.example.grabthisforme.activity.myfragment.view.FragmentMy",
        "com.example.grabthisforme.activity.homeFragment.view.FragmentHomeContainer"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        initNavigationBottom()
        drawerAnimation()
        viewModelObserve()
        waitViewDrawComplete()
        initRvUser()
        initRvStore()
        nestedScrollviewTouchListener()
        initHandleSidebarClick()
    }
    fun viewModelObserve(){
        viewModel.drawerOpenState.observe(this){ openState ->
            if (openState){
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }else{
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }
        viewModel.openNewFragment.observe(this){value ->
            isOpenNewFragment = value
        }
        viewModel.currentUser.observe(this){user ->
            Glide.with(this)
                .load(user?.headPic)
                .placeholder(R.drawable.cat)
                .error(R.drawable.cat)
                .into(binding.ivHeadPic)
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
            viewModel.selectTab(1)
        }
        binding.llHome.setOnClickListener {
            navHostFragment.navController.navigate(R.id.fragmentHomeContainer)
            viewModel.selectTab(0)
        }
        binding.llInformation.setOnClickListener {
            navHostFragment.navController.navigate(R.id.fragmentInformation)
            viewModel.selectTab(2)
        }
        binding.llMy.setOnClickListener {
            navHostFragment.navController.navigate(R.id.fragmentMy)
            viewModel.selectTab(3)
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
                    if (viewModel.drawerOpenState.value == true){
                        viewModel.drawerOpenStateToClose()
                        return
                    }
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
                                viewModel.selectTab(0)
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
    fun initHandleSidebarClick(){
        binding.llRegisterStoreOwner.setOnClickListener {
            intentToMiscFragment(R.id.action_blankFragment_to_fragmentMyStore)
        }
        binding.llSetBtn.setOnClickListener {
            intentToMiscFragment(R.id.action_blankFragment_to_setFragment)
        }
        binding.llHistoryOrderBtn.setOnClickListener {
            intentToMiscFragment_Order_ac(2)
        }
        binding.llCouponBtn.setOnClickListener {
            intentToMiscFragment(R.id.action_blankFragment_to_couponFragment)
        }

    }

    fun intentToMiscFragment(id : Int){
        val navController = navNewFragment.navController
        navController.navigate(id)
        if (viewModel.drawerOpenState.value == true){
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }

    }
    fun intentToMiscFragment_Order_ac(ac : Int){
        val navController = navNewFragment.navController
        val action = BlankFragmentDirections.actionBlankFragmentToOrderExecutorFragment(ac)
        navController.navigate(action)
        if (viewModel.drawerOpenState.value == true){
            binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
    }
    fun NewNavController_navgite(action: NavDirections){
        val navController = navNewFragment.navController.navigate(action)
    }
    fun innerBottomBar(){
        viewModel.openNewFragment_ture()
    }
    fun showBottomBar(){
        viewModel.openNewFragment_false()
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
