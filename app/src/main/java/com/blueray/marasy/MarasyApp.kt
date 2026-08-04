package com.blueray.marasy

import android.app.Application
import android.content.Context
import android.util.Log
import com.onesignal.OneSignal

class MarasyApp : Application() {

    companion object {
        private const val ONESIGNAL_APP_ID = "642b49ce-f58b-465c-b018-305c68b679fb"
        private const val PREF_PLAYER_ID = "onesignal_player_id"

        fun getDeviceId(context: Context): String {
            // Return cached player_id first (guaranteed registered)
            val prefs = context.getSharedPreferences("MARASY_KEY", Context.MODE_PRIVATE)
            val cached = prefs.getString(PREF_PLAYER_ID, "") ?: ""
            if (cached.isNotEmpty()) {
                Log.d("****OneSignal", "device_id: $cached")
                return cached
            }
            // Fallback: read directly (may be empty until registration completes)
            val live = OneSignal.getDeviceState()?.userId ?: ""
            Log.d("****OneSignal", "device_id: $live")
            return live
        }
    }

    override fun onCreate() {
        super.onCreate()

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)

        val state = OneSignal.getDeviceState()

        OneSignal.addSubscriptionObserver { stateChanges ->
            val playerId    = stateChanges.to.userId
            val isSubscribed = stateChanges.to.isSubscribed
            val pushToken   = stateChanges.to.pushToken


            if (!playerId.isNullOrEmpty() && isSubscribed == true) {
                getSharedPreferences("MARASY_KEY", Context.MODE_PRIVATE)
                    .edit()
                    .putString(PREF_PLAYER_ID, playerId)
                    .apply()
                Log.d("****OneSignal", "player_id saved: $playerId")
            }
        }
    }
}
