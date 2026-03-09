-- V010__detection_patterns.sql
-- Table des patterns de détection de types de colonnes.
-- Seed initial généré depuis patterns.yml (24 types, 96 patterns).
-- Après ce script, patterns.yml n'est plus utilisé au runtime —
-- PatternCache charge exclusivement depuis cette table.

CREATE TABLE detection_pattern (
    id          BIGSERIAL    PRIMARY KEY,
    column_type VARCHAR(50)  NOT NULL,
    pattern     VARCHAR(255) NOT NULL,
    description VARCHAR(255)
);

CREATE INDEX idx_detection_pattern_type ON detection_pattern(column_type);

-- ── PERSONAL TYPOLOGY ─────────────────────────────────────────────────────────

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('FIRST_NAME', '(?i)^first_?name$',  'EN standard'),
  ('FIRST_NAME', '(?i)^prenom$',       'FR standard'),
  ('FIRST_NAME', '(?i)^forename$',     'EN alternatif'),
  ('FIRST_NAME', '(?i)^given_?name$',  'EN given name');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('LAST_NAME', '(?i)^last_?name$',       'EN standard'),
  ('LAST_NAME', '(?i)^family_?name$',     'EN family'),
  ('LAST_NAME', '(?i)^nom$',              'FR court'),
  ('LAST_NAME', '(?i)^surname$',          'EN surname'),
  ('LAST_NAME', '(?i)^name$',             'EN générique'),
  ('LAST_NAME', '(?i)^nom_?famille$',     'FR nom famille'),
  ('LAST_NAME', '(?i)^nom_?de_?famille$', 'FR nom de famille');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('EMAIL', '(?i)^email$',           'EN standard'),
  ('EMAIL', '(?i)^e_?mail$',         'EN avec tiret'),
  ('EMAIL', '(?i)^electronic_?mail$','EN long'),
  ('EMAIL', '(?i)^mail_?address$',   'EN mail address');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('GENDER', '(?i)^gender$', 'EN standard'),
  ('GENDER', '(?i)^sex$',    'EN sex'),
  ('GENDER', '(?i)^genre$',  'FR genre'),
  ('GENDER', '(?i)^sexe$',   'FR sexe');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('PHONE', '(?i)^phone$',        'EN standard'),
  ('PHONE', '(?i)^telephone$',    'FR/EN long'),
  ('PHONE', '(?i)^tel$',          'FR abrégé'),
  ('PHONE', '(?i)^phone_?number$','EN phone number');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('ADDRESS', '(?i)^address$',      'EN standard'),
  ('ADDRESS', '(?i)^adresse$',      'FR standard'),
  ('ADDRESS', '(?i)^street$',       'EN rue'),
  ('ADDRESS', '(?i)^rue$',          'FR rue'),
  ('ADDRESS', '(?i)^location$',     'EN location'),
  ('ADDRESS', '(?i)^full_?address$','EN adresse complète');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('ZIP_CODE', '(?i)^zip_?code$',   'EN standard'),
  ('ZIP_CODE', '(?i)^postal_?code$','EN postal'),
  ('ZIP_CODE', '(?i)^code_?postal$','FR code postal'),
  ('ZIP_CODE', '(?i)^zip$',         'EN court'),
  ('ZIP_CODE', '(?i)^cp$',          'FR abrégé');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('CITY', '(?i)^city$',         'EN standard'),
  ('CITY', '(?i)^ville$',        'FR standard'),
  ('CITY', '(?i)^town$',         'EN town'),
  ('CITY', '(?i)^municipality$', 'EN municipality');

-- ── NUMERIC TYPOLOGY ──────────────────────────────────────────────────────────

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('INTEGER', '(?i)^age$',          'âge générique'),
  ('INTEGER', '(?i)^age_?actuel$',  'FR âge actuel'),
  ('INTEGER', '(?i)^current_?age$', 'EN current age'),
  ('INTEGER', '(?i)^age_?courant$', 'FR âge courant'),
  ('INTEGER', '(?i)^quantite$',     'FR quantité'),
  ('INTEGER', '(?i)^quantity$',     'EN quantity'),
  ('INTEGER', '(?i)^rang$',         'FR rang'),
  ('INTEGER', '(?i)^rank$',         'EN rank'),
  ('INTEGER', '(?i)^score$',        'score générique'),
  ('INTEGER', '(?i)^note$',         'note générique');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('DECIMAL', '(?i)^decimal$',        'EN standard'),
  ('DECIMAL', '(?i)^float$',          'EN float'),
  ('DECIMAL', '(?i)^double$',         'EN double'),
  ('DECIMAL', '(?i)^montant_?decimal$','FR montant décimal'),
  ('DECIMAL', '(?i)^prix$',           'FR prix');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('PERCENTAGE', '(?i)^percentage$', 'EN standard'),
  ('PERCENTAGE', '(?i)^pourcentage$','FR standard'),
  ('PERCENTAGE', '(?i)^taux$',       'FR taux'),
  ('PERCENTAGE', '(?i)^rate$',       'EN rate'),
  ('PERCENTAGE', '(?i)^pct$',        'EN abrégé');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('BOOLEAN', '(?i)^boolean$', 'EN standard'),
  ('BOOLEAN', '(?i)^bool$',    'EN court'),
  ('BOOLEAN', '(?i)^actif$',   'FR actif'),
  ('BOOLEAN', '(?i)^active$',  'EN active'),
  ('BOOLEAN', '(?i)^enabled$', 'EN enabled'),
  ('BOOLEAN', '(?i)^flag$',    'EN flag');

-- ── FINANCIAL TYPOLOGY ────────────────────────────────────────────────────────

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('AMOUNT', '(?i)^amount$', 'EN standard'),
  ('AMOUNT', '(?i)^montant$','FR standard'),
  ('AMOUNT', '(?i)^sum$',    'EN somme'),
  ('AMOUNT', '(?i)^total$',  'EN total'),
  ('AMOUNT', '(?i)^value$',  'EN valeur'),
  ('AMOUNT', '(?i)^price$',  'EN price');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('ACCOUNT_NUMBER', '(?i)^account_?number$','EN standard'),
  ('ACCOUNT_NUMBER', '(?i)^account$',        'EN court'),
  ('ACCOUNT_NUMBER', '(?i)^compte$',         'FR compte'),
  ('ACCOUNT_NUMBER', '(?i)^iban$',           'IBAN'),
  ('ACCOUNT_NUMBER', '(?i)^bban$',           'BBAN'),
  ('ACCOUNT_NUMBER', '(?i)^account_?id$',    'EN account id');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('CURRENCY', '(?i)^currency$',      'EN standard'),
  ('CURRENCY', '(?i)^devise$',        'FR devise'),
  ('CURRENCY', '(?i)^code_?currency$','EN code currency'),
  ('CURRENCY', '(?i)^currency_?code$','EN currency code'),
  ('CURRENCY', '(?i)^curr$',          'EN abrégé');

-- ── TEMPORAL TYPOLOGY ─────────────────────────────────────────────────────────

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('BIRTH_DATE', '(?i)^date_?birth$',       'EN court'),
  ('BIRTH_DATE', '(?i)^birthdate$',         'EN compact'),
  ('BIRTH_DATE', '(?i)^birth_?date$',       'EN standard'),
  ('BIRTH_DATE', '(?i)^naissance$',         'FR court'),
  ('BIRTH_DATE', '(?i)^date_?naissance$',   'FR standard'),
  ('BIRTH_DATE', '(?i)^date_?de_?naissance$','FR long'),
  ('BIRTH_DATE', '(?i)^date_?of_?birth$',   'EN long'),
  ('BIRTH_DATE', '(?i)^dob$',               'EN abrégé');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('DATE', '(?i)^date$',          'générique'),
  ('DATE', '(?i)^created_?at$',   'audit creation'),
  ('DATE', '(?i)^created_?date$', 'audit creation 2'),
  ('DATE', '(?i)^date_?created$', 'audit creation 3'),
  ('DATE', '(?i)^modified_?at$',  'audit modification'),
  ('DATE', '(?i)^updated_?at$',   'audit mise à jour'),
  ('DATE', '(?i)^timestamp$',     'timestamp générique');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('TIME', '(?i)^time$',       'EN standard'),
  ('TIME', '(?i)^heure$',      'FR heure'),
  ('TIME', '(?i)^hour$',       'EN hour'),
  ('TIME', '(?i)^minute$',     'EN minute'),
  ('TIME', '(?i)^time_?of_?day$','EN time of day');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('TIMEZONE', '(?i)^timezone$',      'EN standard'),
  ('TIMEZONE', '(?i)^tz$',            'EN abrégé'),
  ('TIMEZONE', '(?i)^timezone_?code$','EN code'),
  ('TIMEZONE', '(?i)^time_?zone$',    'EN avec espace');

-- ── TEXT / IDENTIFIER TYPOLOGY ────────────────────────────────────────────────

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('ENUM', '(?i)^enum$',     'EN standard'),
  ('ENUM', '(?i)^statut$',   'FR statut'),
  ('ENUM', '(?i)^status$',   'EN status'),
  ('ENUM', '(?i)^categorie$','FR catégorie'),
  ('ENUM', '(?i)^category$', 'EN category'),
  ('ENUM', '(?i)^type$',     'générique type'),
  ('ENUM', '(?i)^etat$',     'FR état'),
  ('ENUM', '(?i)^state$',    'EN state');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('TEXT', '(?i)^text$',       'EN standard'),
  ('TEXT', '(?i)^texte$',      'FR standard'),
  ('TEXT', '(?i)^description$','EN/FR description'),
  ('TEXT', '(?i)^comment$',    'EN comment'),
  ('TEXT', '(?i)^commentaire$','FR commentaire'),
  ('TEXT', '(?i)^note_?text$', 'note textuelle'),
  ('TEXT', '(?i)^remarks$',    'EN remarks'),
  ('TEXT', '(?i)^remarques$',  'FR remarques');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('UUID', '(?i)^uuid$',       'EN standard'),
  ('UUID', '(?i)^guid$',       'EN guid'),
  ('UUID', '(?i)^identifier$', 'EN identifier'),
  ('UUID', '(?i)^identifiant$','FR identifiant');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('URL', '(?i)^url$',       'EN standard'),
  ('URL', '(?i)^website$',   'EN website'),
  ('URL', '(?i)^site_?web$', 'FR site web'),
  ('URL', '(?i)^lien$',      'FR lien'),
  ('URL', '(?i)^link$',      'EN link'),
  ('URL', '(?i)^webpage$',   'EN webpage');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('IP_ADDRESS', '(?i)^ip_?address$','EN standard'),
  ('IP_ADDRESS', '(?i)^ip$',         'EN court'),
  ('IP_ADDRESS', '(?i)^adresse_?ip$','FR adresse IP'),
  ('IP_ADDRESS', '(?i)^ipv4$',       'EN IPv4'),
  ('IP_ADDRESS', '(?i)^ipv6$',       'EN IPv6');

-- ── GEOGRAPHIC TYPOLOGY ───────────────────────────────────────────────────────

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('COUNTRY', '(?i)^country$',      'EN standard'),
  ('COUNTRY', '(?i)^pays$',         'FR standard'),
  ('COUNTRY', '(?i)^nation$',       'EN nation'),
  ('COUNTRY', '(?i)^country_?code$','EN code pays'),
  ('COUNTRY', '(?i)^code_?pays$',   'FR code pays');

INSERT INTO detection_pattern (column_type, pattern, description) VALUES
  ('COMPANY', '(?i)^company$',     'EN standard'),
  ('COMPANY', '(?i)^entreprise$',  'FR entreprise'),
  ('COMPANY', '(?i)^societe$',     'FR société'),
  ('COMPANY', '(?i)^organization$','EN organization'),
  ('COMPANY', '(?i)^organisation$','FR organisation'),
  ('COMPANY', '(?i)^employeur$',   'FR employeur');
