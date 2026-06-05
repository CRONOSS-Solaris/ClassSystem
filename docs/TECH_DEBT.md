# TECH_DEBT

Wewnętrzny backlog długu technicznego pluginu ClassSystem — refaktoring, testy, proces.
Ten sam charakter i legenda statusów co `CitySystem/docs/TECH_DEBT.md`.

| | |
|---|---|
| **Charakter dokumentu** | Wewnętrzny dług kodu — refaktoring, testy, konwencje. Trzyma file:line references. |
| **Czego NIE zawiera** | User-facing features, bieżący stan kodu (→ [`ARCHITECTURE.md`](ARCHITECTURE.md)), historia (→ `git log`). |
| **Konwencje kodu** | [`../CLAUDE.md`](../CLAUDE.md) · wspólne [`../../CLAUDE.md`](../../CLAUDE.md) |

---

## 1. Legenda statusów

| Status | Znaczenie |
|---|---|
| `proposed` | Pomysł zarejestrowany, brak decyzji o realizacji. |
| `accepted` | Zaakceptowany do realizacji. Notatka określa plan implementacji. |
| `blocked` | Czeka na zewnętrzny warunek. |
| `dropped` | Świadomie odrzucony — wpis zostaje jako zapora przed ponownym zgłaszaniem. |

Każda pozycja: **Why** / **Risk** / **Status** / **Notatka**.

---

## 2. Pozycje

_Brak — szkielet startowy. Pierwsze pozycje pojawią się wraz z rozwojem kodu._
