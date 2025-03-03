package com.atech.bit.ui.fragments.home.adapter

import android.graphics.Color
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.atech.bit.R
import com.atech.bit.databinding.RowSubjectHomeBinding
import com.atech.core.data.room.library.DiffUtilCallbackLibrary
import com.atech.core.data.room.library.LibraryModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.platform.MaterialArcMotion
import com.google.android.material.transition.platform.MaterialContainerTransform

class HomeLibraryAdapter(
    private val onDeleteClick: (LibraryModel) -> Unit = {},
    private val onMarkAsReturnClick: (LibraryModel) -> Unit = { },
) : ListAdapter<LibraryModel, HomeLibraryAdapter.LibraryViewHolder>(
    DiffUtilCallbackLibrary()
) {
    inner class LibraryViewHolder(
        private val binding: RowSubjectHomeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.buttonMarkAsReturned.setOnClickListener {
                absoluteAdapterPosition.let { position ->
                    if (position != RecyclerView.NO_POSITION) {
                        val library = getItem(position)
                        collapseView()
                        onMarkAsReturnClick(library)
                    }
                }
            }
            binding.buttonDelete.setOnClickListener {
                absoluteAdapterPosition.let { position ->
                    if (position != RecyclerView.NO_POSITION) {
                        val library = getItem(position)
                        collapseView()
                        onDeleteClick(library)
                    }
                }
            }

            binding.floatingActionButton.setOnClickListener {
                absoluteAdapterPosition.let {
                    val transform = MaterialContainerTransform().apply {
                        startView = binding.floatingActionButton
                        endView = binding.constraintLayoutMenu
                        addTarget(endView)
                        pathMotion = MaterialArcMotion()
                        scrimColor = Color.TRANSPARENT
                        duration =
                            binding.root.resources.getInteger(R.integer.duration_medium).toLong()
                        endContainerColor = MaterialColors.getColor(
                            binding.root.context,
                            com.google.android.material.R.attr.colorPrimaryContainer,
                            Color.TRANSPARENT
                        )
                        endElevation = 10f
                    }
                    TransitionManager.beginDelayedTransition(binding.root, transform)
                    binding.floatingActionButton.visibility = View.INVISIBLE
                    binding.view.visibility = View.VISIBLE
                    binding.constraintLayoutMenu.visibility = View.VISIBLE
                }
            }
            binding.view.setOnClickListener {
                absoluteAdapterPosition.let {
                    if (it != RecyclerView.NO_POSITION) {
                        collapseView()
                    }
                }
            }
        }

        private fun collapseView() {
            val transform = MaterialContainerTransform().apply {
                startView = binding.constraintLayoutMenu
                endView = binding.floatingActionButton
                duration = binding.root.resources.getInteger(R.integer.duration_medium).toLong()
                addTarget(endView)
                pathMotion = MaterialArcMotion()
                scrimColor = Color.TRANSPARENT
                startElevation = 10f
            }
            TransitionManager.beginDelayedTransition(binding.root, transform)
            binding.floatingActionButton.visibility = View.VISIBLE
            binding.view.visibility = View.GONE
            binding.constraintLayoutMenu.visibility = View.GONE
        }

        fun bind(libraryModel: LibraryModel) {
            binding.apply {
                textViewBookId.text = libraryModel.bookId.ifBlank { "No Id" }
                textViewIssueBookName.text = libraryModel.bookName
                textViewIssueBookName.setCompoundDrawablesWithIntrinsicBounds(
                    when {
                        libraryModel.eventId != -1L -> R.drawable.ic_bell_active
                        libraryModel.markAsReturn -> R.drawable.ic_check_circle
                        else -> 0
                    }, 0, 0, 0
                )
//                buttonMarkAsReturned set icon image button
                (buttonMarkAsReturned as MaterialButton).setIconResource(
                    if (libraryModel.markAsReturn) R.drawable.ic_close else R.drawable.ic_check
                )

                textViewIssueDate.text =
                    String.format("Issued on : %s", libraryModel.issueFormatData)
                floatingActionButton.transitionName = libraryModel.bookName
                textViewReturnDate.text = libraryModel.returnFormatData
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder =
        LibraryViewHolder(
            RowSubjectHomeBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}