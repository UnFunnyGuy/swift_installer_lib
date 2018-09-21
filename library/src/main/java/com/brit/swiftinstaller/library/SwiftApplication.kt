/*
 *
 *  * Copyright (C) 2018 Griffin Millender
 *  * Copyright (C) 2018 Per Lycke
 *  * Copyright (C) 2018 Davide Lilli & Nishith Khanna
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.brit.swiftinstaller.library

import android.content.Intent
import android.content.IntentFilter
import com.brit.swiftinstaller.library.installer.rom.RomInfo
import com.brit.swiftinstaller.library.ui.customize.CustomizeSelection
import com.brit.swiftinstaller.library.utils.AppExtrasHandler
import com.brit.swiftinstaller.library.utils.PackageListener
import com.topjohnwu.superuser.BuildConfig
import com.topjohnwu.superuser.BusyBox
import com.topjohnwu.superuser.ContainerApp
import com.topjohnwu.superuser.Shell
import javax.crypto.Cipher

open class SwiftApplication : ContainerApp() {

    lateinit var romInfo: RomInfo
    lateinit var extrasHandler: AppExtrasHandler

    private var currentSelection: CustomizeSelection = CustomizeSelection()

    var selection: CustomizeSelection
        get() {
            if (currentSelection.isEmpty) {
                currentSelection = romInfo.getCustomizeHandler().getSelection()
            }
            return currentSelection
        }
        set(value) {
            romInfo.getCustomizeHandler().setSelection(value)
            currentSelection = value
        }

    val installApps: ArrayList<String>
        get() = SwiftApplication.staticInstallApps

    val errorMap: HashMap<String, String>
        get() = SwiftApplication.staticErrorMap

    companion object {
        @JvmStatic val staticInstallApps = arrayListOf<String>()
        @JvmStatic val staticErrorMap = HashMap<String, String>()
    }

    var cipher: Cipher? = null

    override fun onCreate() {
        super.onCreate()

        romInfo = RomInfo.createRomInfo(this)
        extrasHandler = createExtrasHandler()
        extrasHandler.initialize()

        cipher = createCipher()

        startReceivers()

        Shell.Config.verboseLogging(BuildConfig.DEBUG)
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR)
        BusyBox.setup(this)
    }

    open fun createCipher(): Cipher? {
        return null
    }

    open fun createExtrasHandler(): AppExtrasHandler {
        return AppExtrasHandler(this)
    }

    private fun startReceivers() {
        val filter = IntentFilter(Intent.ACTION_PACKAGE_ADDED)
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED)
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        filter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
        filter.addDataScheme("package")
        registerReceiver(PackageListener(), filter)
    }
}