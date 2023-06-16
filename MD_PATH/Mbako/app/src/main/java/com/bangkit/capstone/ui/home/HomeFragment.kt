package com.bangkit.capstone.ui.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.filter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bangkit.capstone.R
import com.bangkit.capstone.databinding.FragmentHomeBinding
import com.bangkit.capstone.ui.home.home.HomeAdapter
import com.bangkit.capstone.ui.home.home.ItemsLoadingAdapter
import com.bangkit.capstone.ui.home.sell.SellActivity
import java.util.*
import kotlin.concurrent.schedule


class HomeFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val rvAdapter = HomeAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val mainViewModel = (activity as HomeActivity).getStoryViewModel()
        mainViewModel.items.observe(viewLifecycleOwner) { data ->
            val filteredData = data.filter { it.quantity > 0 }
            rvAdapter.submitData(lifecycle, filteredData)
        }
        binding.progressBar.visibility = View.VISIBLE
        rvAdapter.addLoadStateListener { loadState ->
            val isListEmpty = loadState.refresh is LoadState.NotLoading && rvAdapter.itemCount == 0
            binding.rvItems.isVisible = !isListEmpty
            binding.progressBar.isVisible = loadState.refresh is LoadState.Loading

            if (loadState.refresh is LoadState.NotLoading) {
                binding.progressBar.visibility = View.GONE
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            onRefresh()
        }

        binding.rvItems.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 2)
            isNestedScrollingEnabled = false
            adapter = rvAdapter
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.swipeRefresh.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                if (scrollY > oldScrollY) {
                    binding.btnSell.hide() // Menyembunyikan FAB ketika scroll ke atas
                } else {
                    binding.btnSell.show() // Menampilkan kembali FAB ketika scroll ke bawah
                }
            }
        }

        binding.btnSell.setOnClickListener{
            val intent = Intent(requireContext(), SellActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }


    override fun onRefresh() {
        binding.swipeRefresh.isRefreshing = true
        rvAdapter.refresh()
        Handler(Looper.getMainLooper()).postDelayed({
            binding.swipeRefresh.isRefreshing = false
            binding.rvItems.smoothScrollToPosition(0)
        }, 2000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}