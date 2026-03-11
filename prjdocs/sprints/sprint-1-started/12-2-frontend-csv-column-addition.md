# Histoire Utilisateur 12-2 : Interface Frontend pour Ajout de Colonne lors de l'Upload CSV

## Contexte
En tant qu'utilisateur, je veux une interface intuitive pour ajouter une colonne lors de l'upload CSV directement depuis le modal d'upload.

## Critères d'Acceptation
- [ ] Modal d'upload CSV inclut une section "Ajouter Colonne"
- [ ] Champs : nom de colonne, type (dropdown), valeur par défaut (optionnel)
- [ ] Validation côté client des entrées
- [ ] Aperçu des données avec la nouvelle colonne avant upload
- [ ] Tests Jest pour les composants (couverture > 80%)
- [ ] Accessibilité WCAG 2.1 AA

## Estimation
3 points de complexité (frontend)

## Statut
Ready for dev

## Assigné à
Sally (UX Designer) puis Amelia (Developer Agent)

## Notes Techniques
- Étendre UploadModal avec un panneau d'ajout de colonne
- Intégrer avec ColumnTypeSelector component existant
- Utiliser React Hook Form pour validation