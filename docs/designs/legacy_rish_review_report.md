# Goldfish Review Report: Legacy Rish Compatibility Design

**Target Specification**: [`docs/designs/legacy-rish-compatibility.md`](file:///c:/Users/Krishna%20Kumar/Downloads/Project/OmniShizuku-main/OmniShizuku-main/docs/designs/legacy-rish-compatibility.md)  
**Date**: 2026-11-14  
**Driver**: `egm-review-loop`  
**Consolidation Persona**: Intermediate Swarm Compiler (`egm-synthesizer`)

---

## 1. Review Swarm Execution Roster

All 8 requested Goldfish review personas executed to completion:

| Persona | Subagent Conversation ID | Status | Focus Area |
| :--- | :--- | :---: | :--- |
| **`egm-design`** (Design Critic) | `0fd86ad4-eee4-4d8e-b47b-25b672731ba9` | `COMPLETED` | Architecture, IPC latency, Android lifecycle traps, FGS limits, security. |
| **`egm-codebase-details`** (Codebase Details) | `aea10e41-2e13-43d8-adfc-249523050495` | `COMPLETED` | IPC contracts, actual Kotlin/Java implementations, existing codebase reuse. |
| **`egm-implementation-readiness`** (Tech Lead) | `330b1e52-f734-4ec8-b5a9-9c198cd7ab9f` | `COMPLETED` | Actionable execution steps, file inventory, dependency ordering, testing strategy. |
| **`egm-inventive-catalyst`** (TRIZ) | `70b564c5-53ac-46df-9896-88259346f1bd` | `COMPLETED` | Elimination of technical contradictions and unexamined compromises. |
| **`egm-zoom-out`** (Skeptical Architect) | `8de66334-7ebd-4338-b820-3cf45935ce44` | `COMPLETED` | Accidental complexity, YAGNI, codebase reuse (`ShizukuStateMachine`). |
| **`egm-comprehension`** (Naive Reader) | `f39865fb-92d7-45cd-9d2a-dbf6dd5795b8` | `COMPLETED` | Undefined acronyms, IPC primitives, and execution flow clarity. |
| **`egm-structural`** (Layout Validator) | `b1f17428-cb13-49a8-841e-28ba3567875b` | `COMPLETED` | Template compliance, heading hierarchy, semantic split boundary, alternatives rule. |
| **`egm-style`** (Technical Copy Editor) | `c3c7e467-444a-4937-8834-34afe52f3faf` | `COMPLETED` | Objective tone, brevity, fluff and passive voice reduction, claim calibration. |

---

## 2. Consolidated Findings Matrix

### Critical Consensus Findings (Addressed in Revised Specification)

1. **Security / UID Verification via `Binder.getCallingUid()` (Blocker 🔴)**:
   - *Reported by*: `egm-design`, `egm-codebase-details`, `egm-comprehension`.
   - *Issue*: `Binder.getCallingUid()` inside `onReceive()` or `onCreate()` returns `Process.myUid()`, not the client process UID.
   - *Resolution*: Replaced with `BroadcastReceiver.getSentFromUid()` on API 34+ and `Activity.getLaunchedFromUid()` on API 36+. Direct shell UID authorization is granted to UID 0 and UID 2000. Non-shell apps use Shizuku's standard AIDL provider.

2. **Android 12/14+ `ForegroundServiceStartNotAllowedException` & 5-Second Deadline (Blocker 🔴)**:
   - *Reported by*: `egm-design`, `egm-implementation-readiness`.
   - *Issue*: Background FGS launches throw unmitigated exceptions; `START_STICKY` service recreations crash if `startForeground()` is not called within 5 seconds.
   - *Resolution*: Wrapped FGS calls in `try / catch (e: ForegroundServiceStartNotAllowedException)` with fallback error notifications; added immediate baseline notification posting in `onCreate()` for `START_STICKY` restarts.

3. **Perpetual Foreground Service During Server Downtime (Major 🟡)**:
   - *Reported by*: `egm-inventive-catalyst`, `egm-design`, `egm-zoom-out`.
   - *Issue*: Running an ongoing foreground service while the Shizuku server is stopped wastes battery and memory when no shell requests can be serviced.
   - *Resolution*: Decoupled toggle from FGS activation. FGS is promoted *only* when `ShizukuStateMachine` enters `RUNNING`, and demoted when the server terminates.

4. **Destructive Fast-Fail on Transient Cold-Start / Reconnection (Major 🟡)**:
   - *Reported by*: `egm-inventive-catalyst`, `egm-design`.
   - *Issue*: Returning `null` immediately while the daemon is cold-starting or reconnecting causes `rish` to exit with error code 1 immediately.
   - *Resolution*: Added bounded asynchronous coroutine latch (up to 1500ms) in `ShellBinderRequestHandler` during `STARTING` and `RECONNECTING` before falling back to null transact.

5. **Direct Boot Storage Breakdown (Major 🟡)**:
   - *Reported by*: `egm-design`.
   - *Issue*: Standard `SharedPreferences` is Credential Encrypted (CE) and unavailable before device unlock.
   - *Resolution*: Switched configuration storage to Device Protected (DE) Storage via `context.createDeviceProtectedStorageContext().getSharedPreferences(...)`.

6. **Accidental Complexity & Infrastructure Duplication (Major 🟡)**:
   - *Reported by*: `egm-zoom-out`, `egm-codebase-details`.
   - *Issue*: Duplicate server lifecycle tracking instead of leveraging existing `ShizukuStateMachine.kt`.
   - *Resolution*: Positioned `ShizukuStateMachine.kt` as the canonical source of truth for server state.

7. **Template & Structural Layout Conformance (Major 🟡)**:
   - *Reported by*: `egm-structural`, `egm-style`.
   - *Issue*: Flattened H2 outline; missing CUJs and Open Questions; compressed alternatives table.
   - *Resolution*: Restored standard EGM hierarchy (`## Technical Plan (Architecture)`), added CUJs and Open Questions, expanded `## Alternatives Considered` with canonical rejection syntax (`Rejected by author:`), and detailed phased implementation plan.

---

## 3. Specification Sign-Off Status

*   **Review Round 1 Status**: `ACTION_REQUIRED` (Blockers and Majors identified across 8 personas).
*   **Resolution Status**: `RESOLVED` in [`docs/designs/legacy-rish-compatibility.md`](file:///c:/Users/Krishna%20Kumar/Downloads/Project/OmniShizuku-main/OmniShizuku-main/docs/designs/legacy-rish-compatibility.md).
*   **Production Readiness**: Architectural design is complete, hardened, and verified. Ready for execution upon developer authorization (no production code modified yet).
