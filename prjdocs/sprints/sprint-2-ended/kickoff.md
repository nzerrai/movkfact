---
sprint: 2
title: Data Generation & Configuration
duration: 2 semaines
startDate: 2026-03-17
endDate: 2026-03-30
status: ready
dependsOn: [Sprint 1]
---

# Sprint 2 Kickoff Summary

**Sprint :** Data Generation & Configuration  
**Durée :** 2 semaines (17/03 - 30/03/2026)  
**Objectif :** Implémenter le cœur du produit - moteur de génération de données et interface de configuration

---

## Objectifs du Sprint

1. **Data Generator Engine :** Moteur générant 3 typologies (Personnelles, Financières, Temporelles)
2. **Configuration Interface :** UI intuitive pour configurer les colonnes
3. **CSV Upload :** Charger fichier CSV exemple et parser
4. **Data Output :** Génération JSON et affichage résultats
5. **Type Detection :** Détection intelligente des types de colonnes

---

## Métriques Clés

- **Stories :** 6
- **Points d'effort :** 34
- **Vélocité cible :** 17 points/semaine
- **Risques :** Complexité algo détection types, performance génération
- **Blockers :** Sprint 1 doit être complété

---

## Definition of Done (Sprint 2)

- [ ] Tests unitaires pour DataGeneratorService (>80% coverage)
- [ ] Tests d'intégration pour API génération
- [ ] Performance : Génération 1000 lignes < 2 secondes
- [ ] UI responsive et accessible
- [ ] CSV parser robuste avec error handling
- [ ] Documentation algorithme de détection
- [ ] Story validée par PM et UX

---

## Jalons Clés

- **Jour 1-2 :** DataGeneratorService backend + Faker.js integration
- **Jour 3-4 :** Détection types colonnes
- **Jour 5-7 :** CSV Upload + génération API
- **Jour 8-10 :** UI Configuration (CsvUploader, DataTypeSelector)
- **Jour 11-14 :** Affichage résultats, tests, optimisation performance

---

## Équipe Assignée

- **Backend :** Amelia (Dev) + Winston (Architect)
- **Frontend :** Sally (UX) + Amelia (Dev)
- **QA :** Quinn (QA Engineer) - Tests performance
- **Coordination :** Bob (SM)

---

## Dépendances Externes

- [ ] Sprint 1 complété (APIs Domain + Frontend base)
- [ ] Faker.js disponible (npm install)
- [ ] Librairie CSV parser (papaparse)
- [ ] Tests de charge tool (JMeter optionnel)

---

## Précisions Techniques

- **3 Typologies MVP :** Personnelles (prénoms, emails), Financières (montants), Temporelles (dates)
- **Détection :** Analyser en-têtes CSV + données pour inférer types
- **Performance :** Utiliser Redis cache (optionnel Sprint 2, recommandé Sprint 3)
- **Format sortie :** JSON array, chaque objet = une ligne

---

Voir les [User Stories Sprint 2](stories.md)
