package com.devjeong.todolist_study.view.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.devjeong.todolist_study.BaseFragment
import com.devjeong.todolist_study.databinding.FragmentSearchBinding

class SearchFragment : BaseFragment<FragmentSearchBinding>() {
    private var query: String? = null
    private lateinit var groupRecyclerViews: MutableList<RecyclerView> // 그룹별 RecyclerView 저장 리스트

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSearchBinding {
        return FragmentSearchBinding.inflate(inflater, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            query = it.getString("query")
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        Navigation.setViewNavController(requireView(), navController)

        Log.d("query", query ?: "Query is null")
        binding.searchTitle.text = "$query 로 검색된 결과"
    }
}