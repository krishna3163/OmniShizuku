# OmniShizuku

OmniShizuku es una utilidad de código abierto para Android para flujos de trabajo de Shizuku, ADB y Dhizuku. Proporciona acceso a API de nivel del sistema a través del servicio Shizuku, con módulos para gestión de dispositivos, automatización, optimizaciones y más.

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163/OmniShizuku-blue?logo=github)](https://github.com/krishna3163/OmniShizuku)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android)](https://developer.android.com)

🌐 **Documentation available in:** [English](./README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [العربية](./README.ar.md) • [日本語](./README.ja.md) • [Português](./README.pt.md) • [Қазақша](./README.kk.md) • [Qazaqşa](./README.kk-Latn.md)

---

## Características

### Centro de Esenciales
- **Dynamic Island** — Overlay de píldora flotante para medios en vivo, batería y alertas
- **Smart Pixels (Ahorrador AMOLED)** — Deshabilita el 25–75% de la cuadrícula de subpíxeles OLED para ahorrar 15–44% de energía de pantalla
- **Display Tweaker** — Modificación de resolución, densidad de pantalla (DPI) y tasa de refresco
- **Forzar Modo Oscuro** — Fuerza temas oscuros en todas las aplicaciones instaladas
- **Centro y Guardia de Batería** — Salud de batería en tiempo real, conteo de ciclos, temperatura y guardia de longevidad
- **Guardia de Modo Bolsillo** — Protección de sensor de proximidad y luz contra toques accidentales en el bolsillo
- **Seguridad de Pantalla y Desbloqueo DRM** — Evita FLAG_SECURE para permitir capturas de pantalla en aplicaciones bancarias y de streaming
- **Permisos y AppOps** — Controlador de permisos privilegiados y límites de servicios en segundo plano
- **Ajustes de SystemUI** — Lista negra de iconos de barra de estado, estilos de icono de batería y modo inmersivo
- **Optimizador de Juegos y CPU Axeron** — Perfiles de rendimiento, latencia de animación cero y optimización de caché
- **Forzar Doze** — Automatización agresiva de reposo profundo en espera al apagar la pantalla
- **Controles de Energía Avanzados** — Reinicio, recovery, bootloader, apagado y tecla de suspensión a través de shell
- **Controlador de DNS Privado** — Ajustes preestablecidos de DNS cifrado sin VPN (Cloudflare, AdGuard, Quad9, Google)
- **Evadir Restricciones de Zona Wi-Fi** — Desbloquea el punto de acceso restringido por el operador
- **Contraseñas Wi-Fi Guardadas** — Lee y muestra puntos de acceso Wi-Fi guardados y contraseñas
- **Shut Up! Silenciador de Medios** — Silenciamiento de audio multimedia por aplicación
- **Control Maestro de Flujos de Audio** — Control de volumen independiente para diferentes canales
- **Saltar Pistas con Botones de Volumen** — Salta canciones usando los botones de volumen con la pantalla apagada
- **Iluminación de Bordes de Notificación** — Animación de brillo de neón en los bordes de la pantalla al recibir notificaciones
- **Auto DND (Modo Zen)** — Programación automatizada del modo No Molestar
- **Caffeinate** — Mantiene la pantalla encendida con notificación persistente y mosaico en Ajustes Rápidos
- **Estudio de Marca de Agua y EXIF** — Marco elegante estilo Leica y marca minimalista Hasselblad con tira EXIF en vivo
- **Debloater Universal de Android (UAD)** — Eliminación de aplicaciones según niveles de seguridad de la comunidad y restauración con un toque
- **Congelador de Aplicaciones** — Congela instantáneamente aplicaciones en segundo plano
- **Explorador de Archivos Privilegiado** — Explora rutas protegidas del sistema
- **Instalador de Paquetes Pro** — Instala paquetes divididos APKs, APKS y XAPK

### Módulos Integrados Adicionales
- **Theme Patcher** — Personalización del tema del sistema
- **Sound Master** — Control avanzado de audio
- **Mixed Audio** — Mezcla y enrutamiento de audio
- **LookBack** — Grabación y reproducción de pantalla
- **Universal PiP** — Picture-in-picture para cualquier aplicación
- **Intent Shell** — Comandos de shell mediante intents
- **Local Shell** — Emulador de terminal integrado

### Auto-PIN de ADB Inalámbrico (Accesibilidad)
- **Desactivado** — Escribe el PIN de emparejamiento manualmente
- **Manual** — El administrador te notifica cuando aparece el diálogo
- **Automático** — El servicio de accesibilidad ingresa el PIN almacenado en el diálogo "Vincular con dispositivo"
- Exclusivamente opt-in — no se ejecuta nada hasta que lo habilites en Ajustes

### Rendimiento y Estabilidad
- **Lazy motion blur** — Los bucles de muestreo de fotogramas solo se ejecutan al desplazarse, eliminando el gasto de CPU por fotograma
- **Crashlytics + manejador no capturado** — Los bloqueos se registran detalladamente en lugar de cerrar la app silenciosamente
- **Binder Shizuku defensivo** — Acceso a binder seguro contra valores nulos para prevenir fallos NPE en dispositivos Realme y OEM
- **Carga acotada de DeviceInfo** — Tiempo de espera de 20s en consultas dumpsys, mostrando información parcial en lugar de bloquearse

### Configuración Inicial de Primer Inicio
- 6 pantallas de aviso legal (privilegios, efectos del propietario del dispositivo, riesgos de root, comportamiento de terceros, licencia, advertencias de apps bancarias)
- Acuerdo con temporizador de cuenta regresiva de 5 segundos y casilla de verificación "He leído y acepto"
- Selección de idioma (sistema + 12 idiomas)
- Selección de tema (seguir al sistema / oscuro / claro / noche negra AMOLED / color del sistema)
- Método de activación (depuración inalámbrica / root / conectar a una computadora)

---

## Comparación con Otros Forks de Shizuku

| Proyecto | Inicio Automático | Modo TCP | Dhizuku | Soporte de Módulos | Sigilo (Stealth) | Terminal | Integración IA | UI Expresiva | Bienvenida | Licencia |
|---------|-----------|----------|---------|----------------|---------|----------|----------------|---------------|------------|---------|
| **OmniShizuku** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | Apache-2.0 |
| Porter | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| shevery | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | Material 3 | ❌ | Apache-2.0 |
| Shizako | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| Shizuku (thedjchi fork) | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| Shizuku Next | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | Material 3 | ❌ | Apache-2.0 |
| ShizukuPlus | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| Stellar | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | MPL-2.0 |
| Xhizuku | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | Material 3 Expressive | ✅ | Apache-2.0 |

---

## Requisitos

- Android 5.0 (API 21) o superior
- Servicio Shizuku en ejecución (vía depuración inalámbrica, root o ADB)
- Permiso de Shizuku concedido a la aplicación

---

## Instalación

1. Descarga e instala el APK desde [GitHub Releases](https://github.com/krishna3163/OmniShizuku/releases)
2. Habilita Shizuku en tu dispositivo (depuración inalámbrica o ADB)
3. Concede permisos de Shizuku a OmniShizuku
4. En el primer inicio, completa el asistente de configuración inicial

---

## Compilación desde el Código Fuente

```bash
git clone git@github.com:krishna3163/OmniShizuku.git
cd OmniShizuku
./gradlew :manager:assembleDebug
```

El APK de depuración se encontrará en `manager/build/outputs/apk/debug/manager-debug.apk`.

---

## Colaboradores y Créditos

Agradecemos a todos los que han contribuido con OmniShizuku en GitHub:

- **krishna3163** — Mantenedor y Desarrollador Principal

También agradecemos a los siguientes proyectos de código abierto cuyos aportes, ideas y APIs inspiraron o se integraron en OmniShizuku:

- **Shizuku** (rikkaw) — Framework base de Shizuku y API de Binder
- **Dhizuku** — Integración del servicio Dhizuku
- **shevery** — Diseño Material 3, inicio automático, modo TCP, soporte de módulos, terminal, integración de IA
- **Shizuku Next** — Emparejamiento automático, selección de método de inicio, watchdog, gestor de app-ops/firewall
- **Essentials (sameerasw)** — Módulos esenciales de funciones (Dynamic Island, Smart Pixels, Caffeinate, etc.)
- **App Ops** — Patrones de gestión de permisos
- **Active Launcher** — Atajos de automatización
- **EnforceDoze** — Patrones de automatización para el modo Doze
- **Xhizuku** — Interfaz Material 3 Expressive, asistente de bienvenida, monitor del servidor
- **Stellar** — Implementación simple de terminal
- **ShizukuPlus** — Superficie de API ampliada para desarrolladores
- **Porter** — Referencia de fork minimalista de Shizuku
- **Shizako** — Referencia de reemplazo directo de gestor
- **DFRoot (diabl0w)** — Integración del exploit de kernel DirtyFrag (CVE-2026-43284) (GPL-2.0) como funcionalidad integrada

---

## Mosaicos de Ajustes Rápidos

OmniShizuku incluye mosaicos de Ajustes Rápidos para:
- Caffeinate (mantener pantalla activa)
- Conmutador de ADB inalámbrico
- Iniciar / detener servidor Shizuku
- Conmutador de modo oculto (Stealth)

Añádelos desplegando el panel de notificaciones y tocando el icono del lápiz de edición.

---

## Seguridad

Esta aplicación requiere privilegios elevados del sistema a través de Shizuku. Úsala de forma responsable:

- No otorgues acceso a aplicaciones no confiables
- La depuración inalámbrica y ADB exponen interfaces del sistema
- El acceso root elude el modelo de seguridad de Android
- El modo de propietario del dispositivo (Device Owner) puede tener efectos secundarios (restricciones de restablecimiento de fábrica, límites de desinstalación de apps)

Consulta el aviso legal del primer inicio para obtener todos los detalles.

---

## Licencia

Licencia Apache 2.0. Consulta [LICENSE](LICENSE) para más detalles.

---

## Descargo de Responsabilidad

OmniShizuku se proporciona "tal cual", sin garantías de ningún tipo. Úsalo bajo tu propia responsabilidad. No nos hacemos responsables de pérdidas de datos, anulación de garantías ni del comportamiento de aplicaciones de terceros.
