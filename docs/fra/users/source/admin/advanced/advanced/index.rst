.. _advanced_configuration:
.. include:: ../../../substitutions.txt
.. _admin_how_to_config_db:

Configurer la base de données
=============================

Le catalogue utilise par défaut la base de données (ie. `H2 <http://www.h2database.com/>`_).

Les bases de données actuellement supportées sont les suivantes 
 (ordre alphabétique):

* DB2
* H2
* Mckoi
* MS SqlServer 2008
* MySQL
* Oracle
* PostgreSQL (ou PostGIS)


Configuration simple
--------------------

La connexion à la base de données est définie dans la section *<resources>* du fichier *web/geonetwork/WEB-INF/config.xml*.

Modifier l'attribut *enable* pour activer l'une des connexions. Il n'est pas possible d'activer deux connexions.::

	<resource enabled="true">
		...
		
Une fois activé, configurer :
 
- l'utilisateur, 

- le mot de passe

- le driver (utiliser les exemples fournis dans le fichier)

- l'url de connexion


Exemple d'une configuration pour utiliser PostGIS::

		<resource enabled="true">
			<name>main-db</name>
			<provider>jeeves.resources.dbms.DbmsPool</provider>
			<config>
				<user>www-data</user>
				<password>www-data</password>
				<driver>org.postgresql.Driver</driver>
				<url>jdbc:postgis://localhost:5432/geonetwork</url>
			</config>
		</resource>



Pool de connexions
------------------

Le pool de connexion repose sur Apache Database Connection Pool (Apache DBCP) http://commons.apache.org/dbcp/configuration.html.

Les paramètres du pool sont présentés sur la page http://commons.apache.org/dbcp/configuration.html. 
Un sous ensemble de ces paramètres peuvent être configurés dans l'élément resource du fichier config.xml :


===================================   ============================================================================   =========================================
Paramètre                             Description                                                                    Valeur par défaut               
===================================   ============================================================================   =========================================
maxActive                             taille du pool / nombre de connexions actives max                              10                     
maxIdle                               nombre de connexion au repos (idle)                                            maxActive             
minIdle                               nombre minimum de connexion au repos                                           0                     
maxWait                               temps d'attente en millisecondes pour l'obtention d'une connexionle            200                   
validationQuery                       requête SQL pour la vérification de la connexion                               no default            
timeBetweenEvictionRunsMillis         interval entre les récupérations (eviction) (-1 = ignoré)                      -1                    
testWhileIdle                         valider les connexions au repos                                                false                 
minEvictableIdleTimeMillis            temps de repos avant récupération                                              30 x 60 x 1000 msecs  
numTestsPerEvictionRun                nombre de connexions testées par récupération                                  3                     
maxOpenPreparedStatements             nombre de requête SQL en cache (-1 = aucune, 0 = illimité)                     -1                    
defaultTransactionIsolation           cf http://en.wikipedia.org/wiki/Isolation_%28database_systems%29               READ_COMMITTED
===================================   ============================================================================   =========================================

Pour des raisons de performance, il est recommandé de définir ces paramètres *après* la création de la base de données :

- maxOpenPreparedStatements="300" (au minimum) 

Les paramètres suivants sont définis par le catalogue et ne peuvent être configurés par l'administrateur :

- removeAbandoned - true
- removeAbandonedTimeout - 60 x 60 seconds = 1 hour
- logAbandoned - true
- testOnBorrow - true
- defaultReadOnly - false
- defaultAutoCommit - false
- initialSize - maxActive
- poolPreparedStatements - true, if maxOpenPreparedStatements >= 0, otherwise false 

Note: Certains pare-feux détruisent les connexions au repos après un certain temps, par exemple 1 heure (= 3600 secs). 
Pour conserver ces connexions en utilisant une requête de validation, définir :

- minEvictableIdleTimeMillis à une durée inférieure au timeout, (eg. 2 mins = 120 secs = 120000 millisecs), 
- testWhileIdle à true 
- timeBetweenEvictionRunsMillis et numTestsPerEvictionRun pour valider les connexions fréquemments eg 15 mins = 900 secs = 900000 millisecs et 4 connexions par test

Par exemple ::

    <testWhileIdle>true</testWhileIdle>
    <minEvictableIdleTimeMillis>120000</minEvictableIdleTimeMillis>
    <timeBetweenEvictionRunsMillis>900000</timeBetweenEvictionRunsMillis>
    <numTestsPerEvictionRun>4</numTestsPerEvictionRun>


**Note:**

- Quand le catalogue assure la gestion du pool de connexion, seules les bases de données PostGIS peuvent gérer l'index spatial. 
  Pour les autres bases de données, l'index spatial sera au format ESRI Shapefile. Avec PostGIS, 2 pools de connexions sont alors créés. 
  Le premier est configuré tel que décrit précédemment et le second est créé par la librairie GeoTools et ne peut être configuré. 
  Cette approche est maintenant obsolète et il est recommandé d'utiliser un pool de connexion de type JNDI (cf. :ref:`admin_how_to_config_db_jndi`) pour que la cartouche spatiale de
  la base de données soit utilisé (utilisant NG (Next Generation) GeoTools datastore factories).
  


Drivers JDBC
````````````
Les fichiers jar des drivers JDBC  doivent être dans le répertoire **GEONETWERK_INSTALL_DIR/WEB-INF/lib**. 
Pour les bases de données Open Source, comme MySQL et PostgreSQL, ces fichiers sont déjà installés. 
Pour les bases de données commerciales, il est nécessaire de télécharger ces fichiers manuellement. Celà est lié aux licences.

* `DB2 driver JDBC <https://www-304.ibm.com/support/docview.wss?rs=4020&uid=swg27016878>`_
* `MS Sql Server driver JDBC <http://msdn.microsoft.com/en-us/sqlserver/aa937724>`_
* `Oracle driver JDBC <http://www.oracle.com/technetwork/database/features/jdbc/index-091264.html>`_


Créer et initialiser les tables
```````````````````````````````


Depuis la version 2.6.x, |project_name| dispose d'un **mécanisme de création et migration de la base de données automatique** au démarrage.
Si les tables ne sont pas présentes dans la base de données, le script de création est lancé.

Ensuite, |project_name| vérifie la version de la base de données correspond à la version de l'application
en vérifiant les valeurs dans la table *Settings* du paramètre *version*.

Une autre alternative est de lancer manuellement les scripts SQL: 

* Création : **GEONETWERK_INSTALL_DIR/WEB-INF/classes/setup/sql/create/**
* Données initiales : **GEONETWERK_INSTALL_DIR/WEB-INF/classes/setup/sql/data/**
* Migration :  **GEONETWERK_INSTALL_DIR/WEB-INF/classes/setup/sql/migrate/**

Exemple d'exécution pour DB2::

        db2 create db geonet
        db2 connect to geonet user db2inst1 using mypassword
        db2 -tf GEONETWERK_INSTALL_DIR/WEB-INF/classes/setup/sql/create/create-db-db2.sql > res1.txt
        db2 -tf GEONETWERK_INSTALL_DIR/WEB-INF/classes/setup/sql/data/data-db-default.sql > res2.txt
        db2 connect reset

Après exécution, vérifier **res1.txt** et **res2.txt**.


.. note::

    Problèmes connus avec DB2. Il est possible d'obtenir l'erreur suivante au premier lancement.

        DB2 SQL error: SQLCODE: -805, SQLSTATE: 51002, SQLERRMC: NULLID.SYSLH203

    Solution 1 : installer la base manuellement.
    Solution 2 : supprimer la base, la recréer puis localiser le fichier db2cli.lst dans le répertoire d'installation de DB2, puis exécuter :

        db2 bind @db2cli.lst CLIPKG 30

.. _admin_how_to_config_db_jndi:

Connexion JNDI
--------------

La configuration via JNDI est faite dans `WEB-INF/config.xml` :

::

    <resource enabled="true">
        <name>main-db</name>
        <provider>jeeves.resources.dbms.JNDIPool</provider>
        <config>
            <context>java:/comp/env</context>
            <resourceName>jdbc/geonetwork</resourceName>
            <url>jdbc:oracle:thin:@localhost:1521:XE</url>
            <provideDataStore>true</provideDataStore>
        </config>
    </resource> 

Les paramètres de connexions sont les suivants :

===========================   =======================================================================================================
Paramètre                     Description
===========================   =======================================================================================================
context                       Le nom du context pour obtenir la resource - souvent: java:/comp/env
resourceName                  Le nom de la resource à utiliser
url                           L'URL de la base de données - requis pour déterminer le type de base de données pour GeoTools
provideDataStore              Si "true", utilise la base de données pour l'index spatial, sinon un shapefile
===========================   =======================================================================================================


La configuration de la connexion en tant que tel est faite au niveau du container. Par exemple pour tomcat, la configuration est
faite dans conf/context.xml avec une ressource appelée jdbc/geonetwork. Ci-dessous un exemple pour Oracle:

::

    <Resource name="jdbc/geonetwork"
        auth="Container"
        type="javax.sql.DataSource"
        username="system"
        password="oracle"
        factory="org.apache.commons.dbcp.BasicDataSourceFactory"
        driverClassName="oracle.jdbc.OracleDriver"             
        url="jdbc:oracle:thin:@localhost:1521:XE"
        maxActive="10"
        maxIdle="10"
        removeAbandoned="true"
        removeAbandonedTimeout="3600"
        logAbandoned="true"
        testOnBorrow="true"
        defaultAutoCommit="false" 
        validationQuery="SELECT 1 FROM DUAL"
        accessToUnderlyingConnectionAllowed="true"
    />

Pour Jetty, la configuration est faite dans `WEB-INF/jetty-env.xml`.Ci-dessous un exemple pour PostGIS:

::

  <Configure class="org.eclipse.jetty.webapp.WebAppContext">
    <New id="gnresources" class="org.eclipse.jetty.plus.jndi.Resource">
      <Arg></Arg> 
      <Arg>jdbc/geonetwork</Arg>
      <Arg>
        <New class="org.apache.commons.dbcp.BasicDataSource">
          <Set name="driverClassName">org.postgis.DriverWrapper</Set>
          <Set name="url">jdbc:postgresql_postGIS://localhost:5432/gndb</Set>
          <Set name="username">geonetwork</Set>
          <Set name="password">geonetworkgn</Set>
          <Set name="validationQuery">SELECT 1</Set>
          <Set name="maxActive">10</Set>
          <Set name="maxIdle">10</Set>
          <Set name="removeAbandoned">true</Set>
          <Set name="removeAbandonedTimeout">3600</Set>
          <Set name="logAbandoned">true</Set>
          <Set name="testOnBorrow">true</Set>
          <Set name="defaultAutoCommit">false</Set>
          <!-- 2=READ_COMMITTED, 8=SERIALIZABLE -->
          <Set name="defaultTransactionIsolation">2</Set>
          <Set name="accessToUnderlyingConnectionAllowed">true</Set>
        </New>
        </Arg>
      <Call name="bindToENC">
        <Arg>jdbc/geonetwork</Arg>  
      </Call>
    </New>
  </Configure>


Les paramètres peuvent être spécifiés pour controler le pool DBCP utilisé par le container Java (cf. http://commons.apache.org/dbcp/configuration.html).

Les paramètres suivant doivent être définis pour que le bon fonctionnement du catalogue :

============================================   ============================================================
Tomcat Syntax                                  Jetty Syntax                                                  
============================================   ============================================================
defaultAutoCommit="false"                      <Set name="defaultAutoCommit">false</Set>             
accessToUnderlyingConnectionAllowed="true"     <Set name="accessToUnderlyingConnectionAllowed">true</Set>     
============================================   ============================================================

Pour des raisons de performance, l'administrateur devrait définir les paramètres suivant après la première initialisation de la base de données :

============================================   ============================================================
Tomcat Syntax                                  Jetty Syntax                                                  
============================================   ============================================================
poolPreparedStatements="true"                  <Set name="poolPreparedStatements">true</Set>
maxOpenPreparedStatements="300" (at least)     <Set name="maxOpenPreparedStatements">300</Set>
============================================   ============================================================

Notes:

- Aussi bien PostGIS qu'Oracle créeront une table dans la base de données pour l'index spatial si provideDataStore est fixé à "true".
- Les librairies commons-dbcp-1.3.jar et commons-pool-1.5.5.jar doivent être installée dans le class path du container Java (eg. `common/lib` 
  pour tomcat5 ou `jetty/lib/ext` pour Jetty) car apache common dbcp est le seul DataSourceFactory supporté par GeoTools. 
- les librairies par défaut tomcat-dbcp.jar semble fonctionner correctement avec GeoTools et PostGIS mais semble poser des problèmes avec
  d'autres types de base de données (eg. DB which needs to unwrap the connection in order to do spatial operations - Oracle).
- Oracle ojdbc-14.jar ou ojdbc5.jar ou ojdbc6.jar (en fonction de la version de Java utilisée) et sdoapi.jar
  doivent être installés dans le class path du container Java (eg. `common/lib` pour tomcat5 ou `jetty/lib/ext` pour Jetty)
- Usage avancé : vérifié le niveau d'isolation des transactions en fonction du driver de base de données utilisé. 
  READ_COMMITTED semble être le plus stable pour les bases de données standards avec le catalogue. 
  McKoi supporte uniquement SERIALIZABLE (does anyone still use McKoi?). 
  Pour plus d'information cf. http://en.wikipedia.org/wiki/Isolation_%28database_systems%29.




Personnaliser l'interface
=========================

Service de traduction Google
----------------------------

Dans le fichier config-gui.xml modifier la section::

            <!-- 
                Google translation service (http://code.google.com/apis/language/translate/overview.html):
                Set this parameter to "1" to activate google translation service.
                Google AJAX API Terms of Use http://code.google.com/apis/ajaxlanguage/terms.html
                
                WARNING: "The Google Translate API has been officially deprecated as of May 26, 2011...
                the number of requests you may make per day will be limited and 
                the API will be shut off completely on December 1, 2011".
              -->
             <editor-google-translate>1</editor-google-translate>


.. _how_to_config_edit_mode:

Configurer les vues en mode édition
-----------------------------------

Dans le fichier config-gui.xml, il est possible de définir les modes disponibles en édition::

  <metadata-tab>
    <simple flat="true"  default="true"/>
    <advanced/><!-- This view should not be removed as this is the only view to be able to edit all elements defined in a schema. -->
    <iso/>
    <fra/>
    <!-- This view display all INSPIRE recommended elements
    in a view : 
    * In flat mode, define which non existing children of the exception must be displayed (using ancestorException)
    * or which non existing element must be displayed (using exception)
    -->
    <inspire flat="true">
       <ancestorException for="EX_TemporalExtent,CI_Date,spatialResolution"/>
       <exception for="result,resourceConstraints,pointOfContact,hierarchyLevel,couplingType,operatesOn,distributionInfo,onLine,identifier,language,characterSet,topicCategory,serviceType,descriptiveKeywords,extent,temporalElement,geographicElement,lineage"/>
    </inspire> 
    <xml/>
  </metadata-tab>


L'attribut **flat** permet de n'afficher que les éléments existants.
Mettre les éléments non souhaités en commentaire.



Optimiser la configuration pour les catalogues volumineux
=========================================================

Quelques conseils à prendre en compte pour les catalogues volumineux à partir de 20 000 fiches :

#. **Disque** : Le catalogue utilise une base de données pour le stockage mais utilise 
   un moteur de recherches reposant sur Lucene. Lucene est très rapide et le restera 
   y compris pour des catalogues volumineux à condition de lui fournir des disques rapides 
   (eg. SSD – utiliser la variable de configuration de l'index pour placer uniquement l'index 
   sur le disque SSD, si vous ne pouvez placer toute l'application dessus), 
   de la mémoire (16Gb+) et des CPU dans un environnement 64bits. 
   Par exemple, les phases de moissonnage nécessitent de nombreux accès disques 
   lors de la mise à jour de l'index. Privilégier des disques rapides dans ce cas là.

#. **Base de données** : Privilégier l'utilisation du couple PostgreSQL + PostGIS car 
   l'index spatial au format ESRI Shapefile sera moins performant dans les phases d'indexation
   et de recherche lorsque le nombre de fiches sera important.

#. **CPU** : Depuis septembre 2011, les actions d'indexations et opérations massives 
   peuvent être réparties sur plusieurs processus. Ceci est configurable à partir de
   la configuration du système. Une bonne pratique est de fixer la valeur 
   fonction du nombre de processeurs ou core de la machine.

#. **Base de données / taille du pool** : Ajuster la valeur de la taille du pool fonction
   du nombre de moissonnages pouvant être lancés en parallèle, du nombre d'actions massives
   et du nombre d'utilisateurs simultannés. Plus la taille du pool est importante, moins
   le temps d'attente pour récupérer une connexion libre sera long (le risque de timeout sera également moindre).

#. **Nombre de fichiers ouverts** : La plupart des systèmes d'exploitation limite le nombre 
   de fichiers ouverts. Lors de forte charge de mise à jour de l'index, le nombre
   de fichiers ouverts peut être source d'erreur. Modifier la configuration du
   système en conséquence  (eg. ulimit -n 4096).

#. **Mémoire** : La consigne ici est d'allouer le maximum de mémoire fonction de la machine

#. **Créer un catalogue de plus d'1 million d'enregistrements** : Le catalogue crée
   dans le répertoire DATA un répertoire par fiche contenant lui-même 2 
   répertoires public et private. Il est possible que le nombre maximum d'inode soit
   alors atteint, le système retournant alors des erreurs du type 'out of space' bien que
   le système dispose de place disponible. Le nombre d'inode ne peut être modifié dynamiquement 
   après création du système de fichier. Il est donc important de penser à fixer
   la valeur lors de la création du système de fichier. Une valeur de 5 fois 
   (voire 10 fois) le nombre de fiches prévues devrait permettre de
   stocker le répertoire DATA sur ce système de fichier.
   
   
.. _geonetwork_data_dir:


Répertoire de données du catalogue
==================================

Lors du déploiement du catalogue sur un serveur en particulier, il est alors nécessaire de modifier la configuration par défaut. Une façon de faire 
est de modifier les fichiers de configuration à l'intérieur de l'application web. Dans ce cas, il vous faudra une application différente pour chaque
déploiement ou bien faire à chaque mise à jour les modifications de cette configuration.

|project_name| dispose de 2 méthodes pour simplifier la configuration :

 #. Le répertoire de données du catalogue
 #. La surcharge de configuration (Cf. :ref:`adv_configuration_overriddes`)


Le répertoire de données du catalogue (ou GeoNetwork data directory) est un répertoire sur le système de fichiers dans lequel
|project_name| stocke les fichiers de configuration. Cette configuration définie par exemple:
Quels thesaurus sont utilisés ? Quels standards de métadonnées sont chargés ? 
Le répertoire de données contient également différents fichiers nécessaire au bon fonctionnement de l'application (eg. l'index Lucene, l'index spatial, les logos).


Il est recommandé de définir un répertoire de données lors du passage en production afin de simplifier les mises à jour.

Créer le répertoire de données
------------------------------

Le répertoire de données doit être créé avant le lancement du catalogue. Il doit être possible pour l'utlisateur lançant l'application d'y lire et d'y écrire.
Si le répertoire est vide, le catalogue initialisera sa structure au démarrage. Le plus simple pour créer un nouveau répertoire de données et de copier
le répertoire d'une installation par défaut.

Définir le répertoire de données
--------------------------------

Le répertoire de données peut être configuré de 3 façons différentes:

 - variable d'environnement Java
 - paramètre de context du Servlet
 - variable d'environnement du système


Pour les variables d'environnement Java ou les paramètres de context du Servlet, il faut utiliser :

 - <webappName>.dir sinon geonetwork.dir


Pour les variables d'environnement du système, il faut utiliser :

 - <webappName>_dir sinon geonetwork_dir

L'ordre de résolution est le suivant :

 #. <webappname>.dir
  #. Java environment variable (ie. -D<webappname>.dir=/a/data/dir)
  #. Servlet context parameter (ie. web.xml)
  #. Config.xml appHandler parameter (ie. config.xml)
  #. System environment variable (ie. <webappname>_dir=/a/data/dir)
 #. geonetwork.dir
  #. Java environment variable (ie. -Dgeonetwork.dir=/a/data/dir)
  #. Servlet context parameter (ie. web.xml)
  #. Config.xml appHandler parameter (ie. config.xml)
  #. System environment variable (ie. geonetwork_dir=/a/data/dir)




Variables d'environnement Java
------------------------------

En fonction du container Java, il est possible de définir les variables d'environnement Java. Pour Tomcat, la configuration est ::

  CATALINA_OPTS="-Dgeonetwork.dir=/var/lib/geonetwork_data"


Lancement de l'application en mode lecture-seule
------------------------------------------------

Afin de lancer le catalogue avec le répertoire de l'application en mode lecture seule, l'utilisateur doit configurer 2 variables :

 - <webappName>.dir ou geonetwork.dir pour le répertoire des données.
 - (optionel) la surcharge de configuration si les fichiers de configurations doivent être modifiés (Cf. :ref:`adv_configuration_overriddes`).
 
 
Pour Tomcat, la configuration pourrait être la suivante ::

  CATALINA_OPTS="-Dgeonetwork.dir=/var/lib/geonetwork_data -Dgeonetwork.jeeves.configuration.overrides.file=/var/lib/geonetwork_data/config/my-config.xml"


Structure du répertoire des données
-----------------------------------

La structure du répertoire des données est la suivante ::


 data_directory/
  |--data
  |   |--metadata_data: Les données associées aux fiches
  |   |--resources:
  |   |     |--htmlcache
  |   |     |--images
  |   |     |   |--harvesting
  |   |     |   |--logo
  |   |     |   |--statTmp
  |   |
  |   |--removed: Le répertoire contenant les fiches supprimées
  |   |--svn_repository: Le dépôt subversion pour la gestion des versions
  |
  |--config: Extra configuration (eg. overrides)
  |   |--schemaplugin-uri-catalog.xml
  |   |--JZKitConfig.xml
  |   |--codelist: Les thésaurus au format SKOS
  |   |--schema_plugins: Le répertoire utilisé pour stocker les standards enfichables
  |
  |--index: Les indexes
  |   |--nonspatial: L'index Lucene
  |   |--spatialindex.*: ESRI Shapefile pour l'index spatial (si PostGIS n'est pas utilisé)
  
  

Configuration avancée
---------------------

Tous les sous-répertoires peuvent être configurés séparément. Par exemple, pour positionner l'index dans un répertoire en particulier, il est possible d'utiliser :

 - <webappName>.lucene.dir sinon
 - geonetwork.lucene.dir


Exemple:

 - Ajouter les variables d'environnement Java au script de lancement start-geonetwork.sh ::
    
    java -Xms48m -Xmx512m -Xss2M -XX:MaxPermSize=128m -Dgeonetwork.dir=/app/geonetwork_data_dir -Dgeonetwork.lucene.dir=/ssd/geonetwork_lucene_dir

 - Ajouter les variables systèmes au script de lancement start-geonetwork.sh ::

    # Set custom data directory location using system property
    export geonetwork_dir=/app/geonetwork_data_dir
    export geonetwork_lucene_dir=/ssd/geonetwork_lucene_dir


Information système
-------------------

Toute la configuration peut être consultée depuis l'administration > information système.

    .. figure:: geonetwork-data-dirs.png



Autres variables de configuration
---------------------------------

Dans |project_name|, d'autres variables de configuration sont disponibles:

 * <webappname>.jeeves.configuration.overrides.file - Cf. :ref:`adv_configuration_overriddes`
 * jeeves.configuration.overrides.file - Cf. :ref:`adv_configuration_overriddes`
 * mime-mappings -  mime mappings used by jeeves for generating the response content type
 * http.proxyHost - The internal GeoNetwork Http proxy uses this for configuring how it can access the external network (Note for harvesters there is also a setting in the Settings page of the administration page)
 * http.proxyPort - The internal GeoNetwork Http proxy uses this for configuring how it can access the external network (Note for harvesters there is also a setting in the Settings page of the administration page)
 * geonetwork.sequential.execution - (true,false) Force indexing to occur in current thread rather than being queued in the ThreadPool.  Good for debugging issues.


Dans le cas où plusieurs catalogues sont installés dans le même container, il est recommandé de substituer 
dans les noms des propriétés <webappname> par le nom de la webapp afin d'éviter les conflits entre catalogue.



.. _adv_configuration_overriddes:

Surcharge de configuration
==========================

La surcharge de configuration permet un accès quasi complet à toute les options de configurations afin 
de créer une configuration pour un environnement particulier (eg. DEV, PROD). Le concept de surchage repose
sur la capacité à remplacer une partie de la configuration pour le serveur sur lequel l'application est déployée.
La surcharge ne contient donc que les paramètres à modifier par rapport à la configuration par défaut.

La surcharge est également pratique pour les "fork" de GeoNetwork qui nécessite de s'aligner sur le code source d'origine.

Un scénario classique est d'avoir une instance de test et une instance de production avec 2 configurations différentes. Dans les 2 cas, la configuration
est identique pour 90% des paramètres mais une partie doit être mise à jour.

Un fichier de surcharge peut être défini par une propriété du système ou un paramètre
du servlet : jeeves.configuration.overrides.file.

L'ordre de résolution est :
 * Propriété système avec la clé : {servlet.getServletContext().getServletContextName()}.jeeves.configuration.overrides.file
 * Paramétre du servlet avec la clé : {servlet.getServletContext().getServletContextName()}.configuration.overrides.file
 * Propriété système avec la clé: jeeves.configuration.overrides.file
 * Paramétre du servlet avec la clé: jeeves.configuration.overrides.file

La propriété doit être un chemin ou une URL. La méthode utilisée pour trouver un fichier de surcharge est la suivante :
 #. utlisation comme une URL, si exception,
 #. c'est un chemin. Utilisation du servlet context  pour recherche la ressource. si exception,
 #. c'est un fichier.  Si le fichier n'est pas trouvé, une exception est levée.

Un exemple de surcharge ::
   
   <overrides>
       <!-- import values.  The imported values are put at top of sections -->
       <import file="./imported-config-overrides.xml" />
        <!-- properties allow some properties to be defined that will be substituted -->
        <!-- into text or attributes where ${property} is the substitution pattern -->
        <!-- The properties can reference other properties -->
        <properties>
            <enabled>true</enabled>
            <dir>xml</dir>
            <aparam>overridden</aparam>
        </properties>
        <!-- A regular expression for matching the file affected. -->
        <file name=".*WEB-INF/config\.xml">
            <!-- This example will update the file attribute of the xml element with the name attribute 'countries' -->
            <replaceAtt xpath="default/gui/xml[@name = 'countries']" attName="file" value="${dir}/europeanCountries.xml"/>
            <!-- if there is no value then the attribute is removed -->
            <replaceAtt xpath="default/gui" attName="removeAtt"/>
            <!-- If the attribute does not exist it is added -->
            <replaceAtt xpath="default/gui" attName="newAtt" value="newValue"/>

            <!-- This example will replace all the xml in resources with the contained xml -->
            <replaceXML xpath="resources">
              <resource enabled="${enabled}">
                <name>main-db</name>
                <provider>jeeves.resources.dbms.DbmsPool</provider>
                 <config>
                     <user>admin</user>
                     <password>admin</password>
                     <driver>oracle.jdbc.driver.OracleDriver</driver>
                     <!-- ${host} will be updated to be local host -->
                     <url>jdbc:oracle:thin:@${host}:1521:fs</url>
                     <poolSize>10</poolSize>
                 </config>
              </resource>
            </replaceXML>
            <!-- This example simple replaces the text of an element -->
            <replaceText xpath="default/language">${lang}</replaceText>
            <!-- This examples shows how only the text is replaced not the nodes -->
            <replaceText xpath="default/gui">ExtraText</replaceText>
            <!-- append xml as a child to a section (If xpath == "" then that indicates the root of the document),
                 this case adds nodes to the root document -->
            <addXML xpath=""><newNode/></addXML>
            <!-- append xml as a child to a section, this case adds nodes to the root document -->
            <addXML xpath="default/gui"><newNode2/></addXML>
            <!-- remove a single node -->
            <removeXML xpath="default/gui/xml[@name = countries2]"/>
            <!-- The logging files can also be overridden, although not as easily as other files.  
                 The files are assumed to be property files and all the properties are loaded in order.  
                 The later properties overriding the previously defined parameters. Since the normal
                 log file is not automatically located, the base must be also defined.  It can be the once
                 shipped with GeoNetwork or another. -->
            <logging>
                <logFile>/WEB-INF/log4j.cfg</logFile>
                <logFile>/WEB-INF/log4j-jeichar.cfg</logFile>
            </logging>
        </file>
        <file name=".*WEB-INF/config2\.xml">
            <replaceText xpath="default/language">de</replaceText>
        </file>
        <!-- a normal file tag is for updating XML configuration files -->
        <!-- textFile tags are for updating normal text files like sql files -->
        <textFile name="test-sql.sql">
            <!-- each line in the text file is matched against the linePattern attribute and the new value is used for substitution -->
            <update linePattern="(.*) Relations">$1 NewRelations</update>
            <update linePattern="(.*)relatedId(.*)">$1${aparam}$2</update>
        </textFile>
    </overrides>

