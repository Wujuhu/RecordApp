# AGENTS.md (Repository Rules for Any AI Agent)

## Mandatory first step

- Read `C:\Users\WJH\Downloads\tp\PROJECT_PLAYBOOK.md` before making any changes.

## Mandatory after-change rule

- After every meaningful change (code, config, schema, workflow, or behavior), you MUST update:
  - `C:\Users\WJH\Downloads\tp\PROJECT_PLAYBOOK.md`
- Update at least:
  - Section `8. Change Log (append-only)` with date, summary, key files, and verification commands.
  - Any affected architecture/rule sections when behavior changes.

## Completion criteria

- A task is NOT complete unless the playbook has been updated for that task.
- In the final response, explicitly state that the playbook was updated.

## Verification requirement

- Run build verification for Android changes:
  - `./gradlew :app:compileDebugKotlin`
  - or `./gradlew :app:assembleDebug`

