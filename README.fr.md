# OmniShizuku

OmniShizuku est un utilitaire Android open-source pour les flux de travail Shizuku, ADB et Dhizuku. Il fournit un accès aux API de niveau système via le service Shizuku, avec des modules pour la gestion d'appareils, l'automatisation, les optimisations et plus encore.

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163/OmniShizuku-blue?logo=github)](https://github.com/krishna3163/OmniShizuku)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android)](https://developer.android.com)

---

## Fonctionnalités

### Hub Essentiel
- **Dynamic Island** — Overlay de pilule flottante pour les médias en direct, batterie et alertes
- **Smart Pixels (Économiseur AMOLED)** — Désactive 25–75% de la grille de sous-pixels OLED pour économiser 15–44% d'énergie d'affichage
- **Ajusteur d'Affichage** — Remplacements de résolution, densité d'écran (DPI) et taux de rafraîchissement
- **Forcer le Mode Sombre** — Force les thèmes sombres sur toutes les applications installées
- **Centre et Garde de Batterie** — Santé de batterie en temps réel, comptage de cycles, température et garde de longévité
- **Garde de Mode Poche** — Protection par capteur de proximité et de lumière contre les touches accidentelles en poche
- **Sécurité d'Écran et Déverrouillage DRM** — Contourne FLAG_SECURE pour les captures d'écran dans les applications bancaires et de streaming
- **Permissions et AppOps** — Contrôleur de privilèges et limites des services en arrière-plan
- **Ajusteur SystemUI** — Liste noire d'icônes de barre d'état, styles d'icône de batterie et mode immersif
- **Optimiseur Jeux et CPU Axeron** — Profils de performance, latence d'animation zéro et trim de cache
- **Forcer Doze** — Automatisation de sommeil profond agressif en veille avec écran éteint
- **Contrôles d'Alimentation Avancés** — Redémarrage, récupération, bootloader, arrêt et touche de veille via shell
- **Contrôleur DNS Privé** — Présets DNS chiffrés sans VPN (Cloudflare, AdGuard, Quad9, Google)
- **Contournement d'Anclage Opérateur** — Déverrouille le point d'accès restreint par l'opérateur
- **Mots de Passe Wi-Fi Enregistrés** — Lit et affiche les points d'accès Wi-Fi enregistrés et les mots de passe
- **Coupeur de Média "Shut Up"** — Coupure audio média par application
- **Maître de Flux Audio** — Contrôle indépendant du volume pour média, appel, alarme, voix
- **Saut de Piste avec Touches Volume** — Sauter les pistes avec les boutons de volume écran éteint
- **Éclairage de Bord de Notification** — Animation de lueur néon sur le bord de l'écran lors des notifications
- **Ne Pas Déranger Automatique (Mode Zen)** — Planification automatisée du Ne Pas Déranger selon le focus d'application et les horaires
- **Caffeinate** — Maintenir l'écran allumé avec notification d'arrêt et tuile de Réglages Rapides
- **Studio de Filigrane et EXIF** — Cadre Leica de luxe et marque minimaliste Hasselblad avec EXIF en direct
- **Débloateur Android Universel (UAD)** — Suppression de paquets classée par sécurité de la communauté et restauration en 1 touche
- **Gélificateur d'Applications** — Gèle instantanément les applications en arrière-plan sans désinstaller
- **Explorateur de Fichiers Privilégié** — Parcourir les chemins système protégés et les répertoires Android/data
- **Installeur de Paquets Pro** — Installe les APKs divisés, APKS, XAPK avec marques de rétrogradation et bas SDK

### Auto-PIN ADB Sans Fil (Accessibilité)
- **Désactivé** — Saisissez le PIN de couplage vous-même
- **Manuel** — Le gestionnaire vous indique quand la boîte de dialogue apparaît
- **Automatique** — Service d'accessibilité saisit le PIN enregistré dans la boîte de dialogue "Appairer avec l'appareil"
- Opt-in uniquement — rien ne s'exécute tant que vous ne l'activez pas dans les Paramètres

### Performance et Stabilité
- **Flou de mouvement paresseux** — Les boucles d'échantillonnage de trames ne s'exécutent que pendant le défilement, éliminant la consommation CPU par trame
- **Crashlytics + gestionnaire d'exceptions non attrapées** — Les plantages sont enregistrés au lieu de fermer silencieusement l'application
- **Binder Shizuku défensif** — Accès au binder sécurisé contre les nuls pour éviter les plantages NPE sur Realme/OEM
- **Chargement d'Informations Appareil borné** — Timeout de 20s sur les requêtes dumpsys, affiche des données partielles au lieu d'un spinner bloqué

### Intégration au Premier Lancement
- 6 écrans d'avertissement (privilèges, effets secondaires propriétaire d'appareil, risques root, comportement tiers, licence, avertissements applications bancaires)
- Accord avec compte à rebours de 5 secondes et case "J'ai lu et j'accepte"
- Sélection de langue (système + 12 langues)
- Sélection de thème (suivre le système / sombre / clair / nuit noire AMOLED / utiliser couleur système)
- Méthode d'activation (débogage sans fil / root / connecter à un ordinateur)

---

## Prérequis

- Android 5.0 (API 21) ou supérieur
- Service Shizuku en cours d'exécution (via débogage sans fil, root ou ADB)
- Autorisation Shizuku accordée à l'application

---

## Installation

1. Installez l'APK depuis [GitHub Releases](https://github.com/krishna3163/OmniShizuku/releases)
2. Activez Shizuku sur votre appareil (débogage sans fil ou ADB)
3. Accordez l'autorisation Shizuku à OmniShizuku
4. Au premier lancement, complétez le flux d'intégration

---

## Compilation depuis la Source

```bash
git clone git@github.com:krishna3163/OmniShizuku.git
cd OmniShizuku
./gradlew :manager:assembleDebug
```

L'APK de débogage sera à `manager/build/outputs/apk/debug/manager-debug.apk`.

---

## Sécurité

Cette application demande des privilèges système élevés via Shizuku. Utilisez-la de manière responsable :

- N'accordez pas l'accès à des applications non fiables
- Le débogage sans fil et ADB exposent des interfaces système
- L'accès root contourne le modèle de sécurité d'Android
- Le mode propriétaire d'appareil peut avoir des effets secondaires (restrictions de réinitialisation d'usine, limites de suppression d'applications)

Consultez l'avertissement du premier lancement pour plus de détails.

---

## Licence

Licence Apache 2.0. Voir [LICENSE](LICENSE) pour plus de détails.

---

## Avertissement

OmniShizuku est fourni tel quel, sans garantie d'aucune sorte. Utilisez à vos propres risques. Nous ne sommes pas responsables de la perte de données, de l'annulation de garantie ou du comportement d'applications tierces.
