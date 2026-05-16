package com.example.grabthisforme.activity.fragment_misc.storeFragment.view

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.storeFragment.adpter.CategoryManageRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.storeFragment.adpter.CategorySortRecyclerViewAdapter
import com.example.grabthisforme.databinding.CategoryManagerBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.MaterialShapeDrawable

class CategoryManagerBottomDialogFragment : BottomSheetDialogFragment() {

    private var _binding: CategoryManagerBottomDialogBinding? = null
    private val binding get() = _binding!!

    private lateinit var renameAdapter: CategoryManageRecyclerViewAdapter
    private lateinit var deleteAdapter: CategoryManageRecyclerViewAdapter
    private lateinit var sortAdapter: CategorySortRecyclerViewAdapter

    private val categoryList: MutableList<String> = mutableListOf()
    private var renameTargetIndex: Int = RecyclerView.NO_POSITION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTransparentTheme)
    }
//
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return BottomSheetDialog(requireContext(), theme).apply {
//            setOnShowListener {
//                val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//                bottomSheet?.setBackgroundColor(Color.TRANSPARENT)
//                (bottomSheet?.background as? MaterialShapeDrawable)?.fillColor =
//                    ColorStateList.valueOf(Color.TRANSPARENT)
//            }
//        }
//    }


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
        initClickEvents()
        showTab(TabMode.ADD)
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
            itemAnimator = null
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
                sortAdapter.moveItem(from, to)
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
            dispatchCategoryResult(sortAdapter.currentList.toList())
            dismiss()
        }

        binding.tvTabAdd.setOnClickListener { showTab(TabMode.ADD) }
        binding.tvTabRename.setOnClickListener { showTab(TabMode.RENAME) }
        binding.tvTabDelete.setOnClickListener { showTab(TabMode.DELETE) }
        binding.tvTabSort.setOnClickListener { showTab(TabMode.SORT) }

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

    private fun refreshCategoryAdapters() {
        renameAdapter.submitList(categoryList.toList())
        deleteAdapter.submitList(categoryList.toList())
        sortAdapter.submitList(categoryList.toList())
    }

    private fun showTab(tabMode: TabMode) {
        binding.llAddCategoryContent.visibility = if (tabMode == TabMode.ADD) View.VISIBLE else View.GONE
        binding.llRenameCategoryContent.visibility = if (tabMode == TabMode.RENAME) View.VISIBLE else View.GONE
        binding.llDeleteCategoryContent.visibility = if (tabMode == TabMode.DELETE) View.VISIBLE else View.GONE
        binding.llSortCategoryContent.visibility = if (tabMode == TabMode.SORT) View.VISIBLE else View.GONE
        if (tabMode != TabMode.RENAME) {
            binding.llRenameInputContainer.visibility = View.GONE
        }
        updateTabStyle(binding.tvTabAdd, tabMode == TabMode.ADD)
        updateTabStyle(binding.tvTabRename, tabMode == TabMode.RENAME)
        updateTabStyle(binding.tvTabDelete, tabMode == TabMode.DELETE)
        updateTabStyle(binding.tvTabSort, tabMode == TabMode.SORT)
    }

    private fun updateTabStyle(tabView: TextView, isSelected: Boolean) {
        if (isSelected) {
            tabView.setBackgroundResource(R.drawable.bg_rounded_white)
            tabView.setTextColor(requireContext().getColor(R.color.orange_primary))
            tabView.setTypeface(null, android.graphics.Typeface.BOLD)
        } else {
            tabView.setBackgroundResource(android.R.color.transparent)
            tabView.setTextColor(requireContext().getColor(R.color.gray_800))
            tabView.setTypeface(null, android.graphics.Typeface.NORMAL)
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

        fun newInstance(categoryList: ArrayList<String>): CategoryManagerBottomDialogFragment {
            return CategoryManagerBottomDialogFragment().apply {
                arguments = bundleOf(ARG_CATEGORY_LIST to categoryList)
            }
        }
    }

    private enum class TabMode {
        ADD, RENAME, DELETE, SORT
    }
}
