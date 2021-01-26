package com.example.viana_xplore_reset

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.viana_xplore_reset.Webservices.Fenke
import com.example.viana_xplore_reset.Webservices.PostLogin
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


/*class Fences : AppCompatActivity() {
    private lateinit var mMap: GoogleMap
    private val TAG = "Fences"
    //private lateinit var pendingIntent: PendingIntent

    /*fun Fences(base: Context?) {
        super(base)
    }*/


    /*override fun onCreate() {

        val intent = intent
        val request = Servicos.buildServico(PostLogin::class.java)
        var positionFence: LatLng
        var radiusFence: Int
        val call = request.getFencesID()

        call.enqueue(object : Callback<List<Fenke>> {
            override fun onResponse(call: Call<List<Fenke>>, response: Response<List<Fenke>>) {
                if (response.isSuccessful) {
                    val c = response.body()!!
                    for (FencesFor in c) {
                        positionFence = LatLng(FencesFor.latitude.toDouble(), FencesFor.longitude.toDouble())
                        radiusFence = FencesFor.raio
                        val fence = mMap.addMarker(MarkerOptions().position(positionFence).title("${FencesFor.zona}"))
                        fence.tag = FencesFor.id

                        val circleOptions = CircleOptions()
                        circleOptions.center(positionFence)
                        circleOptions.radius(radiusFence.toDouble())
                        circleOptions.strokeColor(Color.argb(255, 255, 0, 0))
                        circleOptions.fillColor(Color.argb(64, 255, 0, 0))
                        circleOptions.strokeWidth(5F)
                        mMap.addCircle(circleOptions)
                    }
                }
            }

            override fun onFailure(call: Call<List<Fenke>>, t: Throwable) {
                Toast.makeText(this@Fences, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })*/

    }

    /*fun getGeofencingRequest(geofence: Geofence?): GeofencingRequest? {
        return GeofencingRequest.Builder()
                .addGeofence(geofence) //Alterar para passar várias geofences
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build()
    }

    fun getGeofence(ID: String?, latLng: LatLng, radius: Float, transitionTypes: Int): Geofence? {
        return Geofence.Builder()
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setRequestId(ID)
                .setTransitionTypes(transitionTypes)
                .setLoiteringDelay(5000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build()
    }

    fun getPendingIntent(): PendingIntent {
        if (pendingIntent != null) {
            return pendingIntent
        }
        val intent = Intent(this, FenceReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, 2607, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return pendingIntent
    }*/

    /*companion object {
        const val EXTRA_ID_FENCE = "com.example.android.wordlistsql.EXTRA_ID_FENCE"
        const val EXTRA_ZONA = "com.example.android.wordlistsql.EXTRA_ZONA"
        const val EXTRA_LATITUDE_FENCE = "com.example.android.wordlistsql.EXTRA_LATITUDE_FENCE"
        const val EXTRA_LONGITUDE_FENCE = "com.example.android.wordlistsql.EXTRA_LONGITUDE_FENCE"
    }*/
}*/