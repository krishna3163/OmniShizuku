<div align="center">

<img src="../manager/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="128" height="128" alt="OmniShizuku Logo">

#OmniShizuku

**Una bifurcación potente y rica en funciones de Shizuku con módulos mejorados, interfaz de usuario moderna y capacidades extendidas de control del sistema.**

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163%2FOmniShizuku-blue?logo=github&style=for-the-badge)](https://github.com/krishna3163/OmniShizuku)
[![Licencia: Apache-2.0](https://img.shields.io/badge/Licencia-Apache%202.0-blue.svg?style=for-the-badge)](../LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android&style=for-the-badge)](https://developer.android.com)
[![API](https://img.shields.io/badge/API%20Level-21%2B-brightgreen?style=for-the-badge)](https://developer.android.com/guide/topics/manifest/uses-sdk-element)
[![Shizuku](https://img.shields.io/badge/Powered%20by-Shizuku%20API-orange?style=for-the-badge)](https://github.com/RikkaApps/Shizuku)

🌐 **Documentation available in:** [English](../README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [العربية](./README.ar.md) • [日本語](./README.ja.md) • [Português](./README.pt.md) • [Қазақша](./README.kk.md) • [Qazaqşa](./README.kk-Latn.md)

</div>

---

## 📖 Tabla de contenidos

- [Descripción general](#-descripción general)
- [Capturas de pantalla](#-capturas de pantalla)
- [¿Por qué usar OmniShizuku?](#-por qué-usar-omnishizuku)
- [Características clave](#-características clave)
  - [Labs - Herramientas avanzadas del sistema (40 herramientas)](#labs---advanced-system-tools)
  - [Sistema de módulos](#modules-system)
  - [Grabación de llamadas (integración de ShizuCallRecorder)](#call-recording-shizucallrecorder-integration)
  - [AI y funciones para desarrolladores](#ai--developer-features)
  - [Mejoras de UI/UX](#uiux-mejoras)
- [Comparación de funciones] (#-comparación-de-funciones-con-otras-bifurcaciones-shizuku)
- [Requisitos del sistema](#-requisitos-del-sistema)
- [Instalación](#-instalación)
- [Comenzando](#-empezando)
- [Construyendo desde la fuente](#-construcción-desde-la-fuente)
- [Arquitectura y seguridad](#-arquitectura--seguridad)
- [Permisos explicados](#-permisos-explicados)
- [Contribuyendo](#-contribuyendo)
- [Créditos y Agradecimientos](#-créditos--agradecimientos)
- [Licencia](#-licencia)
- [Soporte](#-soporte)

---

## 🔍 Descripción general

**OmniShizuku** es una bifurcación de código abierto ampliamente mejorada del proyecto original [Shizuku](https://github.com/RikkaApps/Shizuku). Amplía el potente acceso API a nivel de sistema de Shizuku combinándolo con una amplia colección de utilidades prácticas, ajustes del sistema, controles de privacidad y una interfaz moderna e intuitiva.

🌐 **Documentation available in:** [English](../README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [العربية](./README.ar.md) • [日本語](./README.ja.md) • [Português](./README.pt.md) • [Қазақша](./README.kk.md) • [Qazaqşa](./README.kk-Latn.md)
Creado con **Material Design 3 Expressive**, OmniShizuku permite a los usuarios tomar el control total de su dispositivo Android sin necesidad de acceso completo a la raíz en la mayoría de los casos, aprovechando las capacidades de puente raíz/ADB de Shizuku. Desde personalización de pantalla y optimización de la batería hasta administración de aplicaciones, eliminación de descargas, grabación de llamadas y automatización avanzada, OmniShizuku es su conjunto de utilidades de sistema todo en uno.

---

## 📸 Capturas de pantalla

| **Inicio y estado** | **Permisos de aplicaciones** | **Módulos ADB** | **Catálogo de laboratorios** | **Computación (Terminal e IA)** | **Configuración** |
|:---:|:---:|:---:|:---:|:---:|:---:|
| <img src="../screenshots/home.png" width="165" alt="Home Screen" /> | <img src="../screenshots/manage_apps.png" width="165" alt="Manage Authorized Apps" /> | <img src="../screenshots/modules.png" width="165" alt="ADB Modules" /> | <img src="../screenshots/labs.png" width="165" alt="Labs Catalog" /> | <img src="../screenshots/comput.png" width="165" alt="Comput Console" /> | <img src="../screenshots/settings.png" width="165" alt="Settings Screen" /> |
| Estado del servicio en tiempo real y emparejamiento ADB inalámbrico | Modo de lista blanca, permisos granulares y acceso solo por sesión | Instalador y administrador del módulo ZIP estilo Magisk/KernelSU | Más de 39 potentes controles y ajustes del sistema | Terminal shell privilegiado con generación Gemini/AI | Propietario del dispositivo, copias de seguridad, automatización y configuración de seguridad |

---

### 🔬 Laboratorios destacados y capacidades del sistema

| **Controles del sistema y herramientas eléctricas** | **Firewall nativo ShizuWall** |
|:---:|:---:|
| <img src="../screenshots/labs_tools.png" width="260" alt="Labs System Tools" /> | <img src="../screenshots/shizuwall.png" width="260" alt="ShizuWall Firewall" /> |
| Enforce Doze, audio mixto, DNS privado, seguridad de pantalla y DFRoot | Firewall nativo Zero-VPN con escáner de seguimiento y reglas de aplicación |

---

## 💡 ¿Por qué utilizar OmniShizuku?

- **Solución todo en uno**: no se necesitan múltiples aplicaciones raíz/ADB. Todo, desde la eliminación hasta la visualización de ajustes en un solo lugar.
- **No se requiere raíz**: funciona con Shizuku a través de ADB inalámbrico (Android 11+) o ADB, lo que lo hace accesible a un público más amplio.
- **UI moderna e intuitiva** - Clean Materia 3 Interfaz expresiva con animaciones fluidas, temas dinámicos y diseños optimizados para teléfonos, tabletas, televisores y Wear OS.
- **Mejorado activamente**: actualizaciones periódicas con nuevas funciones, correcciones de errores y mejoras más allá del Shizuku original.
- **Centrado en la privacidad**: control granular sobre permisos de aplicaciones, aplicaciones, funciones de ocultación y restricciones a nivel del sistema.
- **Seguro y reversible**: la mayoría de los cambios son reversibles, con capacidades de respaldo/restauración para la eliminación y administración de módulos.
- **Sistema de módulos extensibles**: módulos impulsados por la comunidad con soporte de catálogo, descubrimiento e instalación integrados.
- **Estabilidad de nivel empresarial**: prácticas de codificación defensivas, manejo de fallas, protecciones de tiempo de espera y correcciones de compatibilidad OEM.

---

## ✨ Características clave

### Laboratorios: herramientas avanzadas del sistema

El catálogo de Labs alberga 40 herramientas a nivel de sistema categorizadas claramente en administración de aplicaciones, controles del sistema y utilidades:

#### 📱 Gestión de aplicaciones (8 herramientas)
| Característica | Descripción |
| --- | --- |
| **Operaciones de aplicaciones** | Control granular sobre permisos ocultos de aplicaciones, puertas de operaciones de tiempo de ejecución y privilegios en segundo plano. |
| **Cortafuegos** | Firewall de aplicaciones a nivel de red que bloquea Wi-Fi y datos móviles por aplicación sin consumir una ranura VPN. |
| **Inicio automático** | Restrinja las aplicaciones para que no se activen o inicien servicios automáticamente al iniciar el dispositivo. |
| **Modos de ocultación** | Oculte las opciones de desarrollador, la depuración USB/inalámbrica, los servicios de accesibilidad, el DNS privado y los estados de VPN para que no sean detectados. |
| **Forzar modo oscuro** | Tema oscuro forzado en todo el sistema en aplicaciones de terceros, incluso sin soporte nativo para temas oscuros. |
| **Desinflador Universal (UAD)** | Eliminador de paquetes con clasificación de seguridad de la comunidad con calificaciones de seguridad, explicaciones y restauración con 1 toque. |
| **Congelador de aplicaciones** | Congele y suspenda instantáneamente aplicaciones en segundo plano para recuperar RAM y detener el consumo de batería. |
| **Optimizador de juegos Axeron** | Ajuste del rendimiento a nivel del sistema, mejora del juego, ajustes de animación y optimizaciones de procesos. |

#### ⚙️ Controles del sistema (18 herramientas)
| Característica | Descripción |
| --- | --- |
| **Menú de encendido** | Reinicio privilegiado en Sistema, Recuperación, Cargador de arranque/Arranque rápido, Modo seguro o apagado instantáneo. |
| **Ajustador de pantalla** | Anule la resolución de la pantalla, modifique la densidad de la pantalla (DPI) y fuerce frecuencias de actualización personalizadas. |
| **Contraseñas de Wi-Fi** | Vea, copie y exporte las credenciales de red Wi-Fi guardadas directamente a través de un shell privilegiado. |
| **Sintonizador de interfaz de usuario del sistema** | Personalice la visibilidad de los íconos de la barra de estado, los estilos de los íconos de la batería, los segundos del reloj y la navegación inmersiva. |
| **Hacer cumplir el sueño (sueño profundo)** | Fuerce el modo de suspensión profunda Doze agresivo e inmediato cada vez que se apague la pantalla. |
| **Controlador DNS privado** | Cambie entre proveedores de DNS cifrados (Cloudflare, AdGuard, Quad9, Google, NextDNS, Custom) con temporizadores de reversión automática. |
| **Maestro de transmisión de audio** | Controles deslizantes de volumen independientes y detallados para transmisiones de audio de medios, timbres, alarmas, llamadas, voz y sistema. |
| **Anclaje del operador** | Evite las verificaciones y restricciones de derechos de operador en puntos de acceso Wi-Fi y anclaje a red USB. |
| **Audio mixto** | Omisión de reproducción de audio simultánea de múltiples aplicaciones (evita que las aplicaciones en segundo plano se detengan cuando otra aplicación reproduce audio). |
| **Maestro de sonido** | Mezclador de enrutamiento de volumen y enfoque de audio por aplicación. |
| **Imagen en imagen universal** | Fuerce cualquier aplicación no compatible al modo Imagen en imagen (PiP) mediante proyección de pantalla virtual. |
| **Seguridad de pantalla y DRM** | Evite las restricciones de `FLAG_SECURE` para permitir capturas de pantalla y grabaciones de pantalla en aplicaciones bancarias/seguras. |
| **DND automático (modo Zen)** | Reglas automatizadas de No molestar basadas en el enfoque de la aplicación en primer plano, horarios personalizados y activadores. |
| **DFRoot (Explotación DirtyFrag)** | Ayudante de escalada de privilegios integrado que utiliza CVE-2026-43284 para versiones de kernel compatibles. |
| **Azulejos de configuración rápida** | Administrador interactivo y conjunto de configuración para 18 mosaicos de configuración rápida personalizados. |
| **Cortafuegos ShizuWall** | Interfaz de firewall avanzada que incluye un rastreador y escáner de telemetría integrados y un monitor de enchufe. |
| **Cafeinado** | Mantenga la pantalla del dispositivo activa con un mosaico y una notificación persistente para lectura o navegación. |
| **Parcheador de temas** | Colores de acento a nivel del sistema, estilos de fuente y modificaciones del tema de la interfaz de usuario. |

#### 🛠️ Utilidades (14 herramientas)
| Característica | Descripción |
| --- | --- |
| **Concha** | Terminal interactivo de línea de comandos privilegiado con salida de streaming en tiempo real. |
| **Visor de registros** | Visor logcat del sistema en tiempo real con capacidades de exportación, filtrado de niveles, etiquetas y paquetes. |
| **Información del dispositivo** | Hardware completo, kernel, estado de la batería, sensores térmicos y panel de SoC. |
| **Lanzador de actividades** | Inspeccione y ejecute actividades e intenciones de Android exportadas, no exportadas u ocultas. |
| **Archivos** | Explorador de sistema de archivos privilegiado con acceso de lectura/escritura a `/data` y directorios de aplicaciones protegidos. |
| **Instalador de aplicaciones Pro** | Instalador de paquetes privilegiado que admite formatos APK, paquetes divididos APKS y XAPK. |
| **Aplicaciones de terminal** | Conmutador rápido y lanzador para emuladores de terminales externos. |
| **Shell local** | Interfaz de shell local independiente. |
| **Intentos** | Active intenciones de sistema, receptores de transmisión y servicios personalizados, explícitos o implícitos. |
| **Modo sigiloso** | Medidas antidetección para evitar que las aplicaciones de destino detecten Shizuku, raíz o estado de depuración. |
| **Administrador de permisos** | Descripción general y revocación/concesión de permisos elevados en todos los paquetes instalados. |
| **Tienda Shizuku** | Catálogo integrado para descubrir y descargar aplicaciones de código abierto creadas para Shizuku. |
| **Grabación de llamadas (ShizuCallRecorder)** | Grabación nativa de llamadas bidireccionales impulsada por captura de audio de shell privilegiada. |
| **Fondos de pantalla de WallYou** | Navegador, buscador y conmutador de fondos de pantalla en la aplicación. |

### Sistema de módulos

OmniShizuku presenta un sistema de módulos potente y extensible inspirado en los módulos Magisk/KernelSU pero diseñado para mods basados en Shizuku:

- **Catálogo de módulos**: explore los módulos de la comunidad desde un catálogo seleccionado basado en GitHub con búsqueda, filtrado y clasificación.
- **Descubrimiento de módulos**: descubrimiento automático de módulos de repositorios de GitHub etiquetados con "shevery-modules" o fuentes oficiales.
- **Instalación con un solo toque**: descargue, verifique e instale módulos directamente desde la aplicación con seguimiento del progreso.
- **Administrador de módulos**: habilite, deshabilite, actualice y elimine módulos instalados con facilidad.
- **Soporte WebUI**: muchos módulos incluyen interfaces de configuración basadas en web a las que se puede acceder desde la aplicación.
- **Secuencias de comandos de módulo**: ejecute secuencias de comandos de instalación, desinstalación, servicio y arranque con registro y manejo de errores adecuados.
- **Update Checker**: detección automática de actualizaciones para módulos instalados desde sus versiones de GitHub.
- **Instalación segura** - Validación de la estructura del módulo, accesorios y scripts antes de la instalación.
- **UI multiplataforma**: pantallas de módulos optimizadas para teléfonos, tabletas, televisores y Wear OS.

### Grabación de llamadas (integración ShizuCallRecorder)

Soporte integrado para grabación de llamadas a través del acceso privilegiado de Shizuku:

- **Motor de grabación basado en Shell**: utiliza servicios de Shell privilegiados para una captura de audio confiable.
- **Servicio en primer plano**: servicio de grabación persistente con notificaciones adecuadas.
- **Integración de InCallService** - Detección automática de llamadas y gestión de grabaciones.
- **Administración de grabaciones**: vea, reproduzca, cambie el nombre y elimine grabaciones con una interfaz de usuario intuitiva.
- **Opciones configurables**: personalice el formato de grabación, el códec, la fuente y la ubicación de almacenamiento.
- **Interfaces AIDL**: IPC segura entre los componentes de la aplicación para el control de la grabación.
- **Manejo de permisos**: gestión adecuada de permisos en tiempo de ejecución para los requisitos de grabación de llamadas.
- **Operación en segundo plano** - Grabación confiable incluso con la aplicación en segundo plano.

### Funciones de IA y desarrolladores

- **AI Manager**: capacidades de IA integradas con soporte para múltiples proveedores (configurables en la configuración).
- **Opciones de desarrollador**: herramientas avanzadas para desarrolladores, utilidades de depuración y funciones experimentales.
- **Sistema de informe de errores**: informe de errores integrado con registros, información del dispositivo e informes de fallos.
- **Reaplicación de laboratorios**: vuelva a aplicar modificaciones de laboratorio persistentes después de actualizaciones o reinicios.
- **Panel de información del dispositivo**: información completa del dispositivo con propiedades detalladas de hardware, software y sistema.
- **Visor de registros**: visor de logcat en tiempo real con capacidades de filtrado y búsqueda.

### Mejoras en la interfaz de usuario/UX

- **Materia 3 Expressive Design** - El último diseño de materiales compatible con temas de colores dinámicos (Material You).
- **Compatibilidad con factores de forma múltiples**: optimizado para teléfonos, tabletas, dispositivos plegables, Android TV y Wear OS.
- **Modo oscuro y negro AMOLED**: compatibilidad total con temas oscuros con opción de negro puro para pantallas AMOLED.
- **Animaciones fluidas**: transiciones pulidas, optimización del desenfoque de movimiento lento e interacciones fluidas.
- **Diseños adaptables**: interfaz de usuario responsiva que se adapta a los tamaños y orientaciones de la pantalla.
- **Localización** - Amplia compatibilidad con varios idiomas (inglés, hindi, español, francés, chino, ruso, japonés, árabe, kazajo y más).
- **Accesibilidad**: PIN automático ADB inalámbrico con soporte de servicio de accesibilidad (solo suscripción voluntaria).
- **Rendimiento optimizado**: carga diferida, recuperación de datos limitada, protección de tiempo de espera y uso eficiente de la memoria.
- **Resiliencia ante fallos**: acceso defensivo a carpetas, manejo de excepciones no detectadas y degradación elegante en problemas específicos de OEM.

---

## 📊 Comparación de características con otras horquillas Shizuku

| Característica | **OmniShizuku** | **Shizuku (Original)** | **Shizuku Siguiente** | **Xhizuku** | **Shevery** | **ShizukuPlus** | **Estelar** |
| --- | :------------: | :--------------------: | :------------: | :--------: | :--------: | :------------: | :--------: |
| **Funcionalidad básica** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Modo ADB/TCP inalámbrico** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Soporte raíz** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Soporte de Dhizuku** | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ |
| **Inicio automático** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Sistema de módulo** | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ |
| **Herramientas avanzadas de Labs (40 herramientas)** | ✅ | ❌ | Parcial | Parcial | Parcial | ❌ | Parcial |
| **Desinflador de aplicaciones (UAD)** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Congelador de aplicaciones** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Cortafuegos (VPN cero)** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Configuración rápida Tiles Suite (18 mosaicos)** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Firewall nativo ShizuWall** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Rastreador y escáner de telemetría** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Pantalla con cafeína para permanecer despierto** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Ciclos y reversión automática de DNS privado** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **DFRoot (ayuda para explotar el kernel)** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Fondos de pantalla dinámicos de WallYou** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Imagen en imagen universal** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Ajustador de pantalla** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Grabación de llamadas** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Parcheador de temas** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Terminal incorporada** | ✅ | ❌ | ✅ | ❌ | ✅ | ❌ | ✅ |
| **Integración de IA** | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **Materia 3 Expresivo** | ✅ | ❌ | Materia 3 | Materia 3 | Materia 3 | ❌ | ❌ |
| **Compatibilidad con TV/Wear OS** | ✅ | Parcial | Parcial | Parcial | ❌ | ❌ | ❌ |
| **Flujo de incorporación** | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| **Funciones sigilosas** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **PIN automático ADB inalámbrico** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Licencia** | Apache-2.0 | Apache-2.0 | Apache-2.0 | Apache-2.0 | Apache-2.0 | Apache-2.0 | MPL-2.0 |

*La comparación se basa en funciones disponibles públicamente al momento de escribir este artículo. Las características pueden cambiar con el tiempo.*

---

## 📱 Requisitos del sistema

| Componente | Requisito |
| --- | --- |
| **Versión de Android** | Android 5.0 (Lollipop) API 21 o superior |
| **Arquitectura** | arm64-v8a, armeabi-v7a, x86, x86_64 |
| **Shizuku** | El servicio Shizuku debe estar ejecutándose (a través de depuración inalámbrica, ADB a través de USB o raíz) |
| **Permisos** | Se requiere concesión de permiso de Shizuku para la mayoría de las funciones |
| **Almacenamiento** | ~50-80 MB para instalación (varía según las características) |
| **Opcional** | Servicio de accesibilidad para la función Wireless ADB Auto-PIN |

---

## 📥 Instalación

### De las versiones de GitHub (recomendado)

1. Vaya a la [página de lanzamientos] (https://github.com/krishna3163/OmniShizuku/releases)
2. Descargue la última versión `manager-debug.apk` o `manager-release.apk`
3. Instala el APK en tu dispositivo Android
4. Sigue el proceso de configuración

### Vía BAsD

```bash
adb install -r OmniShizuku.apk
```

### Notas importantes

- Si tiene instalado el Shizuku original, generalmente puede instalar OmniShizuku junto con él o reemplazarlo. Verifique la compatibilidad para su caso de uso.
- Algunas funciones requieren versiones específicas de Android o capacidades del dispositivo.
- Para las funciones de grabación de llamadas, es posible que se requieran permisos adicionales según la versión de Android.

---

## 🚀 Empezando

1. **Instalar OmniShizuku** - Descargue e instale el APK
2. **Configurar Shizuku**: inicie el servicio Shizuku mediante depuración inalámbrica (Android 11+) o ADB/Root
3. **Conceder permiso**: abre OmniShizuku y concede permiso a Shizuku cuando se te solicite.
4. **Explorar**: navegue por las pestañas Inicio, Aplicaciones, Módulos, Laboratorios y Configuración.
5. **Personalizar**: dirígete a Essentials Hub o Labs para habilitar las funciones que necesitas
6. **Manténgase seguro**: lea las descripciones de las funciones antes de aplicar cambios a nivel del sistema

**Para depuración inalámbrica (Android 11+):**
- Vaya a `Configuración > Opciones de desarrollador > Depuración inalámbrica` y vincúlelo con OmniShizuku o Shizuku

**Para ADB (a través de PC):**
```bash
adb shell sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh
```
*(O utilice el comando de inicio apropiado para su configuración)*

---

## 🔨 Construyendo desde la fuente

### Requisitos previos

- [Android Studio](https://developer.android.com/studio) (última versión estable) o [Android SDK](https://developer.android.com/studio#command-tools)
- [JDK 17](https://adoptium.net/) o superior
- [Git](https://git-scm.com/)
- Gradle (envoltorio incluido)

### Clonar y construir

```bash
# Clone the repository
git clone https://github.com/krishna3163/OmniShizuku.git
cd OmniShizuku

# Build debug APK
./gradlew :manager:assembleDebug

# Build release APK (requires signing configuration)
./gradlew :manager:assembleRelease

# Install to connected device
./gradlew :manager:installDebug
```

El APK de depuración estará ubicado en: `manager/build/outputs/apk/debug/manager-debug.apk`

### Requisitos de construcción

- `compilarSdk`: 36
- `minSdk`: 21
- `objetivoSdk`: 36
- Kotlin con Jetpack Compose
- R8/ProGuard para versiones de lanzamiento

---

## 🔐 Arquitectura y seguridad

### Medidas de seguridad

- **Programación defensiva**: acceso a carpetas con seguridad nula, bloques try-catch alrededor de operaciones críticas y respaldos elegantes
- **Alcance de permisos**: uso mínimo de permisos con solicitudes de permisos en tiempo de ejecución cuando sea necesario
- **IPC seguro**: interfaces AIDL para comunicación entre procesos con enlace de servicio adecuado
- **Sin telemetría**: sin análisis, seguimiento ni recopilación de datos
- **Código abierto**: código base auditable y totalmente transparente
- **Cambios reversibles**: la mayoría de las modificaciones se pueden deshacer a través de la aplicación
- **Validación**: validación de módulos, verificación de paquetes y prácticas de instalación segura
- **Compatibilidad OEM** - Manejo especial para Realme, OPPO, Xiaomi, OnePlus y otros OEM con optimización agresiva de la batería

### Aspectos destacados de la arquitectura

- **Patrón MVVM**: separación clara de preocupaciones con ViewModels y el estado de la interfaz de usuario
- **Jetpack Compose**: marco de interfaz de usuario declarativo moderno
- **Kotlin Coroutines**: operaciones asincrónicas con manejo adecuado del ciclo de vida
- **Habitación/Preferencias** - Persistencia de datos locales
- **Hilt/Dagger** - Inyección de dependencia (cuando corresponda)
- **Diseño modular**: módulos de funciones para una mejor organización y mantenibilidad

---

## 🔑 Permisos explicados

| Permiso | Propósito | Requerido para |
| --- | --- | --- |
| `QUERY_ALL_PACKAGES` | Enumerar y administrar aplicaciones instaladas | App Ops, Debloater, Freezer, Módulos |
| `READ_EXTERNAL_STORAGE` / `MANAGE_EXTERNAL_STORAGE` | Acceso a archivos | Explorador de archivos, instalación de APK |
| `POST_NOTIFICACIONES` | Mostrar notificaciones | Servicios, Caffeinate, Grabación |
| `SERVICIO_ANTERIOR` | Ejecutar servicios en segundo plano | Grabación de llamadas, ShizuWall, Caffeinate |
| `READ_PHONE_STATE`, `RECORD_AUDIO` | Detección y grabación de llamadas. | ShizuCallRecorder |
| `BIND_ACCESSIBILITY_SERVICE` | PIN ADB inalámbrico de autocompletar | PIN automático ADB inalámbrico (optar) |
| `PACKAGE_USAGE_STATS` | Acceso a datos de uso | Algunas características de optimización |
| `REQUEST_INSTALL_PACKAGES` | Instalar APK | Instalador de paquetes Pro |
| `SYSTEM_ALERT_WINDOW` | Pantalla superpuesta | PiP universal, superposiciones de pantalla |

*La mayoría de los permisos solo se solicitan cuando son necesarios para funciones específicas.*

---

## 🤝 Contribuyendo

¡Las contribuciones son bienvenidas! Ya sean informes de errores, solicitudes de funciones, mejoras de documentación o contribuciones de código.

### Cómo contribuir

1. **Bifurcar** el repositorio
2. **Crear** una rama de funciones (`git checkout -b feature/AmazingCaracterística`)
3. **Confirma** tus cambios (`git commit -m 'Agrega alguna característica increíble'`)
4. **Empujar** a la rama (`git push origin feature/AmazingCaracterística`)
5. **Abrir** una solicitud de extracción

### Directrices

- Siga el estilo y las convenciones del código existente
- Escribe mensajes de confirmación claros
- Pruebe sus cambios a fondo
- Actualizar la documentación si es necesario
- Para conocer las características principales, primero abra un problema para discutirlo

See [CONTRIBUTING.md](../CONTRIBUTING.md) for more details.

---

## 🙏 Créditos y agradecimientos

### Proyectos principales

- **[RikkaApps/Shizuku](https://github.com/RikkaApps/Shizuku)** - El proyecto original que hizo todo esto posible. Muchas gracias a Rikka y a todos los contribuyentes.
- **[Dhizuku](https://github.com/iamr0s/Dhizuku)** - Para soporte de integración de API de Dhizuku.

### Comunidades e inspiraciones

- **[Universal Android Debloater (UAD)](https://github.com/0x192/universal-android-debloater)** - Clasificaciones de seguridad y metodología de eliminación
- **[LSPosed](https://github.com/LSPosed/LSPosed)** - Inspiración para conceptos de sistemas modulares
- **[Magisk](https://github.com/topjohnwu/Magisk)** - Inspiración en la arquitectura del sistema del módulo
- **[KernelSU](https://github.com/tiann/KernelSU)** - Inspiración de solución raíz moderna

### Integraciones y desarrolladores de módulos

- **[kitsumed/ShizuCallRecorder](https://github.com/kitsumed/ShizuCallRecorder)** - Integración de la funcionalidad de grabación de llamadas
- **[RBN-Apps/Quick-Tile-Settings](https://github.com/RBN-Apps/Quick-Tile-Settings)** - Integración de la aplicación complementaria
- **[AhmetCanArslan/ShizuWall](https://github.com/AhmetCanArslan/ShizuWall)** - Integración de la aplicación complementaria
- **[Comunidad de módulos Shevery](https://github.com/topics/shevery-modules)** - Ecosistema de módulos comunitarios

### agradecimiento especial

Un sincero agradecimiento a:

- **La comunidad Shizuku**: para soporte, comentarios y pruebas continuos
- **Todos los colaboradores**: todos los que informaron errores, sugirieron funciones, enviaron relaciones públicas o ayudaron a mejorar OmniShizuku.
- **Traductores** - Por hacer que OmniShizuku sea accesible en varios idiomas
- **Probadores Beta**: para ayudar a detectar errores y garantizar la estabilidad
- **Comunidad OEM**: para compartir soluciones y correcciones específicas del dispositivo

### ¿Por qué elegir OmniShizuku?

OmniShizuku se distingue por combinar el poder de Shizuku con un conjunto completo de herramientas prácticas en una aplicación bellamente diseñada. En lugar de hacer malabares con varias aplicaciones para desbloquear, ajustar, optimizar y administrar su dispositivo, OmniShizuku reúne todo en una experiencia coherente. Ya sea que sea un usuario ocasional que busca desactivar bloatware o un usuario avanzado que busca una personalización profunda del sistema, OmniShizuku proporciona las herramientas que necesita con seguridad, reversibilidad y facilidad de uso como esencia.

---

## 📄 Licencia

This project is licensed under the **Apache Licencia 2.0**. See the [LICENSE](../LICENSE) file for full details.

```
Copyright 2024-2026 krishna3163

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

## 💬 Soporte

- **Problemas de GitHub**: [Informar errores o solicitar funciones](https://github.com/krishna3163/OmniShizuku/issues)
- **Discusiones de GitHub**: [Haga preguntas y comparta ideas](https://github.com/krishna3163/OmniShizuku/discussions)
- **Documentación**: Consulta el archivo README en tu idioma preferido
- **Código fuente**: [github.com/krishna3163/OmniShizuku](https://github.com/krishna3163/OmniShizuku)

---

<div align="center">

**Hecho con ❤️ por [krishna3163](https://github.com/krishna3163) y la increíble comunidad OmniShizuku**

*Si encuentra útil OmniShizuku, ¡considere darle un ⭐ en GitHub!*

</div>
