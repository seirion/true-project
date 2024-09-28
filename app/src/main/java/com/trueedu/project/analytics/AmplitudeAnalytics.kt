package com.trueedu.project.analytics

import android.app.Application
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import org.json.JSONObject

class AmplitudeAnalytics : BaseAnalytics {
    private lateinit var analytics: AmplitudeClient

    override fun init(application: Application) {
        Amplitude.getInstance().trackSessionEvents(true)
        analytics = Amplitude.getInstance()
            .initialize(application.applicationContext, "2e301a7903ed06d9cd208500f4e3d9f2")
            .enableForegroundTracking(application)
    }

    override fun log(event: String, params: Map<String, Any>) {
        analytics.logEvent(event, JSONObject(params))
    }

    override fun setUserId(userId: String) {
        analytics.userId = userId
    }

    override fun setUserProperties(properties: Map<String, Any>) {
        analytics.setUserProperties(JSONObject(properties))
    }
}
