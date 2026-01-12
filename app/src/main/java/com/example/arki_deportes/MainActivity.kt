// app/src/main/java/com/example/arki_deportes/MainActivity.kt

package com.example.arki_deportes

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
import androidx.compose.ui.platform.LocalFocusManager  // ← NUEVO
import androidx.compose.material.icons.filled.KeyboardHide  // ← NUEVO

import com.example.arki_deportes.utils.SportType

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
/**
 * ═══════════════════════════════════════════════════════════════════════════
 * MAIN ACTIVITY - ACTIVIDAD PRINCIPAL
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Punto de entrada de la aplicación.
 *
 * Funciones principales:
 * 1. Inicializa Firebase Authentication (autenticación anónima)
 * 2. Verifica si hay una contraseña memorizada localmente
 * 3. Si hay contraseña guardada, valida contra Firebase
 * 4. Si no hay o es incorrecta, muestra pantalla de login
 * 5. Lee el nodo Acceso/password de Firebase para validar
 *
 * Flujo de seguridad:
 * - El administrador cambia la contraseña en Firebase antes de cada partido
 * - La contraseña se envía por WhatsApp al personal autorizado
 * - La app memoriza la contraseña correcta para no pedirla cada vez
 * - Si la contraseña cambia en Firebase, se solicita la nueva
 *
 * @author ARKI SISTEMAS
 * @version 1.0.0
 */
class MainActivity : ComponentActivity() {

    // ═══════════════════════════════════════════════════════════════════════
    // PROPIEDADES
    // ═══════════════════════════════════════════════════════════════════════

    /** TAG para logs */
    private val TAG = "MainActivity"

    /** Instancia de FirebaseAuth para autenticación */
    private lateinit var auth: FirebaseAuth

    /** Instancia de ConfigManager para gestión de configuración local */
    private lateinit var configManager: ConfigManager

    /** Instancia de Firebase Realtime Database */
    private lateinit var database: FirebaseDatabase

    // ═══════════════════════════════════════════════════════════════════════
    // CICLO DE VIDA
    // ═══════════════════════════════════════════════════════════════════════
// ✅ AGREGAR ESTA LÍNEA:
    private lateinit var authManager: AuthenticationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "═══════════════════════════════════════════════════════")
        Log.d(TAG, "🚀 Iniciando ${Constants.APP_NOMBRE}")
        Log.d(TAG, "📱 Versión: ${Constants.APP_VERSION}")
        Log.d(TAG, "🏢 Empresa: ${Constants.EMPRESA_NOMBRE}")
        Log.d(TAG, "═══════════════════════════════════════════════════════")

        // Inicializar Firebase y configuración
        inicializarFirebase()
        inicializarConfiguracion()
        inicializarAuthManager()  // ← AÑADIR

        // Autenticación anónima con Firebase (permite leer/escribir sin login de usuario)
        signInAnonymously()

        // Habilitar edge-to-edge (pantalla completa)
        enableEdgeToEdge()

        // Configurar el contenido de la UI
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

                    borrarPasswordLocal()
                    configManager.cerrarSesion()
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
                                onLogout = handleLogout
                            )
                        }
                    }
                ) {
                    AppNavGraph(
                        navController = navController,
                        navigator = navigator,
                        loginRoute = { navigatorParam -> PantallaInicio(navigatorParam) },
                        hybridHomeRoute = { navigatorParam -> PantallaBienvenida(navigatorParam, openDrawer = openDrawer) },
                        realTimeRoute = { navigatorParam -> PantallaTiempoReal(navigatorParam, openDrawer = openDrawer) },
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
                        campeonatoFormRoute = { navigatorParam, codigo -> PantallaCampeonatoForm(navigatorParam, codigo) },
                        grupoFormRoute = { navigatorParam, codigo -> PantallaGrupoForm(navigatorParam, codigo) },
                        equipoFormRoute = { navigatorParam, codigo -> PantallaEquipoForm(navigatorParam, codigo) },
                        partidoFormRoute = { navigatorParam, codigo -> PantallaPartidoForm(navigatorParam, codigo) }
                    )
                }
            }

        }
    }

    /**
     * Inicializa el AuthenticationManager
     */
    private fun inicializarAuthManager() {
        try {
            authManager = AuthenticationManager(database, configManager)
            Log.d(TAG, "✅ AuthenticationManager inicializado")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al inicializar AuthManager", e)
        }
    }


    @Composable
    fun PantallaTiempoReal(navigator: AppNavigator, openDrawer: () -> Unit) {

        // ✅ Este es el tipo que espera tu TiempoRealViewModel
        val repository = remember(database, configManager) {
            FirebaseCatalogRepository(
                database = database,
                rootNode = configManager.obtenerNodoRaiz() // usa tu método real
            )
        }

        // ✅ IDs temporales para compilar (luego los conectamos al "partido actual")
        val campeonatoId = remember { "" }
        val partidoId = remember { "" }

        val viewModel: TiempoRealViewModel = viewModel(
            factory = TiempoRealViewModelFactory(repository, campeonatoId, partidoId)
        )

        TiempoRealScreen(
            viewModel = viewModel,
            onNavigateBack = {
                if (!navigator.navigateBack()) {
                    navigator.navigateToHybridHome()
                }
            }
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


    // ═══════════════════════════════════════════════════════════════════════
    // INICIALIZACIÓN
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Inicializa Firebase Authentication y Database
     */
    private fun inicializarFirebase() {
        try {
            auth = FirebaseAuth.getInstance()
            Log.d(TAG, "✅ Firebase Auth inicializado")

            database = FirebaseDatabase.getInstance()
            Log.d(TAG, "✅ Firebase Database inicializado")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al inicializar Firebase", e)
        }
    }

    /**
     * Inicializa el gestor de configuración local (SharedPreferences)
     */
    private fun inicializarConfiguracion() {
        try {
            configManager = ConfigManager(this)
            val nodoRaiz = configManager.obtenerNodoRaiz()
            Log.d(TAG, "✅ ConfigManager inicializado")
            Log.d(TAG, "📍 Nodo raíz configurado: /$nodoRaiz")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al inicializar ConfigManager", e)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // AUTENTICACIÓN FIREBASE
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Realiza autenticación anónima con Firebase
     *
     * ✅ Esta función está CORRECTA
     *
     * La autenticación anónima permite que la app:
     * - Lea datos de Firebase Realtime Database
     * - Escriba datos en Firebase
     * - Sin necesidad de crear cuentas de usuario
     *
     * Es perfecta para este caso de uso donde solo necesitamos
     * un control de acceso simple con contraseña compartida.
     */
    private fun signInAnonymously() {
        Log.d(TAG, "🔐 Iniciando autenticación anónima con Firebase...")

        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d(TAG, "✅ Autenticación anónima exitosa")
                    Log.d(TAG, "👤 UID: ${user?.uid}")
                    Log.d(TAG, "🔓 Usuario puede leer/escribir en Firebase")

                } else {
                    Log.e(TAG, "❌ Error en autenticación anónima", task.exception)
                    Log.e(TAG, "📛 Mensaje: ${task.exception?.message}")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "❌ Fallo crítico en autenticación", exception)
            }
    }

    /**
     * Lee la contraseña actual desde Firebase
     * Ruta: /[NODO_RAIZ]/Acceso/password
     *
     * @param onPasswordRead Callback que recibe la contraseña leída
     */
    private fun leerPasswordFirebase(onPasswordRead: (String?) -> Unit) {
        val nodoRaiz = configManager.obtenerNodoRaiz()
        val reference = database.reference
            .child(nodoRaiz)
            .child(Constants.FirebaseCollections.ACCESO)
            .child(Constants.AccesoFields.PASSWORD)

        Log.d(TAG, "🔍 Leyendo contraseña desde: /$nodoRaiz/Acceso/password")

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val passwordFirebase = snapshot.getValue(String::class.java)

                if (passwordFirebase != null) {
                    Log.d(TAG, "✅ Contraseña leída desde Firebase")
                    onPasswordRead(passwordFirebase)
                } else {
                    Log.w(TAG, "⚠️ No se encontró el nodo Acceso/password en Firebase")
                    Log.d(TAG, "💡 Crea el nodo manualmente en Firebase Console:")
                    Log.d(TAG, "   Ruta: /$nodoRaiz/Acceso/password")
                    Log.d(TAG, "   Valor: tu_contraseña_aqui")
                    onPasswordRead(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "❌ Error al leer contraseña de Firebase", error.toException())
                onPasswordRead(null)
            }
        })
    }

    /**
     * Valida una contraseña contra Firebase
     *
     * @param passwordIngresado Contraseña ingresada por el usuario
     * @param onResult Callback con resultado (true = correcta, false = incorrecta)
     */
    private fun validarPassword(passwordIngresado: String, onResult: (Boolean) -> Unit) {
        leerPasswordFirebase { passwordFirebase ->
            if (passwordFirebase != null) {
                val esCorrecta = passwordIngresado == passwordFirebase

                if (esCorrecta) {
                    Log.d(TAG, "✅ Contraseña correcta")
                    // Memorizar la contraseña correcta
                    guardarPasswordLocal(passwordIngresado)
                } else {
                    Log.d(TAG, "❌ Contraseña incorrecta")
                }

                onResult(esCorrecta)
            } else {
                Log.w(TAG, "⚠️ No se pudo validar (Firebase no respondió)")
                onResult(false)
            }
        }
    }

    /**
     * Guarda la contraseña en SharedPreferences (memorización local)
     *
     * @param password Contraseña a guardar
     */
    private fun guardarPasswordLocal(password: String) {
        val prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE)
        prefs.edit().putString("password_memorizado", password).apply()
        Log.d(TAG, "💾 Contraseña memorizada localmente")
    }

    /**
     * Obtiene la contraseña memorizada localmente
     *
     * @return Contraseña guardada o null si no existe
     */
    private fun obtenerPasswordLocal(): String? {
        val prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE)
        return prefs.getString("password_memorizado", null)
    }

    /**
     * Borra la contraseña memorizada (al cerrar sesión o cambiar de contraseña)
     */
    private fun borrarPasswordLocal() {
        val prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE)
        prefs.edit().remove("password_memorizado").apply()
        Log.d(TAG, "🗑️ Contraseña local borrada")
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UI COMPOSABLE - PANTALLA DE INICIO
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Pantalla de inicio
     * Muestra logo, verifica password memorizado y decide qué mostrar
     */
    @Composable
    fun PantallaInicio(navigator: AppNavigator) {
        var estadoApp by remember { mutableStateOf(EstadoApp.CARGANDO) }
        var autenticacionCompleta by remember { mutableStateOf(false) }

        // Esperar a que la autenticación anónima complete
        LaunchedEffect(Unit) {
            // Esperar a que Firebase Auth complete la autenticación
            delay(2500) // Aumentar el tiempo de espera

            // Verificar si ya estamos autenticados
            val user = auth.currentUser
            if (user != null) {
                Log.d(TAG, "✅ Usuario autenticado: ${user.uid}")
                autenticacionCompleta = true
            } else {
                Log.w(TAG, "⚠️ Autenticación no completada, reintentando...")
                delay(1000)
                val userRetry = auth.currentUser
                if (userRetry != null) {
                    Log.d(TAG, "✅ Usuario autenticado (segundo intento): ${userRetry.uid}")
                    autenticacionCompleta = true
                } else {
                    Log.e(TAG, "❌ No se pudo autenticar")
                    estadoApp = EstadoApp.REQUIERE_LOGIN
                }
            }
        }

        // Al completar autenticación, verificar si hay password memorizado
        LaunchedEffect(autenticacionCompleta) {
            if (!autenticacionCompleta) return@LaunchedEffect

            Log.d(TAG, "🔍 Autenticación completa, verificando contraseña local...")

            val passwordLocal = obtenerPasswordLocal()

            if (passwordLocal != null) {
                Log.d(TAG, "🔑 Hay contraseña memorizada, validando...")

                // Validar contra Firebase
                validarPassword(passwordLocal) { esValida ->
                    if (esValida) {
                        Log.d(TAG, "✅ Contraseña memorizada válida, acceso directo")
                        estadoApp = EstadoApp.AUTENTICADO
                    } else {
                        Log.d(TAG, "❌ Contraseña memorizada no válida (cambió en Firebase)")
                        borrarPasswordLocal()
                        estadoApp = EstadoApp.REQUIERE_LOGIN
                    }
                }
            } else {
                Log.d(TAG, "🔐 No hay contraseña memorizada, mostrar login")
                estadoApp = EstadoApp.REQUIERE_LOGIN
            }
        }

        LaunchedEffect(estadoApp) {
            if (estadoApp == EstadoApp.AUTENTICADO) {
                navigator.navigateToHybridHome(clearBackStack = true)
            }
        }

        // Mostrar pantalla según el estado
        when (estadoApp) {
            EstadoApp.CARGANDO -> PantallaCargando()
            EstadoApp.REQUIERE_LOGIN -> PantallaLogin(navigator)
            EstadoApp.AUTENTICADO -> PantallaCargando()
        }
    }

    /**
     * Pantalla de cargando (splash screen)
     */
    @Composable
    fun PantallaCargando() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Placeholder del logo
                Card(
                    modifier = Modifier.size(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚽",
                            fontSize = 64.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = Constants.EMPRESA_NOMBRE,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(32.dp))

                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Iniciando...",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }


    /**
     * Carga el partido asignado y navega según el estado
     */
    private fun cargarPartidoYNavegar(  // ← RESTAURAR
        codigoPartido: String,
        navigator: AppNavigator
    ) {
        lifecycleScope.launch {
            try {
                val repository = FirebaseCatalogRepository(database, configManager.obtenerNodoRaiz())
                val partido = repository.getPartido(codigoPartido)

                if (partido != null) {
                    // TODO: Verificar si está caducado
                    // if (partido.estaCaducado()) { ... }

                    // Establecer contextos
                    PartidoContext.setPartidoActivo(partido)

                    // Cargar campeonato
                    val campeonato = repository.getCampeonato(partido.CAMPEONATOCODIGO)
                    if (campeonato != null) {
                        CampeonatoContext.seleccionarCampeonato(campeonato)

                        val deporte = SportType.fromId(campeonato.DEPORTE)
                        DeporteContext.seleccionarDeporte(deporte)
                    }

                    // Navegar a Tiempo Real
                    navigator.navigateToTiempoReal(
                        partidoId = codigoPartido,
                        clearBackStack = true
                    )
                } else {
                    // Partido no encontrado
                    navigator.navigateToHybridHome(clearBackStack = true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar partido: ${e.message}")
                navigator.navigateToHybridHome(clearBackStack = true)
            }
        }
    }
    /**
     * Pantalla de login con contraseña
     */
    @Composable
    fun PantallaLogin(navigator: AppNavigator) {
        var usuario by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var mensajeError by remember { mutableStateOf("") }
        var cargando by remember { mutableStateOf(false) }

        // ✅ FocusManager para ocultar teclado
        val focusManager = LocalFocusManager.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // ═══════════════════════════════════════════════════════
                // LOGO
                // ═══════════════════════════════════════════════════════
                Card(
                    modifier = Modifier.size(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "⚽", fontSize = 64.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ═══════════════════════════════════════════════════════
                // TÍTULO
                // ═══════════════════════════════════════════════════════
                Text(
                    text = Constants.APP_NOMBRE,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ingresa tus credenciales",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                // ═══════════════════════════════════════════════════════
                // ✅ BOTÓN OCULTAR TECLADO
                // ═══════════════════════════════════════════════════════
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !cargando
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardHide,
                        contentDescription = "Ocultar teclado"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Ocultar Teclado")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ═══════════════════════════════════════════════════════
                // CAMPO USUARIO
                // ═══════════════════════════════════════════════════════
                OutlinedTextField(
                    value = usuario,
                    onValueChange = {
                        usuario = it
                        mensajeError = ""
                    },
                    label = { Text("Usuario") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Usuario"
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !cargando
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ═══════════════════════════════════════════════════════
                // CAMPO CONTRASEÑA
                // ═══════════════════════════════════════════════════════
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        mensajeError = ""
                    },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            // Trigger login si los campos están completos
                            if (usuario.isNotBlank() && password.isNotBlank()) {
                                // El login se ejecutará desde el botón
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !cargando,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Contraseña"
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible)
                                    "Ocultar contraseña"
                                else
                                    "Mostrar contraseña"
                            )
                        }
                    }
                )

                // ═══════════════════════════════════════════════════════
                // MENSAJE DE ERROR
                // ═══════════════════════════════════════════════════════
                if (mensajeError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = mensajeError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ═══════════════════════════════════════════════════════
                // BOTÓN LOGIN
                // ═══════════════════════════════════════════════════════
                Button(
                    onClick = {
                        // Ocultar teclado primero
                        focusManager.clearFocus()

                        // Validaciones
                        if (usuario.isBlank()) {
                            mensajeError = "Ingresa tu usuario"
                            return@Button
                        }

                        if (password.isBlank()) {
                            mensajeError = "Ingresa tu contraseña"
                            return@Button
                        }

                        // Verificar Firebase Auth
                        val firebaseUser = auth.currentUser
                        if (firebaseUser == null) {
                            mensajeError = "Error: Firebase no está listo. Reinicia la app."
                            Log.e(TAG, "❌ Firebase Auth no está inicializado")
                            return@Button
                        }

                        cargando = true
                        mensajeError = ""

                        Log.d(TAG, "╔═══════════════════════════════════════╗")
                        Log.d(TAG, "🔐 INICIANDO LOGIN")
                        Log.d(TAG, "   Usuario ingresado: '$usuario'")
                        Log.d(TAG, "   Password length: ${password.length}")
                        Log.d(TAG, "   Firebase Auth UID: ${firebaseUser.uid}")
                        Log.d(TAG, "   Ruta Firebase: /AppConfig/Usuarios/$usuario")
                        Log.d(TAG, "╚═══════════════════════════════════════╝")

                        // ✅ AUTENTICACIÓN
                        authManager.login(usuario, password) { resultado ->
                            cargando = false

                            when (resultado) {
                                is ResultadoAutenticacion.Exito -> {
                                    val user = resultado.usuario

                                    // ✅ 1. Establecer usuario en contexto
                                    UsuarioContext.setUsuario(user)

                                    // ✅ 2. Verificar si tiene partido asignado
                                    val partidoAsignado = user.permisos.codigoPartido

                                    if (!partidoAsignado.isNullOrEmpty() &&
                                        partidoAsignado != "NINGUNO") {
                                        // 🎯 CASO CORRESPONSAL: Tiene partido asignado
                                        Log.d(TAG, "🎯 Corresponsal con partido: $partidoAsignado")
                                        cargarPartidoYNavegar(partidoAsignado, navigator)
                                    } else {
                                        // 👑 CASO ADMIN/OPERADOR: Sin partido específico
                                        Log.d(TAG, "👑 Usuario sin partido asignado: acceso completo")
                                        navigator.navigateToHybridHome(clearBackStack = true)
                                    }
                                }

                                is ResultadoAutenticacion.CredencialesInvalidas -> {
                                    mensajeError = "Usuario o contraseña incorrectos"
                                }

                                is ResultadoAutenticacion.UsuarioNoAutorizado -> {
                                    mensajeError = "Usuario no autorizado. Contacte al administrador."
                                }

                                is ResultadoAutenticacion.Error -> {
                                    mensajeError = resultado.mensaje
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !cargando
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Iniciar Sesión", fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ═══════════════════════════════════════════════════════
                // TEXTO DE AYUDA
                // ═══════════════════════════════════════════════════════
                Text(
                    text = "💡 Solicita tus credenciales al administrador",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }


    /**
     * Pantalla de bienvenida (después de autenticar correctamente)
     */
    @Composable
    fun PantallaBienvenida(navigator: AppNavigator, openDrawer: () -> Unit) {
        val repository = remember(database, configManager) {
            Repository(database, configManager)
        }
        val homeViewModel: HomeViewModel = viewModel(
            factory = HomeViewModelFactory(repository)
        )
        HomeRoute(
            viewModel = homeViewModel,
            onOpenDrawer = openDrawer
        )
    }

    /**
     * Estados posibles de la aplicación
     */
    enum class EstadoApp {
        CARGANDO,           // Iniciando y verificando
        REQUIERE_LOGIN,     // Necesita ingresar contraseña
        AUTENTICADO         // Ya autenticado correctamente
    }
}