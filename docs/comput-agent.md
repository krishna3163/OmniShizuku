# Comput AI Agent Guide

The **Comput AI Agent** (`ComputAgent` / `ComputAgentSheet`) is an interactive, step-by-step assistant integrated into the **Comput** console. Unlike one-shot command generation in **Commandium**, the Agent plans a multi-step sequence of privileged shell commands and ADB module service executions, runs them with configurable approval gates, and summarizes the results in your language.

Wiki mirror: https://github.com/HmnDev-Tech/shevery/wiki/Comput-Agent

---

## How It Works

1. **Goal Input**: Open the **Comput** tab, tap the **Agent** button (robot icon), and describe your goal in natural language (for example: *"Check which apps wake up the device the most and list disabled system packages"*).
2. **Planning Phase (`PLANNING`)**:
   - The agent queries your active AI provider (configured in **Settings → AI → AI provider**).
   - Installed ADB modules that include a `service.sh` script are passed as context (`AgentModuleInfo`), allowing the agent to trigger module services when relevant.
   - The AI returns a structured JSON plan (`{"steps": [...]}`) containing up to **10 steps** (`MAX_STEPS_HARD = 10`).
3. **Execution Phase (`EXECUTING`)**:
   - Each step has a `kind` (`SHELL` or `MODULE_SERVICE`), a `target` (the shell command or module ID), and a localized one-sentence `comment` explaining what the step does.
   - Depending on your **Safety Mode**, Shevery either executes the step immediately or pauses to show an interactive approval card.
   - Live `stdout` / `stderr` output and step status (`PENDING`, `RUNNING`, `DONE`, `FAILED`, `DENIED`, `SKIPPED`) are displayed in real time.
4. **Summary Phase (`DONE`)**:
   - Once all steps finish (or if you stop the run early), the agent sends the execution transcript back to the active model to generate a concise verdict and key findings in your current device locale.

---

## Step Types

| Step Kind | JSON `type` | Target | Execution Path |
|---|---|---|---|
| **Shell Command** | `shell` | Raw Android `toybox` shell command (`pm`, `am`, `dumpsys`, `settings`, `cmd`, `service`, `getprop`, `wm`, `soc`, etc.) | Executed via `IShizukuService.newProcess(["sh", "-c", cmd])` |
| **Module Service** | `module_service` | Installed ADB module `id` (must have `service.sh`) | Executed via `AdbModuleManager` service runner |

> **On-device constraints**: The system prompt strictly forbids desktop Linux tools (`apt`, `dpkg`, `systemctl`, `journalctl`, `ifconfig`, `iptables`) that do not exist on Android's `toybox` environment, and restricts `module_service` steps to installed modules that actually provide a `service.sh` script.

---

## Safety Modes & Approval Controls

Configure the agent's behavior under **Settings → AI → Comput agent**:

| Safety Mode | Behavior |
|---|---|
| **Secure** (`secure`, default) | Prompts for confirmation before **every** step (both shell commands and module services). |
| **Turbo** (`turbo`) | Executes the entire plan automatically without pausing for confirmation. |
| **Custom** (`custom`) | Lets you choose the maximum number of steps and toggle confirmation per step type. |

### Custom Mode Options

When **Safety mode** is set to **Custom**, three additional settings appear in **Settings → AI**:

- **Max steps**: `3`, `5` (default), or `10` steps per plan.
- **Ask before shell commands**: Require approval before running `SHELL` steps.
- **Ask before module services**: Require approval before running `MODULE_SERVICE` steps.

### Per-Step Approval Actions

When a step requires approval, the Agent sheet displays an approval card with four choices:

- **Allow**: Run this step once.
- **Edit & Allow**: Edit the shell command or module ID inline before executing the step (`overrideTarget`).
- **Deny**: Skip this step (`DENIED`) and move to the next step in the plan.
- **Allow always**: Run this step and all remaining steps in the current plan without asking again.

---

## Chat History

The Agent sheet keeps a history of its chats so a task can be continued later:

- The **history** button in the sheet header opens the list of saved chats (title, time, message count), with **New chat** to start over and a delete button per chat.
- Opening a chat restores its transcript (your goals and the agent's answers) above the input; the next goal is sent with the previous turns as context, so the agent sees what was already discussed.
- Chats are stored on device as one JSON string in the same `SharedPreferences` file as the Comput macros (`ModuleSettings.getAgentChats` / `setAgentChats`, key `comput_agent_chats`, handled by `AgentChats`). Up to 30 chats and 200 messages per chat are kept; the oldest are dropped.
- A finished turn is committed to the chat when the next run starts, when you switch chats, or when the sheet is closed.

---

## Source Files

- `manager/src/main/java/moe/shizuku/manager/comput/agent/ComputAgent.kt`
- `manager/src/main/java/moe/shizuku/manager/comput/agent/AgentChats.kt`
- `manager/src/main/java/moe/shizuku/manager/logs/ComputAgentSheet.kt`
- `manager/src/main/java/moe/shizuku/manager/logs/ComputScreen.kt`
