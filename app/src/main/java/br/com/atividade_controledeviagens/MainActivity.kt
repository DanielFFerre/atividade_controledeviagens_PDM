package br.com.atividade_controledeviagens

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.atividade_controledeviagens.ui.theme.Atividade_controledeviagensTheme
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "preferencias"
)

val NOME_MOTORISTA = stringPreferencesKey("nome_motorista")

class Viagem(
    val data: String,
    val kmInicial: Double,
    val kmFinal: Double,
    val litros: Double,
    val tipoCombustivel: String,
    val pedagio: Double,
    val valorCombustivel: Double,
    val motorista: String = ""
) {
    fun calcularDistancia(): Double {
        return kmFinal - kmInicial
    }

    fun calcularCustoTotal(): Double {
        return valorCombustivel + pedagio
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Atividade_controledeviagensTheme {
                AppControleDeViagens()
            }
        }
    }
}

@Composable
fun AppControleDeViagens() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var motorista by remember { mutableStateOf("") }

    val motoristaFlow = remember {
        context.dataStore.data.map { preferences ->
            preferences[NOME_MOTORISTA] ?: ""
        }
    }

    val motoristaSalvo by motoristaFlow.collectAsState(initial = "")

    LaunchedEffect(motoristaSalvo) {
        if (motoristaSalvo.isNotBlank()) {
            motorista = motoristaSalvo
        }
    }

    val viagens = remember { mutableStateListOf<Viagem>() }

    var data by remember { mutableStateOf("") }
    var kmInicial by remember { mutableStateOf("") }
    var kmFinal by remember { mutableStateOf("") }
    var litros by remember { mutableStateOf("") }
    var tipoCombustivel by remember { mutableStateOf("") }
    var pedagio by remember { mutableStateOf("") }
    var valorCombustivel by remember { mutableStateOf("") }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "lista",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("lista") {
                TelaListaViagens(
                    viagens = viagens,
                    navController = navController,
                    motorista = motorista,
                    motoristaSalvo = motoristaSalvo,
                    onMotoristaChange = { motorista = it },
                    onSalvarMotorista = {
                        if (motorista.isNotBlank()) {
                            scope.launch {
                                context.dataStore.edit { preferences ->
                                    preferences[NOME_MOTORISTA] = motorista
                                }
                            }
                        }
                    }
                )
            }

            composable("cadastro") {
                TelaCadastroViagem(
                    data = data,
                    onDataChange = { data = it },
                    kmInicial = kmInicial,
                    onKmInicialChange = { kmInicial = it },
                    kmFinal = kmFinal,
                    onKmFinalChange = { kmFinal = it },
                    litros = litros,
                    onLitrosChange = { litros = it },
                    tipoCombustivel = tipoCombustivel,
                    onTipoCombustivelChange = { tipoCombustivel = it },
                    pedagio = pedagio,
                    onPedagioChange = { pedagio = it },
                    valorCombustivel = valorCombustivel,
                    onValorCombustivelChange = { valorCombustivel = it },
                    onCadastrar = {
                        val kmIni = kmInicial.toDoubleOrNull() ?: 0.0
                        val kmFim = kmFinal.toDoubleOrNull() ?: 0.0
                        val lit = litros.toDoubleOrNull() ?: 0.0
                        val ped = pedagio.toDoubleOrNull() ?: 0.0
                        val valComb = valorCombustivel.toDoubleOrNull() ?: 0.0

                        if (data.isNotBlank()) {
                            val nomeMotoristaAtual = motoristaSalvo.ifBlank { motorista }

                            val novaViagem = Viagem(
                                data = data,
                                kmInicial = kmIni,
                                kmFinal = kmFim,
                                litros = lit,
                                tipoCombustivel = tipoCombustivel,
                                pedagio = ped,
                                valorCombustivel = valComb,
                                motorista = nomeMotoristaAtual
                            )

                            viagens.add(novaViagem)

                            data = ""
                            kmInicial = ""
                            kmFinal = ""
                            litros = ""
                            tipoCombustivel = ""
                            pedagio = ""
                            valorCombustivel = ""

                            navController.navigateUp()
                        }
                    },
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun TelaListaViagens(
    viagens: List<Viagem>,
    navController: NavController,
    motorista: String,
    motoristaSalvo: String,
    onMotoristaChange: (String) -> Unit,
    onSalvarMotorista: () -> Unit
) {
    val totalKm = viagens.sumOf { it.calcularDistancia() }
    val totalGasto = viagens.sumOf { it.calcularCustoTotal() }
    val totalLitros = viagens.sumOf { it.litros }
    val mediaKmL = if (totalLitros > 0) totalKm / totalLitros else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Controle de Viagens",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(text = "Motorista", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = motorista,
            onValueChange = onMotoristaChange,
            label = { Text("Nome do motorista") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onSalvarMotorista, modifier = Modifier.fillMaxWidth()) {
            Text("Salvar Motorista")
        }

        if (motoristaSalvo.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Motorista atual guardado: $motoristaSalvo",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Resumo Geral", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Total de Km: %.2f km".format(totalKm))
                Text("Total Gasto: R$ %.2f".format(totalGasto))
                Text("Média de Consumo: %.2f km/l".format(mediaKmL))
            }
        }

        Button(
            onClick = { navController.navigate("cadastro") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("Adicionar Nova Viagem")
        }

        Text(
            text = "Viagens Realizadas",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viagens) { viagem ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (viagem.motorista.isNotBlank()) {
                            Text(
                                text = "Motorista: ${viagem.motorista}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(text = "Data: ${viagem.data}", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Combustível: ${viagem.tipoCombustivel} (${viagem.litros} L)")
                        Text(text = "Distância: ${viagem.calcularDistancia()} km (De ${viagem.kmInicial} a ${viagem.kmFinal})")
                        Text(text = "Pedágio: R$ ${viagem.pedagio} | Valor Combustível: R$ ${viagem.valorCombustivel}")
                        Text(text = "Custo da Viagem: R$ ${viagem.calcularCustoTotal()}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun TelaCadastroViagem(
    data: String,
    onDataChange: (String) -> Unit,
    kmInicial: String,
    onKmInicialChange: (String) -> Unit,
    kmFinal: String,
    onKmFinalChange: (String) -> Unit,
    litros: String,
    onLitrosChange: (String) -> Unit,
    tipoCombustivel: String,
    onTipoCombustivelChange: (String) -> Unit,
    pedagio: String,
    onPedagioChange: (String) -> Unit,
    valorCombustivel: String,
    onValorCombustivelChange: (String) -> Unit,
    onCadastrar: () -> Unit,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Registo de Viagem",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        item {
            OutlinedTextField(
                value = data,
                onValueChange = onDataChange,
                label = { Text("Data (ex: 30/09/2026)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = kmInicial,
                onValueChange = onKmInicialChange,
                label = { Text("Km Inicial") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = kmFinal,
                onValueChange = onKmFinalChange,
                label = { Text("Km Final") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = litros,
                onValueChange = onLitrosChange,
                label = { Text("Litros de Combustível") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = tipoCombustivel,
                onValueChange = onTipoCombustivelChange,
                label = { Text("Tipo de Combustível (ex: Gasolina)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = pedagio,
                onValueChange = onPedagioChange,
                label = { Text("Valor do Pedágio (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = valorCombustivel,
                onValueChange = onValorCombustivelChange,
                label = { Text("Valor Abastecido (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = onCadastrar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Gravar")
                }
            }
        }
    }
}