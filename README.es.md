# OmniShizuku

OmniShizuku es una utilidad de código abierto para Android para flujos de trabajo de Shizuku, ADB y Dhizuku. Proporciona acceso a API de nivel del sistema a través del servicio Shizuku, con módulos para gestión de dispositivos, automatización, optimizaciones y más.

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163/OmniShizuku-blue?logo=github)](https://github.com/krishna3163/OmniShizuku)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android)](https://developer.android.com)

---

## Características

### Centro de Esenciales
- **Dynamic Island** — Overlay de píldora flotante para medios en vivo, batería y alertas
- **Smart Pixels (Ahorrador AMOLED)** — Deshabilita el 25–75% de la cuadrícula de subpíxeles OLED para ahorrar 15–44% de energía de pantalla
- **Ajuste de Pantalla** — Sobrescrituras de resolución, densidad de pantalla (DPI) y tasa de refresco
- **Forzar Modo Oscuro** — Fuerza temas oscuros en todas las aplicaciones instaladas
- **Centro y Guardia de Batería** — Salud de batería en tiempo real, conteo de ciclos, temperatura y guardia de longevidad
- **Guarda de Modo Bolsillo** — Protección de sensor de proximidad y luz contra toques accidentales en el bolsillo
- **Seguridad de Pantalla y Desbloqueo DRM** — Evita FLAG_SECURE para capturas de pantalla en aplicaciones bancarias y de streaming
- **Permisos y AppOps** — Controlador de privilegios y límites de servicios en segundo plano
- **Ajuste de SystemUI** — Lista negra de iconos de barra de estado, estilos de icono de batería y modo inmersivo
- **Optimizador de Juegos y CPU Axeron** — Perfiles de rendimiento, latencia de animación cero y trim de caché
- **Forzar Doze** — Automatización de sueño profundo agresivo en espera con pantalla apagada
- **Controles de Potencia Avanzados** — Reinicio, recuperación, bootloader, apagado y tecla de suspensión a través de shell
- **Controlador de DNS Privado** — Presets de DNS cifrado sin VPN (Cloudflare, AdGuard, Quad9, Google)
- **Evadir Anclaje de Operador** — Desbloquea punto de acceso restringido por operador
- **Contraseñas Wi-Fi Guardadas** — Lee y muestra puntos de acceso Wi-Fi guardados y contraseñas
- **Silenciar Medios** — Silencio de audio de medios por aplicación
- **Maestro de Flujos de Audio** — Control independiente de volumen para medios, llamada, alarma, voz
- **Saltar Pistas con Teclas de Volumen** — Saltar pistas usando botones de volumen con pantalla apagada
- **Iluminación de Bordes de Notificación** — Animación de brillo de neón en los bordes de la pantalla en notificaciones
- **DND Automático (Modo Zen)** — Programación automatizada de No Molestar basada en enfoque de aplicación y horarios
- **Caffeinate** — Mantener la pantalla encendida con notificación de parada y tile de Configuración Rápida
- **Estudio de Marca de Agua y EXIF** — Marco Leica de lujo y marca minimalista Hasselblad con EXIF en vivo
- **Debloater Universal de Android (UAD)** — Eliminación de paquetes clasificada por seguridad de la comunidad y restauración con 1 toque
- **Congelador de Aplicaciones** — Congela aplicaciones en segundo plano al instante sin desinstalar
- **Explorador de Archivos Privilegiado** — Navega rutas protegidas del sistema y directorios Android/data
- **Instalador de Paquetes Pro** — Instala APKs divididos, APKS, XAPK con marcas de degradación y bajo SDK

### Auto-PIN de ADB Inalámbrico (Accesibilidad)
- **Apagado** — Escribe el PIN de emparejamiento tú mismo
- **Manual** — El gestor te avisa cuando aparece el diálogo
- **Automático** — Servicio de accesibilidad ingresa el PIN guardado en el diálogo "Emparejar con dispositivo"
- Solo opt-in — nada se ejecuta hasta que lo actives en Configuración

### Rendimiento y Estabilidad
- **Movimiento difuminado perezoso** — Los bucles de muestreo de frames solo se ejecutan durante el desplazamiento, eliminando el consumo de CPU por frame
- **Crashlytics + controlador de excepciones no capturadas** — Los fallos se registran en lugar de cerrar la aplicación silenciosamente
- **Binder de Shizuku defensivo** — Acceso a binder seguro contra nulos para evitar fallos NPE en Realme/OEM
- **Carga de Información del Dispositivo limitada** — Tiempo de espera de 20s en consultas de dumpsys, muestra datos parciales en lugar de un spinner atascado

### Incorporación en Primer Inicio
- 6 pantallas de descargo de responsabilidad (privilegios, efectos secundarios de propietario de dispositivo, riesgos de root, comportamiento de terceros, licencia, advertencias para aplicaciones bancarias)
- Acuerdo con temporizador de cuenta regresiva de 5 segundos y casilla "He leído y acepto"
- Selección de idioma (sistema + 12 idiomas)
- Selección de tema (seguir sistema / oscuro / claro / noche negra AMOLED / usar color del sistema)
- Método de activación (depuración inalámbrica / root / conectar a una computadora)

---

## Requisitos

- Android 5.0 (API 21) o superior
- Servicio Shizuku ejecutándose (vía depuración inalámbrica, root o ADB)
- Permiso de Shizuku concedido a la aplicación

---

## Instalación

1. Instala el APK desde [GitHub Releases](https://github.com/krishna3163/OmniShizuku/releases)
2. Habilita Shizuku en tu dispositivo (depuración inalámbrica o ADB)
3. Concede el permiso de Shizuku a OmniShizuku
4. En el primer lanzamiento, completa el flujo de incorporación

---

## Compilación desde el Código Fuente

```bash
git clone git@github.com:krishna3163/OmniShizuku.git
cd OmniShizuku
./gradlew :manager:assembleDebug
```

El APK de depuración estará en `manager/build/outputs/apk/debug/manager-debug.apk`.

---

## Seguridad

Esta aplicación solicita privilegios elevados del sistema a través de Shizuku. Úsala de manera responsable:

- No concedas acceso a aplicaciones no confiables
- La depuración inalámbrica y ADB exponen interfaces del sistema
- El acceso root evita el modelo de seguridad de Android
- El modo propietario de dispositivo puede tener efectos secundarios (restricciones de restablecimiento de fábrica, límites de eliminación de aplicaciones)

Consulta el descargo de responsabilidad del primer inicio para más detalles.

---

## Licencia

Licencia Apache 2.0. Consulta [LICENSE](LICENSE) para más detalles.

---

## Descargo de Responsabilidad

OmniShizuku se proporciona tal cual, sin garantía de ningún tipo. Úsalo bajo tu propio riesgo. No somos responsables de pérdida de datos, anulación de garantía o comportamiento de aplicaciones de terceros.
