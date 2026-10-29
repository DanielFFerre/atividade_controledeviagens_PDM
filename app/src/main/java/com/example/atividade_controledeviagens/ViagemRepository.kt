package com.example.atividade_controledeviagens

import android.content.ContentValues
import android.content.Context

class ViagemRepository(context: Context) {

    private val helper = BancoHelper(context.applicationContext)

    fun inserir(
        data: String,
        kmInicial: Double,
        kmFinal: Double,
        litros: Double,
        tipoCombustivel: String,
        pedagio: Double,
        valorCombustivel: Double
    ): Boolean {
        val db = helper.writableDatabase

        val valores = ContentValues().apply {
            put("data", data)
            put("kmInicial", kmInicial)
            put("kmFinal", kmFinal)
            put("litros", litros)
            put("tipoCombustivel", tipoCombustivel)
            put("pedagio", pedagio)
            put("valorCombustivel", valorCombustivel)
        }

        val resultado = db.insert(
            "viagem",
            null,
            valores
        )

        return resultado != -1L
    }

    fun listar(): List<Viagem> {
        val db = helper.readableDatabase
        val viagens = mutableListOf<Viagem>()

        val cursor = db.rawQuery(
            "SELECT * FROM viagem",
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                val data = it.getString(it.getColumnIndexOrThrow("data"))
                val kmInicial = it.getDouble(it.getColumnIndexOrThrow("kmInicial"))
                val kmFinal = it.getDouble(it.getColumnIndexOrThrow("kmFinal"))
                val litros = it.getDouble(it.getColumnIndexOrThrow("litros"))
                val tipoCombustivel = it.getString(it.getColumnIndexOrThrow("tipoCombustivel"))
                val pedagio = it.getDouble(it.getColumnIndexOrThrow("pedagio"))
                val valorCombustivel = it.getDouble(it.getColumnIndexOrThrow("valorCombustivel"))

                viagens.add(
                    Viagem(
                        data,
                        kmInicial,
                        kmFinal,
                        litros,
                        tipoCombustivel,
                        pedagio,
                        valorCombustivel
                    )
                )
            }
        }

        return viagens
    }
}
