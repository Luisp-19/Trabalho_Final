package com.example.viana_xplore_reset

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.Toast
import com.example.viana_xplore_reset.Webservices.Markador
import com.example.viana_xplore_reset.Webservices.PostLogin
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
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

    override fun onCreate(savedInstanceState: Bundle?) {
        longitude = ""
        latitude = ""
        nome = ""
        descricao = ""
        foto = ""

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_atividade_mapa)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val request = Servicos.buildServico(PostLogin::class.java)
        val call = request.getMarcadores()              //pede a API os problemas da BD
        var position: LatLng

        call.enqueue(object : Callback<List<Markador>> {
            override fun onResponse(call: Call<List<Markador>>, response: Response<List<Markador>>) {
                if (response.isSuccessful) {
                    val c = response.body()!!
                    for (marcadorFor in c) {
                        position = LatLng(marcadorFor.latitude.toDouble(), marcadorFor.longitude.toDouble())
                        val marcador = mMap.addMarker(MarkerOptions().position(position).title("${marcadorFor.nome}"))
                        marcador.tag = marcadorFor.id
                    }
                }
            }

            override fun onFailure(call: Call<List<Markador>>, t: Throwable) {
                Toast.makeText(this@AtividadeMapa, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
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

                    //var id = p0.tag.toString().toInt()
                    //intent.putExtra(Marcadores.EXTRA_ID, id)
                    intent.putExtra(Marcadores.EXTRA_ID, p0.tag.toString())
                    intent.putExtra(Marcadores.EXTRA_NOME, nome)
                    intent.putExtra(Marcadores.EXTRA_DESCRICAO, descricao)
                    intent.putExtra(Marcadores.EXTRA_FOTO, foto)

                    startActivity(intent)
                    return false
                }
            })
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
}