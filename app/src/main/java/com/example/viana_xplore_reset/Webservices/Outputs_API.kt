package com.example.viana_xplore_reset.Webservices

data class Output_Login(

    val sucesso: Boolean,       // isSuccessfull
    val utilizador: String,     //campo utilizador
    val resposta: String         //resposta da API
)

data class Output_Marcador(

    val id: Int,
    val nome: String,
    val descricao: String,
    val foto: String,
    val fenceassociada: Int,
    val latitude: String,
    val longitude: String
)
