package com.example.viana_xplore_reset.Webservices

data class Output_Login(

    val sucesso: Boolean,       //isSuccessfull
    val utilizador: String,     //campo utilizador
    val resposta: String        //Resposta da API
)
