package com.example.viana_xplore_reset

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProviders
import com.example.viana_xplore_reset.Webservices.Fenke
import com.example.viana_xplore_reset.Webservices.Markador
import com.example.viana_xplore_reset.Webservices.PostLogin
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
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
    private lateinit var geofencingClient: GeofencingClient
    private var FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private var  BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;

    private lateinit var viewModel: ViewModel
    private lateinit var binding: AtividadeMapa

    //Verificar a versão do SO
    private val runningQOrLater =
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    //Inicializar o BroadcatReceiver
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, FenceReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        longitude = ""
        latitude = ""
        nome = ""
        descricao = ""
        foto = ""
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_atividade_mapa)

        //Para ir buscar os metodos à classe
        viewModel = ViewModelProviders.of(this, SavedStateViewModelFactory(this.application,
                this)).get(ViewModel::class.java)
        geofencingClient = LocationServices.getGeofencingClient(this)       //Para interagir com as APIs de Geofence

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

        //Chamar as fences da base de dados e imprimir
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

    override fun onStart() {
        super.onStart()
        checkPermissionsAndStartGeofencing()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            // We don't rely on the result code, but just check the location setting again
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val extras = intent?.extras
        if(extras != null){
            if(extras.containsKey(Fences.GeofencingConstants.EXTRA_GEOFENCE_INDEX)){
                checkPermissionsAndStartGeofencing()
            }
        }
    }


    private fun checkPermissionsAndStartGeofencing() {
        if (viewModel.geofenceIsActive()) return
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            checkDeviceLocationSettingsAndStartGeofence()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
                settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@AtividadeMapa,
                            REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error geting location settings resolution: " + sendEx.message)
                }
            } /*else {
                Snackbar.make(
                       // binding.activityMapsMain,
                        R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }*/
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                addGeofenceForClue()
            }
        }
    }

    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_FINE_LOCATION))
        val backgroundPermissionApproved =
                if (runningQOrLater) {
                    PackageManager.PERMISSION_GRANTED ==
                            ActivityCompat.checkSelfPermission(
                                    this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            )
                } else {
                    true
                }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    @TargetApi(29 )
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return

        // Else request the permission
        // this provides the result[LOCATION_PERMISSION_INDEX]
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when {
            runningQOrLater -> {
                // this provides the result[BACKGROUND_LOCATION_PERMISSION_INDEX]
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }

        Log.d(TAG, "Request foreground only location permission")
        ActivityCompat.requestPermissions(
                this@AtividadeMapa,
                permissionsArray,
                resultCode
        )
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

    private fun addGeofenceForClue() {
        if (viewModel.geofenceIsActive()) return
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

        // Build the geofence request
        val geofencingRequest = GeofencingRequest.Builder()
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
                        viewModel.geofenceActivated()
                    }
                    addOnFailureListener {
                        // Failed to add geofences.
                        Toast.makeText(this@AtividadeMapa, R.string.geofences_not_added,
                                Toast.LENGTH_SHORT).show()
                        if ((it.message != null)) {
                            Log.w(TAG, it.message!!)
                        }
                    }
                }
            }
        }
    }


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
private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
private const val TAG = "AtividadeMapa"