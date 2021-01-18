/*package com.example.viana_xplore_reset


import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng


class Fences : AppCompatActivity(){

    lateinit var latitude: TextView
    lateinit var longitude: TextView
    var fenceLatitude = 41.47480742234974
    var fenceLongitude = -8.433650855729988
    var raio = 1000.0f        //metros
    var fenceID = 1
    private val geoFencePendingIntent: PendingIntent? = null

    var geofence = Geofence.Builder()
            .setRequestId(fenceID.toString()) // Geofence ID
            .setCircularRegion(fenceLatitude, fenceLongitude, raio) // defining fence region
            .setExpirationDuration(Geofence.NEVER_EXPIRE) // expiring date
            // Transition types that it should look for
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

    var request = GeofencingRequest.Builder() // Notification to trigger when the Geofence is created
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence) // add a Geofence
            .build()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private fun createGeofence(latLng: LatLng, radius: Float): Geofence? {
        //Log.d(TAG, "createGeofence")
        return Geofence.Builder()
                .setRequestId(fenceID.toString())
                .setCircularRegion(fenceLatitude, fenceLongitude, raio)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build()
    }

    // Create a Geofence Request
    private fun createGeofenceRequest(geofence: Geofence): GeofencingRequest? {
       // Log.d(TAG, "createGeofenceRequest")
        return GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build()
    }


    private fun createGeofencePendingIntent(): PendingIntent? {
        //Log.d(TAG, "createGeofencePendingIntent")
        if (geoFencePendingIntent != null) return geoFencePendingIntent
        val intent = Intent(this, GeofenceTrasitionService::class.java)
        return PendingIntent.getService(
                this, fenceID, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private fun addGeofence(request: GeofencingRequest) {
        //Log.d(TAG, "addGeofence")
        if (checkPermission()) LocationServices.GeofencingApi.addGeofences(
                googleApiClient,
                request,
                createGeofencePendingIntent()
        ).setResultCallback(this)
    }

    fun onResult(status: Status) {
        //Log.i(TAG, "onResult: $status")
        if (status.isSuccess()) {
            drawGeofence()
        } else {
            // inform about fail
        }
    }

    // Draw Geofence circle on GoogleMap
    private var geoFenceLimits: Circle? = null
    private fun drawGeofence() {
        //Log.d(TAG, "drawGeofence()")
        if (geoFenceLimits != null) geoFenceLimits!!.remove()
        val circleOptions = CircleOptions()
                .center(geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(raio.toDouble())
        geoFenceLimits = map.addCircle(circleOptions)
    }


}/*