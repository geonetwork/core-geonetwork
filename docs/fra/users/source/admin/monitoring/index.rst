.. _monitoring:

Surveillance du système
=======================

Le système de surveillance permet une surveillance automatisée de la santé du catalogue dans le temps.
La surveillance repose sur la librairies `Metrics <http://metrics.codahale.com/>`_ de Yammer.

Les mesures sont disponibles via JMX ou JSON. Quelque soit le format, les informations sont identiques. Les URL pour la surveillance sont :

    - /monitor/metrics?[pretty=(true|false)][class=metric.name] - retourne une réponse JSON avec l'ensemble des mesures
    - /monitor/threads - retourne une réprésentation textuelle de la "pile de vidage" (stack dump) au moment de l'appel
    - /monitor/healthcheck - lance une validation des points critiques de l'application et retourne un statut 200 si aucun problème n'est détecté, ou un statut 500 sinon.
    - /criticalhealthcheck - lance une validation des points critiques principaux.
    - /warninghealthcheck - lance une validation des points critiques non bloquant (eg. fiche avec erreur d'indexation).
    - /expensivehealthcheck - lance une validation des points critiques nécessitant plus de ressource pour leur vérification (eg. CSW)
    - /monitor - retourne une liste de liens vers les opérations de surveillance

Ces opérations sont également disponible via Administration > Information Système.

Par défaut, les URL /monitor/* sont accessibles uniquement aux profils ''administrator'' ou ''monitor'', 
cependant il est possible dans le fichier web.xml de fournir une liste d'URLs ou d'adresses IP
pour la surveillance de l'application sans besoin de connexion.

Les mesures disponibles sont:

    - Database Health Monitor - valide la connexion à la base de données
    - Index Health Monitor - valide qu'une recherche dans l'index Lucene est possible
    - Index Error Health Monitor - vérifie le nombre de fiche avec le drapeau _indexError == 1 qui correspond à une erreur lors de l'indexation de la fiche
    - CSW GetRecords Health Monitor - valide l'opération CSW GetRecords sur une recherche triviale
    - CSW GetCapabilities Health Monitor - valide l'opération GetCapabilities
    - Database Access timer - Temps nécessaire pour un accès à la base de données.
    - Database Open Timer - Temps où les connexions à la base de données sont ouvertes
    - Database Connection Counter - Nombre de connexions ouvertes
    - Harvester Error Counter - Nombre d'erreurs lors du moissonnage
    - Service timer - Temps d'exécution des services
    - Gui Services timer - Temps d'exécution des GUI services
    - XSL output timer - Temps d'exécution des transformations XSL
    - Log4j integration - Surveille la fréquence des logs et leur niveau de log. Cf  http://metrics.codahale.com/manual/log4j
    

Les mesures activées sont définies dans config-monitoring.xml et peuvent être désactivée si besoin.