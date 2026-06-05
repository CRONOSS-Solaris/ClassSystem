# AI_WORKFLOW — ClassSystem (`cronos.classsystem`)

> Mapa nawigacyjna dla AI. Czytaj przed pracą w tym repo. **Cel: od razu wiedzieć gdzie co jest i co od czego zależy.**
> ⬆ Hub: [`../AI_WORKFLOW.md`](../AI_WORKFLOW.md) · Reguły: [`../CLAUDE.md`](../CLAUDE.md) (wspólne) · [`CLAUDE.md`](CLAUDE.md) (lokalne) · Sąsiad: [`../CitySystem/AI_WORKFLOW.md`](../CitySystem/AI_WORKFLOW.md)
> Stan techniczny: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) · User: [`README.md`](README.md)

## Rola

Plugin Paper 1.21.x do zarządzania klasami postaci — **szkielet startowy**. Gotowe fundamenty
(config + tłumaczenia + baza komend + logowanie); domeny (klasy, umiejętności) dopiero powstają.
Schemat wspólny z CitySystem (wzorzec referencyjny).

## Warstwy i przepływ (co od czego zależy)

```
komenda /klasa  →  ClassMainCommand (dispatch)  →  subcommands/  →  ConfigManager (wiadomości/ustawienia)
ConfigManager   →  MessagesConfigLoader (Translations + fallback pl)
onEnable        →  ConfigMigrator (auto-migracja config.yml + Translations po configVersion)
plugin.sendMessage / getMessage  →  ConfigManager (prefiks + kolory + podstawienia)
loggery (utils) →  ColoredLogger / DebugLogger / ErrorLogFileWriter
```

Stack persystencji (SQLite/MySQL/MariaDB + HikariCP + Gson) jest w `build.gradle`, ale **jeszcze nieużywany**
(brak `db/`, brak migracji) — dojdzie z pierwszą domeną.

## Lifecycle (`ClassSystemPlugin.java`)

`onEnable`: `saveDefaultConfig` → `ColoredLogger` → `ErrorLogFileWriter.initialize` → `DebugLogger.initialize`
→ banner `=== URUCHAMIANIE PLUGINU KLAS ===` → `ConfigMigrator.migrateAll()` (PRZED ConfigManagerem) →
`ConfigManager.loadConfigs()` → rejestracja `ClassMainCommand` (self-register na CommandMap) → `printStartupBanner`.
`onDisable`: banner wyłączania → `ErrorLogFileWriter.shutdown()` na końcu. Singleton `getInstance()` bezpieczny
dopiero po `onEnable`.

---

## Gdzie co jest (pełny spis)

### Główna klasa → [`src/main/java/cronos/classsystem/ClassSystemPlugin.java`](src/main/java/cronos/classsystem/ClassSystemPlugin.java)

Lifecycle, singleton, `getColoredLogger()`, `getConfigManager()`, `getMessage()`, `sendMessage()` (prefiks na pierwszej linii + wsparcie list).

### `config/` → [katalog](src/main/java/cronos/classsystem/config/)

- `ConfigManager` — jedyny punkt dostępu: `getMessage`/`getMessageNoPrefix`/listy, `translateColorCodes`, aliasy/opisy/permissiony komend (`ClassSystem.<name>`), ustawienia (`isSystemEnabled`, `getLanguage`, `isDebugEnabled`).
- `ConfigMigrator` — auto-migracja `config.yml` + `Translations/*.yml` po `configVersion` (deep-merge bundled↔user + backup `*.backup-<ts>`).
- `MessagesConfigLoader` — I/O `Translations/<lang>.yml` + fallback do `pl.yml`.

### `commands/` → [katalog](src/main/java/cronos/classsystem/commands/)

- `ClassMainCommand` — dispatch `/klasa` (aliasy `/klasy`, `/class`): alias→primary, bramka `ClassSystem.<name>`, tab-complete, bez argumentu → pomoc.
- `ArgumentParser` — `parseInt` / `parsePositiveDouble` / `requireMinArgs` z auto-komunikatem błędu.
- `commands/base/` — `AbstractCommand` (self-register przez refleksję na CommandMap), `Subcommand` (interfejs), `AbstractSubcommand` (permission/description/usage z tłumaczeń).
- `commands/subcommands/` — `HelpSubcommand` (lista komend wg uprawnień), `ReloadSubcommand` (reload config + tłumaczeń).

### `utils/` → [katalog](src/main/java/cronos/classsystem/utils/)

- `ColoredLogger` — kolorowe logi ANSI (banner, sekcje, statusy ✓/✗/⚠), `infoIfNotDebug`/`infoAlways`.
- `DebugLogger` — verbose diagnostyka gated `debug.enabled` (singleton, kategorie, `debugException`).
- `ErrorLogFileWriter` — plik-only `logs/errors-YYYY-MM-DD.log` + JUL Handler na WARNING+SEVERE; dzienna rotacja, thread-safe.

### `src/main/resources/` → [katalog](src/main/resources/)

`paper-plugin.yml`, `config.yml` (`configVersion`; sekcje: general[enabled,language], database[type,table-prefix,sqlite], debug[enabled,log-database-operations]), `Translations/{pl,en}.yml` (`prefix`, `info.*`, `errors.*`, `commands.*` z aliasami subkomend), `migrations/` (puste — pierwsza migracja `001_init.sql` gdy powstanie persystencja).

---

## Sąsiedzi

- **build-time (import):** brak. Niezależny projekt Gradle; nie importuje `CitySystem` ani nie jest przez niego importowany.
- **build-time (zewnętrzne, `compileOnly`):** PlaceholderAPI, paper-api, adventure; SQL (MariaDB/MySQL/SQLite) + HikariCP + Gson + Lombok (stack persystencji gotowy, nieużywany); test: JUnit5/Mockito/MockBukkit.
- **runtime:** soft-dep PlaceholderAPI (opcjonalny). Brak krawędzi runtime do [`CitySystem`](../CitySystem/AI_WORKFLOW.md).
- **Hub i konwencje:** [`../AI_WORKFLOW.md`](../AI_WORKFLOW.md), [`../CLAUDE.md`](../CLAUDE.md).

## Dev

`./gradlew build` (JAR w `build/libs/`) · `./gradlew check` (uruchamia `integrationTest`) · `./gradlew test` **celowo wyłączony** (patrz [`CLAUDE.md`](CLAUDE.md)).

## Świeżość

Po dodaniu domeny (`services/`, `db/`, `model/`, `gui/`, `listeners/`), nowej komendy/permission, klucza config,
migracji lub zależności — zaktualizuj ten plik **w tym samym commicie** (reguła §13 w [`../CLAUDE.md`](../CLAUDE.md)).
