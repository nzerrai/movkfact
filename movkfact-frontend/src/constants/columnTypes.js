/**
 * Shared column type definitions used across CSV upload and manual dataset creation.
 * Source of truth: ColumnType.java enum on the backend.
 */
export const COLUMN_TYPE_GROUPS = [
  {
    label: 'Données personnelles',
    types: [
      { value: 'FIRST_NAME',     label: 'Prénom' },
      { value: 'LAST_NAME',      label: 'Nom de famille' },
      { value: 'EMAIL',          label: 'Email' },
      { value: 'PHONE',          label: 'Téléphone' },
      { value: 'GENDER',         label: 'Genre' },
      { value: 'ADDRESS',        label: 'Adresse' },
    ],
  },
  {
    label: 'Données numériques',
    types: [
      { value: 'INTEGER',        label: 'Nombre entier' },
      { value: 'DECIMAL',        label: 'Nombre décimal' },
      { value: 'PERCENTAGE',     label: 'Pourcentage' },
      { value: 'BOOLEAN',        label: 'Booléen' },
    ],
  },
  {
    label: 'Données textuelles',
    types: [
      { value: 'ENUM',           label: 'Liste de valeurs' },
      { value: 'TEXT',           label: 'Texte libre' },
      { value: 'UUID',           label: 'UUID' },
      { value: 'URL',            label: 'URL' },
      { value: 'IP_ADDRESS',     label: 'Adresse IP' },
    ],
  },
  {
    label: 'Données géographiques',
    types: [
      { value: 'COUNTRY',        label: 'Pays' },
      { value: 'CITY',           label: 'Ville' },
      { value: 'COMPANY',        label: 'Entreprise' },
      { value: 'ZIP_CODE',       label: 'Code postal' },
    ],
  },
  {
    label: 'Données financières',
    types: [
      { value: 'AMOUNT',         label: 'Montant' },
      { value: 'CURRENCY',       label: 'Devise' },
      { value: 'ACCOUNT_NUMBER', label: 'Numéro de compte' },
    ],
  },
  {
    label: 'Données temporelles',
    types: [
      { value: 'DATE',           label: 'Date' },
      { value: 'BIRTH_DATE',     label: 'Date de naissance' },
      { value: 'TIME',           label: 'Heure' },
      { value: 'TIMEZONE',       label: 'Fuseau horaire' },
    ],
  },
];

/** Flat array of all type values (for simple select lists) */
export const ALL_COLUMN_TYPES = COLUMN_TYPE_GROUPS.flatMap((g) => g.types);
