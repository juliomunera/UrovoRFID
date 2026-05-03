# UrovoRFID — Proyecto Android (Ingeniería lectura TAG estudiantes)

## Descripción

La app permite **lectura de TAGs RFID UHF a larga distancia** usando el lector integrado del Urovo DT50.

---

## Arquitectura

```
com.ubx.rfid                    ← App principal
├── MainActivity                ← Activity única, gestiona RfidManager y trigger físico
├── MainViewModel               ← Estado compartido (conexión, readId, fragmento actual)
├── RFIDApplication             ← Application class
│
├── p006ui/scan/
│   ├── ScanFragment            ← UI de escaneo de TAGs
│   ├── ScanViewModel           ← Estado de la pantalla de escaneo
│   └── ScanModel               ← Modelo de datos de un TAG
│
├── adapter/
│   └── ScanAdapter             ← RecyclerView adapter para la lista de TAGs
│
└── util/sharedPreference/
    ├── PreKey                  ← Enum de claves de configuración
    └── SPUtils                 ← Wrapper de SharedPreferences

com.ubx.usdk.rfid               ← SDK del sistema Urovo (AIDL)
├── RfidManager                 ← Gestor principal (se conecta al servicio del sistema)
└── aidl/
    ├── IRfidManager            ← Interfaz AIDL del servicio RFID
    ├── IRfidCallback           ← Interfaz AIDL de callbacks
    └── RfidDate                ← Parcelable con configuración del lector
```

---

## Cómo funciona el SDK

El lector RFID del Urovo DT50 está expuesto como un **servicio del sistema Android** via AIDL:

- **Servicio**: `com.ubx.usdk.rfid` / acción `com.ubx.usdk.rfid.RfidService`
- **Interfaz**: `com.ubx.usdk.rfid.aidl.IRfidManager`
- **Puerto serie**: `/dev/ttyHSL0` a 115200 bps (por defecto)

### Flujo de uso

```java
// 1. Crear el manager
RfidManager rfid = new RfidManager(context);

// 2. Vincular al servicio del sistema
rfid.bindService(new RfidManager.ConnectionListener() {
    public void onConnected() {
        // 3. Conectar al lector físico
        rfid.connectCom("/dev/ttyHSL0", 115200);
        byte readId = rfid.getReadId();

        // 4. Registrar callback para recibir TAGs
        rfid.registerCallback(myCallback);

        // 5. Iniciar inventario (lectura continua)
        rfid.customizedSessionTargetInventory(readId, (byte)1, (byte)0, (byte)1);
    }
});

// 6. Implementar el callback
IRfidCallback myCallback = new IRfidCallback.Stub() {
    public void onInventoryTag(byte readId, String pc, String crc, String epc,
            byte ant, String rssiRaw, String freq, int count, int total, String extra) {
        // EPC del tag detectado
        int rssiDbm = Integer.parseInt(rssiRaw) - 129; // convertir a dBm
        Log.d("TAG", "EPC: " + epc + " RSSI: " + rssiDbm + "dBm");
    }

    public void onInventoryTagEnd(int total, int reads, int timeMs, int speed, byte readId) {
        // Fin de ciclo — relanzar para inventario continuo
        rfid.customizedSessionTargetInventory(readId, (byte)1, (byte)0, (byte)1);
    }
    // ... otros métodos
};
```

---

## Parámetros de inventario

### `customizedSessionTargetInventory(readId, session, target, repeat)`

| Parámetro | Descripción | Valores |
|-----------|-------------|---------|
| `readId`  | ID del lector (obtenido con `getReadId()`) | byte |
| `session` | Sesión EPC Gen2 | 0=S0, 1=S1, 2=S2, 3=S3 |
| `target`  | Target de inventario | 0=A, 1=B |
| `repeat`  | Repeticiones por ciclo | 1-255 |

### Potencia de transmisión

```java
// Potencia máxima para larga distancia (33 dBm)
rfid.setOutputPower(readId, (byte) 33);

// Potencia media (26 dBm)
rfid.setOutputPower(readId, (byte) 26);
```

### Perfiles de rendimiento (del APK original)

| Perfil | Session | Potencia | Uso |
|--------|---------|----------|-----|
| 0 | S2 | 30 dBm | Alta velocidad |
| 1 | S0 | 30 dBm | Máxima velocidad |
| 2 | S0 | 33 dBm | Larga distancia |
| 3 | S1 | 30 dBm | Balanceado |
| 4 | S1 | 30 dBm | Estándar |
| 5 | Custom | Custom | Personalizado |

---

## Trigger físico

El Urovo DT50 tiene un gatillo físico con **keyCode 523**. La app lo captura en `MainActivity.dispatchKeyEvent()` y lo delega al `ScanFragment.onTriggerKey()`.

---

## Configuración (SharedPreferences)

| Clave (`PreKey`) | Descripción | Default |
|-----------------|-------------|---------|
| `SERIAL` | Puerto serie | `/dev/ttyHSL0` |
| `SERIAL_PORT` | Baudrate | `115200` |
| `SESSION` | Sesión EPC | `1` (S1) |
| `STATE` | Target | `0` (A) |
| `REPEAT_ONE` | Repeticiones | `1` |
| `POWER` | Potencia dBm | `30` |
| `SOUND_PLAY` | Sonido al leer | `true` |
| `FILTER_WAY` | Modo de filtro | `0` (sin filtro) |

---

## Compilar e instalar

```bash
# Compilar
cd UrovoRFID
./gradlew assembleDebug

# Instalar en el Urovo DT50 (con ADB)
adb install app/build/outputs/apk/debug/app-debug.apk
```

> **Nota**: La app requiere que el servicio `com.ubx.usdk.rfid` esté instalado en el sistema del dispositivo. Este servicio es parte del firmware del Urovo DT50 y no puede instalarse en dispositivos genéricos.

---

## Archivos extraídos del dispositivo

- `RFID.apk` — APK original extraído de `/system/app/RFID/RFID.apk`
- `rfid_decompiled/` — Código fuente decompilado con jadx

---

## Notas de ingeniería

- El APK usa **DataBinding** y **Navigation Component** de AndroidX
- El SDK se comunica con el servicio del sistema via **AIDL Binder**
- Los TAGs se leen usando el protocolo **EPC Gen2 / ISO 18000-6C**
- El RSSI raw se convierte a dBm restando 129: `rssiDbm = rawValue - 129`
- El APK original incluye soporte para exportar a Excel (Apache POI)
- El trigger físico usa keyCode **523** (específico del Urovo DT50)

---

## Explicación detallada: ScanFragment

`ScanFragment` es el corazón de la app. Es la pantalla principal donde se leen los TAGs RFID.

### 1. Estado interno

```java
private byte session = 1;   // Sesión EPC Gen2 (S1)
private byte state   = 0;   // Target A
private byte repeat  = 1;   // 1 ciclo por llamada
private boolean inventoryFlag = false;  // ¿está escaneando?
private final Map<String, Integer> deduplicationMap = new HashMap<>();
```

`deduplicationMap` es clave: guarda `EPC → posición en la lista`. Así sabe si un TAG ya fue detectado antes o es nuevo.

### 2. Flujo principal

```
Usuario pulsa botón (o trigger físico)
        ↓
  toggleInventory()
        ↓
  startInventory()  →  mRfidManager.customizedSessionTargetInventory(...)
                                ↓
                    El lector emite señal UHF
                                ↓
                    TAG responde con su EPC
                                ↓
              onInventoryTag() [hilo Binder]
                                ↓
              mHandler.sendMessage() [→ UI thread]
                                ↓
                    processTag() → actualiza lista
                                ↓
              onInventoryTagEnd() → relanza inventario
                                ↓
              (bucle continuo hasta stopInventory())
```

### 3. El callback RFID (clase interna `RFIDCallback`)

Es el receptor de eventos del lector. Corre en el **hilo Binder** (no en el UI thread), por eso usa un `Handler` para pasar los datos a la UI:

```java
public void onInventoryTag(..., String rssiRaw, ..., String epc, ...) {
    // Convierte RSSI: el lector devuelve un número raw, hay que restar 129
    rssiStr = (Integer.parseInt(rssiRaw) - 129) + "dBm";

    ScanModel model = new ScanModel(epc, 1, rssiStr, pc, crc, false);
    mHandler.sendMessage(...);  // pasa al UI thread
}

public void onInventoryTagEnd(...) {
    // Fin de un ciclo → relanza automáticamente para escaneo continuo
    mRfidManager.customizedSessionTargetInventory(readId, session, state, repeat);
}
```

### 4. Deduplicación en `processTag()`

```java
if (deduplicationMap.containsKey(epc)) {
    // TAG ya conocido → solo actualiza RSSI y suma al contador
    existing.setRssi(model.getRssi());
    existing.setCount(existing.getCount() + 1);
    mAdapter.notifyItemChanged(pos);
} else {
    // TAG nuevo → lo agrega a la lista
    deduplicationMap.put(epc, mData.size());
    mData.add(model);
    mAdapter.notifyItemInserted(...);
}
```

Esto explica por qué la lista muestra "tags únicos" vs "total lecturas": el mismo TAG puede ser leído 50 veces pero aparece una sola vez en la lista, con su contador incrementando.

### 5. El Handler (clase interna `ScanHandler`)

Tiene dos responsabilidades:

```java
if (msg.what == MSG_NOTIFY_ITEM) {
    // Procesa un TAG recibido del callback
    processTag((ScanModel) msg.obj);

} else if (msg.what == MSG_TIMER) {
    // Se dispara cada 1 segundo → actualiza tiempo y velocidad
    totalTime++;
    scanViewModel.setScanSpeed(totalCount / totalTime);  // tags/segundo
    mHandler.sendEmptyMessageDelayed(MSG_TIMER, 1000L);  // se reprograma solo
}
```

### 6. Ciclo de vida y el callback

```java
onResume()  → registerCallback()    // empieza a recibir eventos
onPause()   → unregisterCallback()  // deja de recibir, detiene escaneo
```

Si sales de la pantalla, el escaneo se detiene automáticamente y no hay fugas de memoria.

### Resumen

`ScanFragment` arranca el inventario RFID, recibe TAGs por callback desde el lector, los deduplica, los muestra en una lista con RSSI y contador, y mide velocidad/tiempo — todo en bucle continuo hasta que el usuario para.

---

## Flujo completo al detectar un TAG nuevo

### Visión general

Cuando el lector RFID detecta un TAG, la información viaja a través de varias capas antes de aparecer en pantalla. El flujo completo involucra tres hilos distintos y cuatro capas de la arquitectura.

```
┌─────────────────────────────────────────────────────────────────────┐
│  Dispositivo físico (lector UHF integrado en Urovo DT50)            │
│  Emite señal de radio → TAG responde con su EPC                     │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ Puerto serie /dev/ttyHSL0 @ 115200
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Servicio del sistema: com.ubx.usdk.rfid (RfidService)              │
│  Proceso separado — firmware Urovo                                  │
│  Decodifica el protocolo EPC Gen2 / ISO 18000-6C                    │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ Android Binder IPC
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│  IRfidCallback.onInventoryTag()  [HILO BINDER]                      │
│  Parámetros recibidos:                                              │
│    - epc      : código EPC del TAG (ej: "E2003412012345678901")     │
│    - rssiRaw  : señal raw (restar 129 para obtener dBm)             │
│    - pc, crc  : datos de protocolo                                  │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ mHandler.sendMessage()
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│  ScanHandler.handleMessage()  [HILO UI]                             │
│  Convierte RSSI: rssiDbm = Integer.parseInt(rssiRaw) - 129          │
│  Crea ScanModel(epc, rssi, pc, crc)                                 │
│  Llama a processTag(model)                                          │
└──────────────────────────────┬──────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│  ScanFragment.processTag()  [HILO UI]                               │
│                                                                     │
│  ¿El EPC ya está en deduplicationMap?                               │
│  ├── SÍ (TAG duplicado):                                            │
│  │     Actualiza RSSI y suma al contador                            │
│  │     notifyItemChanged() → refresca fila en pantalla              │
│  │     (sin beep, sin guardar en BD)                                │
│  │                                                                  │
│  └── NO (TAG nuevo):                                                │
│        Agrega a la lista con EPC como texto provisional             │
│        notifyItemInserted() → aparece en pantalla inmediatamente    │
│        BeepManager.beep() → emite sonido de confirmación            │
│        Lanza hilo de fondo ──────────────────────────────────────┐  │
└──────────────────────────────────────────────────────────────────┼──┘
                                                                   │
                               ┌───────────────────────────────────┘
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│  Hilo de fondo (Thread)                                             │
│                                                                     │
│  1. InventaryDao.findByTagId(epc)                                   │
│     ¿Existe el TAG en la tabla Inventary?                           │
│     ├── SÍ: obtiene la descripción                                  │
│     │     runOnUiThread:                                            │
│     │       model.setDisplayText(descripcion)                       │
│     │       notifyItemChanged() → fila se actualiza con descripción │
│     └── NO: el EPC raw permanece como texto en pantalla             │
│                                                                     │
│  2. TagReadDao.insert(epc)                                          │
│     Guarda en tabla TagRead:                                        │
│       tagId    = EPC del TAG                                        │
│       readDate = timestamp actual (YYYY-MM-DD HH:mm:ss)            │
│       synced   = 0  (pendiente de envío a la API)                   │
└─────────────────────────────────────────────────────────────────────┘
```

### Detalle de cada paso

**1. Lector físico → RfidService**

El lector UHF integrado en el DT50 se comunica por puerto serie (`/dev/ttyHSL0` a 115200 bps). El servicio del sistema `com.ubx.usdk.rfid` gestiona el protocolo EPC Gen2 y expone los resultados via AIDL.

**2. Callback AIDL → Handler**

`onInventoryTag()` se ejecuta en el hilo Binder, que es un hilo del sistema. No se puede tocar la UI desde ahí. Por eso se usa un `Handler` vinculado al hilo principal para trasladar el evento de forma segura.

**3. Deduplicación**

El `deduplicationMap` es un `HashMap<String, Integer>` que mapea cada EPC a su posición en la lista. Si el mismo TAG se lee 100 veces, solo aparece una fila — el contador se incrementa y el RSSI se actualiza con la lectura más reciente.

**4. Consulta al inventario (asíncrona)**

La consulta a SQLite se hace en un hilo de fondo para no bloquear el hilo UI. El TAG aparece en pantalla de inmediato con su EPC, y en milisegundos se actualiza con la descripción si existe en el inventario local.

**5. Persistencia en TagRead**

Cada TAG nuevo se guarda en la tabla `TagRead` con `synced = 0`. El `SyncWorker` (WorkManager) recoge estos registros periódicamente y los envía a la API central, marcándolos como `synced = 1` tras el envío exitoso.

### Visualización en pantalla

| Caso | Columna EPC / Descripción |
|------|--------------------------|
| TAG en inventario local | **Descripción** (azul índigo) + EPC pequeño en gris debajo |
| TAG no catalogado | EPC raw en fuente monospace gris |

### Ciclo de vida del inventario en BD

```
TAG leído → TagRead (synced=0)
                │
                │  SyncWorker cada 15 min
                ▼
           Envío a API → TagRead (synced=1)
                │
                │  deleteOlderThan(retention_days)
                ▼
           Eliminado (default: 8 días)
```

El valor de `retention_days` es configurable desde el menú **Configuración** de la app y se almacena en la tabla `Settings`.
