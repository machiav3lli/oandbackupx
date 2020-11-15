/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.machiav3lli.backup.R
import com.machiav3lli.backup.databinding.ItemBatchXBinding
import com.machiav3lli.backup.handler.action.BaseAppAction
import com.machiav3lli.backup.utils.ItemUtils.calculateID
import com.machiav3lli.backup.utils.ItemUtils.getFormattedDate
import com.machiav3lli.backup.utils.ItemUtils.pickAppBackupMode
import com.machiav3lli.backup.utils.ItemUtils.pickItemAppType
import com.mikepenz.fastadapter.binding.AbstractBindingItem

class BatchItemX(var app: AppInfoX) : AbstractBindingItem<ItemBatchXBinding>() {
    var isApkChecked = false
    var isDataChecked = false

    val actionMode: Int
        get() = if (isApkChecked && isDataChecked) {
            BaseAppAction.MODE_BOTH
        } else if (isApkChecked) {
            BaseAppAction.MODE_APK
        } else if (isDataChecked) {
            BaseAppAction.MODE_DATA
        } else {
            BaseAppAction.MODE_UNSET
        }

    val isChecked: Boolean
        get() = isDataChecked || isApkChecked

    override var identifier: Long
        get() = calculateID(app)
        set(identifier) {
            super.identifier = identifier
        }

    override val type: Int
        get() = R.id.fastadapter_item

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): ItemBatchXBinding {
        return ItemBatchXBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemBatchXBinding, payloads: List<Any>) {
        binding.apkCheckbox.isChecked = isApkChecked
        binding.dataCheckbox.isChecked = isDataChecked
        binding.label.text = app.packageLabel
        binding.packageName.text = app.packageName
        if (app.hasBackups()) {
            if (app.isUpdated) {
                binding.update.visibility = View.VISIBLE
            } else {
                binding.update.visibility = View.GONE
            }
            binding.lastBackup.text = getFormattedDate(app.latestBackup!!.backupProperties.backupDate!!, false)
        } else {
            binding.update.visibility = View.GONE
            binding.lastBackup.text = null
        }
        pickAppBackupMode(app, binding.root)
        pickItemAppType(app, binding.appType)
    }

    override fun unbindView(binding: ItemBatchXBinding) {
        binding.label.text = null
        binding.packageName.text = null
        binding.lastBackup.text = null
    }
}