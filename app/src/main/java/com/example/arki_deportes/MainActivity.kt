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
import androidx.compose.ui.platform.LocalFocusManager  // ← NUEVO
import androidx.compose.material.icons.filled.KeyboardHide  // ← NUEVO

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
//import com.example.arki_deportes.ui.grupos.GrupoListRoute
import com.example.arki_deportes.ui.equipos.EquipoListRoute
import com.example.arki_deportes.ui.partidos.PartidoListRoute
import com.example.arki_deportes.ui.grupos.GrupoListRoute

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
                        // ═══════════════════════════════════════════════════════════════
                        // RUTAS DE LISTAS
                        // ═══════════════════════════════════════════════════════════════
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
    fun PantallaTiempoReal(
        navigator: AppNavigator,
        campeonatoId: String,
        partidoId: String,
        openDrawer: () -> Unit
    ) {
        // ✅ Este es el tipo que espera tu TiempoRealViewModel
        val repository = remember(database, configManager) {
            FirebaseCatalogRepository(
                database = database,
                rootNode = configManager.obtenerNodoRaiz() // usa tu método real
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
            // ✅ Solo estos dos parámetros
            // No necesita onCreateGrupo ni onEditGrupo
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
                navigator.navigateTo("serie_list/$id") // Ahora lo llevamos a la LISTA primero
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
    /**
     * Valida una contraseña contra Firebase
     *
     * @param usuarioIngresado Usuario ingresado por el usuario (para memorizarlo si todo está OK)
     * @param passwordIngresado Contraseña ingresada por el usuario
     * @param onResult Callback con resultado (true = correcta, false = incorrecta)
     */
    private fun validarPassword(
        usuarioIngresado: String,
        passwordIngresado: String,
        onResult: (Boolean) -> Unit
    ) {
        leerPasswordFirebase { passwordFirebase ->
            if (passwordFirebase != null) {
                val esCorrecta = passwordIngresado == passwordFirebase

                if (esCorrecta) {
                    Log.d(TAG, "✅ Contraseña correcta")
                    // ✅ Memorizar usuario + password
                    guardarCredencialesLocal(usuarioIngresado, passwordIngresado)
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
     * Guarda usuario y contraseña en SharedPreferences (memorización local)
     * ✅ MEJORADO: Con logs detallados para rastrear el guardado
     */
    private fun guardarCredencialesLocal(usuario: String, password: String) {
        Log.d(TAG, "╔═══════════════════════════════════════════════════════════╗")
        Log.d(TAG, "💾 GUARDANDO CREDENCIALES LOCALES")
        Log.d(TAG, "   Usuario a guardar: '${usuario.trim()}'")
        Log.d(TAG, "   Password length: ${password.length}")
        Log.d(TAG, "   SharedPrefs name: ${Constants.SHARED_PREFS_NAME}")

        val prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("usuario_memorizado", usuario.trim())
        editor.putString("password_memorizado", password)
        val success = editor.commit() // ✅ Usar commit() en lugar de apply() para esperar confirmación

        if (success) {
            Log.d(TAG, "✅ Credenciales GUARDADAS exitosamente con commit()")

            // Verificar inmediatamente que se guardaron
            val usuarioVerificado = prefs.getString("usuario_memorizado", null)
            val passwordVerificado = prefs.getString("password_memorizado", null)

            Log.d(TAG, "🔍 Verificación inmediata después de guardar:")
            Log.d(TAG, "   Usuario leído: '$usuarioVerificado'")
            Log.d(TAG, "   Password existe: ${passwordVerificado != null}")
            Log.d(TAG, "   Password length: ${passwordVerificado?.length ?: 0}")
            Log.d(TAG, "   Match usuario: ${usuarioVerificado == usuario.trim()}")
            Log.d(TAG, "   Match password: ${passwordVerificado == password}")
        } else {
            Log.e(TAG, "❌ ERROR: commit() retornó false al guardar credenciales")
        }
        Log.d(TAG, "╚═══════════════════════════════════════════════════════════╝")
    }

// ┌─────────────────────────────────────────────────────────────────────────┐
// │ PASO 2: Reemplazar obtenerUsuarioLocal (línea ~602)                    │
// └─────────────────────────────────────────────────────────────────────────┘

    /**
     * Obtiene el usuario memorizado localmente
     * ✅ MEJORADO: Con logs detallados para rastrear la lectura
     */
    private fun obtenerUsuarioLocal(): String? {
        val prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE)
        val usuario = prefs.getString("usuario_memorizado", null)

        Log.d(TAG, "📖 LEYENDO usuario local:")
        Log.d(TAG, "   SharedPrefs: ${Constants.SHARED_PREFS_NAME}")
        Log.d(TAG, "   Usuario leído: '${usuario ?: "(null)"}'")
        Log.d(TAG, "   Es blank: ${usuario.isNullOrBlank()}")

        return usuario
    }


    /**
     * Obtiene la contraseña memorizada localmente
     * ✅ MEJORADO: Con logs detallados para rastrear la lectura
     */
    private fun obtenerPasswordLocal(): String? {
        val prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE)
        val password = prefs.getString("password_memorizado", null)

        Log.d(TAG, "📖 LEYENDO password local:")
        Log.d(TAG, "   SharedPrefs: ${Constants.SHARED_PREFS_NAME}")
        Log.d(TAG, "   Password existe: ${password != null}")
        Log.d(TAG, "   Password length: ${password?.length ?: 0}")
        Log.d(TAG, "   Es blank: ${password.isNullOrBlank()}")

        return password
    }

    /**
     * Borra usuario y contraseña memorizados (logout)
     * ✅ MEJORADO: Con logs detallados
     */
    private fun borrarCredencialesLocal() {
        Log.d(TAG, "🗑️ BORRANDO CREDENCIALES LOCALES")

        val prefs = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.remove("usuario_memorizado")
        editor.remove("password_memorizado")
        val success = editor.commit()

        if (success) {
            Log.d(TAG, "✅ Credenciales borradas exitosamente")

            // Verificar que realmente se borraron
            val usuarioVerificado = prefs.getString("usuario_memorizado", null)
            val passwordVerificado = prefs.getString("password_memorizado", null)

            Log.d(TAG, "🔍 Verificación después de borrar:")
            Log.d(TAG, "   Usuario: ${if (usuarioVerificado == null) "NULL ✅" else "EXISTE ❌"}")
            Log.d(TAG, "   Password: ${if (passwordVerificado == null) "NULL ✅" else "EXISTE ❌"}")
        } else {
            Log.e(TAG, "❌ ERROR al borrar credenciales")
        }
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

            Log.d(TAG, "🔍 Autenticación completa, verificando credenciales locales...")

            val usuarioLocal = obtenerUsuarioLocal().orEmpty()
            val passwordLocal = obtenerPasswordLocal().orEmpty()

            if (usuarioLocal.isNotBlank() && passwordLocal.isNotBlank()) {
                Log.d(TAG, "🔑 Credenciales encontradas localmente")
                Log.d(TAG, "   Usuario: $usuarioLocal")
                Log.d(TAG, "   Password: ${passwordLocal.length} caracteres")
                Log.d(TAG, "📋 Mostrando login con campos precargados")
                estadoApp = EstadoApp.REQUIERE_LOGIN
            } else {
                Log.d(TAG, "🔐 No hay credenciales guardadas, mostrar login vacío")
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


    private fun cargarPartidoYNavegar(
        campeonatoId: String,
        partidoId: String,
        navigator: AppNavigator
    ) {
        lifecycleScope.launch {
            try {
                val repository = FirebaseCatalogRepository(database, configManager.obtenerNodoRaiz())

                // ✅ 1. Obtener el partido
                val partido = repository.getPartido(campeonatoId, partidoId)

                if (partido != null) {
                    // ✅ 2. Verificar caducidad del partido
                    val haCaducado = verificarCaducidadPartido(partido)

                    if (haCaducado) {
                        Log.w(TAG, "⚠️ Partido CADUCADO: ${partido.CODIGOPARTIDO}")
                        Log.w(TAG, "   Fecha del partido: ${partido.FECHA_PARTIDO}")
                        Log.w(TAG, "   Eliminando asignación del usuario...")

                        // ✅ 3. Eliminar partido asignado del usuario en Firebase
                        eliminarPartidoAsignado()

                        // ✅ 4. Navegar a lista de partidos (HybridHome)
                        Log.d(TAG, "🏠 Navegando a Home (partido caducado)")
                        navigator.navigateToHybridHome(clearBackStack = true)
                    } else {
                        // ✅ Partido válido - flujo normal
                        Log.d(TAG, "✅ Partido VIGENTE: ${partido.CODIGOPARTIDO}")

                        PartidoContext.setPartidoActivo(partido)

                        // Cargar campeonato
                        val campeonato = repository.getCampeonato(campeonatoId)
                        if (campeonato != null) {
                            CampeonatoContext.seleccionarCampeonato(campeonato)
                            val deporte = SportType.fromId(campeonato.DEPORTE)
                            DeporteContext.seleccionarDeporte(deporte)
                        }

                        // Navegar a tiempo real
                        Log.d(TAG, "🎯 Navegando a TiempoReal")
                        navigator.navigateToTiempoReal(
                            campeonatoId = campeonatoId,
                            partidoId = partidoId,
                            clearBackStack = true
                        )
                    }
                } else {
                    // Partido no encontrado
                    Log.w(TAG, "⚠️ Partido NO ENCONTRADO: $partidoId")
                    navigator.navigateToHybridHome(clearBackStack = true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al cargar partido: ${e.message}", e)
                navigator.navigateToHybridHome(clearBackStack = true)
            }
        }
    }

    /**
     * Verifica si un partido ha caducado
     * Un partido caduca si su fecha + 1 día es menor a la fecha actual
     *
     * @param partido El partido a verificar
     * @return true si ha caducado, false si aún está vigente
     */
    private fun verificarCaducidadPartido(partido: Partido): Boolean {
        try {
            // ✅ Parsear fecha del partido (formato esperado: "yyyy-MM-dd" o "dd/MM/yyyy")
            val fechaPartido = parsearFechaPartido(partido.FECHA_PARTIDO)

            if (fechaPartido == null) {
                Log.w(TAG, "⚠️ No se pudo parsear la fecha del partido: ${partido.FECHA_PARTIDO}")
                return false // Si no se puede parsear, asumimos que no ha caducado
            }

            // ✅ Calcular fecha de caducidad (fecha del partido + 1 día)
            val fechaCaducidad = fechaPartido.plusDays(1)

            // ✅ Obtener fecha actual
            val fechaActual = LocalDate.now()

            // ✅ Comparar: si la fecha actual es mayor a la fecha de caducidad, ha caducado
            val caducado = fechaActual.isAfter(fechaCaducidad)

            Log.d(TAG, "📅 Verificación de caducidad:")
            Log.d(TAG, "   Fecha del partido: $fechaPartido")
            Log.d(TAG, "   Fecha de caducidad: $fechaCaducidad")
            Log.d(TAG, "   Fecha actual: $fechaActual")
            Log.d(TAG, "   ¿Ha caducado?: $caducado")

            return caducado

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al verificar caducidad: ${e.message}", e)
            return false // En caso de error, asumimos que no ha caducado
        }
    }

    /**
     * Parsea la fecha del partido desde diferentes formatos posibles
     *
     * Formatos soportados:
     * - "yyyy-MM-dd" (ejemplo: "2025-01-10")
     * - "dd/MM/yyyy" (ejemplo: "10/01/2025")
     * - "yyyy/MM/dd" (ejemplo: "2025/01/10")
     *
     * @param fechaStr La fecha en formato string
     * @return LocalDate o null si no se pudo parsear
     */
    private fun parsearFechaPartido(fechaStr: String): LocalDate? {
        if (fechaStr.isBlank()) return null

        val formatos = listOf(
            // Formatos ISO y estándar
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),

            // Formatos con barras (/) - CON ceros
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),

            // Formatos con barras (/) - SIN ceros (FLEXIBLES)
            DateTimeFormatter.ofPattern("d/M/yyyy"),    // 12/1/2026
            DateTimeFormatter.ofPattern("M/d/yyyy"),    // 1/12/2026 (USA)

            // Formatos con guiones (-) - CON ceros
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),

            // Formatos con guiones (-) - SIN ceros (FLEXIBLES)
            DateTimeFormatter.ofPattern("d-M-yyyy")     // 12-1-2026
        )

        for (formato in formatos) {
            try {
                val fecha = LocalDate.parse(fechaStr.trim(), formato)
                Log.d(TAG, "✅ Fecha parseada exitosamente: $fechaStr → $fecha")
                return fecha
            } catch (e: DateTimeParseException) {
                // Intentar con el siguiente formato
            }
        }

        // Si ningún formato funcionó
        Log.w(TAG, "❌ No se pudo parsear la fecha con ningún formato: '$fechaStr'")
        return null
    }


    /**
     * Elimina el partido asignado del usuario actual en Firebase
     * Actualiza la ruta: /AppConfig/Usuarios/{usuario}/permisos
     * Elimina los campos: codigoCampeonato y codigoPartido
     */
    private fun eliminarPartidoAsignado() {
        try {
            val usuario = UsuarioContext.getUsuario()
            if (usuario == null) {
                Log.w(TAG, "⚠️ No hay usuario en contexto para eliminar partido asignado")
                return
            }

            val nombreUsuario = usuario.usuario
            if (nombreUsuario.isBlank()) {
                Log.w(TAG, "⚠️ Nombre de usuario vacío")
                return
            }

            // ✅ Ruta en Firebase: /AppConfig/Usuarios/{nombreUsuario}/permisos
            val nodoRaiz = configManager.obtenerNodoRaiz()
            val rutaPermisos = database.reference
                .child(nodoRaiz)
                .child("AppConfig")
                .child("Usuarios")
                .child(nombreUsuario)
                .child("permisos")

            // ✅ Crear map con los valores a actualizar (null = eliminar)
            val actualizaciones = mapOf(
                "codigoCampeonato" to null,
                "codigoPartido" to null
            )

            // ✅ Actualizar Firebase
            rutaPermisos.updateChildren(actualizaciones)
                .addOnSuccessListener {
                    Log.d(TAG, "✅ Partido asignado eliminado correctamente de Firebase")
                    Log.d(TAG, "   Usuario: $nombreUsuario")

                    // ✅ Actualizar contexto local
                    UsuarioContext.limpiarPartidoAsignado()
                }
                .addOnFailureListener { error ->
                    Log.e(TAG, "❌ Error al eliminar partido asignado de Firebase: ${error.message}", error)
                }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al eliminar partido asignado: ${e.message}", e)
        }
    }




    /**
     * Pantalla de login con contraseña
     * ✅ MEJORADO: Inicialización directa de credenciales con logs
     */
    @Composable
    fun PantallaLogin(navigator: AppNavigator) {
        Log.d(TAG, "═══════════════════════════════════════════════════════════")
        Log.d(TAG, "🔐 COMPONIENDO PantallaLogin")
        Log.d(TAG, "═══════════════════════════════════════════════════════════")

        var passwordVisible by remember { mutableStateOf(false) }
        var mensajeError by remember { mutableStateOf("") }
        var cargando by remember { mutableStateOf(false) }

        // ✅ SOLUCIÓN: Inicializar DIRECTAMENTE con los valores guardados
        // En lugar de usar LaunchedEffect que se ejecuta DESPUÉS
        val usuarioInicial = remember {
            val usr = obtenerUsuarioLocal().orEmpty()
            Log.d(TAG, "📝 Valor INICIAL usuario: '$usr'")
            usr
        }

        val passwordInicial = remember {
            val pwd = obtenerPasswordLocal().orEmpty()
            Log.d(TAG, "📝 Valor INICIAL password: ${if (pwd.isNotEmpty()) "Cargado (${pwd.length} chars)" else "Vacío"}")
            pwd
        }

        var usuario by rememberSaveable { mutableStateOf(usuarioInicial) }
        var password by rememberSaveable { mutableStateOf(passwordInicial) }

        // ✅ Log cuando cambian los valores
        LaunchedEffect(usuario) {
            Log.d(TAG, "👤 Campo usuario cambió a: '$usuario'")
        }

        LaunchedEffect(password) {
            Log.d(TAG, "🔑 Campo password cambió (length: ${password.length})")
        }

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
                // ═══════════════════════════════════════════════════════════
                // LOGO
                // ═══════════════════════════════════════════════════════════
                Image(
                    painter = painterResource(id = R.drawable.logowilmer),
                    contentDescription = "Logo Wilmer",
                    modifier = Modifier.size(140.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ═══════════════════════════════════════════════════════════
                // TÍTULO
                // ═══════════════════════════════════════════════════════════
                Text(
                    text = Constants.APP_NOMBRE,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ✅ Mensaje dinámico si ya hay credenciales
                Text(
                    text = if (usuarioInicial.isNotBlank())
                        "Bienvenido de nuevo, $usuarioInicial"
                    else
                        "Ingresa tus credenciales",
                    fontSize = 14.sp,
                    color = if (usuarioInicial.isNotBlank())
                        MaterialTheme.colorScheme.primary
                    else
                        Color.Gray,
                    textAlign = TextAlign.Center
                )

                // ═══════════════════════════════════════════════════════════
                // ✅ BOTÓN OCULTAR TECLADO
                // ═══════════════════════════════════════════════════════════
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        Log.d(TAG, "⌨️ Ocultando teclado")
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

                // ═══════════════════════════════════════════════════════════
                // CAMPO USUARIO
                // ═══════════════════════════════════════════════════════════
                OutlinedTextField(
                    value = usuario,
                    onValueChange = {
                        Log.d(TAG, "👤 Usuario cambió de '$usuario' a '$it'")
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

                // ═══════════════════════════════════════════════════════════
                // CAMPO CONTRASEÑA
                // ═══════════════════════════════════════════════════════════
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        Log.d(TAG, "🔑 Password cambió (new length: ${it.length})")
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
                            Log.d(TAG, "⌨️ Usuario presionó Done en password")
                            focusManager.clearFocus()
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
                        IconButton(onClick = {
                            passwordVisible = !passwordVisible
                            Log.d(TAG, "👁️ Password visible: $passwordVisible")
                        }) {
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

                // ═══════════════════════════════════════════════════════════
                // MENSAJE DE ERROR
                // ═══════════════════════════════════════════════════════════
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

                // ═══════════════════════════════════════════════════════════
                // BOTÓN LOGIN
                // ═══════════════════════════════════════════════════════════
                Button(
                    onClick = {
                        Log.d(TAG, "╔═══════════════════════════════════════════════════════════╗")
                        Log.d(TAG, "🔐 BOTÓN LOGIN PRESIONADO")
                        Log.d(TAG, "   Usuario ingresado: '$usuario'")
                        Log.d(TAG, "   Password length: ${password.length}")
                        Log.d(TAG, "╚═══════════════════════════════════════════════════════════╝")

                        // Ocultar teclado primero
                        focusManager.clearFocus()

                        // Validaciones
                        if (usuario.isBlank()) {
                            mensajeError = "Ingresa tu usuario"
                            Log.w(TAG, "⚠️ Usuario vacío")
                            return@Button
                        }

                        if (password.isBlank()) {
                            mensajeError = "Ingresa tu contraseña"
                            Log.w(TAG, "⚠️ Password vacío")
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

                        Log.d(TAG, "╔═══════════════════════════════════════════════════════════╗")
                        Log.d(TAG, "🔐 INICIANDO LOGIN")
                        Log.d(TAG, "   Usuario ingresado: '$usuario'")
                        Log.d(TAG, "   Password length: ${password.length}")
                        Log.d(TAG, "   Firebase Auth UID: ${firebaseUser.uid}")
                        Log.d(TAG, "   Ruta Firebase: /AppConfig/Usuarios/$usuario")
                        Log.d(TAG, "╚═══════════════════════════════════════════════════════════╝")

                        // ✅ AUTENTICACIÓN
                        authManager.login(usuario, password) { resultado ->
                            cargando = false

                            when (resultado) {
                                is ResultadoAutenticacion.Exito -> {
                                    val user = resultado.usuario

                                    guardarCredencialesLocal(usuario, password)  // ← ESTO FALTA
                                    Log.d(TAG, "✅ LOGIN EXITOSO")
                                    Log.d(TAG, "   Usuario: ${user.usuario}")
                                    Log.d(TAG, "   Nombre: ${user.nombre}")
                                    Log.d(TAG, "   Rol: ${user.rol}")

                                    // ✅ 1. Establecer usuario en contexto
                                    UsuarioContext.setUsuario(user)

                                    // ✅ 2. Verificar si tiene partido asignado
                                    val partidoId = user.permisos.codigoPartido
                                    val campeonatoId = user.permisos.codigoCampeonato

                                    if (!campeonatoId.isNullOrBlank() && campeonatoId != "NINGUNO" &&
                                        !partidoId.isNullOrBlank() && partidoId != "NINGUNO"
                                    ) {
                                        Log.d(TAG, "🎯 Asignado: campeonato=$campeonatoId partido=$partidoId")
                                        cargarPartidoYNavegar(campeonatoId, partidoId, navigator)
                                    } else {
                                        Log.d(TAG, "🏠 Navegando a Home")
                                        navigator.navigateToHybridHome(clearBackStack = true)
                                    }
                                }

                                is ResultadoAutenticacion.CredencialesInvalidas -> {
                                    Log.w(TAG, "❌ Credenciales inválidas")
                                    mensajeError = "Usuario o contraseña incorrectos"
                                }

                                is ResultadoAutenticacion.UsuarioNoAutorizado -> {
                                    Log.w(TAG, "❌ Usuario no autorizado")
                                    mensajeError = "Usuario no autorizado. Contacte al administrador."
                                }

                                is ResultadoAutenticacion.Error -> {
                                    Log.e(TAG, "❌ Error en login: ${resultado.mensaje}")
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

                // ═══════════════════════════════════════════════════════════
                // TEXTO DE AYUDA
                // ═══════════════════════════════════════════════════════════
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
            onNavigateToPartidos = { navigator.navigateToPartidoList() },
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