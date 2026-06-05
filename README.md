# ClassSystem

Plugin serwerowy Paper 1.21.x do zarządzania klasami postaci w Minecrafcie. Szkielet startowy —
funkcjonalności będą dorastać. Schemat repo (układ plików, dokumentacja, konwencje) wspólny z
CitySystem, opisany w [`CLAUDE.md`](CLAUDE.md) oraz wspólnym [`../CLAUDE.md`](../CLAUDE.md).

| | |
|---|---|
| **Wersja** | 0.3.0 |
| **Platforma** | Paper 1.21.x |
| **Java** | 21 |
| **Plik wtyczki** | `paper-plugin.yml` (Paper plugin loader) |
| **Język domyślny** | `pl` (przełączany w `config.yml`) |
| **Dokumentacja techniczna** | [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) |
| **Dług techniczny** | [`docs/TECH_DEBT.md`](docs/TECH_DEBT.md) |
| **Konwencje kodu** | [`CLAUDE.md`](CLAUDE.md) · [`../CLAUDE.md`](../CLAUDE.md) (wspólne) |

---

## Spis treści

1. [Wymagania](#1-wymagania)
2. [Instalacja](#2-instalacja)
3. [Komendy](#3-komendy)
4. [Permissions](#4-permissions)
5. [Konfiguracja](#5-konfiguracja)
6. [Tłumaczenia](#6-tłumaczenia)

---

## 1. Wymagania

| Plugin | Wersja | Wymagany | Cel |
|---|---|---|---|
| Paper | 1.21.x | tak | server core |
| Java | 21 | tak | runtime |
| PlaceholderAPI | ≥ 2.11.6 | nie | placeholdery (gdy dojdą) |

## 2. Instalacja

1. Pobierz `ClassSystem-X.Y.Z.jar` z [Releases](../../releases/latest) i skopiuj do `plugins/`.
2. Uruchom serwer raz, aby wygenerować `plugins/ClassSystem/config.yml` oraz `Translations/{pl,en}.yml`.
3. Dostosuj `config.yml` i uruchom ponownie.

## 3. Komendy

Komendy rejestrowane przez kod (nie w `paper-plugin.yml`). Aliasy subkomend są tłumaczalne
(`Translations/{pl,en}.yml`, sekcja `commands.subcommands.<name>.aliases`).

### `/klasa` (aliasy `/klasy`, `/class`)

| Komenda | Działanie |
|---|---|
| `/klasa` lub `/klasa pomoc` | Lista dostępnych komend, filtrowana wg uprawnień gracza. |
| `/klasa reload` | Przeładowanie configu i tłumaczeń bez restartu serwera. |

## 4. Permissions

Prefiks: `ClassSystem.<...>`. Domyślnie: `true` = wszyscy, `op` = operatorzy.

| Permission | Domyślnie | Opis |
|---|---|---|
| `ClassSystem.help` | `true` | Dostęp do `/klasa` i `/klasa pomoc`. Brak = brak dostępu do dispatcha. |
| `ClassSystem.reload` | `op` | `/klasa reload`. |

## 5. Konfiguracja

Plik `plugins/ClassSystem/config.yml`. Sekcje: `general` (enabled, language), `database`
(type, table-prefix, sqlite), `debug` (enabled, log-database-operations). Komentarze przy kluczach
w samym pliku. Pole `configVersion` jest zarządzane automatycznie przez `ConfigMigrator` — przy bumpie
wersji nowe klucze dolewają się, zachowując ustawienia operatora (backup `config.yml.backup-<ts>`).

## 6. Tłumaczenia

Pliki `plugins/ClassSystem/Translations/{pl,en}.yml`, wybór przez `general.language`.
Brakujący klucz w aktywnym języku → fallback do `pl.yml`. Klucze: `prefix`, `info.*`, `errors.*`,
`commands.*` (nazwa głównej komendy, aliasy, opisy + aliasy subkomend).
