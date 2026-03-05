# Story S5.2: Données Techniques — URL, IP_ADDRESS, UUID, SOFTWARE_VERSION, FILE_SIZE

**Sprint:** Sprint 5
**Points:** 6
**Epic:** EPIC 5 - Extended Data Types
**Type:** Full-Stack Feature
**Lead:** Amelia (Backend) + Sally (Frontend)
**Status:** Backlog
**Dependencies:** S2.1 DataGeneratorService, S2.6 Data Configuration UI

---

## User Story

**En tant que** développeur backend ou architecte système,
**Je veux** pouvoir générer des colonnes de type technique (URL, IP, UUID, version logicielle, taille de fichier),
**Afin de** créer des datasets réalistes pour tester des APIs, des logs système ou des inventaires d'actifs IT.

---

## Contexte métier

Le PRD section 2.2 définit les **Données techniques** comme suit :
> URLs, adresses IP, versions logicielles, tailles de fichiers — pour des domaines 'Système' ou 'API'.

Ces types sont critiques pour les équipes QA testant des systèmes d'API management, des systèmes de monitoring, des registres de ressources cloud.

---

## ✅ Acceptance Criteria

### AC1 — Nouveaux types dans ColumnType enum
- [ ] `URL("technical", "URL")` ajouté dans `ColumnType.java`
- [ ] `IP_ADDRESS("technical", "Adresse IP")` ajouté dans `ColumnType.java`
- [ ] `UUID_TYPE("technical", "UUID")` ajouté (nom `UUID_TYPE` pour éviter conflit avec `java.util.UUID`)
- [ ] `SOFTWARE_VERSION("technical", "Version logicielle")` ajouté dans `ColumnType.java`
- [ ] `FILE_SIZE("technical", "Taille de fichier")` ajouté dans `ColumnType.java`
- [ ] La catégorie `"technical"` est reconnue dans `DataTypeDetectionService`

### AC2 — Générateurs backend
- [ ] `UrlGenerator` créé dans `service/generator/technical/`
  - Format : `https://[domain].[tld]/[path]`
  - Domaines : api, app, service, data, cdn, assets, static
  - TLDs : com, io, net, org, dev
  - Paths aléatoires (1-3 segments, ex: `/users/42`, `/api/v2/products`)
- [ ] `IpAddressGenerator` créé dans `service/generator/technical/`
  - Mode IPv4 par défaut : `xxx.xxx.xxx.xxx` (plage 1-254 par octet)
  - Support IPv6 optionnel via `additionalConfig.version = "ipv6"`
  - Éviter les adresses réservées (0.x.x.x, 127.x.x.x, 255.x.x.x)
- [ ] `UuidGenerator` créé dans `service/generator/technical/`
  - Utilise `java.util.UUID.randomUUID().toString()`
  - Format standard : `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`
- [ ] `SoftwareVersionGenerator` créé dans `service/generator/technical/`
  - Format SemVer : `MAJOR.MINOR.PATCH` (ex: `1.4.2`, `2.0.0`, `3.12.1`)
  - MAJOR : 0-5, MINOR : 0-20, PATCH : 0-99
  - Support format avec suffixe via config : `1.4.2-SNAPSHOT`, `2.0.0-rc1`
- [ ] `FileSizeGenerator` créé dans `service/generator/technical/`
  - Génère une valeur numérique avec unité : `KB`, `MB`, `GB`
  - Par défaut : 1 KB → 10 GB (réaliste)
  - Format : `42.5 MB`, `1.2 GB`, `856 KB`
  - Config optionnelle `additionalConfig.unit` pour forcer une unité
- [ ] `GeneratorFactory` mis à jour pour les 5 nouveaux `ColumnType`

### AC3 — API sans régression
- [ ] `POST /api/datasets/generate` accepte les nouveaux types
- [ ] `POST /api/batch/generate` accepte les nouveaux types
- [ ] Les tests existants passent toujours (0 régression)

### AC4 — Composant frontend `TechnicalFieldConfig`
- [ ] Composant React `TechnicalFieldConfig.jsx` créé dans `DataConfigurationPanel/`
- [ ] Affiché pour les colonnes de type `URL`, `IP_ADDRESS`, `UUID_TYPE`, `SOFTWARE_VERSION`, `FILE_SIZE`
- [ ] `IpAddressGenerator` : option radio IPv4/IPv6 dans la config
- [ ] `FileSizeGenerator` : sélecteur unité (KB/MB/GB/Auto)
- [ ] `SoftwareVersionGenerator` : toggle "avec suffixe" (SNAPSHOT/rc)
- [ ] `URL` et `UUID_TYPE` : pas de config supplémentaire (affichage d'exemple seulement)
- [ ] Le composant s'intègre dans `ConfigurationPanel.jsx`

### AC5 — Détection automatique depuis CSV
- [ ] Détecte colonnes nommées `url`, `endpoint`, `lien`, `link` → type `URL`
- [ ] Détecte `ip`, `ip_address`, `adresse_ip`, `host` → type `IP_ADDRESS`
- [ ] Détecte `uuid`, `id`, `guid`, `identifier` (avec heuristique sur valeurs) → type `UUID_TYPE`
- [ ] Détecte `version`, `version_logicielle`, `release`, `build` → type `SOFTWARE_VERSION`
- [ ] Détecte `taille`, `size`, `file_size`, `poids` → type `FILE_SIZE`

### AC6 — Tests
- [ ] Tests unitaires pour les 5 générateurs (min 3 tests chacun)
- [ ] `UuidGenerator` : vérification format regex `[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-...`
- [ ] `IpAddressGenerator` : vérification que chaque octet ∈ [1, 254]
- [ ] `SoftwareVersionGenerator` : vérification format MAJOR.MINOR.PATCH
- [ ] Test d'intégration : génération complète avec colonnes techniques
- [ ] Tests Jest pour `TechnicalFieldConfig.jsx`
- [ ] Coverage global maintenu > 80%

---

## 🏗️ Spécifications Techniques

### Nouveaux fichiers à créer

```
backend/
  src/main/java/com/movkfact/service/generator/technical/
    UrlGenerator.java
    IpAddressGenerator.java
    UuidGenerator.java
    SoftwareVersionGenerator.java
    FileSizeGenerator.java

frontend/
  src/components/DataConfigurationPanel/
    TechnicalFieldConfig.jsx
    TechnicalFieldConfig.test.js
```

### Modifications requises

```
backend/
  src/main/java/com/movkfact/enums/ColumnType.java
    + URL("technical", "URL")
    + IP_ADDRESS("technical", "Adresse IP")
    + UUID_TYPE("technical", "UUID")
    + SOFTWARE_VERSION("technical", "Version logicielle")
    + FILE_SIZE("technical", "Taille de fichier")

  src/main/java/com/movkfact/service/generator/GeneratorFactory.java
    + case URL: return new UrlGenerator(columnConfig);
    + case IP_ADDRESS: return new IpAddressGenerator(columnConfig);
    + case UUID_TYPE: return new UuidGenerator(columnConfig);
    + case SOFTWARE_VERSION: return new SoftwareVersionGenerator(columnConfig);
    + case FILE_SIZE: return new FileSizeGenerator(columnConfig);

  src/main/java/com/movkfact/service/DataTypeDetectionService.java
    + Patterns pour les 5 nouveaux types

frontend/
  src/components/DataConfigurationPanel/ConfigurationPanel.jsx
    + Importer TechnicalFieldConfig
    + Ajouter rendu conditionnel pour les 5 types techniques
```

### UuidGenerator — Implémentation simple

```java
import java.util.UUID;

@Override
public Object generate() {
    return UUID.randomUUID().toString();
}
```

### IpAddressGenerator — Éviter adresses réservées

```java
@Override
public Object generate() {
    int octet1 = 1 + random.nextInt(223); // Évite 0 et 224+
    if (octet1 == 127) octet1 = 128; // Évite loopback
    int octet2 = 1 + random.nextInt(254);
    int octet3 = 1 + random.nextInt(254);
    int octet4 = 1 + random.nextInt(254);
    return octet1 + "." + octet2 + "." + octet3 + "." + octet4;
}
```

---

## 📊 Estimation

| Composant | Effort | Responsable |
|-----------|--------|-------------|
| 5 générateurs Java | 1.5j | Amelia |
| Mise à jour ColumnType + Factory + Detection | 0.5j | Amelia |
| Tests backend (5 générateurs × 3 tests min) | 0.5j | Amelia |
| `TechnicalFieldConfig.jsx` + tests | 1j | Sally |
| Intégration ConfigurationPanel | 0.5j | Sally |
| **Total** | **4j** | **6 pts** |
