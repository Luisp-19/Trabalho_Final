package com.example.viana_xplore_reset

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.viana_xplore_reset.Webservices.Fenke
import com.example.viana_xplore_reset.Webservices.PostLogin
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun errorMessage(context: Context, errorCode: Int): String {
    val resources = context.resources
    return when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
                R.string.geofence_not_available
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
                R.string.geofence_too_many_geofences
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
                R.string.geofence_too_many_pending_intents
        )
        else -> resources.getString(R.string.unknown_geofence_error)
    }
}

data class Fences (val id: String, val name: Int, val latLong: LatLng)  {

    internal object GeofencingConstants {

        /**
         * Used to set an expiration time for a geofence. After this amount of time, Location services
         * stops tracking the geofence. For this sample, geofences expire after one hour.
         */
        val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = java.util.concurrent.TimeUnit.HOURS.toMillis(1)

        val LANDMARK_DATA = arrayOf(
                Fences(
                        "Viana",
                        R.string.Viana_location,
                        LatLng(41.697398752697346, -8.836316563402274)),

                Fences(
                        "Darque",
                        R.string.Darque_location,
                        LatLng(41.68362630510686, -8.793922025402004)),

        )

        val NUM_LANDMARKS = LANDMARK_DATA.size
        const val GEOFENCE_RADIUS_IN_METERS = 1600f
        const val EXTRA_GEOFENCE_INDEX = "GEOFENCE_INDEX"
    }

    companion object {
        const val EXTRA_POSITION = "com.example.android.wordlistsql.EXTRA_POSITION"
    }
}