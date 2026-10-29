package com.example.atividade_controledeviagens

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BancoHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE viagem (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                data TEXT NOT NULL,
                kmInicial REAL NOT NULL,
                kmFinal REAL NOT NULL,
                litros REAL NOT NULL,
                tipoCombustivel TEXT NOT NULL,
                pedagio REAL NOT NULL,
                valorCombustivel REAL NOT NULL
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Tratar mudanças na estrutura quando a versão do banco for incrementada.
    }

    companion object {
        private const val DATABASE_NAME = "controledeviagens.db"
        private const val DATABASE_VERSION = 1
    }
}
