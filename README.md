# ClassSystem

Plugin serwerowy Paper 1.21.x do zarządzania klasami postaci w Minecrafcie. Szkielet startowy —
funkcjonalności będą dorastać. Schemat repo (układ plików, dokumentacja, konwencje) wspólny z
CitySystem, opisany w [`CLAUDE.md`](CLAUDE.md) oraz wspólnym [`../CLAUDE.md`](../CLAUDE.md).

| | |
|---|---|
| **Wersja** | 0.2.0 |
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

_Brak — szkielet startowy. Lista będzie uzupełniana wraz z funkcjami (komendy rejestrowane przez kod,
nie w `paper-plugin.yml`)._

## 4. Permissions

_Brak — uzupełniane wraz z komendami. Konwencja prefiksu: `ClassSystem.<...>`._

## 5. Konfiguracja

Plik `plugins/ClassSystem/config.yml`. Sekcje: `general` (enabled, language), `database`
(type, table-prefix, sqlite), `debug`. Komentarze przy kluczach w samym pliku.

## 6. Tłumaczenia

Pliki `plugins/ClassSystem/Translations/{pl,en}.yml`, wybór przez `general.language`.
Brakujący klucz w aktywnym języku → fallback do `pl.yml`.
