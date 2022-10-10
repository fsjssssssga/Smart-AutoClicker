/*
 * Copyright (C) 2022 Nain57
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
 */
package com.buzbuz.smartautoclicker.overlays.copy.events

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView

import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration

import com.buzbuz.smartautoclicker.R
import com.buzbuz.smartautoclicker.domain.Event
import com.buzbuz.smartautoclicker.databinding.DialogEventCopyBinding
import com.buzbuz.smartautoclicker.overlays.utils.LoadableListDialog
import com.buzbuz.smartautoclicker.overlays.utils.setIconTint

import com.google.android.material.bottomsheet.BottomSheetDialog

import kotlinx.coroutines.launch

/**
 *
 */
class EventCopyDialog(
    context: Context,
    private val scenarioId: Long,
    private val onEventSelected: (Event) -> Unit,
) : LoadableListDialog(context) {

    /** View model for this content. */
    private val viewModel: EventCopyModel by lazy { ViewModelProvider(this).get(EventCopyModel::class.java) }
    /** ViewBinding containing the views for this dialog. */
    private lateinit var viewBinding: DialogEventCopyBinding
    /** Adapter displaying the list of events. */
    private lateinit var eventCopyAdapter: EventCopyAdapter

    override val emptyTextId: Int = R.string.dialog_event_copy_empty

    override fun getListBindingRoot(): View = viewBinding.root

    override fun onCreateDialog(): BottomSheetDialog {
        viewBinding = DialogEventCopyBinding.inflate(LayoutInflater.from(context))
        viewModel.setCurrentScenario(scenarioId)

        return BottomSheetDialog(context).apply {
            //setCustomTitle(null)
            setContentView(viewBinding.root)
            //setPositiveButton(android.R.string.cancel, null)
        }
    }

    override fun onDialogCreated(dialog: BottomSheetDialog) {
        super.onDialogCreated(dialog)

        viewBinding.search.apply {
            findViewById<ImageView>(androidx.appcompat.R.id.search_button).setIconTint(R.color.overlayViewPrimary)
            findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn).setIconTint(R.color.overlayViewPrimary)

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?) = false
                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.updateSearchQuery(newText)
                    return true
                }
            })
        }

        eventCopyAdapter = EventCopyAdapter { selectedEvent ->
            viewModel.let {
                onEventSelected(it.getCopyEvent(selectedEvent))
                dismiss()
            }
        }

        listBinding.list.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            adapter = eventCopyAdapter
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventList.collect { eventList ->
                    updateLayoutState(eventList)
                    eventCopyAdapter.submitList(if (eventList == null) ArrayList() else ArrayList(eventList))
                }
            }
        }
    }
}