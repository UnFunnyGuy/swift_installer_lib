package com.brit.swiftinstaller.ui.applist

import android.content.Context
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.brit.swiftinstaller.installer.rom.RomInfo
import com.brit.swiftinstaller.ui.activities.InstallSummaryActivity
import com.brit.swiftinstaller.utils.Utils

class AppsTabPagerAdapter(fm: FragmentManager, summary: Boolean, vararg tabs: Int) : FragmentPagerAdapter(fm) {

    private val mApps = HashMap<Int, ArrayList<AppItem>>()
    private var mFragments: ArrayList<AppListFragment> = ArrayList()
    private val requiredApps = HashMap<Int, Array<String>>()
    private val mHandler = Handler()

    init {
        for (index in tabs) {
            mFragments.add(AppListFragment.instance(summary,
                    (index == InstallSummaryActivity.FAILED_TAB)))
            mApps[index] = ArrayList()
        }
    }

    fun setAlertIconClickListener(listener: AppListFragment.AlertIconClickListener) {
        mFragments.forEach {
            it.alertIconClickListener = listener
        }
    }

    fun setAppCheckBoxClickListener(listener: AppListFragment.AppCheckBoxClickListener) {
        mFragments.forEach {
            it.appCheckBoxClickListener = listener
        }
    }

    fun setViewClickListener(listener: AppListFragment.ViewClickListener) {
        mFragments.forEach {
            it.viewClickListener = listener
        }
    }

    fun querySearch(tab: Int, query: String) {
        mHandler.post { mFragments[tab].querySearch(query) }
    }

    fun addApp(tab: Int, app: AppItem) {
        mApps[tab]!!.add(app)
        notifyFragmentDataSetChanged(tab)
    }

    fun setRequiredApps(tab: Int, apps: Array<String>) {
        requiredApps[tab] = apps
        notifyFragmentDataSetChanged(tab)
    }

    fun getAppsCount(tab: Int): Int {
        return mApps[tab]!!.size
    }

    fun getCheckableCount(context: Context, tab: Int): Int {
        val checkableList = arrayListOf<AppItem>()
        for (item in mApps[tab]!!) {
            if (Utils.checkVersionCompatible(context, item.packageName) ||
                    RomInfo.getRomInfo(context).isOverlayInstalled(item.packageName)) {
                checkableList.add(item)
            }
        }
        return checkableList.size
    }

    fun clearApps() {
        for (key in mApps.keys) {
            mApps[key]!!.clear()
            mFragments[key].selectAll(false)
            notifyFragmentDataSetChanged(key)
        }
    }

    fun selectAll(index: Int, checked: Boolean) {
        mFragments[index].selectAll(checked)
    }

    fun getCheckedCount(index: Int): Int {
        return mFragments[index].getCheckedItems().count()
    }

    fun getApps(tab: Int): ArrayList<AppItem> {
        return mApps[tab]!!
    }

    fun getCheckedItems(index: Int): ArrayList<AppItem> {
        return mFragments[index].getCheckedItems()
    }

    override fun getItem(position: Int): Fragment {
        return mFragments[position]
    }

    fun notifyFragmentDataSetChanged(position: Int) {
        mHandler.post {
            mFragments[position].setAppList(mApps[position])
            if (requiredApps[position] != null) {
                mFragments[position].setRequiredAppList(requiredApps[position]!!)
            }
        }
    }

    override fun getCount(): Int {
        return mFragments.size
    }
}