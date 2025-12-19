package com.ahmedsamy.purelink

import android.content.Context
import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class PureLinkTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        
        // 1. قراءة الحالة الحالية
        val prefs = getSharedPreferences("PureLinkPrefs", Context.MODE_PRIVATE)
        val isActive = prefs.getBoolean("monitoring_active", true)
        
        // 2. عكس الحالة (لو شغال اطفيه، والعكس)
        val newState = !isActive
        prefs.edit().putBoolean("monitoring_active", newState).apply()
        
        // 3. تحديث شكل الزرار فوراً
        updateTileState()
        
        // 4. رسالة للمستخدم
        val stateMsg = if (newState) "Monitor ON 🟢" else "Monitor PAUSED ⏸️"
        Toast.makeText(this, stateMsg, Toast.LENGTH_SHORT).show()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val prefs = getSharedPreferences("PureLinkPrefs", Context.MODE_PRIVATE)
        val isActive = prefs.getBoolean("monitoring_active", true)
        
        // التأكد إن خدمة النظام نفسها شغالة
        if (isAccessibilityServiceEnabled()) {
            if (isActive) {
                tile.state = Tile.STATE_ACTIVE
                tile.label = "PureLink: ON"
            } else {
                tile.state = Tile.STATE_INACTIVE
                tile.label = "PureLink: PAUSED"
            }
        } else {
            // لو الخدمة الرئيسية مقفولة من الإعدادات، نبه المستخدم
            tile.state = Tile.STATE_UNAVAILABLE
            tile.label = "Service Disabled"
        }
        tile.updateTile()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val prefString = android.provider.Settings.Secure.getString(contentResolver, android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val serviceId = packageName + "/" + packageName + ".ClipboardService"
        return prefString != null && prefString.contains(serviceId)
    }
}
