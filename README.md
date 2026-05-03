# UrovoRFID — Proyecto Android (Ingeniería Inversa de RFID.apk)

## Descripción

Proyecto Android reconstruido a partir de ingeniería inversa del APK `RFID.apk` instalado en el dispositivo **Urovo DT50** (`/system/app/RFID/RFID.apk`).

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

## Notas de ingeniería inversa

- El APK usa **DataBinding** y **Navigation Component** de AndroidX
- El SDK se comunica con el servicio del sistema via **AIDL Binder**
- Los TAGs se leen usando el protocolo **EPC Gen2 / ISO 18000-6C**
- El RSSI raw se convierte a dBm restando 129: `rssiDbm = rawValue - 129`
- El APK original incluye soporte para exportar a Excel (Apache POI)
- El trigger físico usa keyCode **523** (específico del Urovo DT50)
