# 🎯 Guide d'Utilisation: Accéder à Vos Uploads CSV

## Bonne nouvelle! 2 façons simples d'accéder à vos uploads.

---

## 🚀 Solution 1: Accès Rapide (RECOMMANDÉE)

### ⚡ **Le plus simple - Un clic!**

**Étapes:**

1️⃣ Allez à **Domain Management**
```
http://localhost:3000/domains
```

2️⃣ Trouvez votre Domain dans le tableau

3️⃣ Cliquez le bouton **📊 "View Uploaded Datasets"** (en gris)
```
Tableau                    Actions
┌─────────────────────────┬────────────────────────┐
│ Domain Name             │ 📊  ☁️  ✏️  🗑️        │
│ My Domain               │ ▲
│                         │ Cliquez ici!
└─────────────────────────┴────────────────────────┘
```

4️⃣ Une modal s'ouvre montrant **TOUS vos uploads**!

### ✅ Ce que vous verrez:

```
┌──────────────────────────────────────────────────┐
│ 📊 Uploaded Datasets                    [↻] [×]  │
├──────────────────────────────────────────────────┤
│ Domain: Mon Domain (3 datasets)                  │
│ ℹ️ Tous les fichiers CSV uploadés                │
├──────────────────────────────────────────────────┤
│ TABLEAU:                                         │
│ ┌─────────────────────────────────────────────┐  │
│ │ Name        | Rows | Cols | Status | Date  │  │
│ ├─────────────────────────────────────────────┤  │
│ │ data_1.csv  | 1500 |  5   | Active | 01/03 │  │
│ │ 234 KB      |      |      |        | 10:00 │  │
│ ├─────────────────────────────────────────────┤  │
│ │ data_2.csv  | 2000 |  8   | Active | 28/02 │  │
│ │ 567 KB      |      |      |        | 14:30 │  │
│ └─────────────────────────────────────────────┘  │
├──────────────────────────────────────────────────┤
│                                    [ Close ]     │
└──────────────────────────────────────────────────┘
```

### 🎮 Dans la modal, vous pouvez:

| Action | Comment |
|--------|---------|
| **Voir les données** | Cliquer "View" sur un dataset |
| **Supprimer** | Cliquer "Delete" sur un dataset |
| **Recharger** | Cliquer le bouton ↻ Refresh |

---

## 📤 Solution 2: Pendant un Upload

### Si vous venez d'uploader un CSV:

1️⃣ Cliquer **"Upload CSV"** sur un Domain

2️⃣ Passer les étapes:
- 📁 Drag & Drop le CSV
- 👁️ Voir l'aperçu
- ✓ Confirmer la configuration

3️⃣ **À la fin**, vous verrez automatiquement:
```
✅ Configuration Saved Successfully!

📊 Configured Columns (5):
┌────────────────────────────────┐
│ Column   | Type   | Confidence │
├────────────────────────────────┤
│ ID       | Number | 100%  🟢   │
│ Name     | Text   | 95%   🟢   │
│ Email    | Email  | 87%   🟢   │
│ Created  | Date   | 92%   🟢   │
│ Status   | Categ. | 78%   🟡   │
└────────────────────────────────┘

🆕 📊 LISTE DE VOS UPLOADS
┌────────────────────────────────┐
│ Tous vos fichiers uploadés     │
│                                │
│ 3 datasets dans ce Domain      │
└────────────────────────────────┘
```

4️⃣ Voir **tous vos anciens uploads** directement!

---

## 💡 Procédures Communes

### Procédure 1: Vérifier un Upload

**Question:** "Est-ce que mon CSV a bien uploadé?"

**Réponse:**
```
1. Aller à Domain Management
2. Cliquer 📊 View Datasets
3. Chercher le fichier dans la liste
4. Si présent = ✅ Upload réussi
```

### Procédure 2: Voir les Données

**Question:** "Je veux revoir les données de mon CSV"

**Réponse:**
```
1. Cliquer 📊 View Datasets
2. Trouver le dataset
3. Cliquer le bouton "View"
4. → DataViewer s'ouvre avec les données
5. Filtrer, trier, paginer, exporter
```

### Procédure 3: Nettoyer les Vieux Uploads

**Question:** "Comment supprimer les anciens uploads?"

**Réponse:**
```
1. Cliquer 📊 View Datasets
2. Cliquer "Delete" sur l'upload à supprimer
3. Confirmer la suppression
4. ✅ Supprimé! Liste se met à jour automat.
```

### Procédure 4: Upload Rapide

**Question:** "Comment uploader un nouveau CSV rapidement?"

**Réponse:**
```
1. Aller à Domain Management
2. Cliquer ☁️ "Upload CSV" (2e bouton)
3. Sélectionner fichier
4. Voir l'aperçu
5. Confirmer
6. ✅ Done! Voir l'historique automatiquement
```

---

## 🎯 Cheat Sheet Rapide

### Souris Survolée sur les Boutons:

```
┌─────────────────────────────────────────────┐
│ Actions pour chaque Domain:                 │
├─────────────────────────────────────────────┤
│ 📊 = View Uploaded Datasets (NOUVEAU!)      │
│ ☁️ = Upload New CSV                         │
│ ✏️ = Edit Domain Details                    │
│ 🗑️ = Delete Domain                         │
└─────────────────────────────────────────────┘
```

### Raccourcis Clavier:

```
F5 = Rafraîchir la page
Esc = Fermer les modals
Tab = Naviguer entre boutons
```

---

## ✨ Ce Qui Est Nouveau

### Avant (S2.5 - Phase 1)
❌ Pas d'accès aux uploads  
❌ Pas de liste d'historique  
❌ Passer par upload pour voir les données  

### Maintenant (S2.5 - Phase 2)  
✅ Accès direct avec bouton 📊  
✅ Liste complète de tous les uploads  
✅ View/Delete datasets facilement  
✅ Historique visible après chaque upload  

---

## 🔧 Troubleshooting

### "Je ne vois pas le bouton 📊"
**Solution:**
```
1. Rafraîchir la page: F5
2. Vérifier que le Backend est lancé (http://localhost:8080)
3. Aller à http://localhost:3000/domains et réessayer
```

### "La liste ne charge pas"
**Solution:**
```
1. Cliquer le bouton ↻ Refresh dans la modal
2. Vérifier la Console (F12 → Console) pour erreurs
3. Relancer le Frontend: npm start
```

### "Je vois une erreur 404"
**Solution:**
```
1. Vérifier que le Backend fonctionne: 
   curl http://localhost:8080/api/domains/1/data-sets
2. Relancer le Backend
3. Si toujours erreur, contacter l'équipe
```

### "La modal ne s'ouvre pas"
**Solution:**
```
1. Vérifier que JavaScript est activé
2. Ouvrir Console (F12) pour voir erreurs
3. Essayer un autre navigateur
4. Rafraîchir complète: Ctrl+Shift+R
```

---

## 📊 Informations Affichées

### Dans la Modal "View Datasets":

```
┌────────────────────────────────────────┐
│ Colonne              | Description     │
├────────────────────────────────────────┤
│ Dataset Name         | Nom du fichier  │
│ Size                 | Taille (B/KB/MB)│
│ Rows                 | # de lignes     │
│ Columns              | # de colonnes   │
│ Status               | Actif/Échoué    │
│ Created At           | Date/Heure      │
│ Actions (View/Delete)| Boutons d'action│
└────────────────────────────────────────┘
```

---

## 🚀 Pour Aller Plus Loin

### Intégrations Possibles:
- View Dataset → Voir dans DataViewer
- Exporter Dataset → CSV, JSON, Excel
- Dupliquer Dataset → Copier configuration
- Planifier Génération → À partir des données

---

## 📞 Questions Fréquentes

**Q: Où mes données sont-elles stockées?**  
R: Sur le serveur Backend (base de données)

**Q: Combien de temps les uploads sont conservés?**  
R: Jusqu'à suppression manuelle

**Q: Puis-je exporter les données?**  
R: Oui! Cliquer "View" puis "Export" dans DataViewer

**Q: Le bouton 📊 apparaît pour tous les Domains?**  
R: Oui, pour chaque Domain du tableau

**Q: La liste se met à jour automatiquement?**  
R: Oui après un upload ou après cliquer Refresh

---

## 🎓 Résumé

**TL;DR:**
```
Domain Management → 📊 View Datasets → Voir/Gérer uploads
```

🎉 **C'est aussi simple que ça!**

---

**Document Version:** 2.0  
**Date:** 01/03/2026  
**Statut:** Production Ready ✅
