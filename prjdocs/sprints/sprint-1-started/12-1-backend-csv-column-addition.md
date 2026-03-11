# Histoire Utilisateur 12-1 : Support Backend pour Ajout de Colonne lors de l'Upload CSV

## Contexte
En tant qu'utilisateur, je veux pouvoir ajouter une colonne supplémentaire lors de l'upload d'un fichier CSV pour enrichir les données avec des informations personnalisées.

## Critères d'Acceptation
- [ ] L'API d'upload CSV accepte un paramètre pour définir une nouvelle colonne (nom, type, valeur par défaut)
- [ ] La colonne est ajoutée à toutes les lignes du dataset importé
- [ ] Validation des types de données pour la nouvelle colonne
- [ ] Tests unitaires pour la fonctionnalité (couverture > 80%)
- [ ] Documentation API mise à jour

## Estimation
5 points de complexité (backend)

## Statut
Ready for dev

## Assigné à
Amelia (Developer Agent)

## Notes Techniques
- Étendre DataUploadService pour gérer l'ajout de colonnes
- Utiliser ColumnType enum pour validation
- Intégrer avec DataGeneratorService pour génération de valeurs par défaut si nécessaire