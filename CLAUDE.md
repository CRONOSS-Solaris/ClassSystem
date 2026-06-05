# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> **Wspólne konwencje** dla wszystkich pluginów w repo `CityProject` (układ plików, model
> dokumentacji, wersjonowanie, styl kodu, dyscyplina commitów, response discipline) żyją w
> **[`../CLAUDE.md`](../CLAUDE.md)** (root repo nadrzędnego). Claude Code czyta CLAUDE.md
> hierarchicznie — pracując w tym podprojekcie masz oba pliki w kontekście. Ten plik trzyma
> **tylko specyfikę ClassSystem**. Gdy reguła jest ogólna (dotyczy też CitySystem) — należy do
> `../CLAUDE.md`, nie tutaj.

## Project

Paper plugin dla Minecraft 1.21.x, Java 21, Gradle. Kod/komentarze/logi/wartości configu — po polsku;
nazwy klas/metod/pól i klucze configu — po angielsku (szczegóły w `../CLAUDE.md`). Plugin name:
`ClassSystem`, root pakietu Java: `cronos.classsystem`. Używa `paper-plugin.yml` (nowszy loader Paper),
nie legacy `plugin.yml` — komendy NIE są tam deklarowane (samorejestracja przez kod, wzorzec z CitySystem).

Cel pluginu: zarządzanie klasami postaci (skill/role gracza). Szkielet startowy — domeny będą dorastać.

## Build & test

- `./gradlew build` — pełny build, JAR w `build/libs/ClassSystem-<version>.jar`.
- `./gradlew check` → uruchamia `integrationTest` (source set `src/integrationTest/java`).
- **`./gradlew test` jest celowo wyłączony** (`test { enabled = false }` w `build.gradle`) — ta sama
  konwencja co w CitySystem. Testy JUnit 5 / Mockito / MockBukkit pod `src/test/java` uruchamia się
  ad-hoc (tymczasowy `enabled = true` lub przez IDE). Nie „naprawiać" flagi bez pytania.
- `byte-buddy:1.17.8` + `asm:9.6` są force'owane dla Mockito 5 / Java 21 — nie bumpować bez testów.

## Lifecycle (ClassSystemPlugin)

`ClassSystemPlugin` to obecnie minimalny szkielet (`onEnable`/`onDisable` + singleton `getInstance()`).
Gdy dojdą serwisy zależne od innych pluginów — stosować staggered init z `runTaskLater` (wzorzec z
CitySystem). `getInstance()` bezpieczny dopiero po `onEnable` — NIE z `onLoad`/static initializerów.

## Persistence

Stack persystencji (SQLite/MySQL/MariaDB + HikariCP + Gson) jest już w `build.gradle` żeby zachować
schemat zgodny z CitySystem. Migracje: `src/main/resources/migrations/{NNN}_{nazwa}.sql`, składnia
kompatybilna jednocześnie z MySQL + MariaDB + SQLite. Pierwsza migracja schematu → `001_init.sql`.
Prefiks tabel: `cls_` (`database.table-prefix`).

## Dokumentacja (ten sam podział co CitySystem)

| Lokalizacja | Charakter |
|---|---|
| [`README.md`](README.md) | User-facing — operator/gracz: instalacja, komendy, permissions, config. |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Techniczna — bieżący stan kodu, lifecycle, niezmienniki (file:line). |
| [`docs/TECH_DEBT.md`](docs/TECH_DEBT.md) | Wewnętrzny dług kodu — refaktor, testy, proces. |
| `git log` + tagi | Historia zmian. |

Reguły doc-as-you-code (każda nowa rzecz dokumentowana w tym samym commicie) — patrz `../CLAUDE.md`.

## Response discipline

Patrz `../CLAUDE.md`. Skrót: nie zgaduj API/plików, najpierw cytuj/sprawdź potem odpowiadaj, „nie wiem"
gdy nie wiesz.
