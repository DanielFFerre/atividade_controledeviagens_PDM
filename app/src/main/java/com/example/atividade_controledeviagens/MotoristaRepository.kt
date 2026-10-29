package com.example.atividade_controledeviagens

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property para instanciar o DataStore
val Context.dataStore by preferencesDataStore(name = "configuracoes")

class MotoristaRepository(private val context: Context) {

    companion object {
        val NOME_MOTORISTA = stringPreferencesKey("nome_motorista")
    }

    // Retorna um Flow contendo o nome do motorista salvo
    val motoristaFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            // O valor padrão é string vazia caso não exista nada salvo
            preferences[NOME_MOTORISTA] ?: ""
        }

    // Função para salvar o nome do motorista
    suspend fun salvarMotorista(nome: String) {
        context.dataStore.edit { preferences ->
            preferences[NOME_MOTORISTA] = nome
        }
    }
}
