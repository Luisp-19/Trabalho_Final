package com.example.viana_xplore_reset

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.nfc.NfcAdapter.EXTRA_ID
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.viana_xplore_reset.Webservices.Output_Marcador
import com.example.viana_xplore_reset.Webservices.PostLogin
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL

/*private lateinit var marcador_latitude: TextView
private lateinit var marcador_longitude: TextView

private lateinit var nome: TextView
private lateinit var descricao: TextView
private lateinit var coordenadas: TextView
private lateinit var foto: ImageView*/

class Marcadores : AppCompatActivity() {

    private lateinit var nome: TextView
    private lateinit var descricao: TextView
    private lateinit var coordenadas: TextView
    private lateinit var foto: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_atividade_marcador)

        nome = findViewById<TextView>(R.id.nome)
        descricao = findViewById<TextView>(R.id.descricao)
        foto = findViewById<ImageView>(R.id.foto)


        val sharedPref: SharedPreferences = getSharedPreferences( getString(R.string.preference_file_key), Context.MODE_PRIVATE )
        //Inicia variaveis e realiza as calls para popular o mapa
        val utilizador: String = sharedPref.getString(getString(R.string.automatic_login_username), null)!!
        val intent = intent

        //Popula com os valores retirados dos companions
        val id = intent.getIntExtra(EXTRA_ID,-1)

        val latitude = intent.getStringExtra(EXTRA_LATITUDE)
        val longitude = intent.getStringExtra(EXTRA_LONGITUDE)
        val request = Servicos.buildServico(PostLogin::class.java)

        //Define a call para receber os problemas da API

        val call = request.getMarcadoresID(id)                  //Criar a api no ficheiro para ir buscar o marcador do id
        call.enqueue(object : Callback<List<Output_Marcador>> {
            override fun onResponse(call: Call<List<Output_Marcador>>, response: Response<List<Output_Marcador>>) {
                if (response.isSuccessful) {
                    val c = response.body()!!
                    for (marcador in c) {
                        //val intent = Intent(this@Marcadores, AtividadeMarcador::class.java)
                        nome.text = marcador.nome
                        descricao.text = marcador.descricao
                        coordenadas.text = "${marcador.latitude}, ${marcador.longitude}"

                        Thread {
                            val url = URL("${marcador.foto}")
                            val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())

                            runOnUiThread {
                                foto.setImageBitmap(bmp)
                                foto.tag = marcador.foto
                            }
                        }.start()
                    }
                }
            }
                override fun onFailure(call: Call<List<Output_Marcador>>, t: Throwable) {
                    Toast.makeText(this@Marcadores, "Falhou", Toast.LENGTH_SHORT).show()
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