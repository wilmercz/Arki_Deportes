// app/src/main/java/com/example/arki_deportes/MainActivity.kt

package com.example.arki_deportes

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale

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

import com.example.arki_deportes.ui.realtime.TiempoRealRoute
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.arki_deportes.ui.campeonatos.CampeonatoListRoute

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
                val handleLogout: () -> Unit = {
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
                        loginRoute = { navigatorParam: AppNavigator ->
                            PantallaInicio(navigatorParam)
                        },
                        hybridHomeRoute = { navigatorParam: AppNavigator ->
                            PantallaBienvenida(navigatorParam, openDrawer = openDrawer)
                        },
                        realTimeRoute = { navigatorParam: AppNavigator ->
                            PantallaTiempoReal(navigatorParam, openDrawer = openDrawer)
                        },
                        catalogsRoute = { navigatorParam: AppNavigator ->
                            PantallaCatalogos(navigatorParam, openDrawer = openDrawer)
                        },
                        mencionesRoute = { navigatorParam: AppNavigator ->
                            PantallaMenciones(navigatorParam, openDrawer = openDrawer)
                        },
                        equipoProduccionRoute = { navigatorParam: AppNavigator ->
                            PantallaEquipoProduccion(navigatorParam, openDrawer = openDrawer)
                        },
                        settingsRoute = { navigatorParam: AppNavigator ->
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
                        // ═══════════════════════════════════════════════════════════════
                        // RUTAS DE FORMULARIOS
                        // ═══════════════════════════════════════════════════════════════
                        campeonatoFormRoute = { navigatorParam: AppNavigator, codigo: String? ->
                            PantallaCampeonatoForm(navigatorParam, codigo)
                        },
                        grupoFormRoute = { navigatorParam: AppNavigator, codigo: String? ->
                            PantallaGrupoForm(navigatorParam, codigo)
                        },
                        equipoFormRoute = { navigatorParam: AppNavigator, codigo: String? ->
                            PantallaEquipoForm(navigatorParam, codigo)
                        },
                        partidoFormRoute = { navigatorParam: AppNavigator, codigo: String? ->
                            PantallaPartidoForm(navigatorParam, codigo)
                        }
                    )
                }
            }

        }
    }

    @Composable
    fun PantallaTiempoReal(navigator: AppNavigator, openDrawer: () -> Unit) {
        val repository = remember(database, configManager) {
            Repository(database, configManager)
        }
        val viewModel: TiempoRealViewModel = viewModel(
            factory = TiempoRealViewModelFactory(repository)
        )

        TiempoRealRoute(
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
    fun PantallaGrupoList(navigator: AppNavigator, openDrawer: () -> Unit) {
        GrupoListRoute(
            onNavigateBack = {
                if (!navigator.navigateBack()) {
                    navigator.navigateToHybridHome()
                }
            },
            onOpenDrawer = openDrawer,
            onCreateGrupo = {
                navigator.navigateToGrupoForm(null)
            },
            onEditGrupo = { codigo ->
                navigator.navigateToGrupoForm(codigo)
            }
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
            EstadoApp.REQUIERE_LOGIN -> PantallaLogin { password ->
                validarPassword(password) { esValida ->
                    if (esValida) {
                        estadoApp = EstadoApp.AUTENTICADO
                    }
                }
            }
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
                // Logo más pequeño (180dp -> 179dp)
                Image(
                    painter = painterResource(id = R.drawable.logo_color),
                    contentDescription = "Logo ARKI Deportes",
                    modifier = Modifier.size(150.dp),
                    contentScale = ContentScale.Fit
                )

                // Separación entre logo y texto (24dp -> 26dp)
                Spacer(modifier = Modifier.height(26.dp))

                // Texto con constante y letra más pequeña (24sp -> 21sp)
                Text(
                    text = Constants.EMPRESA_NOMBRE,
                    fontSize = 18.sp,
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
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }

    /**
     * Pantalla de login con contraseña
     */
    @Composable
    fun PantallaLogin(onPasswordSubmit: (String) -> Unit) {
        var password by remember { mutableStateOf("") }
        var passwordVisible by remember { mutableStateOf(false) }
        var mensajeError by remember { mutableStateOf("") }
        var cargando by remember { mutableStateOf(false) }

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
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.logo_color),
                    contentDescription = "Logo ARKI Deportes",
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = Constants.APP_NOMBRE,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ingresa la contraseña de acceso",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Campo de contraseña
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
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !cargando,
                    isError = mensajeError.isNotEmpty()
                )

                if (mensajeError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = mensajeError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de acceder
                Button(
                    onClick = {
                        if (password.isBlank()) {
                            mensajeError = "Ingresa una contraseña"
                        } else {
                            cargando = true
                            mensajeError = ""
                            onPasswordSubmit(password)

                            // Resetear estado después de 2 segundos si falla
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                cargando = false
                                mensajeError = "Contraseña incorrecta"
                            }, 2000)
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
                        Text("Acceder", fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "💡 Solicita la contraseña al administrador",
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