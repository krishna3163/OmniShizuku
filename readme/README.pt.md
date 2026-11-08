<div align="center">

<img src="../manager/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="128" height="128" alt="OmniShizuku Logo">

#OmniShizuku

**Um fork poderoso e rico em recursos do Shizuku com módulos aprimorados, interface de usuário moderna e recursos estendidos de controle do sistema.**

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163%2FOmniShizuku-blue?logo=github&style=for-the-badge)](https://github.com/krishna3163/OmniShizuku)
[![Licença: Apache-2.0](https://img.shields.io/badge/Licença-Apache%202.0-blue.svg?style=for-the-badge)](../LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android&style=for-the-badge)](https://developer.android.com)
[![API](https://img.shields.io/badge/API%20Level-21%2B-brightgreen?style=for-the-badge)](https://developer.android.com/guide/topics/manifest/uses-sdk-element)
[![Shizuku](https://img.shields.io/badge/Powered%20by-Shizuku%20API-orange?style=for-the-badge)](https://github.com/RikkaApps/Shizuku)

🌐 **Documentation available in:** [English](../README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [العربية](./README.ar.md) • [日本語](./README.ja.md) • [Português](./README.pt.md) • [Қазақша](./README.kk.md) • [Qazaqşa](./README.kk-Latn.md)

</div>

---

## 📖 Índice

- [Visão geral](#-visão geral)
- [Capturas de tela](#-capturas de tela)
- [Por que usar OmniShizuku?](#-por que usar-omnishizuku)
- [Recursos principais](#-recursos-chave)
  - [Labs - Ferramentas de sistema avançadas (40 ferramentas)](#labs---ferramentas de sistema avançadas)
  - [Sistema de Módulos](#sistema de módulos)
  - [Gravação de chamadas (integração Gravador ShizuCall)](#call-recording-shizucallrecorder-integration)
  - [Recursos de IA e desenvolvedor](#ai--developer-features)
  - [Melhorias UI/UX](#uiux-melhorias)
- [Comparação de recursos](#-comparação-de-recursos-com-outros-garfos-shizuku)
- [Requisitos do sistema](#-requisitos do sistema)
- [Instalação](#-instalação)
- [Primeiros passos](#-primeiros passos)
- [Construindo a partir da fonte](#-construindo a partir da fonte)
- [Arquitetura e Segurança](#-arquitetura--segurança)
- [Permissões explicadas](#-permissões explicadas)
- [Contribuindo](#-contribuindo)
- [Créditos e Agradecimentos](#-créditos--agradecimentos)
- [Licença](#-licença)
- [Suporte](#-suporte)

---

## 🔍 Visão geral

**OmniShizuku** é um fork de código aberto amplamente aprimorado do projeto original [Shizuku](https://github.com/RikkaApps/Shizuku). Ele amplia o poderoso acesso à API em nível de sistema do Shizuku, combinando-o com uma vasta coleção de utilitários práticos, ajustes de sistema, controles de privacidade e uma interface moderna e intuitiva.

🌐 **Documentation available in:** [English](../README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [العربية](./README.ar.md) • [日本語](./README.ja.md) • [Português](./README.pt.md) • [Қазақша](./README.kk.md) • [Qazaqşa](./README.kk-Latn.md)
Construído com **Material Design 3 Expressive**, o OmniShizuku permite que os usuários assumam o controle total de seus dispositivos Android sem exigir acesso root total na maioria dos casos - aproveitando os recursos de ponte ADB/root do Shizuku. Desde personalização de exibição e otimização de bateria até gerenciamento de aplicativos, eliminação, gravação de chamadas e automação avançada, OmniShizuku é o seu conjunto completo de utilitários de sistema.

---

## 📸 Capturas de tela

| **Casa e status** | **Permissões do aplicativo** | **Módulos ADB** | **Catálogo de laboratórios** | **Computação (Terminal e IA)** | **Configurações** |
|:---:|:---:|:---:|:---:|:---:|:---:|
| <img src="../screenshots/home.png" width="165" alt="Home Screen" /> | <img src="../screenshots/manage_apps.png" width="165" alt="Manage Authorized Apps" /> | <img src="../screenshots/modules.png" width="165" alt="ADB Modules" /> | <img src="../screenshots/labs.png" width="165" alt="Labs Catalog" /> | <img src="../screenshots/comput.png" width="165" alt="Comput Console" /> | <img src="../screenshots/settings.png" width="165" alt="Settings Screen" /> |
| Status do serviço em tempo real e emparelhamento ADB sem fio | Modo lista de permissões, permissões granulares e acesso somente por sessão | Instalador e gerenciador de módulo ZIP estilo Magisk/KernelSU | Mais de 39 controles e ajustes poderosos do sistema | Terminal shell privilegiado com geração Gemini/AI | Proprietário do dispositivo, backups, automação e configuração de segurança |

---

### 🔬 Laboratórios e recursos do sistema em destaque

| **Controles do sistema e ferramentas elétricas** | **Firewall ShizuWall nativo** |
|:---:|:---:|
| <img src="../screenshots/labs_tools.png" width="260" alt="Labs System Tools" /> | <img src="../screenshots/shizuwall.png" width="260" alt="ShizuWall Firewall" /> |
| Aplicar Doze, áudio misto, DNS privado, segurança de tela e DFRoot | Firewall nativo Zero-VPN com scanner rastreador e regras de aplicativos |

---

## 💡 Por que usar OmniShizuku?

- **Solução completa** - Não há necessidade de vários aplicativos root/ADB. Tudo, desde a remoção até a exibição de ajustes em um só lugar.
- **Não é necessário root** - Funciona com Shizuku via Wireless ADB (Android 11+) ou ADB, tornando-o acessível a um público mais amplo.
- **UI moderna e intuitiva** - Clean Material 3 Interface expressiva com animações suaves, temas dinâmicos e layouts otimizados para telefones, tablets, TVs e Wear OS.
- **Ativamente aprimorado** – Atualizações regulares com novos recursos, correções de bugs e melhorias além do Shizuku original.
- **Focado na privacidade** - Controle granular sobre permissões de aplicativos, aplicativos, recursos de ocultação e restrições no nível do sistema.
- **Seguro e reversível** - A maioria das alterações é reversível, com recursos de backup/restauração para destruição e gerenciamento de módulos.
- **Sistema de módulos extensíveis** – Módulos orientados pela comunidade com catálogo integrado, descoberta e suporte de instalação.
- **Estabilidade de nível empresarial** – Práticas de codificação defensivas, tratamento de falhas, proteções de tempo limite e correções de compatibilidade de OEM.

---

## ✨ Principais recursos

### Laboratórios - Ferramentas avançadas de sistema

O catálogo do Labs abriga 40 ferramentas de nível de sistema categorizadas de forma clara em gerenciamento de aplicativos, controles de sistema e utilitários:

#### 📱 Gerenciamento de aplicativos (8 ferramentas)
| Recurso | Descrição |
| --- | --- |
| **Operações de aplicativos** | Controle granular sobre permissões ocultas de aplicativos, portas operacionais de tempo de execução e privilégios de segundo plano. |
| **Firewall** | Firewall de aplicativo em nível de rede bloqueando Wi-Fi e dados móveis por aplicativo sem consumir um slot VPN. |
| **Início automático** | Impeça que aplicativos sejam ativados ou iniciem serviços automaticamente na inicialização do dispositivo. |
| **Modos de ocultação** | Ocultar opções do desenvolvedor, depuração USB/sem fio, serviços de acessibilidade, DNS privado e estados de VPN da detecção. |
| **Forçar modo escuro** | Tema escuro forçado em todo o sistema em aplicativos de terceiros, mesmo sem suporte nativo para tema escuro. |
| **Debloater Universal (UAD)** | Debloater de pacotes com classificação de segurança da comunidade com classificações de segurança, explicações e restauração com um toque. |
| ** Congelador de aplicativos ** | Congele e suspenda instantaneamente aplicativos em segundo plano para recuperar RAM e interromper o consumo da bateria. |
| **Otimizador de jogos Axeron** | Ajuste de desempenho em nível de sistema, aumento de jogo, ajustes de animação e otimizações de processo. |

#### ⚙️ Controles do sistema (18 ferramentas)
| Recurso | Descrição |
| --- | --- |
| **Menu de energia** | Reinicialização privilegiada para Sistema, Recuperação, Bootloader/Fastboot, Modo de Segurança ou desligamento instantâneo. |
| **Ajuste de exibição** | Substitua a resolução da tela, modifique a densidade da tela (DPI) e force taxas de atualização personalizadas. |
| **Senhas de Wi-Fi** | Visualize, copie e exporte credenciais de rede Wi-Fi salvas diretamente por meio de shell privilegiado. |
| **Sintonizador SystemUI** | Personalize a visibilidade dos ícones da barra de status, os estilos dos ícones da bateria, os segundos do relógio e a navegação envolvente. |
| **Aplicar Soneca (Sono Profundo)** | Force o modo Soneca profundo imediato e agressivo sempre que a tela for desligada. |
| **Controlador DNS privado** | Alterne entre provedores de DNS criptografados (Cloudflare, AdGuard, Quad9, Google, NextDNS, Custom) com temporizadores de reversão automática. |
| **Mestre de fluxo de áudio** | Controles deslizantes de volume independentes e refinados para fluxos de mídia, toque, alarme, chamada, voz e áudio do sistema. |
| **Ancoragem da operadora** | Ignore verificações de titularidade da operadora e restrições em pontos de acesso Wi-Fi e tethering USB. |
| **Áudio misto** | Ignorar a reprodução de áudio simultânea de vários aplicativos (evita que aplicativos em segundo plano sejam pausados quando outro aplicativo reproduz áudio). |
| **SoundMaster** | Foco de áudio por aplicativo e mixer de roteamento de volume. |
| **Imagem em imagem universal** | Force qualquer aplicativo não compatível no modo Picture-in-Picture (PiP) usando projeção de exibição virtual. |
| **Segurança de tela e DRM** | Ignore as restrições `FLAG_SECURE` para permitir capturas de tela e gravação de tela em aplicativos seguros/bancários. |
| **DND automático (modo Zen)** | Regras automatizadas de Não perturbe com base no foco do aplicativo em primeiro plano, programações personalizadas e gatilhos. |
| **DFRoot (exploração DirtyFrag)** | Auxiliar integrado de escalonamento de privilégios usando CVE-2026-43284 para versões de kernel suportadas. |
| ** Blocos de configurações rápidas ** | Gerenciador interativo e conjunto de configuração para 18 blocos personalizados de configurações rápidas. |
| **Firewall ShizuWall** | Interface de firewall avançada com rastreador integrado, scanner de telemetria e monitor de soquete. |
| **Cafeína** | Mantenha a tela do dispositivo ativa com um bloco e notificação persistente para leitura ou navegação. |
| **Atualizador de tema** | Cores de destaque no nível do sistema, estilo de fonte e modificações no tema da interface do usuário. |

#### 🛠️ Utilitários (14 ferramentas)
| Recurso | Descrição |
| --- | --- |
| **Concha** | Terminal de linha de comando privilegiado interativo com saída de streaming em tempo real. |
| **Visualizador de registros** | Visualizador de logcat do sistema em tempo real com recursos de tag, pacote, filtragem de nível e exportação. |
| **Informações do dispositivo** | Hardware abrangente, kernel, integridade da bateria, sensores térmicos e painel SoC. |
| **Lançador de atividades** | Inspecione e inicie atividades e intenções do Android exportadas, não exportadas ou ocultas. |
| **Arquivos** | Navegador de sistema de arquivos privilegiado com acesso de leitura/gravação a `/data` e diretórios de aplicativos protegidos. |
| **Instalador de aplicativo Pro** | Instalador de pacote privilegiado com suporte para APK, pacotes divididos de APKS e formatos XAPK. |
| **Aplicativos de terminal** | Alternador rápido e iniciador para emuladores de terminal externo. |
| **LocalShell** | Interface de shell local independente. |
| **Intenções** | Dispare intenções de sistema, receptores de transmissão e serviços personalizados, explícitos ou implícitos. |
| **Modo Furtivo** | Medidas antidetecção para evitar que aplicativos alvo detectem Shizuku, root ou estado de depuração. |
| **Gerenciador de permissões** | Visão geral e revogação/concessão de permissões elevadas em todos os pacotes instalados. |
| **Loja Shizuku** | Catálogo integrado para descobrir e baixar aplicativos de código aberto desenvolvidos para Shizuku. |
| **Gravação de chamadas (Gravador ShizuCall)** | Gravação de chamadas bidirecionais nativas com captura de áudio de shell privilegiada. |
| ** Papéis de parede WallYou ** | Navegador de papel de parede no aplicativo, buscador e alternador de papel de parede. |

### Sistema de Módulos

OmniShizuku apresenta um sistema de módulos poderoso e extensível inspirado nos módulos Magisk/KernelSU, mas projetado para mods baseados em Shizuku:

- **Catálogo de Módulos** - Navegue pelos módulos da comunidade em um catálogo selecionado baseado no GitHub com pesquisa, filtragem e classificação.
- **Module Discovery** – Descoberta automática de módulos de repositórios GitHub marcados com `shevery-modules` ou fontes oficiais.
- **Instalação com um toque** - Baixe, verifique e instale módulos diretamente do aplicativo com acompanhamento de progresso.
- **Gerenciador de Módulos** - Habilite, desabilite, atualize e remova módulos instalados com facilidade.
- **Suporte WebUI** - Muitos módulos incluem interfaces de configuração baseadas na Web acessíveis de dentro do aplicativo.
- **Scripts de módulo** - Execute scripts de instalação, desinstalação, manutenção e inicialização com registro adequado e tratamento de erros.
- **Verificador de atualizações** – Detecção automática de atualizações para módulos instalados de suas versões do GitHub.
- **Instalação Segura** - Validação da estrutura do módulo, adereços e scripts antes da instalação.
- **IU multiplataforma** – Telas de módulos otimizadas para telefones, tablets, TVs e Wear OS.

### Gravação de chamadas (integração Gravador ShizuCall)

Suporte integrado para gravação de chamadas através do acesso privilegiado de Shizuku:

- **Mecanismo de gravação baseado em shell** – Usa serviços de shell privilegiados para captura de áudio confiável.
- **Serviço em primeiro plano** - Serviço de gravação persistente com notificações adequadas.
- **Integração InCallService** - Detecção automática de chamadas e gerenciamento de gravação.
- **Gerenciamento de gravação** - Visualize, reproduza, renomeie e exclua gravações com uma interface intuitiva.
- **Opções configuráveis** - Personalize formato de gravação, codec, fonte e local de armazenamento.
- **Interfaces AIDL** - IPC seguro entre componentes do aplicativo para controle de gravação.
- **Tratamento de permissões** - Gerenciamento adequado de permissões em tempo de execução para requisitos de gravação de chamadas.
- **Operação em segundo plano** - Gravação confiável mesmo com o aplicativo em segundo plano.

### Recursos de IA e desenvolvedor

- **AI Manager** – Recursos integrados de IA com suporte para vários provedores (configuráveis nas configurações).
- **Opções do desenvolvedor** – Ferramentas avançadas para desenvolvedores, utilitários de depuração e recursos experimentais.
- **Sistema de relatório de bugs** - Relatório de bugs integrado com registros, informações do dispositivo e relatórios de falhas.
- **Reaplicação de laboratório**: reaplique modificações de laboratório persistentes após atualizações ou reinicializações.
- **Painel de informações do dispositivo** – Informações abrangentes do dispositivo com propriedades detalhadas de hardware, software e sistema.
- **Visualizador de registros** - Visualizador de logcat em tempo real com recursos de filtragem e pesquisa.

### Melhorias de UI/UX

- **Material 3 Expressive Design** - Material Design mais recente com suporte para temas de cores dinâmicas (Material You).
- **Suporte a vários formatos** - Otimizado para telefones, tablets, dobráveis, Android TV e Wear OS.
- **Modo Escuro e Preto AMOLED** - Suporte total ao tema escuro com opção de preto puro para monitores AMOLED.
- **Animações suaves** – Transições refinadas, otimização de desfoque de movimento lento e interações fluidas.
- **Layouts adaptáveis** – UI responsiva que se adapta aos tamanhos e orientações da tela.
- **Localização** - Amplo suporte multilíngue (inglês, hindi, espanhol, francês, chinês, russo, japonês, árabe, cazaque e muito mais).
- **Acessibilidade** - PIN automático ADB sem fio com suporte de serviço de acessibilidade (somente opcional).
- **Desempenho otimizado** – Carregamento lento, busca de dados limitada, proteção de tempo limite e uso eficiente de memória.
- **Resiliência contra falhas** – Acesso defensivo ao fichário, tratamento de exceções não detectadas e degradação suave em problemas específicos de OEM.

---

## 📊 Comparação de recursos com outros garfos Shizuku

| Recurso | **OmniShizuku** | **Shizuku (Original)** | **Shizuku Próxima** | **Xhizuku** | **Muito** | **ShizukuPlus** | **Estelar** |
| --- | :------------: | :--------------------: | :------------: | :--------: | :--------: | :------------: | :--------: |
| **Funcionalidade Básica** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Modo ADB/TCP sem fio** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Suporte raiz** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Suporte Dhizuku** | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ |
| **Início automático** | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Sistema de Módulos** | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ |
| **Ferramentas avançadas do Labs (40 ferramentas)** | ✅ | ❌ | Parcial | Parcial | Parcial | ❌ | Parcial |
| **Debloater de aplicativos (UAD)** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| ** Congelador de aplicativos ** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Firewall (VPN zero)** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| ** Conjunto de blocos de configurações rápidas (18 blocos) ** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Firewall nativo ShizuWall** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Rastreador e scanner de telemetria** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Tela de cafeína para ficar acordado** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Reversão automática e ciclos de DNS privado** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **DFRoot (Ajudante de Exploração do Kernel)** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| ** Papéis de parede dinâmicos WallYou ** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Imagem em imagem universal** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Ajuste de exibição** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Gravação de chamadas** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Atualizador de tema** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Terminal integrado** | ✅ | ❌ | ✅ | ❌ | ✅ | ❌ | ✅ |
| **Integração de IA** | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **Material 3 Expressivo** | ✅ | ❌ | Material 3 | Material 3 | Material 3 | ❌ | ❌ |
| **Suporte para TV/Wear OS** | ✅ | Parcial | Parcial | Parcial | ❌ | ❌ | ❌ |
| **Fluxo de integração** | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| **Recursos furtivos** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| ** PIN automático ADB sem fio ** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| **Licença** | Apache-2.0 | Apache-2.0 | Apache-2.0 | Apache-2.0 | Apache-2.0 | Apache-2.0 | MPL-2.0 |

*A comparação é baseada em recursos disponíveis publicamente no momento da redação deste artigo. Os recursos podem mudar com o tempo.*

---

## 📱 Requisitos do sistema

| Componente | Requisito |
| --- | --- |
| **Versão Android** | Android 5.0 (Lollipop) API 21 ou superior |
| **Arquitetura** | arm64-v8a, armeabi-v7a, x86, x86_64 |
| **Shizuku** | O serviço Shizuku deve estar em execução (via depuração sem fio, ADB sobre USB ou Root) |
| **Permissões** | Concessão de permissão Shizuku necessária para a maioria dos recursos |
| **Armazenamento** | ~50-80 MB para instalação (varia de acordo com os recursos) |
| **Opcional** | Serviço de acessibilidade para recurso Wireless ADB Auto-PIN |

---

## 📥 Instalação

### Das versões do GitHub (recomendado)

1. Vá para a [página de lançamentos](https://github.com/krishna3163/OmniShizuku/releases)
2. Baixe o `manager-debug.apk` ou `manager-release.apk` mais recente
3. Instale o APK no seu dispositivo Android
4. Siga o processo de configuração

### Através do ADB

```bash
adb install -r OmniShizuku.apk
```

### Notas importantes

- Se você tiver o Shizuku original instalado, geralmente poderá instalar o OmniShizuku junto com ele ou substituí-lo. Verifique a compatibilidade para seu caso de uso.
- Alguns recursos exigem versões específicas do Android ou recursos do dispositivo.
- Para recursos de gravação de chamadas, podem ser necessárias permissões adicionais dependendo da versão do Android.

---

## 🚀 Primeiros passos

1. **Instalar OmniShizuku** - Baixe e instale o APK
2. **Configuração do Shizuku** - Inicie o serviço Shizuku via depuração sem fio (Android 11+) ou ADB/Root
3. **Conceder permissão** - Abra OmniShizuku e conceda permissão a Shizuku quando solicitado
4. **Explorar** - Navegue pelas guias Página inicial, Aplicativos, Módulos, Laboratórios e Configurações
5. **Personalizar** - Acesse Essentials Hub ou Labs para ativar os recursos necessários
6. **Fique seguro** - Leia as descrições dos recursos antes de aplicar alterações no nível do sistema

**Para depuração sem fio (Android 11+):**
- Vá para `Configurações> Opções do desenvolvedor> Depuração sem fio` e emparelhe com OmniShizuku ou Shizuku

**Para ADB (via PC):**
```bash
adb shell sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh
```
*(Ou use o comando de início apropriado para sua configuração)*

---

## 🔨 Construindo a partir da fonte

### Pré-requisitos

- [Android Studio](https://developer.android.com/studio) (última versão estável) ou [Android SDK](https://developer.android.com/studio#command-tools)
- [JDK 17](https://adoptium.net/) ou superior
- [Git](https://git-scm.com/)
- Gradle (invólucro incluído)

### Clonar e construir

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

O APK de depuração estará localizado em: `manager/build/outputs/apk/debug/manager-debug.apk`

### Requisitos de construção

- `compileSdk`: 36
- `minSdk`: 21
- `targetSdk`: 36
- Kotlin com Jetpack Compose
- R8/ProGuard para compilações de lançamento

---

## 🔐 Arquitetura e Segurança

### Medidas de segurança

- **Programação Defensiva** - Acesso a binders com segurança nula, blocos try-catch em torno de operações críticas e fallbacks elegantes
- **Escopo de permissão** – Uso mínimo de permissão com solicitações de permissão de tempo de execução quando necessário
- **Secure IPC** – Interfaces AIDL para comunicação entre processos com ligação de serviço adequada
- **Sem telemetria** - Sem análise, rastreamento ou coleta de dados
- **Código aberto** – Base de código totalmente transparente e auditável
- **Alterações reversíveis** - A maioria das modificações pode ser desfeita através do aplicativo
- **Validação** – Validação de módulo, verificação de pacote e práticas de instalação segura
- **Compatibilidade OEM** - Manuseio especial para Realme, OPPO, Xiaomi, OnePlus e outros OEMs com otimização agressiva de bateria

### Destaques da arquitetura

- **Padrão MVVM** – Separação limpa de preocupações com ViewModels e estado da UI
- **Jetpack Compose** – Estrutura de UI declarativa moderna
- **Corrotinas Kotlin**: operações assíncronas com tratamento adequado do ciclo de vida
- **Sala/Preferências** - Persistência de dados locais
- **Hilt/Dagger** - Injeção de dependência (quando aplicável)
- **Design Modular** – Módulos de recursos para melhor organização e facilidade de manutenção

---

## 🔑 Permissões explicadas

| Permissão | Objetivo | Obrigatório para |
| --- | --- | --- |
| `QUERY_ALL_PACKAGES` | Listar e gerenciar aplicativos instalados | Operações de aplicativos, Debloater, Freezer, Módulos |
| `READ_EXTERNAL_STORAGE` / `MANAGE_EXTERNAL_STORAGE` | Acesso a arquivos | Explorador de arquivos, instalação de APK |
| `POST_NOTIFICAÇÕES` | Mostrar notificações | Serviços, Cafeína, Gravação |
| `FOREGROUND_SERVICE` | Execute serviços em segundo plano | Gravação de chamadas, ShizuWall, Cafeinato |
| `READ_PHONE_STATE`, `RECORD_AUDIO` | Detecção e gravação de chamadas | Gravador ShizuCall |
| `BIND_ACCESSIBILITY_SERVICE` | Preenchimento automático do PIN ADB sem fio | PIN automático ADB sem fio (opcional) |
| `PACKAGE_USAGE_STATS` | Acesso aos dados de uso | Alguns recursos de otimização |
| `REQUEST_INSTALL_PACKAGES` | Instalar APKs | Instalador de pacotes Pro |
| `SYSTEM_ALERT_WINDOW` | Exibição de sobreposição | PiP universal, sobreposições de tela |

*A maioria das permissões só é solicitada quando necessária para recursos específicos.*

---

## 🤝 Contribuindo

Contribuições são bem-vindas! Sejam relatórios de bugs, solicitações de recursos, melhorias na documentação ou contribuições de código.

### Como contribuir

1. **Bifurque** o repositório
2. **Crie** um branch de recurso (`git checkout -b feature/AmazingRecurso`)
3. **Comprometa** suas alterações (`git commit -m 'Add some AmazingRecurso'`)
4. **Push** para o branch (`git push origin feature/AmazingRecurso`)
5. **Abra** uma solicitação pull

### Diretrizes

- Siga o estilo e as convenções de código existentes
- Escreva mensagens de commit claras
- Teste suas alterações completamente
- Atualize a documentação se necessário
- Para recursos principais, abra um problema primeiro para discutir

See [CONTRIBUTING.md](../CONTRIBUTING.md) for more details.

---

## 🙏 Créditos e Agradecimentos

### Projetos principais

- **[RikkaApps/Shizuku](https://github.com/RikkaApps/Shizuku)** - O projeto original que tornou tudo isso possível. Muito obrigado a Rikka e a todos os colaboradores.
- **[Dhizuku](https://github.com/iamr0s/Dhizuku)** - Para suporte à integração da API Dhizuku.

### Comunidades e inspirações

- **[Universal Android Debloater (UAD)](https://github.com/0x192/universal-android-debloater)** - Classificações de segurança e metodologia de depuração
- **[LSPosed](https://github.com/LSPosed/LSPosed)** - Inspiração para conceitos de sistema de módulos
- **[Magisk](https://github.com/topjohnwu/Magisk)** - Inspiração da arquitetura do sistema de módulo
- **[KernelSU](https://github.com/tiann/KernelSU)** – Inspiração de solução raiz moderna

### Desenvolvedores e integrações de módulos

- **[kitsumed/Gravador ShizuCall](https://github.com/kitsumed/Gravador ShizuCall)** - Integração da funcionalidade de gravação de chamadas
- **[RBN-Apps/Quick-Tile-Settings](https://github.com/RBN-Apps/Quick-Tile-Settings)** - Integração de aplicativo complementar
- **[AhmetCanArslan/ShizuWall](https://github.com/AhmetCanArslan/ShizuWall)** - Integração de aplicativo complementar
- **[Comunidade Shevery Modules](https://github.com/topics/shevery-modules)** - Ecossistema de módulo comunitário

### Agradecimentos especiais

Um sincero obrigado a:

- **Comunidade Shizuku** – Para suporte, feedback e testes contínuos
- **Todos os colaboradores** - Todos que relataram bugs, sugeriram recursos, enviaram PRs ou ajudaram a melhorar o OmniShizuku
- **Tradutores** - Para tornar OmniShizuku acessível em vários idiomas
- **Beta Testers** – Para ajudar a detectar bugs e garantir estabilidade
- **Comunidade OEM** – Para compartilhar soluções alternativas e correções específicas de dispositivos

### Por que escolher OmniShizuku?

OmniShizuku se destaca por combinar o poder do Shizuku com um conjunto abrangente de ferramentas práticas em um aplicativo lindamente projetado. Em vez de fazer malabarismos com vários aplicativos para desinchar, ajustar, otimizar e gerenciar seu dispositivo, OmniShizuku reúne tudo em uma experiência coesa. Quer você seja um usuário casual que deseja desabilitar bloatware ou um usuário avançado que busca uma personalização profunda do sistema, o OmniShizuku fornece as ferramentas que você precisa com segurança, reversibilidade e facilidade de uso em sua essência.

---

## 📄 Licença

This project is licensed under the **Apache Licença 2.0**. See the [LICENSE](../LICENSE) file for full details.

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

## 💬 Suporte

- **Problemas do GitHub**: [Relatar bugs ou solicitar recursos](https://github.com/krishna3163/OmniShizuku/issues)
- **Discussões no GitHub**: [Faça perguntas e compartilhe ideias](https://github.com/krishna3163/OmniShizuku/discussions)
- **Documentação**: Verifique o README no seu idioma preferido
- **Código Fonte**: [github.com/krishna3163/OmniShizuku](https://github.com/krishna3163/OmniShizuku)

---

<div align="center">

**Feito com ❤️ por [krishna3163](https://github.com/krishna3163) e pela incrível comunidade OmniShizuku**

*Se você achar OmniShizuku útil, considere dar um ⭐ no GitHub!*

</div>
