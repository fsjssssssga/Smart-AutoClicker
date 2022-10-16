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
package com.buzbuz.smartautoclicker.overlays.bindings

import com.buzbuz.smartautoclicker.databinding.IncludeDialogNavigationTopBarBinding

fun IncludeDialogNavigationTopBarBinding.setButtonEnabledState(buttonType: DialogNavigationButton, enabled: Boolean) {
    when (buttonType) {
        DialogNavigationButton.SAVE -> buttonSave.isEnabled = enabled
        DialogNavigationButton.DISMISS -> buttonDismiss.isEnabled = enabled
        DialogNavigationButton.DELETE -> buttonDelete.isEnabled = enabled
    }
}

fun IncludeDialogNavigationTopBarBinding.setButtonVisibility(buttonType: DialogNavigationButton, visibility: Int) {
    when (buttonType) {
        DialogNavigationButton.SAVE -> buttonSave.visibility = visibility
        DialogNavigationButton.DISMISS -> buttonDismiss.visibility = visibility
        DialogNavigationButton.DELETE -> buttonDelete.visibility = visibility
    }
}

enum class DialogNavigationButton {
    DISMISS,
    DELETE,
    SAVE,
}