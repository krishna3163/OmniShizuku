# OmniShizuku Project Memory

This directory is the persistent project memory for OmniShizuku UI/UX engineering work.
Any future agent must read these files before continuing work to understand project history,
decisions, and next steps.

## Directory Structure

| File | Purpose |
|------|---------|
| `README.md` | This file - overview of the memory system |
| `CURRENT_STATE.md` | Always-maintained snapshot of project identity, build status, active task |
| `MASTER_TODO.md` | Authoritative project TODO with task IDs, priorities, acceptance criteria |
| `TASK_BACKLOG.md` | Prioritized list of pending tasks |
| `COMPLETED_WORK.md` | Record of every completed task with files, decisions, results |
| `IN_PROGRESS.md` | Currently active tasks |
| `NEXT_STEPS.md` | Recommended next tasks after current ones complete |
| `BLOCKERS.md` | Active and resolved blockers |
| `DECISION_LOG.md` | Architectural and design decisions with rationale |
| `CHANGELOG.md` | Chronological record of all changes |
| `TEST_HISTORY.md` | Test results and verification records |
| `KNOWN_ISSUES.md` | Known bugs, limitations, and workarounds |
| `FILE_OWNERSHIP.md` | Which files/modules are owned by which area |
| `FEATURE_STATUS.md` | Status of every feature in the app |
| `SESSION_HANDOFF.md` | End-of-session handoff for the next agent |
| `history/README.md` | History directory overview |
| `history/YYYY-MM-DD.md` | Dated development history entries (append-only) |

## How to Use

1. **Start of session**: Read README.md, CURRENT_STATE.md, SESSION_HANDOFF.md, MASTER_TODO.md
2. **During work**: Update IN_PROGRESS.md, COMPLETED_WORK.md, DECISION_LOG.md as you go
3. **End of session**: Update all files, write SESSION_HANDOFF.md, append to history/

## Golden Rules

- Never rely on chat history or temporary agent memory
- Never delete previous history entries - append corrections instead
- Keep all files synchronized (no contradictory status information)
- Never claim success without evidence
- Preserve existing application, features, integrations, and working code
