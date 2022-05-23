## Sextant 7 migration

### CSW virtuels sont maintenant des portails

cf. 
https://gitlab.ifremer.fr/sextant/geonetwork/-/blob/sextant-7.0.x/web/src/main/webapp/WEB-INF/classes/setup/sql-sextant/migrate/7.0.0/maintenance-db-238.sql

### Base de données

cf. 
https://gitlab.ifremer.fr/sextant/geonetwork/-/blob/sextant-7.0.x/web/src/main/webapp/WEB-INF/classes/setup/sql-sextant/migrate/7.0.0/migrate-default.sql


### Vignette / Migrer les URLs vers l'API

https://sextant.ifremer.fr/geonetwork/srv/fr/resources.get?uuid=01051510-a178-11dc-8c36-000086f6a62e&fname=ifr_peupl_ChasseGlemarec_GolfeGascogne_Imagette.jpg devrait être
 https://sextant.ifremer.fr/geonetwork/srv/api/records/01051510-a178-11dc-8c36-000086f6a62e/attachments/ifr_peupl_ChasseGlemarec_GolfeGascogne_Imagette.jpg

Vérifier:
```sql
SELECT count(*) FROM metadata WHERE data LIKE '%resources.get%' AND isharvested = 'n';
```

Accèder à http://localhost:8080/geonetwork/doc/api/index.html#/tools/callStep
et lancer `org.fao.geonet.MetadataResourceDatabaseMigration`.

Rejouer la requête
S’il en reste, les traiter à la main? Sur ma base, il en reste 7


### Checkpoint 

Le schéma `iso19115-3` n'est plus maintenu. Migration des fiches vers `iso19115-3.2018`

Recherche les fiches checkpoint http://localhost:8080/geonetwork/srv/eng/catalog.edit#/board?sortBy=dateStamp&sortOrder=desc&isTemplate=%5B%22y%22,%22n%22%5D&resultType=manager&from=1&to=20&any=q(%2BdocumentStandard:iso19115-3)

Sélectionner tout.

Dans http://localhost:8080/geonetwork/doc/api/index.html#/processes/processRecords avec process `iso19115-3.2018-schemaupgrade` pour le bucket `e101`.


### Timezone

La timezone pour sextant est fixée à Europe/Paris (cf. admin > settings).

La migration consiste en la migration des dates en bases de données sur UTC. Cf. https://github.com/geonetwork/core-geonetwork/pull/5061

