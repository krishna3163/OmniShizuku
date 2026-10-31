# OmniShizuku

OmniShizuku est un utilitaire open-source pour Android dédié aux flux de travail Shizuku, ADB et Dhizuku. Il fournit un accès aux API de niveau système via le service Shizuku, avec des modules pour la gestion d'appareils, l'automatisation, les optimisations et bien plus encore.

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163/OmniShizuku-blue?logo=github)](https://github.com/krishna3163/OmniShizuku)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android)](https://developer.android.com)

🌐 **Documentation available in:** [English](./README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [العربية](./README.ar.md) • [日本語](./README.ja.md) • [Português](./README.pt.md) • [Қазақша](./README.kk.md) • [Qazaqşa](./README.kk-Latn.md)

---

## Fonctionnalités

### Hub Essentiel
- **Dynamic Island** — Overlay en forme de pilule flottante pour les médias en direct, la batterie et les alertes
- **Smart Pixels (Économiseur AMOLED)** — Désactive 25 à 75 % de la grille de sous-pixels OLED pour économiser 15 à 44 % d'énergie d'affichage
- **Ajusteur d'Affichage** — Remplacements de résolution, densité d'écran (DPI) et taux de rafraîchissement
- **Forcer le Mode Sombre** — Force les thèmes sombres sur toutes les applications installées
- **Centre et Protection de Batterie** — État de santé en temps réel, nombre de cycles, température et protection de longévité
- **Protection Mode Poche** — Protection par capteurs de proximité et de luminosité contre les touches involontaires
- **Sécurité d'Écran et Déverrouillage DRM** — Contourne FLAG_SECURE pour autoriser les captures d'écran dans les applications bancaires et de streaming
- **Permissions et AppOps** — Contrôleur de permissions privilégiées et limites des services en arrière-plan
- **Ajusteur SystemUI** — Liste noire des icônes de barre d'état, styles d'icône de batterie et mode immersif
- **Optimiseur Jeux et CPU Axeron** — Profils de performance, latence d'animation nulle et nettoyage du cache
- **Forcer Doze** — Automatisation agressive de mise en veille profonde lors de l'extinction de l'écran
- **Contrôles d'Alimentation Avancés** — Redémarrage, récupération (recovery), bootloader, arrêt et mise en veille via shell
- **Contrôleur DNS Privé** — Préréglages DNS chiffrés sans VPN (Cloudflare, AdGuard, Quad9, Google)
- **Contournement de Partage Opérateur** — Déverrouille le partage de connexion restreint par l'opérateur
- **Mots de Passe Wi-Fi Enregistrés** — Lit et affiche les réseaux Wi-Fi enregistrés et leurs clés d'accès
- **Shut Up! Silence Média** — Coupure audio multimédia par application
- **Gestionnaire de Flux Audio** — Contrôle indépendant du volume pour chaque flux
- **Saut de Piste avec Touches Volume** — Passer au morceau suivant à l'aide des touches de volume écran éteint
- **Éclairage des Bords de Notification** — Animation lumineuse néon sur le contour de l'écran lors des notifications
- **NPD Automatique (Mode Zen)** — Programmation automatisée du mode Ne Pas Déranger
- **Caffeinate** — Maintient l'écran allumé avec notification d'arrêt et tuile dans les Réglages Rapides
- **Studio Filigrane Photo et EXIF** — Cadre de style Leica et logo minimaliste Hasselblad avec informations EXIF en direct
- **Débloateur Android Universel (UAD)** — Désinstallation sécurisée basée sur les classements communautaires et restauration en un geste
- **Gel d'Applications** — Gèle instantanément les applications en arrière-plan
- **Explorateur de Fichiers Privilégié** — Parcourez les répertoires système protégés
- **Installateur de Paquets Pro** — Installe les fichiers fractionnés APKs, APKS et XAPK

### Modules Intégrés Supplémentaires
- **Theme Patcher** — Personnalisation du thème du système
- **Sound Master** — Contrôle audio avancé
- **Mixed Audio** — Mixage et routage audio multi-sources
- **LookBack** — Enregistrement et lecture d'écran
- **Universal PiP** — Image dans l'image (Picture-in-Picture) pour n'importe quelle application
- **Intent Shell** — Exécution de commandes shell via des intents
- **Local Shell** — Émulateur de terminal intégré

### Auto-PIN ADB Sans Fil (Accessibilité)
- **Désactivé** — Saisie manuelle du code PIN de jumelage
- **Manuel** — Le gestionnaire vous notifie dès que la boîte de dialogue s'affiche
- **Automatique** — Le service d'accessibilité saisit le code PIN enregistré dans la boîte de dialogue "Associer avec l'appareil"
- Strictement sur adhésion (opt-in) — aucun service ne s'exécute tant qu'il n'est pas activé dans les Paramètres

### Performance et Stabilité
- **Flou de mouvement paresseux (Lazy motion blur)** — Les calculs de flou ne tournent que lors du défilement, éliminant la consommation CPU continue
- **Crashlytics + gestionnaire non intercepté** — Les plantages sont enregistrés avec précision plutôt que de fermer l'application silencieusement
- **Binder Shizuku défensif** — Accès sécurisé contre les valeurs nulles évitant les crashs NPE sur Realme et autres surcouches OEM
- **Chargement délimité de DeviceInfo** — Délai d'expiration de 20s sur les requêtes dumpsys, affichant des données partielles au lieu de bloquer l'interface

### Assistant de Premier Lancement
- 6 écrans d'avertissement légal (privilèges, impacts Device Owner, risques root, tiers, licence, applications bancaires)
- Accord avec compte à rebours de 5 secondes et case à cocher "J'ai lu et j'accepte"
- Choix de la langue (système + 12 langues)
- Choix du thème (système / sombre / clair / nuit noire AMOLED / couleurs dynamiques du système)
- Méthode d'activation (débogage sans fil / root / connexion ordinateur)

---

## Comparaison avec les Autres Forks de Shizuku

| Projet | Démarrage Auto | Mode TCP | Dhizuku | Support Modules | Mode Furtif | Terminal | Intégration IA | UI Expressive | Bienvenue | Licence |
|---------|-----------|----------|---------|----------------|---------|----------|----------------|---------------|------------|---------|
| **OmniShizuku** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | Apache-2.0 |
| Porter | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| shevery | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ | Material 3 | ❌ | Apache-2.0 |
| Shizako | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| Shizuku (fork thedjchi) | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| Shizuku Next | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | Material 3 | ❌ | Apache-2.0 |
| ShizukuPlus | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | Apache-2.0 |
| Stellar | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | MPL-2.0 |
| Xhizuku | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | Material 3 Expressive | ✅ | Apache-2.0 |

---

## Prérequis

- Android 5.0 (API 21) ou version ultérieure
- Service Shizuku actif (via débogage sans fil, root ou ADB)
- Autorisation Shizuku accordée à l'application

---

## Installation

1. Téléchargez et installez l'APK depuis [GitHub Releases](https://github.com/krishna3163/OmniShizuku/releases)
2. Activez Shizuku sur votre appareil (débogage sans fil ou ADB)
3. Accordez l'autorisation Shizuku à OmniShizuku
4. Au premier démarrage, effectuez les étapes du guide de bienvenue

---

## Compilation depuis les Sources

```bash
git clone git@github.com:krishna3163/OmniShizuku.git
cd OmniShizuku
./gradlew :manager:assembleDebug
```

L'APK de débogage sera généré dans `manager/build/outputs/apk/debug/manager-debug.apk`.

---

## Contributeurs et Remerciements

Merci à toutes les personnes qui contribuent à OmniShizuku sur GitHub :

- **krishna3163** — Mainteneur et Développeur Principal

Nous remercions également les projets open-source suivants dont le code, les concepts et les API ont inspiré ou été intégrés dans OmniShizuku :

- **Shizuku** (rikkaw) — Framework Shizuku fondamental et API Binder
- **Dhizuku** — Intégration du service Dhizuku
- **shevery** — Design Material 3, autostart, mode TCP, support des modules, terminal, intégration IA
- **Shizuku Next** — Appairage automatisé, sélection de méthode de démarrage, watchdog, gestionnaire app-ops/firewall
- **Essentials (sameerasw)** — Modules de fonctionnalités clés (Dynamic Island, Smart Pixels, Caffeinate, etc.)
- **App Ops** — Modèles de gestion fine des permissions
- **Active Launcher** — Raccourcis d'automatisation
- **EnforceDoze** — Modèles d'automatisation du mode Doze
- **Xhizuku** — Interface Material 3 Expressive, assistant de démarrage, moniteur de serveur
- **Stellar** — Implémentation épurée du terminal
- **ShizukuPlus** — Couverture d'API étendue pour les développeurs
- **Porter** — Référence de fork Shizuku minimaliste
- **Shizako** — Référence de gestionnaire de substitution directe
- **DFRoot (diabl0w)** — Intégration de l'exploit de noyau DirtyFrag (CVE-2026-43284) (GPL-2.0) en tant que fonctionnalité native

---

## Tuiles de Réglages Rapides

OmniShizuku propose des tuiles de réglages rapides pour :
- Caffeinate (garder l'écran allumé)
- Commutateur ADB sans fil
- Démarrer/arrêter le serveur Shizuku
- Commutateur du mode furtif (Stealth)

Ajoutez-les en déroulant votre panneau de notifications et en appuyant sur l'icône de crayon d'édition.

---

## Sécurité

Cette application requiert des privilèges système élevés par le biais de Shizuku. À utiliser avec discernement :

- Ne donnez jamais d'accès à des applications non vérifiées
- Le débogage sans fil et ADB exposent des interfaces système sensibles
- L'accès root outrepasse le modèle de sécurité d'Android
- Le mode propriétaire d'appareil (Device Owner) peut engendrer des restrictions (verrouillage de réinitialisation d'usine, limitation de désinstallation)

Consultez les avertissements de l'assistant de premier lancement pour les détails complets.

---

## Licence

Licence Apache 2.0. Consultez [LICENSE](LICENSE) pour plus d'informations.

---

## Clause de Non-responsabilité

OmniShizuku est fourni "en l'état", sans garantie d'aucune sorte. Vous l'utilisez à vos propres risques. Nous déclinons toute responsabilité en cas de perte de données, d'annulation de garantie ou de comportements imprévus d'applications tierces.
