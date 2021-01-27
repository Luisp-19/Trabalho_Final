package com.example.viana_xplore_reset

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.viana_xplore_reset.Webservices.Fenke
import com.example.viana_xplore_reset.Webservices.Markador
import com.example.viana_xplore_reset.Webservices.PostLogin
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AtividadeMapa : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap


    //private var LOCATION_PERMISSION_REQUEST_CODE = 1
    lateinit var nome: String
    lateinit var descricao: String
    lateinit var foto: String
    lateinit var longitude: String
    lateinit var latitude: String

    //Geofence
    private lateinit var GeofencingClient: GeofencingClient
    private var FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private var  BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, FenceReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        longitude = ""
        latitude = ""
        nome = ""
        descricao = ""
        foto = ""
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        GeofencingClient = LocationServices.getGeofencingClient(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_atividade_mapa)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val request = Servicos.buildServico(PostLogin::class.java)
        val call = request.getMarcadores()              //pede a API os problemas da BD
        var position_marcadores: LatLng
        val call_fence = request.getFences()
        var position_fences: LatLng
        var radius: Float

        call_fence.enqueue(object : Callback<List<Fenke>> {
            override fun onResponse(call_fence: Call<List<Fenke>>, response: Response<List<Fenke>>) {
                if (response.isSuccessful) {
                    val c = response.body()!!
                    for (FencesFor in c) {
                        val intent_fence = Intent(this@AtividadeMapa, Fences::class.java)

                        position_fences = LatLng(FencesFor.latitude.toDouble(), FencesFor.longitude.toDouble())
                        radius = (FencesFor.raio.toFloat())
                        val fence = mMap.addCircle(CircleOptions().center(position_fences)
                                .radius(radius.toDouble())
                                .strokeColor(Color.GREEN)
                                .fillColor(Color.argb(64, 255, 255, 0))
                                .strokeWidth(4f))
                        fence.tag = FencesFor.id

                        Geofence.Builder()
                                // Set the request ID, string to identify the geofence.
                                .setRequestId(fence.id)
                                // Set the circular region of this geofence.
                                .setCircularRegion(FencesFor.latitude.toDouble(),
                                        FencesFor.longitude.toDouble(),
                                        radius
                                )
                                // Set the expiration duration of the geofence. This geofence gets
                                // automatically removed after this period of time.
                                .setExpirationDuration(Fences.GeofencingConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                                // Set the transition types of interest. Alerts are only generated for these
                                // transition. We track entry and exit transitions in this sample.
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                                //.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                                .build()

                        /*val geofencingRequest = GeofencingRequest.Builder()
                                // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
                                // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
                                // is already inside that geofence.
                                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)

                                // Add the geofences to be monitored by geofencing service.
                                .addGeofence(geofence)
                                .build()

                        // First, remove any existing geofences that use our pending intent
                        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
                            // Regardless of success/failure of the removal, add the new geofence
                            addOnCompleteListener {
                                // Add the new geofence request with the new geofence
                                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                                    addOnSuccessListener {
                                        // Geofences added.
                                        Toast.makeText(this@AtividadeMapa, R.string.geofences_added,
                                                Toast.LENGTH_SHORT)
                                                .show()
                                        Log.e("Add Geofence", geofence.requestId)
                                        // Tell the viewmodel that we've reached the end of the game and
                                        // activated the last "geofence" --- by removing the Geofence.
                                    }
                                    addOnFailureListener {
                                        // Failed to add geofences.
                                        Toast.makeText(this@AtividadeMapa, R.string.geofences_not_added,
                                                Toast.LENGTH_SHORT).show()
                                        if ((it.message != null)) {
                                            Log.w(TAG, it.message)
                                        }
                                    }
                                }
                            }
                        }*/

                        intent_fence.putExtra(Fences.EXTRA_POSITION, position_fences)
                    }
                }
            }

            override fun onFailure(call_fence: Call<List<Fenke>>, t: Throwable) {
                Toast.makeText(this@AtividadeMapa, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        call.enqueue(object : Callback<List<Markador>> {
            override fun onResponse(call: Call<List<Markador>>, response: Response<List<Markador>>) {
                if (response.isSuccessful) {
                    val c = response.body()!!
                    for (marcadorFor in c) {
                        position_marcadores = LatLng(marcadorFor.latitude.toDouble(), marcadorFor.longitude.toDouble())
                        val marcador = mMap.addMarker(MarkerOptions().position(position_marcadores).title("${marcadorFor.nome}"))
                        marcador.tag = marcadorFor.id
                    }
                }
            }

            override fun onFailure(call: Call<List<Markador>>, t: Throwable) {
                Toast.makeText(this@AtividadeMapa, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        createChannel(this)
    }

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera. In this case,
         * we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to install
         * it inside the SupportMapFragment. This method will only be triggered once the user has
         * installed Google Play services and returned to the app.
         */

        override fun onMapReady(googleMap: GoogleMap) {
            mMap = googleMap

            mMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
                override fun onMarkerClick(p0: Marker): Boolean {
                    val intent = Intent(this@AtividadeMapa, Marcadores::class.java)

                    var id = p0.tag.toString()
                    intent.putExtra(Marcadores.EXTRA_ID, id)
                    intent.putExtra(Marcadores.EXTRA_NOME, nome)
                    intent.putExtra(Marcadores.EXTRA_DESCRICAO, descricao)
                    intent.putExtra(Marcadores.EXTRA_FOTO, foto)

                    startActivity(intent)
                    return false
                }
            })

            val viana = LatLng(41.7065, -8.8158)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(viana, 12f))

            enableUserLocation()

        }

    /*private fun BuilderGeofence() {
        val currentGeofenceIndex = viewModel.nextGeofenceIndex()
        if(currentGeofenceIndex >= Fences.GeofencingConstants.NUM_LANDMARKS) {
            return
        }
        val currentGeofenceData = Fences.GeofencingConstants.LANDMARK_DATA[currentGeofenceIndex]

        // Build the Geofence Object
        val geofence = Geofence.Builder()
                // Set the request ID, string to identify the geofence.
                .setRequestId(currentGeofenceData.id)
                // Set the circular region of this geofence.
                .setCircularRegion(currentGeofenceData.latLong.latitude,
                        currentGeofenceData.latLong.longitude,
                        Fences.GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
                )
                // Set the expiration duration of the geofence. This geofence gets
                // automatically removed after this period of time.
                .setExpirationDuration(Fences.GeofencingConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()
    }*/


    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We need to show user a dialog for displaying why the permission is needed and then ask for the permission...
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_LOCATION_ACCESS_REQUEST_CODE)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_LOCATION_ACCESS_REQUEST_CODE)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                mMap.isMyLocationEnabled = true
            } else {
                //We do not have the permission..
            }
        }
        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                Toast.makeText(this, "You can add geofences...", Toast.LENGTH_SHORT).show()
            } else {
                //We do not have the permission..
                Toast.makeText(this, "Background location access is neccessary for geofences to trigger...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Cria/Chama o menu no mapa
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_mapa, menu)
        return true
    }

    //Chama o menu com a opção de logout
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            //passa os valores do login automatico a false e null e manda para a atividade principal
            R.id.botao_logout -> {
                val sharedPref: SharedPreferences = getSharedPreferences(
                        getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putBoolean(getString(R.string.automatic_login_check), false)
                    putString(getString(R.string.automatic_login_username), null)
                    commit()
                }

                val intent = Intent(this@AtividadeMapa, MainActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
                "com.example.android.wordlistsql.ACTION_GEOFENCE_EVENT"
    }

}