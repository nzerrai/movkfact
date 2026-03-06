---
sprint: 8
title: Extended Types + RGPD Anonymization + Batch Fix
duration: 2 semaines
startDate: 2026-03-05
endDate: 2026-03-18
status: in-progress
dependsOn: [Sprint 7]
---

# Sprint 8 Kickoff Summary

**Sprint :** Extended Types + RGPD Anonymization + Batch Fix
**Duree :** 2 semaines (05/03 - 18/03/2026)
**Objectif :** Enrichir les typologies de colonnes (ENUM, PERCENTAGE), livrer la fonctionnalite d'anonymisation RGPD irreversible avec sauvegarde en dataset, et corriger les blocages de la generation batch.

---

## Objectifs du Sprint

1. **Types etendus (Epic 5 - S5-3)** — Ajout PERCENTAGE et ENUM comme typologies natives du moteur de generation
2. **Anonymisation RGPD (Epic 11 - nouveau)** — Module complet d'anonymisation irreversible de fichiers CSV/JSON avec sauvegarde en dataset par domaine
3. **Corrections techniques** — Fix batch "Aucune configuration", zone JSON manquante sur API Reference POST

---

## Metriques Cles

- **Stories :** 5
- **Points d'effort :** 17 pts
- **Statut :** En cours (demarrage 05/03/2026)
- **Risques :** Volume fichier anonymisation, unicite noms datasets, disponibilite domaines pour batch

---

## Stories du Sprint

| Story | Titre | Points | Statut |
|-------|-------|--------|--------|
| S8.1 | Type ENUM — liste de valeurs configurables | 3 | Done |
| S8.2 | Anonymisation RGPD fichiers CSV/JSON | 8 | Done |
| S8.3 | Sauvegarde resultat anonymisation en dataset | 3 | Done |
| S8.4 | Fix batch generation — Aucune configuration | 2 | Done |
| S8.5 | API Reference — zone JSON body pour POST | 1 | Done |

**Total :** 17 pts — toutes les stories livrees

---

## Definition of Done (Sprint 8)

- [x] ENUM : saisie valeurs separees par virgules, generation aleatoire dans la liste
- [x] PERCENTAGE : generation float [0.0, 100.0] precision 2 decimales
- [x] Anonymisation : strategies par ColumnType, transformations irreversibles
- [x] IBAN et IP completement regeneres (pas de hash)
- [x] Aucune donnee originale persistee cote serveur
- [x] Sauvegarde dataset anonymise avec selecteur domaine + nom
- [x] Batch : domains avec wizard utilisables sans upload CSV prealable
- [x] API Reference : body JSON editable pour endpoints POST

---

## Decisions Techniques Cles

### Anonymisation RGPD
- **Pas de hachage** : toutes les valeurs sont remplacees par des donnees synthetiques du meme type
- **Detection de format** : pour TEXT et default, le type reel de la valeur (entier, decimal, booleen, texte) est detecte et respecte
- **IBAN** : 27 caracteres regeneres aleatoirement (FR + 25 chiffres)
- **IP** : 4 octets completement regeneres
- **Sel runtime** : `UUID.randomUUID()` au demarrage, jamais persiste ni expose — garantit l'irreversibilite meme avec acces au code source

### Fix Batch
- Cause : `column_configurations` n'etait alimentee qu'apres upload CSV
- Solution : `DataGenerationController.generateDataset()` sauvegarde desormais les configs de colonnes a chaque creation de dataset (wizard ou CSV)

---

## Equipe Assignee

- **Backend :** Amelia
- **Frontend :** Sally + Amelia
- **Demandeur :** Nouredine

---

Voir les [Stories Sprint 8](stories.md)
Voir la spec : [Epic 11 Anonymisation RGPD](../../epics/epic-11-anonymisation-rgpd/11-1-anonymisation-rgpd.md)
