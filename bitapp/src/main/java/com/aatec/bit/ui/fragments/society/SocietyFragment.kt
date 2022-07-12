package com.aatec.bit.ui.fragments.society

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aatec.bit.R
import com.aatec.bit.databinding.FragmentSocietyBinding
import com.aatec.bit.ui.custom_views.DividerItemDecorationNoLast
import com.aatec.bit.utils.MainStateEvent
import com.aatec.bit.utils.addMenuHost
import com.aatec.core.utils.changeStatusBarToolbarColor
import com.aatec.core.utils.onScrollColorChange
import com.aatec.core.utils.showSnackBar
import com.aatec.core.data.network.society.SocietyNetworkEntity
import com.aatec.core.utils.DataState
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SocietyFragment : Fragment(R.layout.fragment_society) {

    private val binding: FragmentSocietyBinding by viewBinding()
    private val viewModel: SocietyViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, /* forward= */ true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, /* forward= */ false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        val societyAdapter = SocietyAdapter { society, view ->
            setOnSocietyClickListener(society, view)
        }
        binding.apply {
            showSociety.apply {
                adapter = societyAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(
                    DividerItemDecorationNoLast(
                        requireContext(),
                        LinearLayoutManager.VERTICAL
                    ).apply {
                        setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider))
                    }
                )
            }
        }
        societyAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        viewModel.setStateEvent(MainStateEvent.GetData)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.dataState.observe(viewLifecycleOwner) { dataState ->
                when (dataState) {
                    DataState.Loading -> {

                    }
                    is DataState.Success -> {
                        societyAdapter.submitList(dataState.data)
                    }
                    DataState.Empty -> {

                    }
                    is DataState.Error -> {
                        binding.root.showSnackBar(dataState.exception.message.toString(), -1)
                    }
                }
            }
        }
        detectScroll()
        restoreColor()
        addMenuHost()
    }

    private fun restoreColor() {
//        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
//            viewModel.isColored.collect {
//                if (it) {
//                    activity?.changeStatusBarToolbarColor(
//                        R.color.bottom_nav
//                    )
//                } else {
//                    activity?.changeStatusBarToolbarColor(
//                        R.color.MainColor
//                    )
//                }
//            }
//        }
    }

    private fun setOnSocietyClickListener(society: SocietyNetworkEntity, view: View) {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        val action =
            SocietyFragmentDirections.actionSocietyFragmentToEventSocietyDescriptionFragment(
                society = society,
                title = society.name
            )
        findNavController().navigate(action)
    }

    /**
     * @since 4.0.4
     * @author Ayaan
     */
    private fun detectScroll() {
        activity?.onScrollColorChange(binding.nestedScrollViewSociety, {
            activity?.changeStatusBarToolbarColor(
                R.id.toolbar,
                com.google.android.material.R.attr.colorSurface
            )
        }, {
            activity?.changeStatusBarToolbarColor(
                R.id.toolbar,
                R.attr.bottomBar
            )
        })
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }
}