# TP Project Playbook (AI Handoff)

> Purpose: let any new AI understand architecture, workflow, and critical rules in 5-10 minutes.
> Scope: repository root `C:\Users\WJH\Downloads\tp` (main Android project is `TPAPP`).
> Last updated: 2026-03-02

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
  - `AccountEntity` (FK to app, cascade delete, optional `imageUri`)
  - `RecordEntity` (`isDeleted`, `isCollapsed`)
- `data/dao`
  - `AppDao`: active/deleted apps, soft delete, restore, ordering, import helpers
  - `AccountDao`: account CRUD
  - `RecordDao`: active/deleted records, soft delete, restore, collapse state
- `data/TPRepository.kt`
  - Central business logic
  - JSON import/export and import modes (single file contains records + password apps/accounts)
  - Encrypts/decrypts `AccountEntity.password` at repository boundary (UI keeps plaintext).
  - Handles plaintext-to-ciphertext migration during first read/write of legacy account rows.
- `data/security`
  - Android Keystore backed AES-GCM password encryption component.
- `ui/component/PasswordGeneratorDialog.kt`
  - Random generator for password and username/account

---

## 4. Database and Migration Rules

- Database: `AppDatabase`
- Current version: `5`
- Existing migrations:
  - `2 -> 3`: add `apps.isPinned`
  - `3 -> 4`: add `apps.isDeleted`
  - `4 -> 5`: add nullable `accounts.imageUri`

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
- After creating a new app and then adding its first account, save should land on that app's account list page (app detail), not back on app list.
- App supports persistent pin and reorder operations:
  - move up, move down, move to top, move to bottom, pin to top
- Account password is encrypted at rest (Room `accounts.password` is no longer stored as plaintext).
- AES-GCM with random IV is used.
- Secret key is managed by Android Keystore.
- Repository decrypts before returning to UI and encrypts before persistence.
- Legacy plaintext rows are migrated on first read/write.
- Account image is optional, persisted as URI/path string (`accounts.imageUri`), displayed below account info when present, and supports full-screen preview with pinch zoom, drag/pan, and double-tap zoom toggle.

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
- `password_default_visible = true`
- `startup_tab = 0` (`0=Record`, `1=Password`)
- `theme_mode = -1`
- `font_size = 1`
- `max_lines = 5`
- `show_index = true`

### 5.5 Unified import/export (records + password)

- Export: active records + active password apps/accounts are exported to one JSON file.
- Import requires mode selection and applies to both modules:
  - `OVERWRITE`:
    - soft-delete all current active apps to password recycle bin
    - if import file contains `records` field: soft-delete all current active records to record recycle bin
    - if import file has no `records` field (legacy password-only JSON): keep existing records unchanged
    - then import file content
  - `MERGE`:
    - if import file contains `records` field: records are appended as new records
    - if import file has no `records` field: records are not changed
    - password apps use exact app-name match:
      - if exists: add accounts under that app
      - else: create app then add accounts
- Entry points:
  - Password page can import/export unified file.
  - Record page can import/export unified file.

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

### 2026-02-27 (Fix AccountEditScreen weight compile error)

- Summary:
  - Fixed Kotlin compile error in `AccountEditScreen.kt`:
    - `Cannot access val RowColumnParentData?.weight: Float, it is internal in file`
  - Applied minimal change by removing an unnecessary explicit import of `androidx.compose.foundation.layout.weight`.
  - Kept account image upload/display behavior unchanged (`OpenDocument`, persisted URI permission, `AsyncImage` preview, replace/clear actions).
- Key files:
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/screen/AccountEditScreen.kt`
  - `PROJECT_PLAYBOOK.md`
- Verification:
  - `cd TPAPP && bash ./gradlew :app:compileDebugKotlin --no-daemon` (blocked in sandbox: Gradle wrapper download requires network)
  - `cd TPAPP && bash ./gradlew :app:assembleDebug --no-daemon` (not executed successfully for same reason)
  - Note: initial run also hit `~/.gradle` permission; rerun with `GRADLE_USER_HOME=/tmp/gradle-home` bypassed that but remained blocked by network restriction.


### 2026-02-27 (A plan: password-at-rest encryption via Android Keystore)

- Request:
  - Implement A plan: only encrypt persisted `AccountEntity.password`, keep UI plaintext contract, and support legacy plaintext migration.
- Implementation:
  - Added independent encryption module under `data/security` using Android Keystore AES key + AES-GCM (`random IV + Base64 payload`).
  - Repository now enforces:
    - encrypt on account insert/update/reorder persistence path;
    - decrypt on account read path for UI (`getAccountsByAppId`, `getAccountById`, `getAppWithAccounts`);
    - lazy legacy migration: when plaintext password is read, it is transparently re-saved as ciphertext.
  - Export now always outputs plaintext password by decrypting DB values on export path, preserving import/export usability.
- Key files:
  - `TPAPP/app/src/main/java/com/tp/tpapp/data/security/PasswordCipher.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/data/security/KeystoreAesGcmPasswordCipher.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/data/TPRepository.kt`
- Verification:
  - `./gradlew :app:compileDebugKotlin` (blocked locally: Android SDK location not configured in this environment)
  - `./gradlew :app:assembleDebug` (not run for same reason)
- Notes/Risks:
  - If Keystore key is invalidated/removed, previously encrypted passwords may become undecryptable.
  - Current decrypt failure fallback returns original stored value to avoid crash.

### 2026-02-27 (Password accounts: image upload + display)

- Request:
  - Add image upload for each account under password apps and display images in account UI.
- Implementation:
  - Data layer:
    - Added nullable `imageUri` field to `AccountEntity`.
    - Upgraded Room DB to version `5` and added migration `4 -> 5` (`ALTER TABLE accounts ADD COLUMN imageUri TEXT`).
  - Add/Edit account page:
    - Added image picker using `OpenDocument` with persisted read permission.
    - Added image preview and clear-image actions.
    - Save path now persists selected `imageUri`.
  - Account display:
    - Account card now renders image when `imageUri` exists; no-image cards keep original content flow.
  - Import/Export JSON:
    - Added optional `imageUri` in `ExportAccount`.
    - Export writes this field; import reads it when present; old JSON without this field remains compatible.
  - Dependency:
    - Added `coil-compose` for Compose image loading.
- Key files:
  - `TPAPP/app/src/main/java/com/tp/tpapp/data/model/AccountEntity.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/data/AppDatabase.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/data/TPRepository.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/viewmodel/AccountEditViewModel.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/screen/AccountEditScreen.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/screen/AppDetailScreen.kt`
  - `TPAPP/gradle/libs.versions.toml`
  - `TPAPP/app/build.gradle.kts`
- Verification:
  - `cd TPAPP && bash ./gradlew :app:compileDebugKotlin --no-daemon` (attempted; failed in this environment: Gradle wrapper distribution download blocked by network sandbox).
  - `cd TPAPP && bash ./gradlew :app:assembleDebug --no-daemon` (attempted; failed in this environment: Gradle wrapper distribution download blocked by network sandbox).
- Notes/Risks:
  - Persisted URI depends on provider availability; if source file is removed or permission revoked, image may fail to load.

### 2026-02-27 (Account image moved below info + zoomable preview)

- Request:
  - Move account image in account card from top to below account info.
  - Add image preview interaction with pinch zoom and drag.
  - Keep no-image account UI and existing copy/show-hide password interactions unchanged.
- Implementation:
  - Updated `AccountItemCard` layout order so image section renders after username/password/note/tags/update time block.
  - Added image tap preview dialog (`ImagePreviewDialog`) with gesture transforms:
    - pinch zoom (`1x` to `5x`)
    - drag/pan while viewing
  - Added close button in preview overlay; did not alter account card copy username/password and show/hide password logic.
- Key files:
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/screen/AppDetailScreen.kt`
  - `PROJECT_PLAYBOOK.md`
- Verification:
  - `cd TPAPP && bash ./gradlew :app:compileDebugKotlin --no-daemon` (attempted; failed in this environment: network sandbox blocks Gradle distribution download)
  - `cd TPAPP && bash ./gradlew :app:assembleDebug --no-daemon` (attempted; failed in this environment: network sandbox blocks Gradle distribution download)
- Notes/Risks:
  - Preview currently supports gesture zoom/pan and close button; no additional rotation/double-tap behavior is implemented.

### 2026-02-27 (Password account image preview: disable pan, keep pinch zoom)

- Request:
  - In password module account image preview dialog, remove drag/pan gesture and keep pinch zoom only.
- Implementation:
  - Updated `ImagePreviewDialog` transform state to consume only `zoomChange`.
  - Removed preview translation state (`offsetX`, `offsetY`) and corresponding `graphicsLayer` translation assignments.
  - Kept existing preview open/close interactions and image display behavior unchanged.
- Key files:
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/screen/AppDetailScreen.kt`
  - `PROJECT_PLAYBOOK.md`
- Verification:
  - `cd TPAPP && GRADLE_USER_HOME=/tmp/gradle-home bash ./gradlew :app:compileDebugKotlin --no-daemon` (failed in this environment: sandbox blocks network; Gradle wrapper cannot download `gradle-9.2.1-bin.zip`)
- Notes/Risks:
  - Build was not fully verifiable in this sandbox due outbound network restriction; runtime gesture logic change is localized to preview transform handling only.

### 2026-02-28 (Password page: default password visibility setting)

- Request:
  - Add a settings option to control whether account passwords are shown by default on the password app detail page.
- Implementation:
  - Added DataStore setting key `password_default_visible` with default `false`.
  - Added corresponding flow/state and setter in settings repository/view model.
  - Added settings switch item: `密码默认展示` in settings screen.
  - Connected password app detail account cards to this setting so initial password visibility follows the switch:
    - ON: passwords are visible by default; tap eye icon to hide.
    - OFF: passwords are hidden by default; tap eye icon to show.
- Key files:
  - `TPAPP/app/src/main/java/com/tp/tpapp/data/SettingsRepository.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/viewmodel/SettingsViewModel.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/screen/SettingsScreen.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/screen/AppDetailScreen.kt`
  - `PROJECT_PLAYBOOK.md`
- Verification:
  - `cd TPAPP && ./gradlew :app:compileDebugKotlin`
- Notes/Risks:
  - The default visibility setting affects account cards on app detail page; account edit page input behavior remains unchanged.

### 2026-03-02 (Password input always visible + full-screen image preview UX)

- Request:
  - Remove show/hide toggle from account password input so password is always visible while editing/creating an account.
  - Fix account image preview to use full-screen behavior with normal zoom interaction.
- Implementation:
  - `AccountEditScreen`:
    - Removed password visibility toggle state and eye icon.
    - Password field now always shows plain text input; random-password generator action is kept.
  - `AppDetailScreen` image preview dialog:
    - Switched to full-screen dialog (`DialogProperties(usePlatformDefaultWidth = false)`).
    - Preview image now fills viewport (`ContentScale.Fit`) and supports:
      - pinch zoom (`1x..5x`)
      - drag/pan while zoomed (with boundary clamp)
      - double-tap toggle (`1x <-> 2x`)
    - Kept top-right close action.
  - Also fixed several malformed legacy display strings in `AppDetailScreen` that caused Kotlin parse errors, to keep compile status green.
- Key files:
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/screen/AccountEditScreen.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/screen/AppDetailScreen.kt`
  - `PROJECT_PLAYBOOK.md`
- Verification:
  - `cd TPAPP && ./gradlew :app:compileDebugKotlin` passed
- Notes/Risks:
  - Password default visibility setting in Settings still applies to account cards on app detail page; this change only forces plain-text behavior in account edit/add input.

### 2026-03-02 (Unified import/export file for records + passwords)

- Request:
  - Adapt import/export for the Record page and make backup/restore include both records and password data in one file.
- Implementation:
  - Data layer:
    - `TPRepository.exportToJson` now exports:
      - active apps + accounts
      - active records
    - Export schema upgraded to `version = 2` and adds `records` collection.
    - `TPRepository.importFromJson` now imports records and password data together.
    - `OVERWRITE` mode soft-deletes active apps before import; for records it applies overwrite only when file includes `records` field.
    - `MERGE` mode keeps previous app/account merge logic and appends imported records when file includes `records` field.
  - DAO:
    - Added `RecordDao.softDeleteAllActive()` for overwrite import flow.
  - UI:
    - Added import/export entry to Record page top bar (with mode selection dialog and snackbar feedback).
    - Password page import/export messages and wording updated to reflect unified file behavior.
- Key files:
  - `TPAPP/app/src/main/java/com/tp/tpapp/data/TPRepository.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/data/dao/RecordDao.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/viewmodel/RecordListViewModel.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/screen/RecordListScreen.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/viewmodel/AppListViewModel.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/screen/AppListScreen.kt`
  - `PROJECT_PLAYBOOK.md`
- Verification:
  - `cd TPAPP && ./gradlew :app:compileDebugKotlin` passed
- Notes/Risks:
  - Import mode remains global per file; there is no per-module selective import toggle.

### 2026-03-02 (Legacy password-only JSON compatibility for unified import)

- Request:
  - Ensure old-version JSON files (password-only export without `records` field) remain importable after unified import/export update.
- Implementation:
  - In `TPRepository.importFromJson`, parse JSON root and detect whether `records` field exists.
  - Legacy file behavior (`records` absent):
    - import passwords/apps as before
    - do not overwrite or append records
  - Unified file behavior (`records` present):
    - keep current unified logic for records + passwords
- Key files:
  - `TPAPP/app/src/main/java/com/tp/tpapp/data/TPRepository.kt`
  - `PROJECT_PLAYBOOK.md`
- Verification:
  - `cd TPAPP && ./gradlew :app:compileDebugKotlin` passed
- Notes/Risks:
  - Legacy compatibility detection is field-based (`records` exists or not), independent of `version` number.

### 2026-03-02 (Default password visibility switched to ON)

- Request:
  - Make `密码默认展示` enabled by default.
- Implementation:
  - Updated DataStore fallback default for `password_default_visible` to `true`.
  - Updated `SettingsViewModel` initial state for `passwordDefaultVisible` to `true` to avoid first-frame mismatch.
- Key files:
  - `TPAPP/app/src/main/java/com/tp/tpapp/data/SettingsRepository.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/viewmodel/SettingsViewModel.kt`
  - `PROJECT_PLAYBOOK.md`
- Verification:
  - `cd TPAPP && ./gradlew :app:compileDebugKotlin` passed
- Notes/Risks:
  - Existing users who previously saved `password_default_visible=false` will keep that explicit setting; this change affects default/fallback behavior.

### 2026-03-03 (Post-save navigation for first account after new app creation)

- Request:
  - Change flow so that after adding a new app and saving the first account/password, user lands on that app's account list page, not the app list.
- Implementation:
  - Added `onSaveSuccess` callback to `AccountEditScreen` to separate "back button" behavior from "save completion" behavior.
  - Updated `Navigation.kt` account-edit route handling:
    - If previous destination is app detail, save pops back normally.
    - Otherwise (e.g. came from app list right after adding app), save pops account edit and then navigates to `app_detail/{appId}`.
- Key files:
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/screen/AccountEditScreen.kt`
  - `TPAPP/app/src/main/java/com/tp/tpapp/ui/navigation/Navigation.kt`
  - `PROJECT_PLAYBOOK.md`
- Verification:
  - `cd TPAPP && ./gradlew :app:compileDebugKotlin`
- Notes/Risks:
  - Save flow now has context-aware destination logic; back button behavior remains unchanged (`popBackStack`).

---

## 9. Known Risks and Notes

- Some older files contain mojibake in comments/UI text from historical encoding issues.
- Passwords are encrypted at rest using Android Keystore AES-GCM; key invalidation can make old ciphertext undecryptable.
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
