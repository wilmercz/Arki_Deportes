package com.example.arki_deportes.ui.realtime.components

class MotorCronometro {

    private var inicio: Long = 0L
    private var pausaAcumulada: Long = 0L // en milisegundos
    private var offset: Long = 0L // en milisegundos

    private var enPausa: Boolean = false
    private var finalizado: Boolean = false

    private var momentoPausa: Long = 0L

    // ▶️ INICIAR
    fun iniciar() {
        inicio = System.currentTimeMillis()
        pausaAcumulada = 0L
        offset = 0L
        enPausa = false
        finalizado = false
    }

    // ⏸️ PAUSAR
    fun pausar() {
        if (!enPausa && !finalizado) {
            enPausa = true
            momentoPausa = System.currentTimeMillis()
        }
    }

    // ▶️ REANUDAR
    fun reanudar() {
        if (enPausa && !finalizado) {
            val ahora = System.currentTimeMillis()
            pausaAcumulada += (ahora - momentoPausa)
            enPausa = false
        }
    }

    // ⛔ FINALIZAR
    fun finalizar() {
        if (!finalizado) {
            if (!enPausa) {
                // Si estaba corriendo, marcamos el momento exacto del fin
                momentoPausa = System.currentTimeMillis()
            }
            // IMPORTANTE: NO sumar a pausaAcumulada aquí.
            // La pausa acumulada solo crece cuando REANUDAS.
            enPausa = true
            finalizado = true
        }
    }

    // 🔄 OFFSET
    fun agregarOffset(segundos: Int) {
        offset += (segundos * 1000L)
    }

    // ⏱️ TIEMPO ACTUAL (milisegundos)
    fun obtenerTiempo(): Long {
        if (inicio == 0L) return 0L

        val ahora = System.currentTimeMillis()

        val tiempoBase = if (enPausa) {
            (momentoPausa - inicio)
        } else {
            (ahora - inicio)
        }

        return tiempoBase - pausaAcumulada + offset
    }

    // ⏱️ SEGUNDOS
    fun obtenerTiempoSegundos(): Long {
        return obtenerTiempo() / 1000
    }

    // 🧾 FORMATO mm:ss
    fun obtenerTiempoFormateado(): String {
        val totalSeg = obtenerTiempoSegundos()

        val min = totalSeg / 60
        val seg = totalSeg % 60

        return String.format("%02d:%02d", min, seg)
    }

    // 📡 EXPORTAR A FIREBASE
    fun toFirebaseMap(): Map<String, Any> {
        return mapOf(
            "FECHA_PLAY" to inicio,
            "CRONO_PAUSA_ACUMULADA" to (pausaAcumulada / 1000), // Segundos de pausas PASADAS
            "CRONO_OFFSET" to (offset / 1000),
            "CRONO_EN_PAUSA" to enPausa,
            "CRONO_FINALIZADO" to finalizado,
            "CRONO_INICIO_PAUSA" to momentoPausa // En milisegundos para precisión
        )
    }

    // 📥 IMPORTAR DESDE FIREBASE
    fun cargarDesdeFirebase(data: Map<String, Any>) {
        // 1. Cargamos los valores básicos (convertimos segundos de Firebase a milisegundos)
        inicio = (data["FECHA_PLAY"] as? Long) ?: 0L
        pausaAcumulada = ((data["CRONO_PAUSA_ACUMULADA"] as? Long) ?: 0L) * 1000
        offset = ((data["CRONO_OFFSET"] as? Long) ?: 0L) * 1000

        // 2. Cargamos los estados
        enPausa = data["CRONO_EN_PAUSA"] as? Boolean ?: false
        finalizado = data["CRONO_FINALIZADO"] as? Boolean ?: false

        // 3. 🎯 LA CLAVE: Cargamos el momento exacto de la pausa que guardó el control.
        // NO usamos System.currentTimeMillis() aquí porque eso "resetea" la pausa.
        momentoPausa = (data["CRONO_INICIO_PAUSA"] as? Long) ?: 0L
    }

    fun estaEnPausa() = enPausa
    fun estaFinalizado() = finalizado
}