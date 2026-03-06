---
epic: 11
title: Anonymisation RGPD
sprint: 8
status: done
stories: [S8.2, S8.3]
---

# Epic 11 — Anonymisation RGPD

## Objectif

Permettre l'anonymisation irreversible de fichiers de donnees (CSV/JSON) de toute taille, en respectant les exigences du Reglement General sur la Protection des Donnees (RGPD). Le resultat est sauvegarde comme dataset MockFact dans un domaine choisi par l'utilisateur.

## Perimetre fonctionnel

| Fonctionnalite | Statut |
|----------------|--------|
| Upload fichier CSV/JSON et inspection des colonnes | Done |
| Selection des colonnes a anonymiser avec strategie par type | Done |
| Transformations irreversibles par ColumnType | Done |
| Sauvegarde du resultat en dataset par domaine | Done |
| Lien direct vers le dataset cree | Done |

## Architecture

### Backend

**Endpoint d'inspection**
```
POST /api/anonymize/inspect
  multipart: file, format (csv|json)
  -> { columns: [string] }
```

**Endpoint de traitement (telechargement direct)**
```
POST /api/anonymize/process
  multipart: file, format, config (JSON)
  -> fichier anonymise en streaming
```

**Endpoint de sauvegarde en dataset**
```
POST /api/anonymize/save
  multipart: file, format, config (JSON), domainId, datasetName
  -> { id, name, domainId, rowCount, columnCount }
```

**Service `AnonymizationService`**

Garanties RGPD implementees :
- Aucune donnee originale persistee cote serveur
- Sel runtime (`UUID.randomUUID()`) genere au demarrage, jamais log, jamais expose
- Transformations irréversibles meme avec acces complet au code source
- Pas de hachage : remplacement par des donnees synthetiques du meme type

### Frontend

**`AnonymizationPage.jsx`** — Stepper 3 etapes :
1. Upload fichier (CSV/JSON auto-detecte)
2. Tableau de configuration colonnes + selecteur domaine + nom dataset
3. Confirmation avec lien vers le dataset cree

## Strategies d'anonymisation par type

| ColumnType | Strategie |
|------------|-----------|
| FIRST_NAME | Prenom synthetique (liste interne) |
| LAST_NAME | Nom synthetique (liste interne) |
| EMAIL | prenom.nom{random}@domaine-fictif |
| PHONE | Numero mobile francais synthetique (0[67]XXXXXXXX) |
| ADDRESS | Numero + Rue + Ville (tous synthetiques) |
| GENDER | Conserve (non-identifiant direct) |
| BIRTH_DATE | Annee uniquement (generalisation RGPD) |
| DATE | Decalage aleatoire +/- 180 jours (format original preserv) |
| ACCOUNT_NUMBER | IBAN fictif regenere : FR + 25 chiffres aleatoires |
| IP_ADDRESS | 4 octets completement regeneres |
| UUID | UUID v4 aleatoire |
| URL | URL fictive avec domaine et chemin aleatoires |
| EMAIL | Email fictif (prenom.nom@domaine) |
| COMPANY | Entreprise synthetique (liste interne) |
| CITY | Ville synthetique (liste interne) |
| COUNTRY | Pays synthetique (liste interne) |
| ZIP_CODE | Code postal aleatoire |
| INTEGER | Entier du meme ordre de grandeur |
| DECIMAL / AMOUNT | Decimal avec meme nombre de decimales |
| PERCENTAGE | Float [0.0, 100.0] |
| BOOLEAN | Booleen aleatoire dans le meme format (true/false, oui/non, 1/0) |
| CURRENCY | Conserve (code devise non-identifiant) |
| TIMEZONE | Conserve (non-identifiant) |
| TEXT | Detection du format reel (int/decimal/bool/texte) puis generation adequate |
| ENUM | Valeur piochee dans une liste generique |
| default | Detection du format reel de la valeur originale |

## Decisions de conception cles

1. **Pas de hachage** : Le SHA-256 etait initialement prevu mais retire car il genere des valeurs non-representatives (hexadecimal pour des noms, des montants, etc.). Les donnees synthetiques respectent la meme typology.

2. **Detection de format pour TEXT/default** : Si une colonne de type TEXT contient des entiers, le remplacement sera aussi un entier — respect du format reel, pas seulement du type declare.

3. **IBAN et IP integralement regeneres** : Exigence explicite du client. Pas de derivation depuis la valeur originale.

4. **Sauvegarde en dataset** : Le resultat n'est pas simplement telecharge mais persiste dans MockFact, accessible depuis la vue Data Viewer, telechargeble en CSV/JSON, et reutilisable.

## References

- Sprint : [Sprint 8 Kickoff](../../sprints/sprint-8-started/kickoff.md)
- Stories : [S8.2 + S8.3](../../sprints/sprint-8-started/stories.md)
- Design RGPD : [rgpd-anonymization-design.md](../../planning/rgpd-anonymization-design.md)
- Code backend : `src/main/java/com/movkfact/service/AnonymizationService.java`
- Code backend : `src/main/java/com/movkfact/controller/AnonymizationController.java`
- Code frontend : `movkfact-frontend/src/pages/AnonymizationPage.jsx`
