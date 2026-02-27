# TP Project Playbook (AI Handoff)

> Purpose: let any new AI understand architecture, workflow, and critical rules in 5-10 minutes.
> Scope: repository root `C:\Users\WJH\Downloads\tp` (main Android project is `TPAPP`).
> Last updated: 2026-02-23

---

## 1. Repository Layout

- Root: `C:\Users\WJH\Downloads\tp`
  - `README.md`: early product notes (useful but not always equal to current code).
  - `TPAPP`: Android Studio/Gradle project root.
- Main source path:
  - `C:\Users\WJH\Downloads\tp\TPAPP\app\src\main\java\com\tp\tpapp`
  - `data`: Room, DAO, repository, DataStore.
  - `ui/navigation`: app routes and graph.
  - `ui/screen`: Compose screens.
  - `ui/viewmodel`: screen view models.
  - `ui/component`: reusable UI components.

---

## 2. Stack and Build

- Language: Kotlin
- UI: Jetpack Compose + Material3
- Architecture: MVVM
- Local DB: Room (`tp_database`)
- Preferences: DataStore Preferences (`settings`)
- Import/export: Gson JSON
- Min SDK: 26

Key files:

- `C:\Users\WJH\Downloads\tp\TPAPP\app\build.gradle.kts`
- `C:\Users\WJH\Downloads\tp\TPAPP\gradle\libs.versions.toml`

Common commands (run in `C:\Users\WJH\Downloads\tp\TPAPP`):

```powershell
./gradlew :app:compileDebugKotlin
./gradlew :app:assembleDebug
```

---

## 3. Architecture Snapshot

### 3.1 Entry and navigation

- Entry: `MainActivity`
  - Reads `SettingsRepository.themeModeFlow` and `startupTabFlow`.
  - Hosts `TPNavigation()`.
- Navigation file: `ui/navigation/Navigation.kt`
  - Bottom tabs: Records, Passwords.
  - Routes include:
    - Record list/edit/recycle bin
    - Password app list/detail/account edit/password recycle bin
    - Settings

### 3.2 Data flow

- Screen -> ViewModel -> `TPRepository` -> DAO -> Room
- Settings flow: Screen -> `SettingsViewModel` -> `SettingsRepository` -> DataStore

### 3.3 Core modules

- `data/model`
  - `AppEntity` (`isPinned`, `sortOrder`, `isDeleted`)
  - `AccountEntity` (FK to app, cascade delete)
  - `RecordEntity` (`isDeleted`, `isCollapsed`)
- `data/dao`
  - `AppDao`: active/deleted apps, soft delete, restore, ordering, import helpers
  - `AccountDao`: account CRUD
  - `RecordDao`: active/deleted records, soft delete, restore, collapse state
- `data/TPRepository.kt`
  - Central business logic
  - JSON import/export and import modes
- `ui/component/PasswordGeneratorDialog.kt`
  - Random generator for password and username/account

---

## 4. Database and Migration Rules

- Database: `AppDatabase`
- Current version: `4`
- Existing migrations:
  - `2 -> 3`: add `apps.isPinned`
  - `3 -> 4`: add `apps.isDeleted`

When changing any Room entity field, always update all of:

1. `AppDatabase` version + migration SQL
2. DAO query filters/updates
3. Repository read/write logic
4. Import/export schema if affected
5. Build verification (`assembleDebug`)

---

## 5. Critical Business Rules

### 5.1 Records

- Creating a new record does not pre-insert an empty row.
- In record edit save:
  - If title and content are both blank, do not keep the record.
- Record delete is soft delete to record recycle bin.
- Record delete confirmation is configurable in settings (default ON).
- Collapse logic:
  - `isCollapsed = true`: show preview lines
  - `isCollapsed = false`: show full content

### 5.2 Password module

- App delete is soft delete to password recycle bin.
- Account delete is hard delete (no account recycle bin currently).
- App supports persistent pin and reorder operations:
  - move up, move down, move to top, move to bottom, pin to top

### 5.3 Recycle bins

- Record recycle bin:
  - restore, hard delete, empty bin
- Password recycle bin:
  - restore app, hard delete app, empty bin
- Hard delete / empty actions can require confirmation (settings controlled)

### 5.4 Settings keys (DataStore)

Defaults:

- `record_delete_confirm = true`
- `password_delete_confirm = true`
- `startup_tab = 0` (`0=Record`, `1=Password`)
- `theme_mode = -1`
- `font_size = 1`
- `max_lines = 5`
- `show_index = true`

### 5.5 Password import/export

- Export: active apps + accounts to JSON.
- Import requires mode selection:
  - `OVERWRITE`:
    - soft-delete all current active apps to recycle bin
    - then import file content
  - `MERGE`:
    - exact app name match on active apps
    - if exists: add accounts under that app
    - else: create app then add accounts

---

## 6. Suggested Development Workflow

1. Read this file first.
2. Read `AGENTS.md` and follow repository hard rules.
3. Locate impacted layers (UI/VM/Repository/DAO/DB/Settings).
4. If data rules change, update data layer first, then UI.
5. After each task, run at least:
   - `./gradlew :app:compileDebugKotlin`
6. Before handoff, run:
   - `./gradlew :app:assembleDebug`
7. Append a new entry to change log (Section 8). This is mandatory.

---

## 7. Fast Onboarding Checklist for New AI

1. Read `PROJECT_PLAYBOOK.md`.
2. Read these files next:
   - `TPAPP/app/src/main/java/com/tp/tpapp/ui/navigation/Navigation.kt`
   - `TPAPP/app/src/main/java/com/tp/tpapp/data/TPRepository.kt`
   - `TPAPP/app/src/main/java/com/tp/tpapp/data/AppDatabase.kt`
3. If changing settings, also read:
   - `SettingsRepository.kt`, `SettingsViewModel.kt`, `SettingsScreen.kt`
4. If changing delete/recycle logic, also read:
   - `RecordListScreen.kt`, `RecycleBinScreen.kt`
   - `AppListScreen.kt`, `PasswordRecycleBinScreen.kt`
5. Update Section 8 after completing changes.

---

## 8. Change Log (append-only)

Rule:

- Every user-visible or rule-changing update must add one entry.
- Use date + summary + key files + verification commands.

### 2026-02-23 (baseline snapshot)

- App renamed to `Record`; launcher icon replaced.
- Random generator supports fixed string and position (`START/END/ANYWHERE`) and can be used for username/account and password.
- Delete confirmation added for records and passwords (both configurable; default ON).
- Password recycle bin added.
- Password import now supports mode selection (`OVERWRITE`/`MERGE`).
- Key files:
  - `TPAPP/app/src/main/java/com/tp/tpapp/data/TPRepository.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/screen/AppListScreen.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/screen/PasswordRecycleBinScreen.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/screen/RecycleBinScreen.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/screen/RecordListScreen.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/screen/SettingsScreen.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/data/AppDatabase.kt`
- Verification:
  - `./gradlew :app:assembleDebug` passed

### 2026-02-23 (AI rule + startup default tab setting)

- Added repository-level hard rule file for all AI agents:
  - `AGENTS.md` now requires updating this playbook after every meaningful change.
- Added README entry link for AI rules (`AGENTS.md`) to improve discoverability.
- Added settings option: app startup default tab (Record or Password).
  - Default remains Record.
  - Main entry now reads startup tab setting and sets navigation start destination.
- Key files:
  - `AGENTS.md`
  - `README.md`
  - `TPAPP/app/src/main/java/com/tp/tpapp/data/SettingsRepository.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/viewmodel/SettingsViewModel.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/screen/SettingsScreen.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/MainActivity.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/navigation/Navigation.kt`
- Verification:
  - `./gradlew :app:assembleDebug` passed

### 2026-02-27 (Repository push bootstrap)

- Request:
  - Push current `RecordAPP` directory contents to `https://github.com/Wujuhu/RecordApp.git`.
- Implementation:
  - Configured git remote `origin` to the target GitHub repository.
  - Created initial repository commit from current project files.
  - Attempted `git push -u origin master`; push failed in this execution environment due outbound network connectivity (`github.com:443` unreachable).
- Key files:
  - `PROJECT_PLAYBOOK.md`
- Verification:
  - `git status -sb`
  - `git remote -v`
  - `git log --oneline -n 1`
  - `git push -u origin master` (failed due network)
- Notes/Risks:
  - Local branch contains committed project snapshot; remote sync still pending once network access to GitHub is available.

---

## 9. Known Risks and Notes

- Some older files contain mojibake in comments/UI text from historical encoding issues.
- Passwords are currently stored in plain text (no encryption layer yet).
- `fallbackToDestructiveMigration()` is deprecated and can be upgraded later.

---

## 10. Change Log Template

```md
### YYYY-MM-DD (short title)
- Request:
- Implementation:
- Key files:
  - path1
  - path2
- Verification:
  - ./gradlew :app:compileDebugKotlin
  - ./gradlew :app:assembleDebug
- Notes/Risks:
```
