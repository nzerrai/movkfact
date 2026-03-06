---
title: Design RGPD — Module d'Anonymisation
date: 06 mars 2026
sprint: 8
status: implemented
author: Winston (Architect) + Paige (Tech-writer)
---

# Design RGPD — Module d'Anonymisation MockFact

## Contexte et Objectif

Le module d'anonymisation permet de traiter des fichiers de donnees reelles (CSV/JSON) afin que le fichier resultant ne permette pas la re-identification des personnes concernees, meme en cas de fuite.

**Exigence fondamentale :** Les transformations doivent etre irreversibles. Meme un attaquant disposant du code source complet ne doit pas pouvoir retrouver les valeurs originales.

---

## Decisions architecturales

### Decision 1 — Pas de hachage, donnees synthetiques

**Probleme :** SHA-256 avec sel produit des valeurs hexadecimales non-representativas (un nom de personne deviendrait "a3f9c2d1b4e8" — format incohérent avec une colonne de texte lisible).

**Decision :** Remplacer chaque valeur par une donnee synthetique du meme type structurel :
- Prenom → autre prenom (liste interne de 24 prenoms)
- Email → email fictif (prenom.nom{random}@domaine-fictif)
- IBAN → IBAN fictif complet regenere (FR + 25 chiffres aleatoires)
- Integer → entier du meme ordre de grandeur

### Decision 2 — IBAN et IP integralement regeneres

**Exigence Nouredine (explicite) :** "IBAN doit etre completement regenere aleatoirement" + "Idem pour les IP"

**Implementation :**
```java
// IBAN : FR + 25 chiffres aleatoires
private String generateIban() {
    StringBuilder sb = new StringBuilder("FR");
    for (int i = 0; i < 25; i++) sb.append(RANDOM.nextInt(10));
    return sb.toString();
}

// IP : 4 octets completement independants
private String generateIp() {
    return RANDOM.nextInt(223) + 1 + "." + RANDOM.nextInt(256) + "."
        + RANDOM.nextInt(256) + "." + (RANDOM.nextInt(254) + 1);
}
```

### Decision 3 — Sel runtime jamais persiste

**Probleme :** Un sel stocke en base ou en config pourrait etre recupere par un attaquant et utilise pour recalculer les valeurs originales (attaque par dictionnaire).

**Decision :** Le sel est genere une seule fois au demarrage de la JVM (`UUID.randomUUID()`) et n'est jamais :
- Logue (pas dans les logs applicatifs)
- Expose (pas dans les API responses)
- Persiste (renouvele a chaque redemarrage)

**Consequence :** Si le sel etait encore utilise (il a ete remplace par des donnees synthetiques), chaque redemarrage rendrait les valeurs precedentes non-reproductibles.

### Decision 4 — Detection du format reel pour TEXT et default

**Probleme :** Une colonne declaree TEXT peut contenir des entiers ("42", "1500") ou des decimaux ("3.14"). Remplacer par du texte libre briserait la coherence structurelle du fichier.

**Decision :** `generateMatchingType(String original)` detecte le type reel de la valeur :
1. Tente `Long.parseLong()` → genere un entier du meme ordre de grandeur
2. Tente `Double.parseDouble()` → genere un decimal avec le meme nombre de decimales
3. Detecte booleen (true/false, oui/non, 1/0) → genere un booleen dans le meme format
4. Fallback → deux mots aleatoires

### Decision 5 — BIRTH_DATE : generalisation par l'annee

**Principe RGPD :** La generalisation reduit la precision d'une donnee sans la supprimer. Une date de naissance precise est un quasi-identifiant (combinee avec genre + ville, elle peut re-identifier 87% des individus aux USA - etude Sweeney).

**Decision :** BIRTH_DATE → annee uniquement (ex: "1985-04-12" → "1985")

### Decision 6 — DATE : decalage aleatoire

**Decision :** Les dates generiques (non naissance) sont decalees de +/- 180 jours aleatoirement, en preservant le format original (yyyy-MM-dd, dd/MM/yyyy, etc.)

---

## Garanties RGPD implementees

| Garantie | Statut | Mecanisme |
|----------|--------|-----------|
| Irreversibilite | Oui | Donnees synthetiques independantes |
| Pas de stockage original | Oui | Streaming — jamais persiste |
| Sel non-expose | Oui | Runtime-only, non logue |
| Coherence structurelle | Oui | Detection du format reel |
| Re-identification impossible | Oui | Quasi-identifiants generalises |

---

## Flux de traitement

```
Fichier input (CSV/JSON)
        |
        v
POST /api/anonymize/inspect
  -> Lecture entetes uniquement
  -> Retourne liste colonnes
        |
        v
[Utilisateur configure les colonnes]
  - Coche anonymiser (oui/non)
  - Choisit le type ColumnType
  - Choisit le domaine + nom dataset
        |
        v
POST /api/anonymize/save
  -> Lecture ligne par ligne
  -> anonymizeValue(valeur, columnType) pour chaque colonne cochee
  -> Accumulation List<Map<String, Object>>
  -> Serialisation JSON
  -> Sauvegarde DataSet entity
  -> Retourne { id, name, rowCount, columnCount }
        |
        v
Dataset disponible dans MockFact
  -> Data Viewer
  -> Telechargement CSV/JSON
  -> Reutilisation batch
```

---

## Limites connues

1. **Volume fichier :** Le endpoint /save charge tout le fichier en memoire pour construire la liste de lignes. Pour des fichiers > 100MB, preferer /process (streaming vers OutputStream).

2. **Donnees structurees imbriquees (JSON) :** Seules les colonnes du premier niveau des objets JSON sont anonymisees. Les objets/tableaux imbriques sont traites comme du texte.

3. **Validation metier du format IBAN :** L'IBAN genere (FR + 25 chiffres) n'est pas valide au sens bancaire (pas de cle de controle RIB). C'est intentionnel — il ne doit pas etre utilisable.

---

## References

- Code : `src/main/java/com/movkfact/service/AnonymizationService.java`
- Code : `src/main/java/com/movkfact/controller/AnonymizationController.java`
- Epic : [Epic 11 Anonymisation RGPD](../epics/epic-11-anonymisation-rgpd/11-1-anonymisation-rgpd.md)
- Sprint : [Sprint 8 Stories S8.2 + S8.3](../sprints/sprint-8-started/stories.md)
