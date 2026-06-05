# AI_WORKFLOW — ClassSystem (`cronos.classsystem`)

> Mapa nawigacyjna dla AI. Czytaj przed pracą w tym repo.
> ⬆ Hub: [`../AI_WORKFLOW.md`](../AI_WORKFLOW.md) · Reguły: [`../CLAUDE.md`](../CLAUDE.md) (wspólne) · [`CLAUDE.md`](CLAUDE.md) (lokalne) · Sąsiad: [`../CitySystem/AI_WORKFLOW.md`](../CitySystem/AI_WORKFLOW.md)

## Rola

Plugin Paper 1.21.x do zarządzania klasami postaci — **szkielet startowy**. Gotowe są fundamenty
(config + tłumaczenia + baza komend + logowanie); właściwe domeny (klasy, umiejętności) dopiero powstają.
Schemat (układ plików, dokumentacja, konwencje) wspólny z CitySystem.

## Gdzie co jest

| Ścieżka | Co tam jest |
|---|---|
| [`src/main/java/cronos/classsystem/ClassSystemPlugin.java`](src/main/java/cronos/classsystem/ClassSystemPlugin.java) | Główna klasa: `onEnable` (logery → `ConfigMigrator` → `ConfigManager` → rejestracja komendy → banner), `onDisable`, singleton, `sendMessage`/`getMessage`. |
| [`config/`](src/main/java/cronos/classsystem/config/) | `ConfigManager` (wiadomości/kolory/aliasy/ustawienia), `ConfigMigrator` (auto-migracja config.yml + Translations po `configVersion`), `MessagesConfigLoader` (Translations + fallback pl). |
| [`commands/`](src/main/java/cronos/classsystem/commands/) | `ClassMainCommand` (dispatch `/klasa`), `ArgumentParser`; `commands/base/` (AbstractCommand, Subcommand, AbstractSubcommand); `commands/subcommands/` (`HelpSubcommand`, `ReloadSubcommand`). |
| [`utils/`](src/main/java/cronos/classsystem/utils/) | `ColoredLogger`, `DebugLogger`, `ErrorLogFileWriter` (ten sam styl logów co CitySystem). |
| [`src/main/resources/`](src/main/resources/) | `paper-plugin.yml`, `config.yml` (z `configVersion`), `Translations/{pl,en}.yml`, `migrations/` (puste — pierwsza migracja `001_init.sql` gdy powstanie persystencja). |

## Sąsiedzi

- **build-time (import):** brak. Niezależny projekt Gradle; nie importuje `CitySystem` ani nie jest przez niego importowany.
- **build-time (zewnętrzne, `compileOnly`):** PlaceholderAPI, paper-api, adventure; SQL (MariaDB/MySQL/SQLite) + HikariCP + Gson (stack persystencji gotowy w `build.gradle`, jeszcze nieużywany); test: JUnit5/Mockito/MockBukkit.
- **runtime (działający serwer):** **soft-dep** PlaceholderAPI (opcjonalny). Brak bezpośredniej krawędzi runtime do [`CitySystem`](../CitySystem/AI_WORKFLOW.md).
- **Hub i konwencje:** [`../AI_WORKFLOW.md`](../AI_WORKFLOW.md), [`../CLAUDE.md`](../CLAUDE.md).

## Dev

`./gradlew build` (JAR w `build/libs/`) · `./gradlew check` (uruchamia `integrationTest`) · `./gradlew test` **celowo wyłączony** (patrz [`CLAUDE.md`](CLAUDE.md)).

## Świeżość

Po dodaniu domeny (serwisy, baza, GUI), nowej komendy/permission, klucza config lub zależności — zaktualizuj
ten plik **w tym samym commicie** (reguła §13 w [`../CLAUDE.md`](../CLAUDE.md)). Bieżący stan techniczny:
[`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).
