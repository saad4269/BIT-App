/*
 * BIT Lalpur App
 *
 * Created by Ayaan on 2/21/22, 6:02 PM
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 2/21/22, 12:37 PM
 */

package com.atech.bit.ui.fragments.society.description

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.atech.core.api.society.Society
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SocietyViewModel @Inject constructor(
    private val state: SavedStateHandle,
) : ViewModel() {

    var society = state.get<Society>("society")
        set(value) {
            field = value
            state["society"] = value
        }
    var title = state.get<String>("title")
        set(value) {
            field = value
            state["title"] = value
        }
}