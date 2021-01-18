package com.example.viana_xplore_reset

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.nfc.NfcAdapter.EXTRA_ID
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.viana_xplore_reset.Webservices.Markador
import com.example.viana_xplore_reset.Webservices.PostLogin
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL

/*private lateinit var marcador_latitude: TextView
private lateinit var marcador_longitude: TextView*/


class Marcadores : AppCompatActivity() {

    lateinit var nome: TextView
    lateinit var descricao: TextView
    lateinit var foto: ImageView
    //lateinit var coordenadas: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_atividade_marcador)


        val sharedPref: SharedPreferences = getSharedPreferences( getString(R.string.preference_file_key), Context.MODE_PRIVATE )
        //Inicia variaveis e realiza as calls para popular o mapa
        val utilizador: String = sharedPref.getString(getString(R.string.automatic_login_username), null)!!
        val intent = intent

        //Popula com os valores retirados dos companions
        val id = intent.getStringExtra(EXTRA_ID)
        var call_id= id?.toInt()
        val latitude = intent.getStringExtra(EXTRA_LATITUDE)
        val longitude = intent.getStringExtra(EXTRA_LONGITUDE)
        val request = Servicos.buildServico(PostLogin::class.java)

        //Define a call para receber os problemas da API

        val call = request.getMarcadoresID(call_id!!)                  //Criar a api no ficheiro para ir buscar o marcador do id
        call.enqueue(object : Callback<List<Markador>> {
            override fun onResponse(call: Call<List<Markador>>, response: Response<List<Markador>>) {
                if (response.isSuccessful) {
                    val c = response.body()!!
                    for (marcadorFor in c) {
                        //val intent = Intent(this@Marcadores, AtividadeMarcador::class.java)
                        nome = findViewById(R.id.nomeView)
                        descricao = findViewById(R.id.descricaoView)
                        foto = findViewById(R.id.fotoView)
                        nome.text = marcadorFor.nome
                        descricao.text = marcadorFor.descricao
                        //coordenadas.text = "${marcador.latitude}, ${marcador.longitude}"

                        Thread {
                            val url = URL("${marcadorFor.foto}")
                            val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())

                            runOnUiThread {
                                foto.setImageBitmap(bmp)
                                foto.tag = marcadorFor.foto
                            }
                        }.start()
                    }
                }
            }
                override fun onFailure(call: Call<List<Markador>>, t: Throwable) {
                    Toast.makeText(this@Marcadores, "Erro imprimir marcadores", Toast.LENGTH_SHORT).show()
                }
        })
    }

    companion object {
        const val EXTRA_ID = "com.example.android.wordlistsql.EXTRA_ID"
        const val EXTRA_NOME = "com.example.android.wordlistsql.EXTRA_NOME"
        const val EXTRA_DESCRICAO = "com.example.android.wordlistsql.EXTRA_DESCRICACAO"
        const val EXTRA_FOTO = "com.example.android.wordlistsql.EXTRA_FOTO"


        //const val EXTRA_EDITABLE = "estg.ipvc.pm_app.activity.notedetails.EXTRA_EDITABLE"
        const val EXTRA_LATITUDE = "com.example.android.wordlistsql.LAT"
        const val EXTRA_LONGITUDE = "com.example.android.wordlistsql.LON"
    }
}