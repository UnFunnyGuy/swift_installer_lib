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

package com.brit.swiftinstaller.library.installer.rom

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.brit.swiftinstaller.library.BuildConfig
import com.brit.swiftinstaller.library.R
import com.brit.swiftinstaller.library.ui.customize.CategoryMap
import com.brit.swiftinstaller.library.ui.customize.CustomizeCategory
import com.brit.swiftinstaller.library.ui.customize.CustomizeHandler
import com.brit.swiftinstaller.library.ui.customize.CustomizeSelection
import com.brit.swiftinstaller.library.ui.customize.Option
import com.brit.swiftinstaller.library.ui.customize.OptionsMap
import com.brit.swiftinstaller.library.ui.customize.PreviewHandler
import com.brit.swiftinstaller.library.ui.customize.SliderOption
import com.brit.swiftinstaller.library.utils.*
import com.brit.swiftinstaller.library.utils.OverlayUtils.getOverlayPackageName
import com.brit.swiftinstaller.library.utils.OverlayUtils.getOverlayPath
import kotlinx.android.synthetic.main.customize_preview_settings.view.*
import kotlinx.android.synthetic.main.customize_preview_sysui.view.*
import java.io.File

class SamsungRomInfo(context: Context) : RomInfo(context) {

    override fun requiresRoot(): Boolean {
        return false
    }

    override fun getChangelogTag(): String {
        return "samsung"
    }

    override fun getDisabledOverlays(): ArrayList<String> {
        val disable = ArrayList<String>()
        disable.add("com.android.emergency")
        return disable
    }

    override fun getRequiredApps(): Array<String> {
        return Array(31) {
            when (it) {
                0 -> "android"
                1 -> "com.android.systemui"
                2 -> "com.amazon.clouddrive.photos"
                3 -> "com.android.settings"
                4 -> "com.anydo"
                5 -> "com.apple.android.music"
                6 -> "com.ebay.mobile"
                7 -> "com.embermitre.pixolor.app"
                8 -> "com.google.android.apps.genie.geniewidget"
                9 -> "com.google.android.apps.inbox"
                10 -> "com.google.android.apps.messaging"
                11 -> "com.google.android.gm"
                12 -> "com.google.android.talk"
                13 -> "com.mxtech.videoplayer.ad"
                14 -> "com.mxtech.videoplayer.pro"
                15 -> "com.pandora.android"
                16 -> "com.simplecity.amp.pro"
                17 -> "com.Slack"
                18 -> "com.samsung.android.incallui"
                19 -> "com.twitter.android"
                20 -> "com.samsung.android.contacts"
                21 -> "com.samsung.android.scloud"
                22 -> "com.samsung.android.themestore"
                23 -> "com.samsung.android.lool"
                24 -> "com.samsung.android.samsungpassautofill"
                25 -> "com.google.android.gms"
                26 -> "com.sec.android.daemonapp"
                27 -> "de.axelspringer.yana.zeropage"
                28 -> "com.google.android.apps.nexuslauncher"
                29 -> "com.lastpass.lpandroid"
                30 -> "com.weather.Weather"
                else -> ""
            }
        }
    }

    override fun postInstall(uninstall: Boolean, apps: ArrayList<String>, oppositeApps: ArrayList<String>?, intent: Intent?) {
        val extraIntent = intent != null

        if (ShellUtils.isRootAvailable) {
            if (!uninstall && oppositeApps != null && oppositeApps.isNotEmpty()) {
                for (app in oppositeApps) {
                    uninstallOverlay(context, app)
                }
            }
            if (intent != null) {
                context.applicationContext.startActivity(intent)
            }
            return
        }

        if (apps.contains("android")) {
            val index = apps.indexOf("android")
            apps.removeAt(index)
            apps.add(0, "android")
        }
        if (apps.contains("com.google.android.packageinstaller")) {
            val index = apps.indexOf("com.google.android.packageinstaller")
            apps.removeAt(index)
            apps.add(0, "com.google.android.packageinstaller")
        }

        val intents = Array(if (!extraIntent) {
            apps.size
        } else {
            apps.size + 1
        }) { i ->
            val index = if (extraIntent) {
                i - 1
            } else {
                i
            }
            if (!extraIntent || i > 0) {
                val appInstall = Intent()
                if (uninstall) {
                    appInstall.action = Intent.ACTION_UNINSTALL_PACKAGE
                    appInstall.data = Uri.fromParts("package",
                            getOverlayPackageName(apps.elementAt(index)), null)
                } else {
                    appInstall.action = Intent.ACTION_INSTALL_PACKAGE
                    appInstall.data = FileProvider.getUriForFile(context,
                            "com.brit.swiftinstaller.myprovider",
                            File(getOverlayPath(apps.elementAt(index))))
                }
                appInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                appInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                appInstall
            } else {
                intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent
            }
        }

        if (!intents.isEmpty()) {
            context.startActivities(intents)
        }

        if (oppositeApps != null && !oppositeApps.isEmpty()) {
            val oppositeIntents = Array(oppositeApps.size) {
                val appInstall = Intent()
                if (!uninstall) {
                    appInstall.action = Intent.ACTION_UNINSTALL_PACKAGE
                    appInstall.data = Uri.fromParts("package",
                            getOverlayPackageName(oppositeApps[it]), null)
                } else {
                    appInstall.action = Intent.ACTION_INSTALL_PACKAGE
                    appInstall.data = FileProvider.getUriForFile(context,
                            BuildConfig.APPLICATION_ID + ".myprovider",
                            File(getOverlayPath(oppositeApps[it])))
                }
                appInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                appInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                appInstall
            }
            context.startActivities(oppositeIntents)
        }

        clearAppsToUninstall(context)
        clearAppsToInstall(context)
    }

    override fun installOverlay(context: Context, targetPackage: String, overlayPath: String) {
        if (ShellUtils.isRootAvailable) {
            runCommand("pm install -r $overlayPath", true)
        }
    }

    override fun uninstallOverlay(context: Context, packageName: String) {
        if (ShellUtils.isRootAvailable) {
            runCommand("pm uninstall " + getOverlayPackageName(packageName), true)
        } else {
            addAppToUninstall(context, getOverlayPackageName(packageName))
        }
    }

    override fun createCustomizeHandler(): CustomizeHandler {
        return object : CustomizeHandler(context) {
            override fun getDefaultSelection(): CustomizeSelection {
                val selection = super.getDefaultSelection()
                selection["samsung_oreo_icons"] = "stock_accent"
                selection["samsung_oreo_clock"] = "right"
                selection["samsung_oreo_notif_style"] = "default"
                selection["samsung_oreo_qs_alpha"] = "0"
                return selection
            }
            override fun populateCustomizeOptions(categories: CategoryMap) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                    populateOreoCustomizeOptions(categories)
                }
                super.populateCustomizeOptions(categories)
            }

            override fun createPreviewHandler(context: Context): PreviewHandler {
                return SamsungOreoPreviewHandler(context)
            }
        }
    }

    private class SamsungOreoPreviewHandler(context: Context): PreviewHandler(context) {
        override fun updateView(palette: MaterialPalette, selection: CustomizeSelection) {
            super.updateView(palette, selection)
            if (selection.containsKey("samsung_oreo_clock")) {
                val clockSelection = selection["samsung_oreo_clock"]
                settingsPreview!!.clock_right.setVisible(clockSelection == "right")
                settingsPreview!!.clock_left.setVisible(clockSelection == "left")
                settingsPreview!!.clock_centered.setVisible(clockSelection == "centered")
            }

            if (selection.containsKey("samsung_oreo_qs_alpha")) {
                val qsAlpha = selection.getInt("samsung_oreo_qs_alpha")
                systemUiPreview!!.preview_wallpaper.setColorFilter(
                        ColorUtils.addAlphaColor(palette.backgroundColor,
                                qsAlpha), PorterDuff.Mode.SRC_OVER)
            }

            if (selection.containsKey("samsung_oreo_notif_style")) {
                val darkNotif = (selection["notif_background"]) == "dark"
                if (selection["samsung_oreo_notif_style"] == "p") {
                    systemUiPreview!!.notif_bg_layout.setImageResource(R.drawable.notif_bg_rounded)
                    if (darkNotif) {
                        systemUiPreview!!.notif_bg_layout.drawable.setTint(
                                ColorUtils.handleColor(palette.backgroundColor, 8))
                    } else {
                        systemUiPreview!!.notif_bg_layout.drawable.setTint(
                                Color.parseColor("#f5f5f5"))

                    }
                }
            }
        }

        override fun updateIcons(selection: CustomizeSelection) {
            for (icon in settingsIcons) {
                val type: String = when {
                    selection["samsung_oreo_icons"] == "aosp" -> {
                        icon.setColorFilter(selection.accentColor)
                        "aosp"
                    }
                    selection["samsung_oreo_icons"] == "p" -> {
                        icon.clearColorFilter()
                        "p"
                    }
                    selection["samsung_oreo_icons"] == "stock_multi" -> {
                        icon.clearColorFilter()
                        "stock"
                    }
                    else -> {
                        icon.setColorFilter(selection.accentColor)
                        "stock"
                    }
                }

                val idName = "ic_${context.resources.getResourceEntryName(icon.id)}_$type"
                val id = context.resources.getIdentifier("com.brit.swiftinstaller:drawable/$idName", null, null)
                if (id > 0) {
                    icon.setImageDrawable(context.getDrawable(id))
                }
            }
            for (icon in systemUiIcons) {
                val idName = "ic_${context.resources.getResourceEntryName(icon.id)}_${selection["samsung_oreo_icons"]}"
                val id = context.resources.getIdentifier("com.brit.swiftinstaller:drawable/$idName", null, null)
                if (id > 0) {
                    icon.setImageDrawable(context.getDrawable(id))
                }
                icon.setColorFilter(selection.accentColor)
            }
        }
    }

    private fun populateOreoCustomizeOptions(categories: CategoryMap) {
        val requiredApps = ArrayList<String>()
        val iconOptions = OptionsMap()
        iconOptions.add(Option(context.getString(R.string.aosp_icons), "aosp", "aosp", true))
        iconOptions.add(
                Option(context.getString(R.string.stock_icons), "stock_accent", "stock", true))
        iconOptions.add(Option(context.getString(R.string.stock_icons_multi), "stock_multi", "stock", false))
        iconOptions.add(Option(context.getString(R.string.android_p), "p", "p", false))
        requiredApps.add("com.android.systemui")
        requiredApps.add("com.samsung.android.lool")
        requiredApps.add("com.samsung.android.themestore")
        requiredApps.add("com.android.settings")
        requiredApps.add("com.samsung.android.app.aodservice")
        requiredApps.add("android")
        categories.add(CustomizeCategory(context.getString(R.string.category_icons), "samsung_oreo_icons", "stock_accent", iconOptions, requiredApps))
        requiredApps.clear()

        val clockOptions = OptionsMap()
        clockOptions.add(Option(context.getString(R.string.right), "right"))
        clockOptions.add(Option(context.getString(R.string.left), "left"))
        clockOptions.add(Option(context.getString(R.string.centered), "centered"))
        requiredApps.add("com.android.systemui")
        categories.add(CustomizeCategory(context.getString(R.string.clock), "samsung_oreo_clock", "right", clockOptions, requiredApps))
        requiredApps.clear()

        val notifOptions = OptionsMap()
        notifOptions.add(Option(context.getString(R.string.default_style), "default"))
        notifOptions.add(Option(context.getString(R.string.android_p_rounded_style), "p"))
        val trans = SliderOption(context.getString(R.string.qs_transparency), "samsung_oreo_qs_alpha")
        trans.current = 0
        notifOptions.add(trans)
        notifOptions["p"]!!.infoDialogTitle = context.getString(R.string.rounded_dialog_title)
        notifOptions["p"]!!.infoDialogText = context.getString(R.string.rounded_dialog_info)
        requiredApps.add("com.android.systemui")
        requiredApps.add("android")
        categories.add(CustomizeCategory(context.getString(R.string.notification_style), "samsung_oreo_notif_style", "default", notifOptions, requiredApps))
        requiredApps.clear()
    }
}