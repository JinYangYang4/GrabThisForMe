package com.example.grabthisforme.activity.fragment_misc.storeFragment.view

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.storeFragment.adpter.CategoryManageRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.storeFragment.adpter.CategorySortRecyclerViewAdapter
import com.example.grabthisforme.databinding.CategoryManagerBottomDialogBinding
import com.example.grabthisforme.ui.liquidglass.components.LiquidBottomTab
import com.example.grabthisforme.ui.liquidglass.rememberShapeBitmapPainter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import android.graphics.Canvas
import android.view.KeyEvent
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.grabthisforme.ui.liquidglass.components.LiquidBottomTabs
import com.example.grabthisforme.ui.liquidglass.dialog.LiquidMessageDialogFragment

class CategoryManagerBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: CategoryManagerBottomDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var renameAdapter: CategoryManageRecyclerViewAdapter
    private lateinit var deleteAdapter: CategoryManageRecyclerViewAdapter
    private lateinit var sortAdapter: CategorySortRecyclerViewAdapter

    private val categoryList: MutableList<String> = mutableListOf()
    private var renameTargetIndex: Int = RecyclerView.NO_POSITION
    private var currentTabMode by mutableStateOf(TabMode.ADD)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTransparentTheme)
        isCancelable = false
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CategoryManagerBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        initCategoryData()
        initRecyclerViews()
        initClosePromptResultListener()
        prepareTabContentContainers()
        initClickEvents()
        prepareTabContentContainers()
        initTabGroup()
        showTab(currentTabMode)
    }

    override fun onStart() {
        super.onStart()
        val bottomSheetDialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
        val behavior = bottomSheetDialog.behavior

        behavior.state = BottomSheetBehavior.STATE_COLLAPSED // 正常展开
        behavior.isDraggable =false

        bottomSheetDialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        bottomSheetDialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (event.action == KeyEvent.ACTION_UP) {
                    showSaveChangesPrompt()
                }
                true
            } else {
                false
            }
        }
    }
    private fun initClosePromptResultListener() {
        childFragmentManager.setFragmentResultListener(
            CLOSE_PROMPT_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, result ->
            val shouldSave = result.getBoolean(LiquidMessageDialogFragment.RESULT_CONFIRMED, false)
            if (shouldSave) {
                dispatchCategoryResult(categoryList)
            }
            dismiss()
        }
    }

    private fun showSaveChangesPrompt() {
        if (childFragmentManager.findFragmentByTag(LiquidMessageDialogFragment.TAG) != null) return
        LiquidMessageDialogFragment.show(
            fragmentManager = childFragmentManager,
            title = "保存更改",
            message = "是否保存当前分类管理的更改",
            positiveText = "确认更改",
            negativeText = "不保存",
            requestKey = CLOSE_PROMPT_REQUEST_KEY,
            cancelable = false
        )
    }

    private fun initCategoryData() {
        val initialCategories = arguments?.getStringArrayList(ARG_CATEGORY_LIST).orEmpty()
        categoryList.clear()
        categoryList.addAll(
            initialCategories.map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
        )
        if (!categoryList.contains(CATEGORY_ALL)) {
            categoryList.add(0, CATEGORY_ALL)
        }
        if (!categoryList.contains(CATEGORY_UNCLASSIFIED)) {
            val insertIndex = if (categoryList.firstOrNull() == CATEGORY_ALL) 1 else 0
            categoryList.add(insertIndex, CATEGORY_UNCLASSIFIED)
        }
    }

    private fun initRecyclerViews() {
        renameAdapter = CategoryManageRecyclerViewAdapter(actionText = "重命名") { category, position ->
            renameAdapter.updateSelectedPosition(position)
            renameTargetIndex = categoryList.indexOf(category)
            binding.llRenameInputContainer.visibility = View.VISIBLE
            binding.tvRenameTarget.text = "当前分类：$category"
            binding.etRenameCategoryName.setText(category)
            binding.etRenameCategoryName.setSelection(binding.etRenameCategoryName.text?.length ?: 0)
        }

        deleteAdapter = CategoryManageRecyclerViewAdapter(actionText = "删除") { category, _ ->
            if (isProtectedCategory(category)) {
                Toast.makeText(requireContext(), "该分类暂不支持删除", Toast.LENGTH_SHORT).show()
                return@CategoryManageRecyclerViewAdapter
            }
            categoryList.remove(category)
            refreshCategoryAdapters()
            binding.llRenameInputContainer.visibility = View.GONE
            renameTargetIndex = RecyclerView.NO_POSITION
            Toast.makeText(requireContext(), "已删除，商品将转入未分类", Toast.LENGTH_SHORT).show()
        }

        sortAdapter = CategorySortRecyclerViewAdapter()

        binding.rvRenameCategory.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = renameAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }

        binding.rvDeleteCategory.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = deleteAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }

        binding.rvSortCategory.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = sortAdapter
            setHasFixedSize(true)
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags = when (recyclerView.layoutManager) {
                    is GridLayoutManager -> {
                        ItemTouchHelper.UP or ItemTouchHelper.DOWN or
                            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    }

                    else -> ItemTouchHelper.UP or ItemTouchHelper.DOWN
                }
                return makeMovementFlags(dragFlags, 0)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                if (viewHolder.itemViewType != target.itemViewType) return false
                val from = viewHolder.bindingAdapterPosition
                val to = target.bindingAdapterPosition
                if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false
                sortAdapter.moveItem(from, to)
                applySortedResult(sortAdapter.getCurrentItems())
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

            override fun isLongPressDragEnabled(): Boolean = true
        })
        itemTouchHelper.attachToRecyclerView(binding.rvSortCategory)
        refreshCategoryAdapters()
    }

    private fun initClickEvents() {
        binding.tvCancel.setOnClickListener { dismiss() }
        binding.tvConfirm.setOnClickListener {
            dispatchCategoryResult(categoryList)
            dismiss()
        }

        binding.tvAddCategoryCancel.setOnClickListener {
            binding.etNewCategoryName.text?.clear()
        }

        binding.tvAddCategorySubmit.setOnClickListener {
            val categoryName = binding.etNewCategoryName.text?.toString().orEmpty().trim()
            if (categoryName.isBlank()) {
                Toast.makeText(requireContext(), "分类名不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (categoryList.contains(categoryName)) {
                Toast.makeText(requireContext(), "分类名已存在", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            categoryList.add(categoryName)
            binding.etNewCategoryName.text?.clear()
            refreshCategoryAdapters()
        }

        binding.tvRenameCancel.setOnClickListener {
            binding.llRenameInputContainer.visibility = View.GONE
            renameTargetIndex = RecyclerView.NO_POSITION
            binding.etRenameCategoryName.text?.clear()
        }

        binding.tvRenameSubmit.setOnClickListener {
            if (renameTargetIndex !in categoryList.indices) return@setOnClickListener
            val newName = binding.etRenameCategoryName.text?.toString().orEmpty().trim()
            if (newName.isBlank()) {
                Toast.makeText(requireContext(), "分类名不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (categoryList.contains(newName) && categoryList[renameTargetIndex] != newName) {
                Toast.makeText(requireContext(), "分类名已存在", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isProtectedCategory(categoryList[renameTargetIndex])) {
                Toast.makeText(requireContext(), "该分类暂不支持重命名", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            categoryList[renameTargetIndex] = newName
            binding.llRenameInputContainer.visibility = View.GONE
            renameTargetIndex = RecyclerView.NO_POSITION
            refreshCategoryAdapters()
        }
    }


    private fun initTabGroup() {
        binding.llTabGroup.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.llTabGroup.setContent {
            BottomTabsContent()
        }
    }
    @Composable
    private fun BottomTabsContent(){
        val backdrop = rememberLayerBackdrop()
        Image(
            painter = rememberShapeBitmapPainter(R.drawable.bg_round_stripe),
//            painter = painterResource(R.drawable.ic_back_charactor2),
            contentDescription = null,
            modifier = Modifier
                .layerBackdrop(backdrop)
                .height(80.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        )


        LiquidBottomTabs(
            selectedTabIndex = { currentTabMode.ordinal },
            onTabSelected = { index ->
                TabMode.values().getOrNull(index)?.let{showTab(it,false)}
            },
            backdrop = backdrop,
            tabsCount = TabMode.values().size,
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 20.dp),
            onDragProgress = {
                TabMode.values().getOrNull(it)?.let{(showTab(it,true))}
            }
        ) {
            TabMode.values().forEach { tabMode ->
                LiquidBottomTab(
                    onClick = { showTab(tabMode,false) }
                ) {
                    BasicText(
                        tabMode.title,
                        style = TextStyle(ComposeColor.Black, 12f.sp)
                    )
                }
            }
        }

    }

    private fun prepareTabContentContainers() {
        val contentViews = listOf(
            binding.llAddCategoryContent,
            binding.llRenameCategoryContent,
            binding.llDeleteCategoryContent,
            binding.llSortCategoryContent
        )
        contentViews.forEach { content ->
            content.visibility = View.VISIBLE
            content.alpha = 0f
            content.isEnabled = false
            content.isClickable = false
            content.isFocusable = false
        }
    }

    private fun setTabContentVisible(content: View, visible: Boolean) {
        content.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        content.alpha = if (visible) 1f else 0f
        content.isEnabled = visible
        content.isClickable = visible
        content.isFocusable = visible
    }

    private fun applySortedResult(sorted: List<String>) {
        if (categoryList == sorted) return
        categoryList.clear()
        categoryList.addAll(sorted)
        renameAdapter.submitList(sorted)
        deleteAdapter.submitList(sorted)
    }
    private fun refreshCategoryAdapters() {
        renameAdapter.submitList(categoryList.toList())
        deleteAdapter.submitList(categoryList.toList())
        sortAdapter.submitList(categoryList.toList())
    }

    private fun showTab(tabMode: TabMode,comeFromDrag : Boolean = true) {
        Log.d("test11", "showTab: $comeFromDrag")
        if (!comeFromDrag){
            currentTabMode = tabMode
        }
        setTabContentVisible(binding.llAddCategoryContent, tabMode == TabMode.ADD)
        setTabContentVisible(binding.llRenameCategoryContent, tabMode == TabMode.RENAME)
        setTabContentVisible(binding.llDeleteCategoryContent, tabMode == TabMode.DELETE)
        setTabContentVisible(binding.llSortCategoryContent, tabMode == TabMode.SORT)
        if (tabMode != TabMode.RENAME) {
            binding.llRenameInputContainer.visibility = View.GONE
        }
    }

    private fun isProtectedCategory(category: String): Boolean {
        return category == CATEGORY_ALL || category == CATEGORY_UNCLASSIFIED
    }

    private fun dispatchCategoryResult(categories: List<String>) {
        parentFragmentManager.setFragmentResult(
            REQUEST_KEY,
            bundleOf(RESULT_KEY_CATEGORY_LIST to ArrayList(categories))
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val REQUEST_KEY = "category_manager_request_key"
        const val RESULT_KEY_CATEGORY_LIST = "category_manager_result_category_list"

        private const val ARG_CATEGORY_LIST = "arg_category_list"
        private const val CATEGORY_ALL = "全部"
        private const val CATEGORY_UNCLASSIFIED = "未分类"
        private const val CLOSE_PROMPT_REQUEST_KEY = "category_manager_close_prompt_request_key"

        fun newInstance(categoryList: ArrayList<String>): CategoryManagerBottomDialogFragment {
            return CategoryManagerBottomDialogFragment().apply {
                arguments = bundleOf(ARG_CATEGORY_LIST to categoryList)
            }
        }
    }

    private enum class TabMode(val title: String) {
        ADD("新增"),
        RENAME("重命名"),
        DELETE("删除"),
        SORT("排序")
    }
}
