package com.aatec.bit.ui.fragments.notice.description

import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.viewbinding.library.fragment.viewBinding
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.aatec.bit.NavGraphDirections
import com.aatec.bit.R
import com.aatec.bit.databinding.FragmentNoticeDetailBinding
import com.aatec.bit.ui.fragments.course.CourseFragment
import com.aatec.bit.ui.fragments.notice.ImageGridAdapter
import com.aatec.core.data.network.notice.Attach
import com.aatec.core.data.ui.notice.Notice3
import com.aatec.core.data.ui.notice.SendNotice3
import com.aatec.core.utils.*
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine

@AndroidEntryPoint
class NoticeDetailFragment : Fragment(R.layout.fragment_notice_detail) {

    private val viewModel: Notice3DescriptionViewModel by viewModels()
    private val binding: FragmentNoticeDetailBinding by viewBinding()
    private var isEmpty: Boolean = false
    private var hasAttach = false
    private lateinit var attach: List<Attach>
    private lateinit var notice3: Notice3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment
            duration = resources.getInteger(R.integer.duration_medium).toLong()
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(Color.TRANSPARENT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (myScrollViewerInstanceState != null) {
            binding.nestedScrollViewNotice.onRestoreInstanceState(CourseFragment.myScrollViewerInstanceState)
        }
        binding.root.transitionName = viewModel.path
        detectScroll()
        getNotice()
        setHasOptionsMenu(true)
    }

    private fun getNotice() = lifecycleScope.launchWhenStarted {
        viewModel.notice(viewModel.path!!).combine(
            viewModel.attach(viewModel.path!!)
        ) { notice, attach ->
            FullNotice(notice, attach)
        }.collectLatest { fullNotice ->
            when (fullNotice.notice) {
                DataState.Empty -> {}
                is DataState.Error -> {
                    if (fullNotice.notice.exception is NoItemFoundException) {
                        binding.textViewNoData.isVisible = true
                        binding.imageViewNoData.isVisible = true
                        binding.progressBarLinear.isVisible = false
                        isEmpty = true
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "${fullNotice.notice.exception}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                DataState.Loading -> {

                }
                is DataState.Success -> {
                    notice3 = fullNotice.notice.data
                    setViews(fullNotice.notice.data)
                }
            }
            when (fullNotice.attach) {
                DataState.Empty -> {}
                is DataState.Error -> {
                    if (fullNotice.attach.exception is NoItemFoundException) {
                        binding.attachmentRecyclerView.isVisible = false
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "${fullNotice.attach.exception}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                DataState.Loading -> {

                }
                is DataState.Success -> {
                    hasAttach = fullNotice.attach.data.isNotEmpty()
                    attach = fullNotice.attach.data
                    setAttach(fullNotice.attach.data)
                }
            }
        }
    }

    private fun setAttach(attach: List<Attach>) = binding.apply {
        val imageGridAdapter = ImageGridAdapter {
            navigateToImageView(it)
        }
        attachmentRecyclerView.apply {
            isVisible = true
            layoutManager = GridLayoutManager(
                requireContext(),
                MAX_SPAWN
            ).apply {
                spanSizeLookup = imageGridAdapter.variableSpanSizeLookup
            }
            adapter = imageGridAdapter
            imageGridAdapter.submitList(attachments = attach)
        }
    }

    private fun setViews(sendNotice: Notice3) {
        binding.apply {
            progressBarLinear.isVisible = false
            subjectTextView.text = sendNotice.title
            senderTextView.text = resources.getString(
                R.string.notice_sender,
                sendNotice.sender,
            )
            textViewDate.text =
                binding.root.context.resources.getString(
                    R.string.notice_date,
                    sendNotice.created.convertLongToTime("dd/MM/yyyy")
                )
            bodyTextView.text = sendNotice.body.replace("<br/>", "\n")

            linkIcon.apply {
                isVisible = sendNotice.link.isNotEmpty()
                setOnClickListener {
                    requireContext().openCustomChromeTab(
                        sendNotice.link
                    )
                }
            }
            sendNotice.getImageLinkNotification(requireContext()).loadImage(
                binding.root,
                binding.senderProfileImageView,
                binding.progressBarNoticePreview,
                DEFAULT_CORNER_RADIUS,
                R.drawable.ic_running_error
            )
            cardViewNotice.isVisible = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.notice_description_menu, menu)

    }

    private fun navigateToImageView(link: String) {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, /* forward= */ true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, /* forward= */ false)
        val action = NavGraphDirections.actionGlobalViewImageFragment(link)
        findNavController().navigate(action)
    }

    /**
     * @since 4.0.4
     * @author Ayaan
     */
    private fun detectScroll() {
        activity?.onScrollColorChange(binding.nestedScrollViewNotice, {
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


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_notice_share -> {
                if (!isEmpty) {
                    if (hasAttach) {
                        try {
                            val action =
                                NoticeDetailFragmentDirections.actionNoticeDetailFragmentToChooseImageBottomSheet(
                                    SendNotice3(notice3, attach)
                                )
                            findNavController().navigate(action)
                        } catch (e: Exception) {
                            Toast.makeText(
                                requireContext(),
                                "Multiple clicks detected",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        requireActivity().openShareDeepLink(
                            notice3.title,
                            notice3.path
                        )
                    }
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    data class FullNotice(val notice: DataState<Notice3>, val attach: DataState<List<Attach>>)


    override fun onPause() {
        super.onPause()
        myScrollViewerInstanceState =
            binding.nestedScrollViewNotice.onSaveInstanceState()
    }

    companion object {
        var myScrollViewerInstanceState: Parcelable? = null
    }

}