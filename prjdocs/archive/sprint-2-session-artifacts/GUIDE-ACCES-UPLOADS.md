# Guide: Accéder aux Uploads de CSV

## 🎯 Objectif
Afficher l'historique des fichiers CSV uploadés dans votre Domain

---

## 📍 Chemin d'Accès (3 étapes)

### Étape 1: Aller à la Page Domains
```
1. Cliquez sur "Domain Management" dans le menu
   OU accédez directement à: http://localhost:3000/domains
```

### Étape 2: Sélectionner un Domain
```
2. Vous verrez une liste de vos Domains
3. Trouvez le Domain pour lequel vous voulez voir les uploads
```

### Étape 3: Cliquer sur "Upload CSV"
```
4. Pour chaque Domain, il y a un bouton "Upload CSV" (avec une icône ☁️)
5. Cliquez sur ce bouton
```

**Voilà! Le Dialog "Upload CSV Data" s'ouvre**

---

## 🔄 Dans le Dialog Upload

### Nouvelle Interface:

```
┌─────────────────────────────────────────┐
│     Upload CSV Data Dialog              │
├─────────────────────────────────────────┤
│                                         │
│  📁 Drag & Drop CSV File Ici            │
│     (ou cliquez pour sélectionner)      │
│                                         │
│  Après sélection:                       │
│  ✓ Aperçu des données CSV               │
│  ✓ Détection des types de colonnes      │
│                                         │
│  Après confirmation:                    │
│  ✓ Résumé de l'upload                   │
│  ✓ Colonnes détectées                   │
│                                         │
│  🆕 📊 LISTE DES UPLOADS ← Nouveau!     │
│     (Voir tous les uploads de ce Domain)│
│                                         │
│  Boutons:                               │
│  [ ← Retour ]  [ Procéder → ]           │
│                                         │
└─────────────────────────────────────────┘
```

---

## 📊 Ce que vous Verrez dans la Liste des Uploads

### Tableau avec Colonnes:
| Colonne | Description |
|---------|-------------|
| **Dataset Name** | Nom du fichier CSV uploadé + taille |
| **Rows** | Nombre de lignes dans le CSV |
| **Columns** | Nombre de colonnes détectées |
| **Status** | État du dataset (Actif/Échoué) |
| **Created** | Date et heure de l'upload |
| **Actions** | View / Delete |

### Exemple:
```
Dataset Name          | Rows | Columns | Status | Created            | Actions
─────────────────────────────────────────────────────────────────────────────
data_2026_01.csv      | 1500 |    5    | Active | 01/03/2026 10:00  | View  Delete
234.56 KB             |      |         |        |                   |
─────────────────────────────────────────────────────────────────────────────
customers_list.csv    | 3200 |    8    | Active | 28/02/2026 14:30  | View  Delete
512 KB                |      |         |        |                   |
─────────────────────────────────────────────────────────────────────────────
```

---

## 🎮 Actions Disponibles

### 1️⃣ Bouton "View"
```
→ Ouvre le Data Viewer
→ Vous pouvez voir les données du CSV
→ Filtrer, trier, paginer
→ Exporter en CSV, JSON
```

### 2️⃣ Bouton "Delete"
```
→ Supprime le dataset
→ Confirmation avant suppression
→ Liste se met à jour automatiquement
```

### 3️⃣ Bouton "Refresh"
```
→ Recharge la liste des uploads
→ Voir les nouveaux uploads immédiatement
```

---

## ✨ Features de la Liste d'Upload

### Affichages:
- ✅ **Chargement**: Spinner while données se chargent
- ✅ **Aucun Upload**: Message "No datasets uploaded yet"
- ✅ **Erreur API**: Message d'erreur + bouton Retry
- ✅ **Succès**: Liste complète avec tous les uploads

### Format des Données:
- ✅ **Taille Fichier**: Convertie automatiquement (B, KB, MB, GB)
- ✅ **Date/Heure**: Format locale (ex: 01/03/2026 10:00:00)
- ✅ **Nombre**: Séparation claire rows vs columns

### Mise à Jour:
- ✅ **Auto-refresh**: Après un upload, liste se mets à jour
- ✅ **Manuel Refresh**: Cliquez sur le bouton "Refresh"
- ✅ **Suppression**: Liste se met à jour après delete

---

## 🚀 Cas d'Usage

### Scénario 1: Reporter un Problème
```
1. Aller à /domains
2. Cliquer "Upload CSV" pour un Domain
3. Voir la liste de tous les uploads précédents
4. Demander: "Pourquoi cet upload a échoué?"
→ Vous pouvez voir le statut exact
```

### Scénario 2: Réutiliser un CSV
```
1. Aller à /domains
2. Cliquer "Upload CSV"
3. Voir dans l'historique les anciens uploads
4. Cliquer "View" pour revoir les données
5. Exporter au format voulu (CSV, JSON)
```

### Scénario 3: Nettoyer les Anciens Uploads
```
1. Aller à /domains
2. Cliquer "Upload CSV"
3. Cliquer "Delete" sur les vieux datasets
4. Libérer l'espace disque
```

---

## ⚙️ Intégration API

### Données Affichées:
L'application utilise l'API Backend:
```
GET /api/domains/{domainId}/data-sets

Récupère:
- Nom du fichier
- Nombre de lignes/colonnes
- Statut
- Date de création
- Taille du fichier
```

---

## 🐛 Troubleshooting

### "Je ne vois pas de liste d'uploads"
```
✓ Vérifiez que le Domain est sélectionné
✓ Vérifiez que le Backend est en cours d'exécution
✓ Vérifiez la console du navigateur (F12) pour les erreurs
✓ Essayez de cliquer sur "Refresh"
```

### "Me liste ne se met pas à jour après un upload"
```
✓ Attendez quelques secondes
✓ Cliquez sur "Refresh"
✓ Fermez et réouvrez le Dialog
```

### "Erreur lors de la suppression"
```
✓ Confirmez que vous êtes connecté
✓ Vérifiez que le Backend répond
✓ Essayez de rafraîchir la page
```

---

## 📱 Accès Mobile

```
L'interface est optimisée pour mobile:
✅ Le Dialog s'affiche en plein écran
✅ La liste est scrollable verticalement
✅ Les boutons sont touch-friendly
✅ Les dates sont formatées pour l'espace limité
```

---

## 🔐 Sécurité

```
✓ Suppression possible uniquement par le propriétaire du Domain
✓ Confirmation requise avant suppression
✓ Les données ne sont visibles que pour votre Domain
✓ Les uploads sont soft-deleted (conservés en base de données)
```

---

## 📞 Obtenir de l'Aide

### Si la liste ne s'affiche pas:
1. Vérifiez qu'il y a des uploads dans ce Domain
2. Vérifiez que le Backend est accessible (`curl http://localhost:8080/api/domains/1/data-sets`)
3. Ouvrez la Console du Navigateur (F12 → Console)
4. Vérifiez les messages d'erreur

### Logs Utiles:
```javascript
// Dans la console du navigateur (F12):
console.log('Datasets:', window.localStorage);
Network tab → Vérifier les appels API
```

---

**Prêt à voir vos uploads? 🎉**

Allez à → [http://localhost:3000/domains](http://localhost:3000/domains)
