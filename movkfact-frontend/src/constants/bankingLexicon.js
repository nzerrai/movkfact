/**
 * Lexique bancaire — suggestions de noms de colonnes avec type recommandé.
 * Utilisé dans le wizard de création manuelle de dataset (ColumnRow autocomplete).
 *
 * Chaque entrée : { label: string, type: ColumnType, group: string }
 */
export const BANKING_LEXICON = [
  // ── Identité client ─────────────────────────────────────────
  { label: 'Prénom',               type: 'FIRST_NAME',    group: 'Identité client' },
  { label: 'Nom',                  type: 'LAST_NAME',     group: 'Identité client' },
  { label: 'Civilité',             type: 'GENDER',        group: 'Identité client' },
  { label: 'Genre',                type: 'GENDER',        group: 'Identité client' },
  { label: 'Date de naissance',    type: 'BIRTH_DATE',    group: 'Identité client' },
  { label: 'Email',                type: 'EMAIL',         group: 'Identité client' },
  { label: 'Téléphone',            type: 'PHONE',         group: 'Identité client' },
  { label: 'Téléphone mobile',     type: 'PHONE',         group: 'Identité client' },
  { label: 'Nationalité',          type: 'COUNTRY',       group: 'Identité client' },
  { label: 'Pays de résidence',    type: 'COUNTRY',       group: 'Identité client' },

  // ── Adresse ──────────────────────────────────────────────────
  { label: 'Adresse',              type: 'ADDRESS',       group: 'Adresse' },
  { label: 'Adresse ligne 1',      type: 'ADDRESS',       group: 'Adresse' },
  { label: 'Adresse ligne 2',      type: 'ADDRESS',       group: 'Adresse' },
  { label: 'Code postal',          type: 'ZIP_CODE',      group: 'Adresse' },
  { label: 'Ville',                type: 'CITY',          group: 'Adresse' },
  { label: 'Pays',                 type: 'COUNTRY',       group: 'Adresse' },

  // ── Référentiels client ────────────────────────────────────
  { label: 'Identifiant client',   type: 'UUID',          group: 'Référentiel client' },
  { label: 'Numéro client',        type: 'UUID',          group: 'Référentiel client' },
  { label: 'Identifiant',          type: 'UUID',          group: 'Référentiel client' },
  { label: 'Référence client',     type: 'UUID',          group: 'Référentiel client' },
  { label: 'Segment client',       type: 'TEXT',          group: 'Référentiel client' },
  { label: 'Statut client',        type: 'TEXT',          group: 'Référentiel client' },
  { label: 'Catégorie client',     type: 'TEXT',          group: 'Référentiel client' },
  { label: 'Date d\'ouverture',    type: 'DATE',          group: 'Référentiel client' },
  { label: 'Date de clôture',      type: 'DATE',          group: 'Référentiel client' },
  { label: 'Entreprise',           type: 'COMPANY',       group: 'Référentiel client' },
  { label: 'Société',              type: 'COMPANY',       group: 'Référentiel client' },

  // ── Compte bancaire ────────────────────────────────────────
  { label: 'Numéro de compte',     type: 'ACCOUNT_NUMBER', group: 'Compte bancaire' },
  { label: 'IBAN',                 type: 'ACCOUNT_NUMBER', group: 'Compte bancaire' },
  { label: 'BIC',                  type: 'TEXT',           group: 'Compte bancaire' },
  { label: 'Type de compte',       type: 'TEXT',           group: 'Compte bancaire' },
  { label: 'Devise',               type: 'CURRENCY',       group: 'Compte bancaire' },
  { label: 'Devise du compte',     type: 'CURRENCY',       group: 'Compte bancaire' },
  { label: 'Solde',                type: 'AMOUNT',         group: 'Compte bancaire' },
  { label: 'Solde disponible',     type: 'AMOUNT',         group: 'Compte bancaire' },
  { label: 'Plafond',              type: 'AMOUNT',         group: 'Compte bancaire' },
  { label: 'Limite de crédit',     type: 'AMOUNT',         group: 'Compte bancaire' },
  { label: 'Autorisation de découvert', type: 'AMOUNT',   group: 'Compte bancaire' },

  // ── Transaction ────────────────────────────────────────────
  { label: 'Référence transaction', type: 'UUID',         group: 'Transaction' },
  { label: 'Numéro de transaction', type: 'UUID',         group: 'Transaction' },
  { label: 'Date de transaction',   type: 'DATE',         group: 'Transaction' },
  { label: 'Date de valeur',        type: 'DATE',         group: 'Transaction' },
  { label: 'Date d\'opération',     type: 'DATE',         group: 'Transaction' },
  { label: 'Montant',               type: 'AMOUNT',       group: 'Transaction' },
  { label: 'Montant débit',         type: 'AMOUNT',       group: 'Transaction' },
  { label: 'Montant crédit',        type: 'AMOUNT',       group: 'Transaction' },
  { label: 'Débit',                 type: 'AMOUNT',       group: 'Transaction' },
  { label: 'Crédit',                type: 'AMOUNT',       group: 'Transaction' },
  { label: 'Libellé',               type: 'TEXT',         group: 'Transaction' },
  { label: 'Motif du virement',     type: 'TEXT',         group: 'Transaction' },
  { label: 'Type d\'opération',     type: 'TEXT',         group: 'Transaction' },
  { label: 'Canal',                 type: 'TEXT',         group: 'Transaction' },
  { label: 'Statut transaction',    type: 'TEXT',         group: 'Transaction' },

  // ── Carte bancaire ─────────────────────────────────────────
  { label: 'Numéro de carte',       type: 'TEXT',         group: 'Carte bancaire' },
  { label: 'Date d\'expiration',    type: 'DATE',         group: 'Carte bancaire' },
  { label: 'Type de carte',         type: 'TEXT',         group: 'Carte bancaire' },
  { label: 'Plafond carte',         type: 'AMOUNT',       group: 'Carte bancaire' },

  // ── Crédit / Prêt ──────────────────────────────────────────
  { label: 'Numéro de prêt',        type: 'UUID',         group: 'Crédit / Prêt' },
  { label: 'Taux d\'intérêt',       type: 'PERCENTAGE',   group: 'Crédit / Prêt' },
  { label: 'TAEG',                  type: 'PERCENTAGE',   group: 'Crédit / Prêt' },
  { label: 'Durée du prêt (mois)',  type: 'INTEGER',      group: 'Crédit / Prêt' },
  { label: 'Mensualité',            type: 'AMOUNT',       group: 'Crédit / Prêt' },
  { label: 'Capital restant dû',    type: 'AMOUNT',       group: 'Crédit / Prêt' },
  { label: 'Montant emprunté',      type: 'AMOUNT',       group: 'Crédit / Prêt' },
  { label: 'Date de début',         type: 'DATE',         group: 'Crédit / Prêt' },
  { label: 'Date de fin',           type: 'DATE',         group: 'Crédit / Prêt' },

  // ── Risque / Conformité ────────────────────────────────────
  { label: 'Score de risque',       type: 'INTEGER',      group: 'Risque / Conformité' },
  { label: 'Niveau de risque',      type: 'TEXT',         group: 'Risque / Conformité' },
  { label: 'Score crédit',          type: 'INTEGER',      group: 'Risque / Conformité' },
  { label: 'Résultat KYC',          type: 'BOOLEAN',      group: 'Risque / Conformité' },
  { label: 'Statut AML',            type: 'TEXT',         group: 'Risque / Conformité' },
  { label: 'Numéro de pièce',       type: 'TEXT',         group: 'Risque / Conformité' },
  { label: 'Type de document',      type: 'TEXT',         group: 'Risque / Conformité' },
  { label: 'Date d\'émission pièce', type: 'DATE',        group: 'Risque / Conformité' },

  // ── Frais / Tarification ───────────────────────────────────
  { label: 'Frais',                 type: 'AMOUNT',       group: 'Frais / Tarification' },
  { label: 'Commission',            type: 'PERCENTAGE',   group: 'Frais / Tarification' },
  { label: 'Taux de commission',    type: 'PERCENTAGE',   group: 'Frais / Tarification' },
  { label: 'Cotisation annuelle',   type: 'AMOUNT',       group: 'Frais / Tarification' },

  // ── Technique ─────────────────────────────────────────────
  { label: 'Identifiant unique',    type: 'UUID',         group: 'Technique' },
  { label: 'Date de création',      type: 'DATE',         group: 'Technique' },
  { label: 'Date de mise à jour',   type: 'DATE',         group: 'Technique' },
  { label: 'Actif',                 type: 'BOOLEAN',      group: 'Technique' },
  { label: 'Activé',                type: 'BOOLEAN',      group: 'Technique' },
  { label: 'Version',               type: 'INTEGER',      group: 'Technique' },
  { label: 'Commentaire',           type: 'TEXT',         group: 'Technique' },
  { label: 'Note',                  type: 'TEXT',         group: 'Technique' },
];
