<div align="center">

<img src="../manager/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="128" height="128" alt="OmniShizuku Logo">

#OmniShizuku

**Un fork de Shizuku puissant et riche en fonctionnalités avec des modules améliorés, une interface utilisateur moderne et des capacités de contrôle système étendues.**

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163%2FOmniShizuku-blue?logo=github&style=for-the-badge)](https://github.com/krishna3163/OmniShizuku)
[![Licence: Apache-2.0](https://img.shields.io/badge/Licence-Apache%202.0-blue.svg?style=for-the-badge)](../LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android&style=for-the-badge)](https://developer.android.com)
[![API](https://img.shields.io/badge/API%20Level-21%2B-brightgreen?style=for-the-badge)](https://developer.android.com/guide/topics/manifest/uses-sdk-element)
[![Shizuku](https://img.shields.io/badge/Powered%20by-Shizuku%20API-orange?style=for-the-badge)](https://github.com/RikkaApps/Shizuku)

🌐 **Documentation available in:** [English](../README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [العربية](./README.ar.md) • [日本語](./README.ja.md) • [Português](./README.pt.md) • [Қазақша](./README.kk.md) • [Qazaqşa](./README.kk-Latn.md)

</div>

---

## 📖 Table des matières

- [Aperçu](#-aperçu)
- [Captures d'écran](#-captures d'écran)
- [Pourquoi utiliser OmniShizuku ?](#-pourquoi-utiliser-omnishizuku)
- [Fonctionnalités clés](#-key-features)
  - [Labs - Outils système avancés (40 outils)](#labs---outils-système-avancés)
  - [Système de modules](#modules-system)
  - [Enregistrement d'appel (intégration ShizuCallEnregistreur)](#call-recording-shizucallrecorder-integration)
  - [Fonctionnalités IA et développeur](#ai--developer-features)
  - [Améliorations UI/UX](#uiux-enhancements)
- [Comparaison des fonctionnalités](#-comparaison-des-caractéristiques-avec-d'autres-fourches-shizuku)
- [Exigences du système](#-exigences du système)
- [Mise en place](#-installation)
- [Mise en route](#-getting-started)
- [Construire à partir de la source](#-building-from-source)
- [Architecture et sécurité](#-architecture--sécurité)
- [Autorisations expliquées](#-autorisations-expliquées)
- [Contributeur](#-contributeur)
- [Crédits et remerciements](#-crédits--remerciements)
- [Licence](#-licence)
- [Assistance](#-assistance)

---

## 🔍 Aperçu

**OmniShizuku** est un fork open source largement amélioré du projet original [Shizuku](https://github.com/RikkaApps/Shizuku). Il étend le puissant accès API au niveau du système de Shizuku en le combinant avec une vaste collection d'utilitaires pratiques, des ajustements du système, des contrôles de confidentialité et une interface moderne et intuitive.

🌐 **Documentation available in:** [English](../README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [العربية](./README.ar.md) • [日本語](./README.ja.md) • [Português](./README.pt.md) • [Қазақша](./README.kk.md) • [Qazaqşa](./README.kk-Latn.md)
Construit avec **Material Design 3 Expressive**, OmniShizuku permet aux utilisateurs de prendre le contrôle total de leur appareil Android sans nécessiter un accès root complet dans la plupart des cas - en tirant parti des capacités de pontage ADB/root de Shizuku. De la personnalisation de l'affichage et de l'optimisation de la batterie à la gestion des applications, au débloquage, à l'enregistrement des appels et à l'automatisation avancée, OmniShizuku est votre suite d'utilitaires système tout-en-un.

---

## 📸 Captures d'écran

| **Accueil et statut** | **Autorisations d'application** | **Modules BAD** | **Catalogue des laboratoires** | **Informatique (terminal et IA)** | **Paramètres** |
|:---:|:---:|:---:|:---:|:---:|:---:|
| <img src="../screenshots/home.png" width="165" alt="Home Screen" /> | <img src="../screenshots/manage_apps.png" width="165" alt="Manage Authorized Apps" /> | <img src="../screenshots/modules.png" width="165" alt="ADB Modules" /> | <img src="../screenshots/labs.png" width="165" alt="Labs Catalog" /> | <img src="../screenshots/comput.png" width="165" alt="Comput Console" /> | <img src="../screenshots/settings.png" width="165" alt="Settings Screen" /> |
| État du service en temps réel et couplage ADB sans fil | Mode liste blanche, autorisations granulaires et accès en session uniquement | Installateur et gestionnaire de modules ZIP de style Magisk/KernelSU | Plus de 39 commandes et réglages puissants du système | Terminal shell privilégié avec génération Gemini/AI | Propriétaire de l'appareil, sauvegardes, automatisation et configuration de sécurité |

---

### 🔬 Laboratoires en vedette et capacités du système

| **Commandes système et outils électriques** | **Pare-feu natif ShizuWall** |
|:---:|:---:|
| <img src="../screenshots/labs_tools.png" width="260" alt="Labs System Tools" /> | <img src="../screenshots/shizuwall.png" width="260" alt="ShizuWall Firewall" /> |
| Appliquer Doze, l'audio mixte, le DNS privé, la sécurité de l'écran et DFRoot | Pare-feu natif zéro VPN avec scanner de suivi et règles d'application |

---

## 💡 Pourquoi utiliser OmniShizuku ?

- **Solution tout-en-un** - Pas besoin de plusieurs applications root/ADB. Tout, du déballage à l'affichage des modifications en un seul endroit.
- **Aucune racine requise** - Fonctionne avec Shizuku via Wireless ADB (Android 11+) ou ADB, le rendant accessible à un public plus large.
- **Interface utilisateur moderne et intuitive** - Interface expressive de Clean Matériel 3 avec des animations fluides, des thèmes dynamiques et des mises en page optimisées pour les téléphones, tablettes, téléviseurs et Wear OS.
- **Activement amélioré** - Mises à jour régulières avec de nouvelles fonctionnalités, corrections de bugs et améliorations au-delà du Shizuku original.
- **Axé sur la confidentialité** - Contrôle granulaire sur les autorisations des applications, les applications, les fonctionnalités de masquage et les restrictions au niveau du système.
- **Sûr et réversible** - La plupart des modifications sont réversibles, avec des capacités de sauvegarde/restauration pour le débloquage et la gestion des modules.
- **Système de modules extensibles** – Modules pilotés par la communauté avec catalogue intégré, prise en charge de la découverte et de l'installation.
- **Stabilité de niveau entreprise** - Pratiques de codage défensives, gestion des pannes, protections contre les délais d'attente et correctifs de compatibilité OEM.

---

## ✨ Principales fonctionnalités

### Laboratoires – Outils système avancés

Le catalogue Labs héberge 40 outils au niveau du système classés clairement entre la gestion des applications, les contrôles système et les utilitaires :

#### 📱 Gestion des applications (8 outils)
| Caractéristique | Descriptif |
| --- | --- |
| **Opérations d'application** | Contrôle granulaire sur les autorisations des applications cachées, les portes opérationnelles d'exécution et les privilèges en arrière-plan. |
| **Pare-feu** | Pare-feu d'application au niveau du réseau bloquant le Wi-Fi et les données mobiles par application sans consommer d'emplacement VPN. |
| **Démarrage automatique** | Empêchez les applications de se réveiller ou de démarrer automatiquement les services au démarrage de l'appareil. |
| **Modes de masquage** | Masquez les options de développement, le débogage USB/sans fil, les services d'accessibilité, le DNS privé et les états VPN de la détection. |
| **Forcer le mode sombre** | Thème sombre forcé à l’échelle du système dans les applications tierces, même sans prise en charge native du thème sombre. |
| **Débloueur universel (UAD)** | Déballeur de packages classé par la communauté en matière de sécurité avec évaluations de sécurité, explications et restauration en un seul clic. |
| **Congélateur d'application** | Gèlez et suspendez instantanément les applications en arrière-plan pour récupérer la RAM et arrêter l'épuisement de la batterie. |
| ** Optimiseur de jeu Axeron ** | Optimisation des performances au niveau du système, amélioration du jeu, ajustements de l'animation et optimisations des processus. |

#### ⚙️ Contrôles du système (18 outils)
| Caractéristique | Descriptif |
| --- | --- |
| **Menu Alimentation** | Redémarrage privilégié vers le système, la récupération, le chargeur de démarrage/Fastboot, le mode sans échec ou la mise hors tension instantanée. |
| **Ajusteur d'affichage** | Remplacez la résolution d'affichage, modifiez la densité de l'écran (DPI) et forcez des taux de rafraîchissement personnalisés. |
| **Mots de passe Wi-Fi** | Affichez, copiez et exportez les informations d'identification du réseau Wi-Fi enregistrées directement via un shell privilégié. |
| **Tuner d'interface utilisateur système** | Personnalisez la visibilité des icônes de la barre d'état, les styles d'icônes de batterie, les secondes de l'horloge et la navigation immersive. |
| ** Appliquer Doze (sommeil profond) ** | Forcez le mode de veille profonde Doze agressif immédiat chaque fois que l'écran s'éteint. |
| **Contrôleur DNS privé** | Basculez entre les fournisseurs DNS cryptés (Cloudflare, AdGuard, Quad9, Google, NextDNS, Custom) avec des minuteries de retour automatique. |
| **Maître du flux audio** | Curseurs de volume indépendants à granularité fine pour les flux audio multimédia, sonnerie, alarme, appel, voix et système. |
| **Partage de connexion par l'opérateur** | Contournez les vérifications des droits de l'opérateur et les restrictions sur le point d'accès Wi-Fi et le partage de connexion USB. |
| **Audio mixte** | Contournement de la lecture audio simultanée multi-applications (empêche les applications en arrière-plan de s'arrêter lorsqu'une autre application lit de l'audio). |
| **Maître du son** | Mélangeur de focus audio et de routage de volume par application. |
| **Image dans l'image universelle** | Forcez toute application non prise en charge en mode Picture-in-Picture (PiP) à l’aide de la projection d’affichage virtuel. |
| **Sécurité de l'écran et DRM** | Contournez les restrictions « FLAG_SECURE » pour autoriser les captures d'écran et l'enregistrement d'écran dans les applications sécurisées/bancaires. |
| ** NPD automatique (mode Zen) ** | Règles automatisées Ne pas déranger basées sur le focus de l'application de premier plan, des planifications personnalisées et des déclencheurs. |
| **DFRoot (exploit DirtyFrag)** | Assistant d'élévation de privilèges intégré utilisant CVE-2026-43284 pour les versions de noyau prises en charge. |
| **Vignettes de paramètres rapides** | Gestionnaire interactif et suite de configuration pour 18 vignettes de paramètres rapides personnalisées. |
| **Pare-feu ShizuWall** | Interface de pare-feu avancée comprenant un scanner de suivi et de télémétrie intégré et un moniteur de socket. |
| **Caféine** | Gardez l'écran de l'appareil éveillé avec une vignette et une notification persistante pour la lecture ou la navigation. |
| ** Patcher de thème ** | Couleurs d’accentuation au niveau du système, style de police et modifications du thème de l’interface utilisateur. |

#### 🛠️ Utilitaires (14 outils)
| Caractéristique | Descriptif |
| --- | --- |
| **Coquille** | Terminal de ligne de commande privilégié interactif avec sortie de streaming en temps réel. |
| **Visionneuse de journaux** | Visionneuse de logcat système en temps réel avec fonctionnalités de balises, de packages, de filtrage de niveau et d'exportation. |
| **Informations sur l'appareil** | Matériel complet, noyau, état de la batterie, capteurs thermiques et tableau de bord SoC. |
| **Lanceur d'activités** | Inspectez et lancez les activités et intentions Android exportées, non exportées ou masquées. |
| **Fichiers** | Navigateur de système de fichiers privilégié avec accès en lecture/écriture aux répertoires protégés « /data » et d'applications. |
| **Installateur d'application Pro** | Programme d'installation de packages privilégiés prenant en charge les formats APK, APKS split et XAPK. |
| **Applications de terminal** | Sélecteur rapide et lanceur pour émulateurs de terminaux externes. |
| **Coque locale** | Interface shell locale autonome. |
| **Intentions** | Déclenchez des intentions système, des récepteurs de diffusion et des services personnalisés, explicites ou implicites. |
| **Mode furtif** | Mesures anti-détection pour empêcher les applications cibles de détecter l'état de Shizuku, de racine ou de débogage. |
| **Gestionnaire des autorisations** | Présentation et révocation/octroi d'autorisations élevées sur tous les packages installés. |
| **Magasin Shizuku** | Catalogue intégré pour découvrir et télécharger des applications open source conçues pour Shizuku. |
| **Enregistrement d'appels (ShizuCallEnregistreur)** | Enregistrement d'appels bidirectionnels natif alimenté par une capture audio shell privilégiée. |
| **Fonds d'écran WallYou** | Navigateur de fonds d'écran, outil de récupération et sélecteur de fonds d'écran intégrés à l'application. |

### Système de modules

OmniShizuku propose un système de modules puissant et extensible inspiré des modules Magisk/KernelSU mais conçu pour les mods basés sur Shizuku :

- **Catalogue de modules** : parcourez les modules de la communauté à partir d'un catalogue organisé basé sur GitHub avec recherche, filtrage et tri.
- **Découverte de modules** - Découverte automatique de modules à partir de référentiels GitHub étiquetés « shevery-modules » ou de sources officielles.
- **Mise en place en un clic** - Téléchargez, vérifiez et installez les modules directement depuis l'application avec suivi des progrès.
- **Module Manager** - Activez, désactivez, mettez à jour et supprimez facilement les modules installés.
- **Assistance WebUI** - De nombreux modules incluent des interfaces de configuration Web accessibles depuis l'application.
- **Scripts de module** - Exécutez des scripts d'installation, de désinstallation, de service et de démarrage avec une journalisation et une gestion des erreurs appropriées.
- **Update Checker** - Détection automatique des mises à jour pour les modules installés à partir de leurs versions GitHub.
- **Mise en place sécurisée** - Validation de la structure du module, des accessoires et des scripts avant l'installation.
- **Interface utilisateur multiplateforme** - Écrans de module optimisés pour les téléphones, tablettes, téléviseurs et Wear OS.

### Enregistrement d'appel (intégration ShizuCallEnregistreur)

Prise en charge intégrée de l'enregistrement des appels via l'accès privilégié de Shizuku :

- **Moteur d'enregistrement basé sur le shell** - Utilise des services shell privilégiés pour une capture audio fiable.
- **Service de premier plan** - Service d'enregistrement persistant avec notifications appropriées.
- **Intégration InCallService** - Détection automatique des appels et gestion des enregistrements.
- **Gestion des enregistrements** - Affichez, lisez, renommez et supprimez des enregistrements avec une interface utilisateur intuitive.
- **Options configurables** - Personnalisez le format d'enregistrement, le codec, la source et l'emplacement de stockage.
- **Interfaces AIDL** - IPC sécurisé entre les composants de l'application pour le contrôle de l'enregistrement.
- **Gestion des autorisations** - Gestion appropriée des autorisations d'exécution pour les exigences d'enregistrement des appels.
- **Opération en arrière-plan** - Enregistrement fiable même avec l'application en arrière-plan.

### Fonctionnalités d'IA et de développement

- **AI Manager** - Capacités d'IA intégrées avec prise en charge de plusieurs fournisseurs (configurables dans les paramètres).
- **Options pour les développeurs** - Outils de développement avancés, utilitaires de débogage et fonctionnalités expérimentales.
- **Système de rapport de bugs** - Rapport de bugs intégré avec journaux, informations sur l'appareil et rapports de crash.
- **Labs Reapply** : réappliquez les modifications persistantes de l'atelier après les mises à jour ou les redémarrages.
- **Tableau de bord d'informations sur l'appareil** - Informations complètes sur l'appareil avec des propriétés détaillées sur le matériel, les logiciels et le système.
- **Visionneuse de journaux** - Visionneuse Logcat en temps réel avec capacités de filtrage et de recherche.

### Améliorations de l'interface utilisateur/UX

- **Conception expressive Matériel 3** - Dernière conception matérielle avec prise en charge de la thématique de couleurs dynamique (Material You).
- **Prise en charge multi-format** - Optimisé pour les téléphones, tablettes, appareils pliables, Android TV et Wear OS.
- **Mode sombre et AMOLED noir** - Prise en charge complète du thème sombre avec option noir pur pour les écrans AMOLED.
- **Animations fluides** - Transitions soignées, optimisation du flou de mouvement paresseux et interactions fluides.
- **Mise en page adaptative** - Interface utilisateur réactive qui s'adapte aux tailles et orientations de l'écran.
- **Localisation** - Prise en charge multilingue étendue (anglais, hindi, espagnol, français, chinois, russe, japonais, arabe, kazakh, etc.).
- **Accessibilité** - PIN automatique ADB sans fil avec prise en charge du service d'accessibilité (opt-in uniquement).
- **Performances optimisées** - Chargement paresseux, récupération de données limitée, protections contre les délais d'attente et utilisation efficace de la mémoire.
- **Résilience aux crash** : accès défensif au classeur, gestion des exceptions non interceptées et dégradation progressive des problèmes spécifiques aux OEM.

---

## 📊 Comparaison des fonctionnalités avec d'autres fourches Shizuku

| Caractéristique | **OmniShizuku** | **Shizuku (Original)** | **Shizuku Suivant** | **Xhizuku** | **Shevery** | **ShizukuPlus** | **stellaire** |
| --- | :------------: | :--------------------: | :------------: | :--------: | :--------: | :------------: | :--------: |
| **Fonctionnalité de base** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Mode ADB/TCP sans fil** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Prise en charge racine** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Assistance Dhizuku** | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ |
| **Démarrage automatique** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Système de modules** | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ |
| **Outils avancés de laboratoire (40 outils)** | ✅ | ❌ | Partielle | Partielle | Partielle | ❌ | Partielle |
| ** Débloqueur d'applications (UAD) ** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Congélateur d'application** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Pare-feu (Zéro-VPN)** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Suite de vignettes de paramètres rapides (18 vignettes)** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Pare-feu natif ShizuWall** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Tracker et scanner de télémétrie** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Écran de caféine Restez éveillé** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Retour automatique et cycles DNS privés** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **DFRoot (assistant d'exploitation du noyau)** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| ** Fonds d'écran dynamiques WallYou ** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Image dans l'image universelle** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Ajusteur d'affichage** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Enregistrement d'appel** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| ** Patcher de thème ** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Terminal intégré** | ✅ | ❌ | ✅ | ❌ | ✅ | ❌ | ✅ |
| **Intégration de l'IA** | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **Matériau 3 Expressif** | ✅ | ❌ | Matériel 3 | Matériel 3 | Matériel 3 | ❌ | ❌ |
| **Prise en charge du système d'exploitation TV/Wear** | ✅ | Partielle | Partielle | Partielle | ❌ | ❌ | ❌ |
| **Flux d'intégration** | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| **Fonctionnalités furtives** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **PIN automatique ADB sans fil** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Licence** | Apache-2.0 | Apache-2.0 | Apache-2.0 | Apache-2.0 | Apache-2.0 | Apache-2.0 | MPL-2.0 |

*La comparaison est basée sur les fonctionnalités accessibles au public au moment de la rédaction. Les fonctionnalités peuvent changer avec le temps.*

---

## 📱 Configuration système requise

| Composant | Exigence |
| --- | --- |
| **Version Android** | Android 5.0 (Lollipop) API 21 ou supérieur |
| **Architecture** | arm64-v8a, armeabi-v7a, x86, x86_64 |
| **Shizuku** | Le service Shizuku doit être en cours d'exécution (via le débogage sans fil, ADB sur USB ou Root) |
| **Autorisations** | Octroi d'autorisation Shizuku requis pour la plupart des fonctionnalités |
| **Stockage** | ~ 50-80 Mo pour l'installation (varie selon les fonctionnalités) |
| **Facultatif** | Service d’accessibilité pour la fonction Wireless ADB Auto-PIN |

---

## 📥 Mise en place

### À partir des versions de GitHub (recommandé)

1. Accédez à la [page des versions](https://github.com/krishna3163/OmniShizuku/releases)
2. Téléchargez la dernière version « manager-debug.apk » ou « manager-release.apk »
3. Installez l'APK sur votre appareil Android
4. Suivez le processus de configuration

### Via la BAD

```bash
adb install -r OmniShizuku.apk
```

### Remarques importantes

- Si le Shizuku d'origine est installé, vous pouvez généralement installer OmniShizuku à côté ou le remplacer. Vérifiez la compatibilité pour votre cas d'utilisation.
- Certaines fonctionnalités nécessitent des versions Android ou des capacités de périphérique spécifiques.
- Pour les fonctionnalités d'enregistrement d'appels, des autorisations supplémentaires peuvent être requises en fonction de la version d'Android.

---

## 🚀 Commencer

1. **Installez OmniShizuku** - Téléchargez et installez l'APK
2. **Configurer Shizuku** - Démarrez le service Shizuku via le débogage sans fil (Android 11+) ou ADB/Root
3. ** Accorder l'autorisation ** - Ouvrez OmniShizuku et accordez l'autorisation à Shizuku lorsque vous y êtes invité
4. **Explorer** – Naviguez dans les onglets Accueil, Applications, Modules, Laboratoires et Paramètres.
5. **Personnaliser** – Accédez à Essentials Hub ou Labs pour activer les fonctionnalités dont vous avez besoin.
6. **Restez en sécurité** - Lisez les descriptions des fonctionnalités avant d'appliquer des modifications au niveau du système

**Pour le débogage sans fil (Android 11+) :**
- Accédez à « Paramètres > Options du développeur > Débogage sans fil » et associez-le à OmniShizuku ou Shizuku.

**Pour ADB (via PC) :**
```bash
adb shell sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh
```
*(Ou utilisez la commande de démarrage appropriée pour votre configuration)*

---

## 🔨 Construire à partir de la source

### Conditions préalables

- [Android Studio](https://developer.android.com/studio) (dernière version stable) ou [Android SDK](https://developer.android.com/studio#command-tools)
- [JDK 17](https://adoptium.net/) ou supérieur
- [Git](https://git-scm.com/)
- Gradle (emballage inclus)

### Cloner et construire

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

L'APK de débogage se trouvera à l'emplacement : `manager/build/outputs/apk/debug/manager-debug.apk`

### Exigences de construction

- `compileSdk` : 36
- `minSdk` : 21
- `targetSdk` : 36
- Kotlin avec Jetpack Compose
- R8/ProGuard pour les versions release

---

## 🔐 Architecture et sécurité

### Mesures de sécurité

- **Programmation défensive** - Accès au classeur Null-safe, blocs try-catch autour des opérations critiques et solutions de secours gracieuses
- **Étendue des autorisations** - Utilisation minimale des autorisations avec demandes d'autorisation d'exécution si nécessaire
- **IPC sécurisé** - Interfaces AIDL pour la communication inter-processus avec liaison de service appropriée
- **Pas de télémétrie** - Pas d'analyse, de suivi ou de collecte de données
- **Open Source** - Base de code entièrement transparente et vérifiable
- **Modifications réversibles** - La plupart des modifications peuvent être annulées via l'application
- **Validation** - Validation des modules, vérification des packages et pratiques d'installation sécurisées
- **Compatibilité OEM** - Gestion spéciale pour Realme, OPPO, Xiaomi, OnePlus et autres OEM avec optimisation agressive de la batterie

### Points forts de l'architecture

- **Modèle MVVM** – Séparation nette des problèmes avec les ViewModels et l'état de l'interface utilisateur
- **Jetpack Compose** – Cadre d'interface utilisateur déclaratif moderne
- **Kotlin Coroutines** - Opérations asynchrones avec gestion appropriée du cycle de vie
- **Salle/Préférences** – Persistance des données locales
- **Hilt/Dagger** - Injection de dépendances (le cas échéant)
- **Conception modulaire** - Modules de fonctionnalités pour une meilleure organisation et maintenabilité

---

## 🔑 Autorisations expliquées

| Autorisation | Objectif | Requis pour |
| --- | --- | --- |
| `QUERY_ALL_PACKAGES` | Répertorier et gérer les applications installées | App Ops, Debloater, Freezer, Modules |
| `READ_EXTERNAL_STORAGE` / `MANAGE_EXTERNAL_STORAGE` | Accès aux fichiers | Explorateur de fichiers, installation de l'APK |
| `POST_NOTIFICATIONS` | Afficher les notifications | Services, Caféine, Enregistrement |
| `FOREGROUND_SERVICE` | Exécuter les services en arrière-plan | Enregistrement d'appels, ShizuWall, Caffeinate |
| `READ_PHONE_STATE`, `RECORD_AUDIO` | Détection et enregistrement des appels | ShizuCallEnregistreur |
| `BIND_ACCESSIBILITY_SERVICE` | Remplissage automatique du code PIN ADB sans fil | PIN automatique ADB sans fil (opt-in) |
| `PACKAGE_USAGE_STATS` | Accès aux données d'utilisation | Quelques fonctionnalités d'optimisation |
| `REQUEST_INSTALL_PACKAGES` | Installer des APK | Installateur de packages Pro |
| `SYSTEM_ALERT_WINDOW` | Affichage superposé | PiP universel, superpositions d'écran |

*La plupart des autorisations ne sont demandées que lorsque cela est nécessaire pour des fonctionnalités spécifiques.*

---

## 🤝 Contribuer

Les contributions sont les bienvenues ! Qu'il s'agisse de rapports de bogues, de demandes de fonctionnalités, d'améliorations de la documentation ou de contributions au code.

### Comment contribuer

1. **Fork** le dépôt
2. **Créez** une branche de fonctionnalités (`git checkout -b feature/AmazingCaractéristique`)
3. **Validez** vos modifications (`git commit -m 'Add some AmazingCaractéristique'`)
4. **Push** vers la branche (`git push origin feature/AmazingCaractéristique`)
5. **Ouvrir** une Pull Request

### Lignes directrices

- Suivez le style et les conventions de code existants
- Écrivez des messages de validation clairs
- Testez minutieusement vos modifications
- Mettre à jour la documentation si nécessaire
- Pour les fonctionnalités majeures, ouvrez d'abord un problème pour en discuter

See [CONTRIBUTING.md](../CONTRIBUTING.md) for more details.

---

## 🙏 Crédits et remerciements

### Projets principaux

- **[RikkaApps/Shizuku](https://github.com/RikkaApps/Shizuku)** - Le projet original qui a rendu tout cela possible. Un grand merci à Rikka et à tous les contributeurs.
- **[Dhizuku](https://github.com/iamr0s/Dhizuku)** - Pour la prise en charge de l'intégration de l'API Dhizuku.

### Communautés et inspirations

- **[Universal Android Debloater (UAD)](https://github.com/0x192/universal-android-debloater)** - Classements de sécurité et méthodologie de débloquage
- **[LSPosed](https://github.com/LSPosed/LSPosed)** - Inspiration pour les concepts de système de modules
- **[Magisk](https://github.com/topjohnwu/Magisk)** - Inspiration pour l'architecture du système de modules
- **[KernelSU](https://github.com/tiann/KernelSU)** - Inspiration de solution racine moderne

### Développeurs de modules et intégrations

- **[kitsumed/ShizuCallEnregistreur](https://github.com/kitsumed/ShizuCallEnregistreur)** - Intégration de la fonctionnalité d'enregistrement des appels
- **[RBN-Apps/Quick-Tile-Settings](https://github.com/RBN-Apps/Quick-Tile-Settings)** - Intégration de l'application compagnon
- **[AhmetCanArslan/ShizuWall](https://github.com/AhmetCanArslan/ShizuWall)** - Intégration de l'application Companion
- **[Communauté des modules Shevery](https://github.com/topics/shevery-modules)** - Écosystème de modules communautaires

### Remerciements spéciaux

Un chaleureux merci à :

- **La communauté Shizuku** - Pour une assistance, des commentaires et des tests continus
- **Tous les contributeurs** - Tous ceux qui ont signalé des bugs, suggéré des fonctionnalités, soumis des relations publiques ou contribué à améliorer OmniShizuku.
- **Traducteurs** - Pour rendre OmniShizuku accessible en plusieurs langues
- **Bêta-testeurs** - Pour aider à détecter les bugs et assurer la stabilité
- **Communauté OEM** - Pour partager des solutions de contournement et des correctifs spécifiques à l'appareil

### Pourquoi choisir OmniShizuku ?

OmniShizuku se distingue en combinant la puissance de Shizuku avec une suite complète d'outils pratiques dans une application magnifiquement conçue. Au lieu de jongler avec plusieurs applications pour dégonfler, peaufiner, optimiser et gérer votre appareil, OmniShizuku rassemble le tout dans une expérience cohérente. Que vous soyez un utilisateur occasionnel cherchant à désactiver les bloatwares ou un utilisateur expérimenté recherchant une personnalisation approfondie du système, OmniShizuku fournit les outils dont vous avez besoin avec en son cœur la sécurité, la réversibilité et la facilité d'utilisation.

---

## 📄 Licence

This project is licensed under the **Apache Licence 2.0**. See the [LICENSE](../LICENSE) file for full details.

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

## 💬 Assistance

- **Problèmes GitHub** : [Signaler des bugs ou demander des fonctionnalités](https://github.com/krishna3163/OmniShizuku/issues)
- **Discussions GitHub** : [Posez des questions et partagez des idées](https://github.com/krishna3163/OmniShizuku/discussions)
- **Documentation** : consultez le README dans votre langue préférée
- **Code source** : [github.com/krishna3163/OmniShizuku](https://github.com/krishna3163/OmniShizuku)

---

<div align="center">

**Réalisé avec ❤️ par [krishna3163](https://github.com/krishna3163) et l'incroyable communauté OmniShizuku**

*Si vous trouvez OmniShizuku utile, pensez à lui donner un ⭐ sur GitHub !*

</div>
