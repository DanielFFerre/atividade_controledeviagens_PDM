package com.example.atividade_controledeviagens

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.atividade_controledeviagens.ui.theme.Atividade_controledeviagensTheme
import kotlinx.coroutines.launch

// --- MODELO ---
class Viagem(
    val data: String,
    val kmInicial: Double,
    val kmFinal: Double,
    val litros: Double,
    val tipoCombustivel: String,
    val pedagio: Double,
    val valorCombustivel: Double
) {
    fun calcularDistancia(): Double {
        return kmFinal - kmInicial
    }

    fun calcularCustoTotal(): Double {
        return valorCombustivel + pedagio
    }
}

// --- ACTIVITY PRINCIPAL ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val motoristaRepository = MotoristaRepository(applicationContext)
        val viagemRepository = ViagemRepository(applicationContext)

        setContent {
            Atividade_controledeviagensTheme() {
                AppControleDeViagens(motoristaRepository, viagemRepository)
            }
        }
    }
}

// --- COMPOSABLE DE GESTÃO DE ESTADO E NAVEGAÇÃO ---
@Composable
fun AppControleDeViagens(
    motoristaRepository: MotoristaRepository,
    viagemRepository: ViagemRepository
) {
    val navController = rememberNavController()

    // Lista central de viagens, inicializada com os dados do banco
    var viagens by remember { mutableStateOf(viagemRepository.listar()) }

    // Variáveis de estado do formulário
    var data by remember { mutableStateOf("") }
    var kmInicial by remember { mutableStateOf("") }
    var kmFinal by remember { mutableStateOf("") }
    var litros by remember { mutableStateOf("") }
    var tipoCombustivel by remember { mutableStateOf("") }
    var pedagio by remember { mutableStateOf("") }
    var valorCombustivel by remember { mutableStateOf("") }
    var mensagemErro by remember { mutableStateOf("") }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "lista",
            modifier = Modifier.padding(innerPadding)
        ) {

            // ROTA 1: ECRÃ DE LISTA E RESUMO
            composable("lista") {
                TelaListaViagens(
                    viagens = viagens,
                    navController = navController,
                    motoristaRepository = motoristaRepository
                )
            }

            // ROTA 2: ECRÃ DE REGISTO DE VIAGEM
            composable("cadastro") {
                TelaCadastroViagem(
                    data = data, onDataChange = { data = it },
                    kmInicial = kmInicial, onKmInicialChange = { kmInicial = it },
                    kmFinal = kmFinal, onKmFinalChange = { kmFinal = it },
                    litros = litros, onLitrosChange = { litros = it },
                    tipoCombustivel = tipoCombustivel, onTipoCombustivelChange = { tipoCombustivel = it },
                    pedagio = pedagio, onPedagioChange = { pedagio = it },
                    valorCombustivel = valorCombustivel, onValorCombustivelChange = { valorCombustivel = it },
                    mensagemErro = mensagemErro,
                    onCadastrar = {
                        val kmIni = kmInicial.toDoubleOrNull() ?: 0.0
                        val kmFim = kmFinal.toDoubleOrNull() ?: 0.0
                        val lit = litros.toDoubleOrNull() ?: 0.0
                        val ped = pedagio.toDoubleOrNull() ?: 0.0
                        val valComb = valorCombustivel.toDoubleOrNull() ?: 0.0

                        if (data.isNotBlank()) {
                            val sucesso = viagemRepository.inserir(
                                data = data,
                                kmInicial = kmIni,
                                kmFinal = kmFim,
                                litros = lit,
                                tipoCombustivel = tipoCombustivel,
                                pedagio = ped,
                                valorCombustivel = valComb
                            )
                            
                            if (sucesso) {
                                // Atualiza a lista lendo do banco novamente
                                viagens = viagemRepository.listar()
                                
                                // Limpar campos
                                data = ""
                                kmInicial = ""
                                kmFinal = ""
                                litros = ""
                                tipoCombustivel = ""
                                pedagio = ""
                                valorCombustivel = ""
                                mensagemErro = ""
    
                                // Regressar à lista
                                navController.navigateUp()
                            } else {
                                mensagemErro = "Erro ao salvar no banco de dados."
                            }
                        } else {
                            mensagemErro = "A data não pode estar vazia."
                        }
                    },
                    navController = navController
                )
            }
        }
    }
}

// --- ECRÃ 1: LISTAGEM ---
@Composable
fun TelaListaViagens(
    viagens: List<Viagem>, 
    navController: NavController,
    motoristaRepository: MotoristaRepository
) {
    val scope = rememberCoroutineScope()
    val nomeSalvo by motoristaRepository.motoristaFlow.collectAsState(initial = "")
    var nomeDigitado by remember { mutableStateOf("") }
    
    // Atualiza o campo digitado quando o nome salvo for carregado
    LaunchedEffect(nomeSalvo) {
        if (nomeDigitado.isEmpty() && nomeSalvo.isNotEmpty()) {
            nomeDigitado = nomeSalvo
        }
    }

    // Cálculos de resumo no ecrã de lista
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

        // Seção do Motorista (DataStore Preferences)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Motorista", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nomeDigitado,
                    onValueChange = { nomeDigitado = it },
                    label = { Text("Nome do Motorista") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            motoristaRepository.salvarMotorista(nomeDigitado)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Salvar Motorista")
                }
                
                if (nomeSalvo.isNotBlank()) {
                    Text(
                        text = "Motorista atual: $nomeSalvo",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Cartão de Resumo
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

        // Botão para ir para o registo
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

        // Listagem de Viagens com LazyColumn
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viagens) { viagem ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Data: ${viagem.data}", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Combustível: ${viagem.tipoCombustivel} (${viagem.litros} L)")
                        Text(text = "Distância: ${viagem.calcularDistancia()} km (De ${viagem.kmInicial} a ${viagem.kmFinal})")
                        Text(text = "Pedágio: R$ ${viagem.pedagio} | Valor Combustível: R$ ${viagem.valorCombustivel}")
                        Text(
                            text = "Custo da Viagem: R$ ${viagem.calcularCustoTotal()}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

// --- ECRÃ 2: FORMULÁRIO DE REGISTO ---
@Composable
fun TelaCadastroViagem(
    data: String, onDataChange: (String) -> Unit,
    kmInicial: String, onKmInicialChange: (String) -> Unit,
    kmFinal: String, onKmFinalChange: (String) -> Unit,
    litros: String, onLitrosChange: (String) -> Unit,
    tipoCombustivel: String, onTipoCombustivelChange: (String) -> Unit,
    pedagio: String, onPedagioChange: (String) -> Unit,
    valorCombustivel: String, onValorCombustivelChange: (String) -> Unit,
    mensagemErro: String,
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
                value = data, onValueChange = onDataChange,
                label = { Text("Data (ex: 30/09/2026)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = kmInicial, onValueChange = onKmInicialChange,
                label = { Text("Km Inicial") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = kmFinal, onValueChange = onKmFinalChange,
                label = { Text("Km Final") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = litros, onValueChange = onLitrosChange,
                label = { Text("Litros de Combustível") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = tipoCombustivel, onValueChange = onTipoCombustivelChange,
                label = { Text("Tipo de Combustível (ex: Gasolina)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = pedagio, onValueChange = onPedagioChange,
                label = { Text("Valor do Pedágio (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = valorCombustivel, onValueChange = onValorCombustivelChange,
                label = { Text("Valor Abastecido (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        if (mensagemErro.isNotEmpty()) {
            item {
                Text(
                    text = mensagemErro,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
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
