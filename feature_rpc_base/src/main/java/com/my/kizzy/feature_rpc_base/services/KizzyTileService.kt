/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * KizzyTileService.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.feature_rpc_base.services

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.runtime.mutableStateOf
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.my.kizzy.feature_rpc_base.AppUtils
import com.my.kizzy.feature_rpc_base.startService
import com.my.kizzy.feature_rpc_base.stopService
import com.my.kizzy.preference.Prefs
import com.my.kizzy.resources.R

class KizzyTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val ctx = this
        when (qsTile.state) {
            Tile.STATE_ACTIVE -> {
                ctx.stopService<AppDetectionService>()
                ctx.stopService<MediaRpcService>()
                ctx.stopService<ExperimentalRpc>()
            }
            Tile.STATE_INACTIVE -> {
                showDialog(createRpcChoosingDialog(ctx))
            }
            else -> {}
        }
        updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        tileAdded.value = true
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        tileAdded.value = false
    }

    private fun createRpcChoosingDialog(ctx: Context): Dialog {
        val rpc = arrayOf("Apps Rpc", "Media Rpc", "Experimental Rpc")
        return MaterialAlertDialogBuilder(ContextThemeWrapper(ctx, com.my.kizzy.feature_rpc_base.R.style.MyTileDialogTheme))
            .setTitle("Select a Rpc")
            .setSingleChoiceItems(rpc, -1) { dialog, which ->
                when (which) {
                    0 -> {
                        ctx.startService<AppDetectionService>()
                    }
                    1 -> {
                        ctx.startService<MediaRpcService>()
                    }
                    2 -> {
                        ctx.startService<ExperimentalRpc>()
                    }
                    else -> {}
                }
                dialog.dismiss()
            }
            .create()
    }

    private fun updateTile() {
        if (Prefs[Prefs.TOKEN, ""].isEmpty()) {
            qsTile.state = Tile.STATE_UNAVAILABLE
            qsTile.updateTile()
            return
        }
        when (AppUtils.appDetectionRunning() || AppUtils.mediaRpcRunning() || AppUtils.experimentalRpcRunning()) {
            true -> {
                qsTile.state = Tile.STATE_ACTIVE
                qsTile.icon = Icon.createWithResource(this, R.drawable.ic_tile_stop)
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    qsTile.subtitle = getSubtitle()
                }
            }
            false -> {
                qsTile.state = Tile.STATE_INACTIVE
                qsTile.icon = Icon.createWithResource(this, R.drawable.ic_tile_play)
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                    qsTile.subtitle = "Off"
                }
            }
        }
        qsTile.updateTile()
    }

    private fun getSubtitle(): String {
        return if (AppUtils.appDetectionRunning())
            "Apps Rpc"
        else if (AppUtils.mediaRpcRunning())
            "Media Rpc"
        else
            "Experimental Rpc"
    }

    companion object {
        val tileAdded = mutableStateOf(false)
    }
}