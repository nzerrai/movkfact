# BOB'S SPRINT 8 CLOSURE REPORT

**To:** Team (Amelia, Sally, Winston, John, Paige, Mary)
**From:** Bob (Scrum Master)
**Date:** 06 mars 2026
**Subject:** ✅ SPRINT 8 CLOSED | 5 stories livrées | 17/17 pts | Epic 11 RGPD complet

---

## Executive Summary

Le **Sprint 8 est officiellement clôturé** avec :
- ✅ **Epic 11 (RGPD Anonymisation) livré à 100%** — S8.2 + S8.3 = 11 pts
- ✅ **Epic 5 complété** — ENUM + PERCENTAGE = types étendus natifs (S8.1 = 3 pts)
- ✅ **Fix critique batch** — domaines wizard utilisables en batch (S8.4 = 2 pts)
- ✅ **API Reference enrichie** — zone JSON body pour POST (S8.5 = 1 pt)
- ✅ **Documentation alignée** — Party mode P1/P2/P3 exécuté : architecture.md, backlog, epic specs, design RGPD
- ✅ **Vélocité 100%** — 17/17 pts livrés

---

## Stories Livrées

| Epic | Story | Titre | Points | Statut |
|------|-------|-------|--------|--------|
| E5 | S8.1 | Type ENUM — liste de valeurs configurables | 3 | ✅ Done |
| E11 | S8.2 | Anonymisation RGPD fichiers CSV/JSON | 8 | ✅ Done |
| E11 | S8.3 | Sauvegarde résultat anonymisation en dataset | 3 | ✅ Done |
| E3 | S8.4 | Fix batch — "Aucune configuration" wizard | 2 | ✅ Done |
| E9 | S8.5 | API Reference — zone JSON body POST | 1 | ✅ Done |
| **TOTAL** | | | **17** | **✅ 100%** |

---

## Définition of Done — Validation

| Critère | Statut |
|---------|--------|
| ENUM : saisie valeurs séparées par virgule, génération aléatoire dans la liste | ✅ |
| PERCENTAGE : float [0.0, 100.0], 2 décimales | ✅ |
| Anonymisation : stratégies par ColumnType, transformations irréversibles | ✅ |
| IBAN et IP intégralement régénérés (pas de hash) | ✅ |
| Aucune donnée originale persistée côté serveur | ✅ |
| Dataset anonymisé sauvegardé avec sélecteur domaine + nom | ✅ |
| Batch : domaines wizard utilisables sans upload CSV préalable | ✅ |
| API Reference : body JSON éditable pour endpoints POST | ✅ |

---

## Détail fonctionnel — Epic 11 Anonymisation RGPD

Le module d'anonymisation est **opérationnel de bout en bout** :

```
Étape 1 : Upload fichier CSV/JSON → POST /api/anonymize/inspect → liste des colonnes détectées
Étape 2 : Tableau colonnes — checkbox anonymiser + sélecteur ColumnType + stratégie affichée
          + Sélecteur domaine (getDomains()) + Nom dataset (pré-rempli depuis fichier)
Étape 3 : Confirmation — stats (rowCount, columnCount, durée) + lien direct vers /data-viewer/{id}
```

**Garanties RGPD :**
- Pas de hachage : remplacé par données synthétiques du même type structurel
- `generateMatchingType()` : détecte le format réel de chaque valeur (Long → entier, Double → décimal, true/false → booléen, fallback → texte)
- IBAN : `FR` + 25 chiffres aléatoires
- IP : 4 octets complètement indépendants
- BIRTH_DATE : année uniquement (généralisation RGPD)
- DATE : décalage ±180 jours, format original préservé
- Sel runtime : `UUID.randomUUID()` au démarrage JVM — jamais loggé, jamais exposé, jamais persisté

---

## Bugs Résolus

| Ref | Description | Cause | Fix |
|-----|-------------|-------|-----|
| B1 | "Objects are not valid as a React child" sur erreur anonymisation | Réponse Spring `{error, timestamp, status, path}` rendue directement dans le state | Extraction string : `typeof data === 'string' ? data : data?.message \|\| data?.error` |
| B2 | Batch "Aucune configuration" sur domaines wizard | `column_configurations` non alimentée hors flux CSV | `DataGenerationController.generateDataset()` appelle `columnConfigurationService.saveColumnConfigurations()` après chaque création |

---

## Décisions Architecturales Clés

1. **Anonymisation = données synthétiques, pas de hash** : SHA-256 initialement prévu mais retiré — valeurs hexadécimales non-représentatives pour des colonnes métier. Décision : remplacement par données du même type structurel.

2. **Détection de format réel pour TEXT/default** : Une colonne déclarée TEXT peut contenir des entiers ou des décimaux. `generateMatchingType()` détecte et respecte le type réel de la valeur, pas seulement le type déclaré.

3. **Fix batch centralisé** : Deux flux de création (CSV + wizard) partageaient le moteur de génération mais seul le flux CSV alimentait `column_configurations`. Solution : synchronisation systématique dans `DataGenerationController` pour les deux flux.

4. **Endpoint `/api/anonymize/save`** : Le résultat n'est pas téléchargé directement mais persisté en `DataSet` MockFact — réutilisable depuis Data Viewer, téléchargeable en CSV/JSON, intégrable dans batch.

---

## Travaux Documentation (Party Mode P1/P2/P3)

Suite à l'analyse de décalage docs/code par les agents (Winston, Mary, Paige, John, Bob) :

| Artefact | Action | Statut |
|----------|--------|--------|
| `architecture.md` | Refonte complète — composants réels, 20+ ColumnTypes, sans Redis/JWT/Faker | ✅ |
| `backlog.md` | Réécriture v2.0 — 11 epics, statuts corrects, 18/20 FRs | ✅ |
| `epic-11-anonymisation-rgpd/11-1-anonymisation-rgpd.md` | Création spec complète Epic 11 | ✅ |
| `planning/rgpd-anonymization-design.md` | Création — 6 décisions architecturales, garanties RGPD | ✅ |
| `sprint-8-ended/kickoff.md` | Création — 5 stories, DoD, décisions techniques | ✅ |
| `sprint-8-ended/stories.md` | Création — 5 stories rétroactives détaillées | ✅ |
| `sprint-7-ended/planning-summary.md` | Lien mort corrigé (`sprint-status.yaml` → `backlog.md`) | ✅ |

---

## Vélocité Sprint 8

```
Planifié  : 17 pts (5 stories)
Réalisé   : 17 pts (5/5 stories done)
Taux      : 100%
Note      : Sprint retroactif — travaux déjà livrés avant formalisation du sprint
```

---

## Couverture PRD cumulée après Sprint 8

| FR | Description | Couverture |
|----|-------------|-----------|
| FR-001 | Créer domaine | ✅ Sprint 1 |
| FR-002 | Consulter domaines (stats agrégées) | ⏳ Backlog Sprint 9 |
| FR-003 | Accéder datasets avec statuts | ⏳ Backlog Sprint 9 |
| FR-004/005 | Flux CSV + initiation | ✅ Sprint 2 |
| FR-006/010 | Wizard + lancer depuis DM | ✅ Sprint 6 |
| FR-007 | Générer dataset JSON | ✅ Sprint 2 |
| FR-008 | Typologies complètes (incl. ENUM, PERCENTAGE) | ✅ Sprint 8 |
| FR-009 | Écran Domain Management | ✅ Sprint 1 |
| FR-011/012/016 | Éditeur inline | ✅ Sprint 6 |
| FR-013 | Télécharger dataset | ✅ Sprint 2 |
| FR-014 | API filtrage avancé | ⏳ Backlog Sprint 9 |
| FR-015 | CRUD APIs | ✅ Sprint 2 |
| FR-017/018/019 | Activité + reset + trace | ✅ Sprint 2–3–6 |
| FR-020 | Génération par lots | ✅ Sprint 3–4 |
| **RGPD** | Anonymisation irréversible | ✅ **Sprint 8** |

**→ 18/20 FRs couverts. FR-002/003/014 reportés Sprint 9 (Epic 8 + 9).**

---

## Sprint 9 — Backlog Recommandé

| Epic | Story | Points | Priorité |
|------|-------|--------|----------|
| E8 | S8.1 Backend Agrégats domaines | 4 | HIGH |
| E8 | S8.2 Frontend Enrichissement domaines | 4 | HIGH |
| E9 | S9.1 API Filtrage avancé (rowIds + cols) | 5 | MEDIUM |
| E10 | S3.5b Docker + Documentation finale | 4 | LOW |
| **TOTAL** | | **17** | |

---

## Rétrospective

**Ce qui a bien fonctionné :**
- Anonymisation RGPD livrée complète en un sprint (8+3 pts) grâce à une architecture claire dès le départ
- Fix batch identifié rapidement — cause racine isolée, pas de contournement
- Party mode efficace pour aligner docs/code en une session
- 0 régression sur les fonctionnalités Sprint 1–7

**Points d'amélioration :**
- La formalisation rétroactive du sprint (stories créées après implémentation) génère de la dette documentaire — préférable de créer les stories avant de coder
- Le flux de création de dataset (2 chemins CSV + wizard) devrait avoir un point d'entrée unique à terme pour éviter les bugs de synchronisation

---

*Signé :* Bob (Scrum Master)
*Date :* 06 mars 2026
*Statut :* ✅ Sprint 8 CLOS — 17/17 pts livrés | Epic 11 RGPD complet
