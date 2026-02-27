// app/src/main/java/com/example/arki_deportes/MainActivity.kt

package com.example.arki_deportes

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.arki_deportes.data.model.Partido
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arki_deportes.data.Repository
import com.example.arki_deportes.data.local.ConfigManager
import com.example.arki_deportes.ui.catalogs.CatalogsRoute
import com.example.arki_deportes.ui.home.HomeRoute
import com.example.arki_deportes.ui.home.HomeViewModel
import com.example.arki_deportes.ui.home.HomeViewModelFactory
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository

import com.example.arki_deportes.ui.realtime.TiempoRealScreen
import com.example.arki_deportes.ui.realtime.TiempoRealViewModel
import com.example.arki_deportes.ui.realtime.TiempoRealViewModelFactory

import com.example.arki_deportes.ui.menciones.MencionesRoute
import com.example.arki_deportes.ui.menciones.MencionesViewModel
import com.example.arki_deportes.ui.menciones.MencionesViewModelFactory

import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.arki_deportes.navigation.AppDestinations
import com.example.arki_deportes.navigation.AppNavGraph
import com.example.arki_deportes.navigation.AppNavigator

import com.example.arki_deportes.navigation.DrawerContent


import com.example.arki_deportes.navigation.rememberAppNavigator


import com.example.arki_deportes.ui.produccion.EquipoProduccionRoute
import com.example.arki_deportes.ui.produccion.EquipoProduccionViewModel
import com.example.arki_deportes.ui.produccion.EquipoProduccionViewModelFactory
import com.example.arki_deportes.ui.theme.Arki_DeportesTheme
import com.example.arki_deportes.ui.campeonatos.CampeonatoFormScreen
import com.example.arki_deportes.ui.equipos.EquipoFormScreen
import com.example.arki_deportes.ui.grupos.GrupoFormScreen
import com.example.arki_deportes.ui.partidos.PartidoFormScreen
import com.example.arki_deportes.ui.settings.SettingsRoute
import com.example.arki_deportes.utils.Constants
import com.example.arki_deportes.data.auth.AuthenticationManager
import com.example.arki_deportes.data.context.PartidoContext
import com.example.arki_deportes.data.context.CampeonatoContext
import com.example.arki_deportes.data.context.DeporteContext
import com.example.arki_deportes.data.context.UsuarioContext
import com.example.arki_deportes.data.model.ResultadoAutenticacion
import com.google.firebase.auth.FirebaseAuth

// Compose Foundation
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

// Compose Material Icons - Person, Lock, KeyboardHide
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.KeyboardHide

// Text Input
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalFocusManager
import com.example.arki_deportes.utils.SportType
import androidx.compose.runtime.saveable.rememberSaveable

// ────────────────────────────────────────────────────────────────────────────
// Accompanist (SwipeRefresh)
// ────────────────────────────────────────────────────────────────────────────
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

// ────────────────────────────────────────────────────────────────────────────
// Coroutines
// ────────────────────────────────────────────────────────────────────────────
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
// Imports para listas de catálogos
import com.example.arki_deportes.ui.campeonatos.CampeonatoListRoute
import com.example.arki_deportes.ui.equipos.EquipoListRoute
import com.example.arki_deportes.ui.partidos.PartidoListRoute
import com.example.arki_deportes.ui.grupos.GrupoListRoute
import com.example.arki_deportes.ui.envivo.PartidosEnVivoScreen
import com.example.arki_deportes.ui.envivo.PartidosEnVivoViewModel
import com.example.arki_deportes.ui.envivo.PartidosEnVivoViewModelFactory
import androidx.lifecycle.ViewModelProvider // Necesario para la Factory
/**
 * ═══════════════════════════════════════════════════════════════════════════
 * MAIN ACTIVITY - ACTIVIDAD PRINCIPAL
 * ═══════════════════════════════════════════════════════════════════════════
 */
class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"
    private lateinit var auth: FirebaseAuth
    private lateinit var configManager: ConfigManager
    private lateinit var database: FirebaseDatabase
    private lateinit var authManager: AuthenticationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        inicializarFirebase()
        inicializarConfiguracion()
        inicializarAuthManager()
        signInAnonymously()
        enableEdgeToEdge()

        setContent {
            Arki_DeportesTheme {
                val navController = rememberNavController()
                val navigator = rememberAppNavigator(navController)
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                val drawerEnabled = currentRoute != AppDestinations.LOGIN

                val openDrawer: () -> Unit = {
                    scope.launch { drawerState.open() }
                }
                val closeDrawer: () -> Unit = {
                    scope.launch { drawerState.close() }
                }

                val handleLogout = {
                    borrarCredencialesLocal()
                    configManager.cerrarSesion()
                }

                val catalogRepo = remember(database, configManager) {
                    FirebaseCatalogRepository(
                        database = database,
                        rootNode = configManager.obtenerNodoRaiz()
                    )
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = drawerEnabled,
                    drawerContent = {
                        if (drawerEnabled) {
                            DrawerContent(
                                navigator = navigator,
                                onCloseDrawer = closeDrawer,
                                currentRoute = currentRoute,
                                onLogout = handleLogout,
                                catalogRepository = catalogRepo
                            )
                        }
                    }
                ) {
                    AppNavGraph(
                        navController = navController,
                        navigator = navigator,
                        loginRoute = { navigatorParam -> PantallaInicio(navigatorParam) },
                        hybridHomeRoute = { navigatorParam -> PantallaBienvenida(navigatorParam, openDrawer = openDrawer) },
                        realTimeRoute = { navigatorParam, campeonatoId, partidoId ->
                            PantallaTiempoReal(
                                navigator = navigatorParam,
                                campeonatoId = campeonatoId,
                                partidoId = partidoId,
                                openDrawer = openDrawer
                            )
                        },
                        partidosEnVivoRoute = { n ->
                            PantallaPartidosEnVivo(n, openDrawer = openDrawer) // 👈 PASAR LA NUEVA FUNCIÓN
                        },
                        catalogsRoute = { navigatorParam -> PantallaCatalogos(navigatorParam, openDrawer = openDrawer) },
                        mencionesRoute = { navigatorParam -> PantallaMenciones(navigatorParam, openDrawer = openDrawer) },
                        equipoProduccionRoute = { navigatorParam -> PantallaEquipoProduccion(navigatorParam, openDrawer = openDrawer) },
                        settingsRoute = { navigatorParam ->
                            PantallaConfiguracion(
                                navigator = navigatorParam,
                                openDrawer = openDrawer,
                                onLogout = handleLogout
                            )
                        },
                        campeonatoListRoute = { navigatorParam: AppNavigator ->
                            PantallaCampeonatoList(navigatorParam, openDrawer = openDrawer)
                        },
                        grupoListRoute = { navigatorParam: AppNavigator ->
                            PantallaGrupoList(navigatorParam, openDrawer = openDrawer)
                        },
                        equipoListRoute = { navigatorParam: AppNavigator ->
                            PantallaEquipoList(navigatorParam, openDrawer = openDrawer)
                        },
                        partidoListRoute = { navigatorParam: AppNavigator ->
                            PantallaPartidoList(navigatorParam, openDrawer = openDrawer)
                        },
                        campeonatoFormRoute = { navigatorParam, codigo -> PantallaCampeonatoForm(navigatorParam, codigo) },
                        grupoFormRoute = { navigatorParam, codigo -> PantallaGrupoForm(navigatorParam, codigo) },
                        equipoFormRoute = { navigatorParam, codigo -> PantallaEquipoForm(navigatorParam, codigo) },
                        partidoFormRoute = { navigatorParam, codigo -> PantallaPartidoForm(navigatorParam, codigo) }
                    )
                }
            }
        }
    }

    private fun inicializarAuthManager() {
        try {
            authManager = AuthenticationManager(database, configManager)
            Log.d(TAG, "✅ AuthenticationManager inicializado")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al inicializar AuthManager", e)
        }
    }

    @Composable
    fun PantallaTiempoReal(
        navigator: AppNavigator,
        campeonatoId: String,
        partidoId: String,
        openDrawer: () -> Unit
    ) {
        val repository = remember(database, configManager) {
            FirebaseCatalogRepository(
                database = database,
                rootNode = configManager.obtenerNodoRaiz()
            )
        }

        val viewModel: TiempoRealViewModel = viewModel(
            factory = TiempoRealViewModelFactory(repository, campeonatoId, partidoId)
        )

        TiempoRealScreen(
            viewModel = viewModel,
            onNavigateBack = {
                if (!navigator.navigateBack()) {
                    navigator.navigateToHybridHome()
                }
            },
            onOpenDrawer = openDrawer // 👈 Pasamos la función
        )
    }

    @Composable
    fun PantallaPartidosEnVivo(navigator: AppNavigator, openDrawer: () -> Unit) {
        val catalogRepo = remember(database, configManager) {
            FirebaseCatalogRepository(database, configManager.obtenerNodoRaiz())
        }
        // Asegúrate de importar PartidosEnVivoViewModel, Factory y Screen
        val viewModel: PartidosEnVivoViewModel = viewModel(
            factory = PartidosEnVivoViewModelFactory(catalogRepo)
        )
        PartidosEnVivoScreen(
            viewModel = viewModel,
            onPartidoClick = { campeonatoId, partidoId ->
                // Futuro: navegar al monitor de detalle para el narrador
                // navigator.navigateToMonitor(campeonatoId, partidoId)
            },
            onOpenDrawer = openDrawer
        )
    }

    @Composable
    fun PantallaCatalogos(navigator: AppNavigator, openDrawer: () -> Unit) {
        CatalogsRoute(
            onNavigateBack = {
                if (!navigator.navigateBack()) {
                    navigator.navigateToHybridHome()
                }
            },
            onOpenDrawer = openDrawer
        )
    }

    @Composable
    fun PantallaCampeonatoList(navigator: AppNavigator, openDrawer: () -> Unit) {
        CampeonatoListRoute(
            onNavigateBack = {
                if (!navigator.navigateBack()) {
                    navigator.navigateToHybridHome()
                }
            },
            onOpenDrawer = openDrawer,
            onCreateCampeonato = {
                navigator.navigateToCampeonatoForm(null)
            },
            onEditCampeonato = { codigo ->
                navigator.navigateToCampeonatoForm(codigo)
            }
        )
    }

    @Composable
    fun PantallaGrupoList(navigator: AppNavigator, openDrawer: () -> Unit) {
        GrupoListRoute(
            onNavigateBack = {
                if (!navigator.navigateBack()) {
                    navigator.navigateToHybridHome()
                }
            },
            onOpenDrawer = openDrawer
        )
    }

    @Composable
    fun PantallaEquipoList(navigator: AppNavigator, openDrawer: () -> Unit) {
        EquipoListRoute(
            onNavigateBack = {
                if (!navigator.navigateBack()) {
                    navigator.navigateToHybridHome()
                }
            },
            onOpenDrawer = openDrawer,
            onCreateEquipo = {
                navigator.navigateToEquipoForm(null)
            },
            onEditEquipo = { codigo ->
                navigator.navigateToEquipoForm(codigo)
            }
        )
    }

    @Composable
    fun PantallaPartidoList(navigator: AppNavigator, openDrawer: () -> Unit) {
        PartidoListRoute(
            onNavigateBack = {
                if (!navigator.navigateBack()) {
                    navigator.navigateToHybridHome()
                }
            },
            onOpenDrawer = openDrawer,
            onCreatePartido = {
                navigator.navigateToPartidoForm(null)
            },
            onEditPartido = { codigo ->
                navigator.navigateToPartidoForm(codigo)
            }
        )
    }

    @Composable
    fun PantallaMenciones(navigator: AppNavigator, openDrawer: () -> Unit) {
        val repository = remember(database, configManager) {
            Repository(database, configManager)
        }
        val viewModel: MencionesViewModel = viewModel(
            factory = MencionesViewModelFactory(repository)
        )

        MencionesRoute(
            viewModel = viewModel,
            onNavigateBack = {
                if (!navigator.navigateBack()) {
                    navigator.navigateToHybridHome()
                }
            },
            onOpenDrawer = openDrawer
        )
    }

    @Composable
    fun PantallaEquipoProduccion(navigator: AppNavigator, openDrawer: () -> Unit) {
        val repository = remember(database, configManager) {
            Repository(database, configManager)
        }
        val viewModel: EquipoProduccionViewModel = viewModel(
            factory = EquipoProduccionViewModelFactory(repository)
        )

        EquipoProduccionRoute(
            viewModel = viewModel,
            onBack = {
                if (!navigator.navigateBack()) {
                    navigator.navigateToHybridHome()
                }
            },
            onOpenDrawer = openDrawer
        )
    }

    @Composable
    fun PantallaCampeonatoForm(navigator: AppNavigator, codigo: String?) {
        CampeonatoFormScreen(
            codigoCampeonato = codigo,
            onBack = {
                if (!navigator.navigateBack()) {
                    navigator.navigateToCatalogs()
                }
            },
            onNavigateToSeries = { id ->
                navigator.navigateTo("serie_list/$id")
            }
        )
    }

    @Composable
    fun PantallaGrupoForm(navigator: AppNavigator, codigo: String?) {
        GrupoFormScreen(
            codigoGrupo = codigo,
            onBack = {
                if (!navigator.navigateBack()) {
                    navigator.navigateToCatalogs()
                }
            }
        )
    }

    @Composable
    fun PantallaEquipoForm(navigator: AppNavigator, codigo: String?) {
        EquipoFormScreen(
            codigoEquipo = codigo,
            onBack = {
                if (!navigator.navigateBack()) {
                    navigator.navigateToCatalogs()
                }
            }
        )
    }

    @Composable
    fun PantallaPartidoForm(navigator: AppNavigator, codigo: String?) {
        PartidoFormScreen(
            codigoPartido = codigo,
            onBack = {
                if (!navigator.navigateBack()) {
                    navigator.navigateToCatalogs()
                }
            }
        )
    }

    @Composable
    fun PantallaConfiguracion(
        navigator: AppNavigator,
        openDrawer: () -> Unit,
        onLogout: () -> Unit
    ) {
        var nodeValue by remember { mutableStateOf(configManager.obtenerNodoRaiz()) }
        val lastSync by remember { mutableStateOf(configManager.obtenerUltimaSincronizacion()) }

        SettingsRoute(
            nodeValue = nodeValue,
            onNodeValueChange = { nodeValue = it },
            onSaveNode = { configManager.guardarNodoRaiz(nodeValue) },
            onResetNode = {
                configManager.resetearNodoRaiz()
                nodeValue = configManager.obtenerNodoRaiz()
            },
            onLogout = {
                onLogout()
                navigator.navigateToLogin(clearBackStack = true)
            },
            onOpenDrawer = openDrawer,
            onNavigateBack = {
                if (!navigator.navigateBack()) {
                    navigator.navigateToHybridHome()
                }
            },
            lastSyncTimestamp = lastSync
        )
    }

    private fun inicializarFirebase() {
        try {
            auth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al inicializar Firebase", e)
        }
    }

    private fun inicializarConfiguracion() {
        try {
            configManager = ConfigManager(this)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al inicializar ConfigManager", e)
        }
    }

    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d(TAG, "✅ Autenticación anónima exitosa: ${user?.uid}")
                }
            }
    }

    private fun leerPasswordFirebase(onPasswordRead: (String?) -> Unit) {
        val nodoRaiz = configManager.obtenerNodoRaiz()
        val reference = database.reference
            .child(nodoRaiz)
            .child(Constants.FirebaseCollections.ACCESO)
            .child(Constants.AccesoFields.PASSWORD)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onPasswordRead(snapshot.getValue(String::class.java))
            }
            override fun onCancelled(error: DatabaseError) {
                onPasswordRead(null)
            }
        })
    }

    private fun validarPassword(
        usuarioIngresado: String,
        passwordIngresado: String,
        onResult: (Boolean) -> Unit
    ) {
        leerPasswordFirebase { passwordFirebase ->
            val esCorrecta = passwordIngresado == passwordFirebase
            if (esCorrecta) {
                guardarCredencialesLocal(usuarioIngresado, passwordIngresado)
            }
            onResult(esCorrecta)
        }
    }

    private fun guardarCredencialesLocal(usuario: String, password: String) {
        val prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("usuario_memorizado", usuario.trim())
        editor.putString("password_memorizado", password)
        editor.commit()
    }

    private fun obtenerUsuarioLocal(): String? {
        val prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE)
        return prefs.getString("usuario_memorizado", null)
    }

    private fun obtenerPasswordLocal(): String? {
        val prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE)
        return prefs.getString("password_memorizado", null)
    }

    private fun borrarCredencialesLocal() {
        val prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove("usuario_memorizado")
        editor.remove("password_memorizado")
        editor.commit()
    }

    @Composable
    fun PantallaInicio(navigator: AppNavigator) {
        var estadoApp by remember { mutableStateOf(EstadoApp.CARGANDO) }
        var autenticacionCompleta by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(2500)
            val user = auth.currentUser
            if (user != null) {
                autenticacionCompleta = true
            } else {
                delay(1000)
                if (auth.currentUser != null) autenticacionCompleta = true
                else estadoApp = EstadoApp.REQUIERE_LOGIN
            }
        }

        LaunchedEffect(autenticacionCompleta) {
            if (!autenticacionCompleta) return@LaunchedEffect
            estadoApp = EstadoApp.REQUIERE_LOGIN
        }

        LaunchedEffect(estadoApp) {
            if (estadoApp == EstadoApp.AUTENTICADO) {
                navigator.navigateToHybridHome(clearBackStack = true)
            }
        }

        when (estadoApp) {
            EstadoApp.CARGANDO -> PantallaCargando()
            EstadoApp.REQUIERE_LOGIN -> PantallaLogin(navigator)
            EstadoApp.AUTENTICADO -> PantallaCargando()
        }
    }

    @Composable
    fun PantallaCargando() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.logowilmer),
                    contentDescription = "Logo Wilmer",
                    modifier = Modifier.size(140.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = Constants.EMPRESA_NOMBRE,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Iniciando...", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }

    private fun cargarPartidoYNavegar(
        campeonatoId: String,
        partidoId: String,
        navigator: AppNavigator
    ) {
        lifecycleScope.launch {
            try {
                val repository = FirebaseCatalogRepository(database, configManager.obtenerNodoRaiz())
                val partido = repository.getPartido(campeonatoId, partidoId)

                if (partido != null) {
                    val haCaducado = verificarSiEstaCaducado(partido.FECHA_PARTIDO)
                    if (haCaducado) {
                        Log.w(TAG, "⚠️ Partido CADUCADO. Se mostrará aviso en Home.")
                        // NO BORRAMOS AUTOMÁTICAMENTE. Solo navegamos.
                        navigator.navigateToHybridHome(clearBackStack = true)
                    } else {
                        PartidoContext.setPartidoActivo(partido)
                        val campeonato = repository.getCampeonato(campeonatoId)
                        if (campeonato != null) {
                            CampeonatoContext.seleccionarCampeonato(campeonato)
                            val deporte = SportType.fromId(campeonato.DEPORTE)
                            DeporteContext.seleccionarDeporte(deporte)
                        }
                        navigator.navigateToTiempoReal(
                            campeonatoId = campeonatoId,
                            partidoId = partidoId,
                            clearBackStack = true
                        )
                    }
                } else {
                    navigator.navigateToHybridHome(clearBackStack = true)
                }
            } catch (e: Exception) {
                navigator.navigateToHybridHome(clearBackStack = true)
            }
        }
    }

    private fun verificarSiEstaCaducado(fechaStr: String?): Boolean {
        if (fechaStr.isNullOrBlank()) return false
        val formatos = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
        )
        val hoy = LocalDate.now()
        for (formato in formatos) {
            try {
                val fechaPartido = LocalDate.parse(fechaStr.trim(), formato)
                val fechaLimite = fechaPartido.plusDays(1)
                return hoy.isAfter(fechaLimite)
            } catch (e: Exception) {}
        }
        return false
    }

    private fun eliminarPartidoAsignado() {
        try {
            val usuarioActual = UsuarioContext.getUsuario() ?: return
            val nombreUsuario = usuarioActual.usuario
            if (nombreUsuario.isBlank()) return

            val rutaPermisos = database.reference
                .child("AppConfig")
                .child("Usuarios")
                .child(nombreUsuario)
                .child("permisos")

            val actualizaciones = mapOf(
                "codigoCampeonato" to "NINGUNO",
                "codigoPartido" to "NINGUNO"
            )

            rutaPermisos.updateChildren(actualizaciones)
                .addOnSuccessListener {
                    Log.d(TAG, "✅ Firebase: Permisos reseteados correctamente")
                    UsuarioContext.limpiarPartidoAsignado()
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "❌ Error al resetear permisos: ${error.message}")
                }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error crítico: ${e.message}")
        }
    }

    @Composable
    fun PantallaLogin(navigator: AppNavigator) {
        var passwordVisible by remember { mutableStateOf(false) }
        var mensajeError by remember { mutableStateOf("") }
        var cargando by remember { mutableStateOf(false) }

        val usuarioInicial = remember { obtenerUsuarioLocal().orEmpty() }
        val passwordInicial = remember { obtenerPasswordLocal().orEmpty() }

        var usuario by rememberSaveable { mutableStateOf(usuarioInicial) }
        var password by rememberSaveable { mutableStateOf(passwordInicial) }
        val focusManager = LocalFocusManager.current

        Box(
            modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Image(painter = painterResource(id = R.drawable.logowilmer), contentDescription = "Logo", modifier = Modifier.size(140.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = Constants.APP_NOMBRE, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = if (usuarioInicial.isNotBlank()) "Bienvenido de nuevo, $usuarioInicial" else "Ingresa tus credenciales", fontSize = 14.sp, color = if (usuarioInicial.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = { focusManager.clearFocus() }, modifier = Modifier.fillMaxWidth(), enabled = !cargando) {
                    Icon(imageVector = Icons.Default.KeyboardHide, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ocultar Teclado")
                }
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(value = usuario, onValueChange = { usuario = it; mensajeError = "" }, label = { Text("Usuario") }, singleLine = true, leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), modifier = Modifier.fillMaxWidth(), enabled = !cargando)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = password, onValueChange = { password = it; mensajeError = "" }, label = { Text("Contraseña") }, singleLine = true, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }), modifier = Modifier.fillMaxWidth(), enabled = !cargando, leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) }, trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null) } })
                if (mensajeError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = mensajeError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (usuario.isBlank() || password.isBlank()) { mensajeError = "Ingresa credenciales"; return@Button }
                        cargando = true
                        authManager.login(usuario, password) { resultado ->
                            cargando = false
                            when (resultado) {
                                is ResultadoAutenticacion.Exito -> {
                                    guardarCredencialesLocal(usuario, password)
                                    val user = resultado.usuario
                                    UsuarioContext.setUsuario(user)
                                    val pId = user.permisos.codigoPartido
                                    val cId = user.permisos.codigoCampeonato
                                    if (!cId.isNullOrBlank() && cId != "NINGUNO" && !pId.isNullOrBlank() && pId != "NINGUNO") {
                                        cargarPartidoYNavegar(cId, pId, navigator)
                                    } else {
                                        navigator.navigateToHybridHome(clearBackStack = true)
                                    }
                                }
                                else -> mensajeError = "Error de autenticación"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !cargando
                ) {
                    if (cargando) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    else Text("Iniciar Sesión", fontSize = 16.sp)
                }
            }
        }
    }

    @Composable
    fun PantallaBienvenida(navigator: AppNavigator, openDrawer: () -> Unit) {
        val catalogRepo = remember(database, configManager) {
            FirebaseCatalogRepository(database, configManager.obtenerNodoRaiz())
        }
        val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(catalogRepo, database))
        HomeRoute(
            viewModel = homeViewModel,
            onNavigateToPartidos = { navigator.navigateToPartidoList() },
            onNavigateToCampeonatos = { navigator.navigateToCampeonatoList() },
            onOpenDrawer = openDrawer
        )
    }

    enum class EstadoApp { CARGANDO, REQUIERE_LOGIN, AUTENTICADO }
}
