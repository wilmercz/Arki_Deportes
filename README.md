# Arki Deportes

Aplicación móvil para la operación de transmisiones deportivas conectada a Firebase
Realtime Database. Este documento resume los flujos clave de la app y la
estructura de los nodos que se sincronizan con el backoffice.

## Flujos principales

### Home híbrido
- El `HomeViewModel` escucha el nodo `PartidoActual` en Firebase para decidir si
  existe un partido activo y mostrar la tarjeta "En vivo" con marcador y tiempo
  transcurrido. Cuando el nodo queda vacío, la tarjeta desaparece para evitar
  datos desfasados.【F:app/src/main/java/com/example/arki_deportes/ui/home/HomeViewModel.kt†L24-L83】【F:app/src/main/java/com/example/arki_deportes/ui/home/HomeScreen.kt†L61-L137】
- Al mismo tiempo, la misma pantalla ejecuta `obtenerPartidosRango` sobre el
  repositorio para poblar el listado de partidos ±7 días y permite refrescarlo
  con `SwipeRefresh`, combinando información en vivo y agenda histórica en un
  único flujo híbrido.【F:app/src/main/java/com/example/arki_deportes/ui/home/HomeViewModel.kt†L45-L73】【F:app/src/main/java/com/example/arki_deportes/data/Repository.kt†L62-L117】

### Tiempo Real
- El `TiempoRealViewModel` escucha continuamente `PartidoActual` y transforma
  los valores en un `MarcadorUi` que incluye cronómetro normalizado,
  tarjetas, penales y el estado textual/iconográfico del partido.【F:app/src/main/java/com/example/arki_deportes/ui/tiemporeal/TiempoRealViewModel.kt†L18-L115】
- La UI organiza la información en tres pestañas (Marcador, Estadísticas y
  Alineaciones) e incorpora acciones rápidas (gol, penal, tarjetas) para dejar
  constancia del evento en el histórico local mostrado en pantalla.【F:app/src/main/java/com/example/arki_deportes/ui/tiemporeal/TiempoRealScreen.kt†L41-L202】【F:app/src/main/java/com/example/arki_deportes/ui/tiemporeal/TiempoRealViewModel.kt†L200-L222】

### Catálogos
- El `FirebaseCatalogRepository` centraliza la lectura y escritura de
  campeonatos, grupos, equipos y partidos bajo el nodo raíz configurado, con
  flujos reactivos para observar cambios y métodos suspendidos para operaciones
  CRUD. Así, las pantallas de formularios reutilizan un único backend de datos
  compartido.【F:app/src/main/java/com/example/arki_deportes/data/repository/FirebaseCatalogRepository.kt†L9-L143】

## Firebase Realtime Database

### Nodo `EquipoProduccion`
- Ubicación: `<NODO_RAIZ>/EquipoProduccion/`
- Estructura:
  - `default/` para la configuración global del staff
  - `campeonatos/<CODIGO_CAMPEONATO>/` para personalizaciones por torneo
- Campos disponibles en cada registro: `narrador`, `comentarista`, `bordeCampo`,
  `anfitriones` (lista) y `timestamp`. Los valores se normalizan antes de
  guardarse y se reutilizan en formularios dependiendo del modo seleccionado.【F:app/src/main/java/com/example/arki_deportes/data/model/EquipoProduccion.kt†L16-L73】【F:app/src/main/java/com/example/arki_deportes/data/Repository.kt†L238-L333】

### Nodo `Menciones`
- Ubicación: `<NODO_RAIZ>/Menciones/`
- Cada hijo identificado por un ID único contiene `texto`, `tipo`, `activo`,
  `orden` y `timestamp`. El repositorio obtiene la lista ordenada por `orden`
  (con desempate por `timestamp`) para mantener la secuencia definida en la app.【F:app/src/main/java/com/example/arki_deportes/data/model/Mencion.kt†L12-L48】【F:app/src/main/java/com/example/arki_deportes/data/Repository.kt†L46-L105】

### Nodo `PartidoActual`
- Ubicación: `<NODO_RAIZ>/PartidoActual`
- Además de la estructura clásica (equipos, goles, tarjetas, tiempo y estado),
  la app consume los campos adicionales `CRONOMETRANDO` (bandera del reloj),
  `EN_TRANSMISION` (controla la visibilidad de la tarjeta en Home),
  `ESCUDO1_URL`/`ESCUDO2_URL` (recursos gráficos) y `ULTIMA_ACTUALIZACION` para
  auditar sincronizaciones. Estos valores alimentan tanto la tarjeta del Home
  como el panel de Tiempo Real.【F:app/src/main/java/com/example/arki_deportes/data/model/PartidoActual.kt†L18-L119】【F:app/src/main/java/com/example/arki_deportes/ui/home/HomeViewModel.kt†L55-L83】【F:app/src/main/java/com/example/arki_deportes/ui/tiemporeal/TiempoRealViewModel.kt†L86-L115】

## Operación durante transmisiones

### Compartir menciones por WhatsApp
1. Desde la pantalla **Menciones**, pulsa el botón "WhatsApp" dentro de la
   tarjeta deseada; el sistema genera un `Intent.ACTION_SEND` con el texto y lo
   dirige a la aplicación de WhatsApp mediante un chooser.【F:app/src/main/java/com/example/arki_deportes/ui/menciones/MencionesScreen.kt†L54-L86】【F:app/src/main/java/com/example/arki_deportes/ui/menciones/MencionesScreen.kt†L308-L347】
2. Si el dispositivo no tiene WhatsApp instalado o la mención está vacía, se
   mostrará un snackbar informativo para avisar al operador.【F:app/src/main/java/com/example/arki_deportes/ui/menciones/MencionesScreen.kt†L66-L86】【F:app/src/main/java/com/example/arki_deportes/ui/menciones/MencionesScreen.kt†L308-L347】

### Administración de menciones en vivo
1. Edita el texto directamente en el campo multilinea; los cambios quedan
   marcados como pendientes (`isDirty`) hasta que presionas "Guardar", momento en
   el que se actualiza Firebase con un nuevo `timestamp`.【F:app/src/main/java/com/example/arki_deportes/ui/menciones/MencionesViewModel.kt†L79-L142】
2. Usa el interruptor para activar/desactivar menciones; la app guarda el cambio
   inmediatamente y confirma el resultado mediante snackbar.【F:app/src/main/java/com/example/arki_deportes/ui/menciones/MencionesViewModel.kt†L90-L131】
3. Mantén presionada la manija de arrastre para reordenar; al soltar, se envía
   la nueva secuencia (`orden`) al servidor y se muestra el progreso en la barra
   superior.【F:app/src/main/java/com/example/arki_deportes/ui/menciones/MencionesViewModel.kt†L144-L182】【F:app/src/main/java/com/example/arki_deportes/ui/menciones/MencionesScreen.kt†L231-L320】
4. Recurre a los botones "Copiar" y "WhatsApp" para distribuir rápidamente la
   mención sin abandonar la pantalla, manteniendo la coordinación del equipo de
   producción.【F:app/src/main/java/com/example/arki_deportes/ui/menciones/MencionesScreen.kt†L54-L105】【F:app/src/main/java/com/example/arki_deportes/ui/menciones/MencionesScreen.kt†L308-L347】
