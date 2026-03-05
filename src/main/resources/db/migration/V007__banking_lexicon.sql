-- V007 : Lexique bancaire — noms de colonnes suggérés pour le wizard de création manuelle
-- Chaque entrée : libellé affiché, type suggéré (ColumnType), groupe d'appartenance

CREATE TABLE banking_lexicon (
    id             BIGSERIAL PRIMARY KEY,
    label          VARCHAR(100) NOT NULL,
    suggested_type VARCHAR(50)  NOT NULL,
    lexicon_group  VARCHAR(100) NOT NULL
);

CREATE INDEX idx_banking_lexicon_group ON banking_lexicon(lexicon_group);

-- ── Identité client ────────────────────────────────────────────────────────────
INSERT INTO banking_lexicon (label, suggested_type, lexicon_group) VALUES
  ('Prénom',               'FIRST_NAME',     'Identité client'),
  ('Nom',                  'LAST_NAME',      'Identité client'),
  ('Civilité',             'GENDER',         'Identité client'),
  ('Genre',                'GENDER',         'Identité client'),
  ('Date de naissance',    'BIRTH_DATE',     'Identité client'),
  ('Email',                'EMAIL',          'Identité client'),
  ('Téléphone',            'PHONE',          'Identité client'),
  ('Téléphone mobile',     'PHONE',          'Identité client'),
  ('Nationalité',          'COUNTRY',        'Identité client'),
  ('Pays de résidence',    'COUNTRY',        'Identité client');

-- ── Adresse ───────────────────────────────────────────────────────────────────
INSERT INTO banking_lexicon (label, suggested_type, lexicon_group) VALUES
  ('Adresse',              'ADDRESS',        'Adresse'),
  ('Adresse ligne 1',      'ADDRESS',        'Adresse'),
  ('Adresse ligne 2',      'ADDRESS',        'Adresse'),
  ('Code postal',          'ZIP_CODE',       'Adresse'),
  ('Ville',                'CITY',           'Adresse'),
  ('Pays',                 'COUNTRY',        'Adresse');

-- ── Référentiel client ────────────────────────────────────────────────────────
INSERT INTO banking_lexicon (label, suggested_type, lexicon_group) VALUES
  ('Identifiant client',   'UUID',           'Référentiel client'),
  ('Numéro client',        'UUID',           'Référentiel client'),
  ('Identifiant',          'UUID',           'Référentiel client'),
  ('Référence client',     'UUID',           'Référentiel client'),
  ('Segment client',       'TEXT',           'Référentiel client'),
  ('Statut client',        'TEXT',           'Référentiel client'),
  ('Catégorie client',     'TEXT',           'Référentiel client'),
  ('Date d''ouverture',    'DATE',           'Référentiel client'),
  ('Date de clôture',      'DATE',           'Référentiel client'),
  ('Entreprise',           'COMPANY',        'Référentiel client'),
  ('Société',              'COMPANY',        'Référentiel client');

-- ── Compte bancaire ───────────────────────────────────────────────────────────
INSERT INTO banking_lexicon (label, suggested_type, lexicon_group) VALUES
  ('Numéro de compte',          'ACCOUNT_NUMBER', 'Compte bancaire'),
  ('IBAN',                      'ACCOUNT_NUMBER', 'Compte bancaire'),
  ('BIC',                       'TEXT',           'Compte bancaire'),
  ('Type de compte',            'TEXT',           'Compte bancaire'),
  ('Devise',                    'CURRENCY',       'Compte bancaire'),
  ('Devise du compte',          'CURRENCY',       'Compte bancaire'),
  ('Solde',                     'AMOUNT',         'Compte bancaire'),
  ('Solde disponible',          'AMOUNT',         'Compte bancaire'),
  ('Plafond',                   'AMOUNT',         'Compte bancaire'),
  ('Limite de crédit',          'AMOUNT',         'Compte bancaire'),
  ('Autorisation de découvert', 'AMOUNT',         'Compte bancaire');

-- ── Transaction ───────────────────────────────────────────────────────────────
INSERT INTO banking_lexicon (label, suggested_type, lexicon_group) VALUES
  ('Référence transaction', 'UUID',   'Transaction'),
  ('Numéro de transaction', 'UUID',   'Transaction'),
  ('Date de transaction',   'DATE',   'Transaction'),
  ('Date de valeur',        'DATE',   'Transaction'),
  ('Date d''opération',     'DATE',   'Transaction'),
  ('Montant',               'AMOUNT', 'Transaction'),
  ('Montant débit',         'AMOUNT', 'Transaction'),
  ('Montant crédit',        'AMOUNT', 'Transaction'),
  ('Débit',                 'AMOUNT', 'Transaction'),
  ('Crédit',                'AMOUNT', 'Transaction'),
  ('Libellé',               'TEXT',   'Transaction'),
  ('Motif du virement',     'TEXT',   'Transaction'),
  ('Type d''opération',     'TEXT',   'Transaction'),
  ('Canal',                 'TEXT',   'Transaction'),
  ('Statut transaction',    'TEXT',   'Transaction');

-- ── Carte bancaire ────────────────────────────────────────────────────────────
INSERT INTO banking_lexicon (label, suggested_type, lexicon_group) VALUES
  ('Numéro de carte',    'TEXT',   'Carte bancaire'),
  ('Date d''expiration', 'DATE',   'Carte bancaire'),
  ('Type de carte',      'TEXT',   'Carte bancaire'),
  ('Plafond carte',      'AMOUNT', 'Carte bancaire');

-- ── Crédit / Prêt ─────────────────────────────────────────────────────────────
INSERT INTO banking_lexicon (label, suggested_type, lexicon_group) VALUES
  ('Numéro de prêt',       'UUID',       'Crédit / Prêt'),
  ('Taux d''intérêt',      'PERCENTAGE', 'Crédit / Prêt'),
  ('TAEG',                 'PERCENTAGE', 'Crédit / Prêt'),
  ('Durée du prêt (mois)', 'INTEGER',    'Crédit / Prêt'),
  ('Mensualité',           'AMOUNT',     'Crédit / Prêt'),
  ('Capital restant dû',   'AMOUNT',     'Crédit / Prêt'),
  ('Montant emprunté',     'AMOUNT',     'Crédit / Prêt'),
  ('Date de début',        'DATE',       'Crédit / Prêt'),
  ('Date de fin',          'DATE',       'Crédit / Prêt');

-- ── Risque / Conformité ───────────────────────────────────────────────────────
INSERT INTO banking_lexicon (label, suggested_type, lexicon_group) VALUES
  ('Score de risque',        'INTEGER', 'Risque / Conformité'),
  ('Niveau de risque',       'TEXT',    'Risque / Conformité'),
  ('Score crédit',           'INTEGER', 'Risque / Conformité'),
  ('Résultat KYC',           'BOOLEAN', 'Risque / Conformité'),
  ('Statut AML',             'TEXT',    'Risque / Conformité'),
  ('Numéro de pièce',        'TEXT',    'Risque / Conformité'),
  ('Type de document',       'TEXT',    'Risque / Conformité'),
  ('Date d''émission pièce', 'DATE',    'Risque / Conformité');

-- ── Frais / Tarification ──────────────────────────────────────────────────────
INSERT INTO banking_lexicon (label, suggested_type, lexicon_group) VALUES
  ('Frais',               'AMOUNT',     'Frais / Tarification'),
  ('Commission',          'PERCENTAGE', 'Frais / Tarification'),
  ('Taux de commission',  'PERCENTAGE', 'Frais / Tarification'),
  ('Cotisation annuelle', 'AMOUNT',     'Frais / Tarification');

-- ── Technique ─────────────────────────────────────────────────────────────────
INSERT INTO banking_lexicon (label, suggested_type, lexicon_group) VALUES
  ('Identifiant unique',    'UUID',    'Technique'),
  ('Date de création',      'DATE',    'Technique'),
  ('Date de mise à jour',   'DATE',    'Technique'),
  ('Actif',                 'BOOLEAN', 'Technique'),
  ('Activé',                'BOOLEAN', 'Technique'),
  ('Version',               'INTEGER', 'Technique'),
  ('Commentaire',           'TEXT',    'Technique'),
  ('Note',                  'TEXT',    'Technique');
