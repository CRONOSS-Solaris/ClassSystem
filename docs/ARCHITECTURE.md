# ClassSystem — dokumentacja techniczna

Dokument opisuje strukturę kodu, lifecycle, niezmienniki i punkty rozszerzenia pluginu ClassSystem.
Adresowany do developerów modyfikujących kod oraz agentów AI. Ten sam charakter co
`CitySystem/docs/ARCHITECTURE.md`.

| | |
|---|---|
| **Charakter dokumentu** | Bieżący stan kodu — co istnieje, jak działa, co od czego zależy |
| **Czego NIE zawiera** | Changelog, historia zmian, plany na przyszłość |
| **Wewnętrzny dług techniczny** | [`TECH_DEBT.md`](TECH_DEBT.md) |
| **Historia zmian** | `git log` + tagi |
| **Konwencje kodu i best practices** | [`../CLAUDE.md`](../CLAUDE.md) · wspólne [`../../CLAUDE.md`](../../CLAUDE.md) |
| **Dokumentacja użytkownika** | [`../README.md`](../README.md) |

Konwencja cytowania: `ścieżka:linia` względem root tego podprojektu.

---

## Spis treści

1. [Mapa modułów](#1-mapa-modułów)
2. [Lifecycle pluginu](#2-lifecycle-pluginu)
3. [Build i CI](#3-build-i-ci)

---

## 1. Mapa modułów

Drzewo pakietów `cronos.classsystem.*` (rośnie wraz z funkcjami; konwencja warstw jak w CitySystem —
`commands/`, `commands/base/`, `services/`, `db/`, `db/repositories/`, `model/`, `gui/`, `gui/views/`,
`listeners/`, `utils/`):

```
cronos.classsystem
└── ClassSystemPlugin        — główna klasa, lifecycle + singleton getInstance()
```

## 2. Lifecycle pluginu

`ClassSystemPlugin.onEnable` ustawia singleton i loguje start; `onDisable` loguje stop. Brak na razie
serwisów, bazy, schedulerów. Gdy dojdą — kolejność init/teardown i staggered delays wzorować na
`CitySystem/docs/ARCHITECTURE.md` (sekcja Lifecycle).

## 3. Build i CI

- Gradle, Java 21, `paper-plugin.yml` z tokenem `${version}` filtrowanym przez `processResources`.
- CI: `.github/workflows/build.yml` (PR → `./gradlew build`), `.github/workflows/release.yml`
  (push na master/main → release z wersji z `build.gradle`, idempotentnie). Identyczny schemat z CitySystem.
