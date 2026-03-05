---
sprint: 7
totalStories: 4
totalPoints: 17
---

# Sprint 7 User Stories

## Story S8.1 : Backend Agrégats par domaine

**Points :** 4
**Epic :** EPIC 8 — Domain Overview Enrichment
**Type :** Backend Feature
**Assigné :** Amelia
**FRs :** FR-002, FR-003

### Description
Enrichir `GET /api/domains` avec les statistiques agrégées (nb datasets, total lignes, statuts) et `GET /api/domains/{id}/datasets` avec le statut individuel + date de dernière activité par dataset.

### Acceptance Criteria
- [ ] `GET /api/domains` : chaque domaine inclut `datasetCount`, `totalRows`, `statuses { downloaded, modified, viewed }`
- [ ] `GET /api/domains/{id}/datasets` : chaque dataset inclut `status`, `lastActivity`
- [ ] Requête JPQL sans N+1 (GROUP BY + LEFT JOIN en une seule requête)
- [ ] `downloaded = true` si au moins une `Activity(DOWNLOADED)` sur le dataset
- [ ] `modified = true` si `dataset.version > 0`
- [ ] `viewed = true` si au moins une `Activity(VIEWED)` sur le dataset
- [ ] Performance : < 200ms pour 100 domaines / 500 datasets
- [ ] Zéro régression sur les champs existants
- [ ] Tests unitaires + intégration >80%

**Spec complète :** [8-1-backend-domain-aggregates.md](../../epic-8-domain-enrichment/8-1-backend-domain-aggregates.md)

---

## Story S8.2 : Frontend Enrichissement liste domaines

**Points :** 4
**Epic :** EPIC 8 — Domain Overview Enrichment
**Type :** Frontend Feature
**Assigné :** Sally
**FRs :** FR-002, FR-003

### Description
Ajouter les colonnes statistiques (nb datasets, lignes totales, badges statuts) dans `DomainTable` et enrichir `DomainDatasetsModal` avec les statuts et dates de modification par dataset.

### Acceptance Criteria
- [ ] `DomainTable` : colonnes "Datasets" (chip compteur), "Lignes totales" (formaté), "Statuts" (badges)
- [ ] Badge "Téléchargé" (vert), "Modifié" (orange), "Consulté" (bleu), "Nouveau" (gris)
- [ ] `StatusBadge.jsx` composant partagé (domain list + modal)
- [ ] `DomainDatasetsModal` : statuts + date dernière modif formatée ("il y a 2h") + tri/filtre
- [ ] Skeleton loaders pendant chargement initial
- [ ] Colonnes masquables sur mobile (< 768px)
- [ ] Tests Jest >80% sur composants modifiés/créés

**Spec complète :** [8-2-frontend-domain-stats.md](../../epic-8-domain-enrichment/8-2-frontend-domain-stats.md)

---

## Story S9.1 : API Filtrage avancé — rowIds + sélection colonnes

**Points :** 5
**Epic :** EPIC 9 — API d'accès avancé
**Type :** Backend Feature
**Assigné :** Amelia
**FRs :** FR-014, FR-015

### Description
Étendre `GET /api/data-sets/{id}/rows` avec les paramètres optionnels `rowIds` (filtrage par index) et `cols` (sélection de colonnes), permettant aux systèmes CI/CD d'extraire des sous-ensembles ciblés.

### Acceptance Criteria
- [ ] `POST /api/datasets/preview` retourne `400` si un type de colonne est inconnu (ex: `"INVALID_TYPE"`), message explicite *(ajouté party mode 04/03)*
- [ ] `GET /api/data-sets/{id}/rows?rowIds=0,5,10` retourne uniquement les lignes aux index listés
- [ ] `GET /api/data-sets/{id}/rows?cols=firstName,email` retourne uniquement les colonnes listées
- [ ] Combinaison `?rowIds=1,5&cols=firstName,email` fonctionne
- [ ] rowIds hors-bornes : ignorés silencieusement (pas de 404)
- [ ] Colonnes inconnues : ignorées silencieusement
- [ ] Réponse : `{ rows: [...], requestedCount: N, returnedCount: M }`
- [ ] `GET /api/data-sets/{id}/rows/{rowIndex}?cols=...` supporte aussi la sélection
- [ ] Performance : < 500ms pour 100 rowIds sur dataset 100k lignes
- [ ] Documentation Swagger complète avec exemples
- [ ] Tests intégration : 6 scénarios (rowIds, cols, combiné, hors-bornes, inconnus)

**Spec complète :** [9-1-api-row-filtering.md](../../epic-9-advanced-api/9-1-api-row-filtering.md)

---

## Story S3.5b : Docker Deployment & Documentation

**Points :** 4
**Epic :** EPIC 10 — Quality Gate & Déploiement
**Type :** DevOps + Documentation
**Assigné :** Amelia
**FRs :** NFR §4.3, §5 (stack PostgreSQL)

### Description
Livrer le déploiement containerisé production-ready : Dockerfiles multi-stage, docker-compose avec PostgreSQL, README complet et Swagger docs finalisées.

### Acceptance Criteria
- [ ] `Dockerfile.backend` : multi-stage Maven → openjdk:17-slim, health check actuator
- [ ] `Dockerfile.frontend` : multi-stage node:18 → nginx:alpine, `nginx.conf` SPA fallback
- [ ] `docker-compose.yml` : services `backend` + `frontend` + `postgres:15-alpine`, depends_on health
- [ ] Volume persistant `postgres_data`, réseau interne `movkfact_net`
- [ ] `.env.example` avec toutes les variables documentées
- [ ] `docker-compose up --build` démarre en < 2min sur machine propre
- [ ] `smoke-test.sh` : 3 appels séquentiels (`POST /api/domains` → `GET /api/domains` → `GET /actuator/health`), exit 0 si tous 2xx *(ajouté party mode 04/03)*
- [ ] `README.md` : prérequis, dev local, Docker, lien Swagger, section "Vérification post-déploiement"
- [ ] Swagger UI : tous les endpoints Sprint 1–7 documentés

**Spec complète :** [s3-5b-docker-documentation.md](../../epic-10-quality-gate/s3-5b-docker-documentation.md)

---

## Résumé Sprint 7

**Total Points :** 17
**Estimation effort :** 2 semaines (Amelia backend/devops + Sally frontend)

| Story | Points | Assigné | Type | Dépend de |
|-------|--------|---------|------|-----------|
| S8.1 Backend agrégats | 4 | Amelia | Backend | S1.2, S2.8, S3.1 |
| S8.2 Frontend stats | 4 | Sally | Frontend | S8.1 |
| S9.1 API filtrage avancé | 5 | Amelia | Backend | S6.1 |
| S3.5b Docker + Docs | 4 | Amelia | DevOps | Sprints 1–6 complets |

**Parallélisation :**
- S8.1 et S9.1 peuvent démarrer simultanément (Amelia, indépendants)
- S8.2 démarre dès S8.1 contrats API validés (J2)
- S3.5b démarre après S9.1 (J4–J5) ou en parallèle dès J3

**État :** Backlog (démarre après Sprint 6)
