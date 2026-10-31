# OmniShizuku

O OmniShizuku é um utilitário de código aberto para Android focado em fluxos de trabalho com Shizuku, ADB e Dhizuku. Ele oferece acesso a APIs em nível de sistema por meio do serviço Shizuku, com módulos avançados para gerenciamento de dispositivos, automações, otimizações e muito mais.

[![GitHub](https://img.shields.io/badge/GitHub-krishna3163/OmniShizuku-blue?logo=github)](https://github.com/krishna3163/OmniShizuku)
[![License: Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-5.0%2B-green?logo=android)](https://developer.android.com)

🌐 **Documentation available in:** [English](./README.md) • [हिन्दी](./README.hi.md) • [Español](./README.es.md) • [Français](./README.fr.md) • [中文（简体）](./README.zh.md) • [中文（繁體）](./README.zh_tw.md) • [Русский](./README.ru.md) • [العربية](./README.ar.md) • [日本語](./README.ja.md) • [Português](./README.pt.md) • [Қазақша](./README.kk.md) • [Qazaqşa](./README.kk-Latn.md)

---

## Recursos

### Hub de Essenciais (Essentials Hub)
- **Dynamic Island** — Pílula flutuante no recorte da câmera para mídia ao vivo, bateria e alertas
- **Smart Pixels (Economizador AMOLED)** — Desativa de 25 a 75% da matriz de subpixels OLED para poupar 15 a 44% de energia da tela
- **Ajustador de Tela (Display Tweaker)** — Substituição forçada de resolução, densidade de tela (DPI) e taxa de atualização
- **Forçar Modo Escuro (Force Dark Mode)** — Aplica temas escuros em todos os aplicativos instalados
- **Central e Protetor de Bateria** — Saúde da bateria em tempo real, contagem de ciclos, temperatura e proteção de longevidade
- **Modo Bolso (Pocket Mode Guard)** — Proteção com sensores de proximidade e luz contra toques acidentais
- **Segurança de Tela e Desbloqueio DRM** — Ignora o FLAG_SECURE para permitir capturas de tela em aplicativos bancários e de streaming
- **Permissões e AppOps** — Controlador de permissões privilegiadas e limites de serviços em segundo plano
- **Ajustes de SystemUI** — Lista negra de ícones da barra de status, estilos de ícone de bateria e modo imersivo
- **Otimizador de Jogos e CPU Axeron** — Perfis de desempenho, latência zero de animação e limpeza de cache
- **Forçar Doze (Enforce Doze)** — Automação agressiva de repouso profundo em espera ao desligar a tela
- **Controles Avançados de Energia** — Reinicialização, recovery, bootloader, desligamento e suspensão via shell
- **Controlador de DNS Privado** — Predefinições de DNS criptografado sem necessidade de VPN (Cloudflare, AdGuard, Quad9, Google)
- **Desbloqueio de Roteamento de Dados** — Libera o ponto de acesso Wi-Fi restringido pela operadora
- **Senhas de Wi-Fi Salvas** — Lê e exibe redes Wi-Fi salvas no aparelho e suas respectivas senhas
- **Shut Up! Silenciador de Mídia** — Silenciamento de áudio multimídia por aplicativo específico
- **Mestre de Fluxos de Áudio** — Controle independente de volume para diferentes canais de som
- **Pular Faixas com Teclas de Volume** — Pula faixas de música com os botões de volume quando a tela estiver apagada
- **Iluminação de Bordas para Notificações** — Animação em brilho neon nas bordas da tela ao receber notificações
- **Não Perturbe Automático (Modo Zen)** — Agendamento automatizado do Não Perturbe
- **Caffeinate** — Mantém a tela ligada com notificação de parada e atalho nas Configurações Rápidas
- **Estúdio de Marcas d'Água e EXIF** — Moldura elegante no estilo Leica e marca Hasselblad com dados EXIF ao vivo
- **Debloater Universal para Android (UAD)** — Remoção segura de pacotes com classificação comunitária e restauração com 1 toque
- **Congelador de Aplicativos (App Freezer)** — Congela instantaneamente aplicativos em segundo plano sem desinstalar
- **Explorador de Arquivos Privilegiado** — Navegue por diretórios protegidos do sistema
- **Instalador de Pacotes Pro** — Instala pacotes divididos APKs, APKS e XAPK

### Módulos Integrados Adicionais (Additional Integrated Modules)
- **Theme Patcher** — Personalização de temas do sistema
- **Sound Master** — Controle refinado de áudio
- **Mixed Audio** — Mixagem e roteamento de fontes de áudio
- **LookBack** — Gravação e reprodução rápida de tela
- **Universal PiP** — Picture-in-picture para qualquer aplicativo
- **Intent Shell** — Execução de comandos shell através de intents
- **Local Shell** — Emulador de terminal integrado

### Auto-PIN de ADB Sem Fio (Acessibilidade)
- **Desativado** — Digite o PIN de pareamento manualmente
- **Manual** — O gerenciador avisa assim que a janela de pareamento surgir
- **Automático** — O serviço de acessibilidade insere automaticamente o PIN gravado na caixa de diálogo "Parear com dispositivo"
- Exclusivamente opt-in — nenhum serviço é executado até que você o ative nas Configurações

### Desempenho e Estabilidade
- **Desfoque de movimento econômico (Lazy motion blur)** — Os loops de amostragem só rodam ao rolar a tela, eliminando desperdício contínuo de CPU
- **Crashlytics + interceptador de exceções** — Falhas são registradas em log detalhado em vez de fechar o app silenciosamente
- **Binder Shizuku defensivo** — Chamadas null-safe evitam erros NPE em dispositivos Realme e outras interfaces OEM
- **Carregamento com limite em DeviceInfo** — Timeout de 20s em consultas dumpsys, exibindo dados parciais em vez de travar o carregamento

### Configuração no Primeiro Início
- 6 telas de avisos legais (privilégios, efeitos do proprietário do dispositivo, riscos de root, comportamento de terceiros, licença, avisos bancários)
- Contrato com temporizador de 5 segundos e caixa de seleção "Li e concordo"
- Seleção de idioma (sistema + 12 idiomas)
- Seleção de tema (acompanhar o sistema / escuro / claro / preto AMOLED / cores dinâmicas do sistema)
- Método de ativação (depuração sem fio / root / conexão via computador)

---

## Comparação com Outros Forks do Shizuku

| Projeto | Início Automático | Modo TCP | Dhizuku | Suporte a Módulos | Modo Furtivo | Terminal | Integração com IA | UI Expressiva | Onboarding | Licença |
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

## Requisitos

- Android 5.0 (API 21) ou superior
- Serviço Shizuku ativo (via depuração sem fio, root ou ADB)
- Permissão do Shizuku concedida ao aplicativo

---

## Instalação

1. Baixe e instale o APK a partir de [GitHub Releases](https://github.com/krishna3163/OmniShizuku/releases)
2. Ative o Shizuku no seu aparelho (via depuração sem fio ou ADB)
3. Conceda a permissão do Shizuku para o OmniShizuku
4. Na primeira abertura, complete as etapas do assistente inicial

---

## Compilação a partir do Código-Fonte

```bash
git clone git@github.com:krishna3163/OmniShizuku.git
cd OmniShizuku
./gradlew :manager:assembleDebug
```

O APK de depuração compilado estará em `manager/build/outputs/apk/debug/manager-debug.apk`.

---

## Colaboradores e Créditos

Agradecemos a todos que contribuíram para o OmniShizuku no GitHub:

- **krishna3163** — Mantenedor e Desenvolvedor Principal (Maintainer & Lead Developer)

Agradecemos também aos seguintes projetos de código aberto cujas soluções, ideias e APIs serviram de base ou foram integradas ao OmniShizuku:

- **Shizuku** (rikkaw) — Framework base do Shizuku e API Binder
- **Dhizuku** — Integração com serviço Device Owner
- **shevery** — Design Material 3, autostart, modo TCP, ecossistema de módulos, terminal e IA
- **Shizuku Next** — Pareamento automático, seleção de inicialização, watchdog e gerenciamento de permissões/firewall
- **Essentials (sameerasw)** — Módulos essenciais de utilidades (Dynamic Island, Smart Pixels, Caffeinate etc.)
- **App Ops** — Padrões avançados de gerenciamento de permissões
- **Active Launcher** — Atalhos de automação rápida
- **EnforceDoze** — Padrões de automação para o modo Doze
- **Xhizuku** — Interface Material 3 Expressive, assistente de configuração e monitoramento de servidor
- **Stellar** — Implementação leve de terminal
- **ShizukuPlus** — Superfície expandida de APIs para desenvolvedores
- **Porter** — Referência de fork minimalista do Shizuku
- **Shizako** — Referência para gerenciador de substituição direta
- **DFRoot (diabl0w)** — Integração do exploit de kernel DirtyFrag (CVE-2026-43284) (GPL-2.0) como recurso nativo embutido

---

## Blocos de Configurações Rápidas (Quick Settings Tiles)

O OmniShizuku inclui blocos para o painel de configurações rápidas:
- Caffeinate (manter a tela ligada)
- Alternador de ADB sem fio
- Iniciar / parar servidor Shizuku
- Alternador do modo furtivo (Stealth)

Adicione-os puxando o painel de notificações para baixo e tocando no ícone de lápis para editar.

---

## Segurança

Este aplicativo requer privilégios elevados do sistema através do Shizuku. Use com prudência:

- Não conceda acesso a aplicativos desconhecidos ou não confiáveis
- A depuração sem fio e o ADB expõem interfaces de sistema sensíveis
- O acesso Root contorna o modelo de segurança e sandbox do Android
- O modo Proprietário do Dispositivo (Device Owner) pode impor restrições (bloqueio de restauração de fábrica, impedimento de desinstalação)

Consulte o aviso legal inicial para obter todos os detalhes.

---

## Licença

Distribuído sob a licença Apache 2.0. Veja [LICENSE](LICENSE) para mais informações.

---

## Isenção de Responsabilidade

O OmniShizuku é fornecido "no estado em que se encontra" (as-is), sem garantias de qualquer natureza. O uso é por sua própria conta e risco. Não nos responsabilizamos por perdas de dados, anulação de garantias do aparelho ou comportamento de aplicativos de terceiros.
