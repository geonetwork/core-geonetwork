.. _preface:
.. include:: ../substitutions.txt

Préambule
=========

À propos de ce projet
---------------------

Ce document est le guide officiel d'installation, de configuration et d'utilisation de |project_name|.

Le projet |project_name| est né de la volonté de la `Food and Agriculture organisation of the United Nations (FAO) <http://www.fao.org>`_, 
du `Programme d'alimentation mondial (PAM ou WFP) <http://vam.wfp.org>`_ et de l'`United Nations Environmental Programme (UNEP) <http://www.unep.org>`_ 
de mettre en place un système de catalogage d'information géographique.

Actuellement le projet est très largement utilisé comme base d'infrastructure de données geographiques de part le monde.

Ce projet fait partie de l'Open Source Geospatial Foundation (OSGeo) et est accessible `ici <http://geonetwork-opensource.org>`_.

.. image:: OSGeo_project.png
   :align: center

Information sur la licence
--------------------------

Logiciel
````````

Le logiciel |project_name| est distribué sous licence `GPL v2 <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>`_.


Documentation
`````````````

Vous êtes libres de reproduire, distribuer et communiquer cette création au public, selon les conditions suivantes :

- Paternité — Vous devez citer le nom de l'auteur original de la manière indiquée par l'auteur 
  de l'oeuvre ou le titulaire des droits qui vous confère cette autorisation (mais pas 
  d'une manière qui suggérerait qu'ils vous soutiennent ou approuvent votre utilisation de l'oeuvre). 

- Pas de Modification — Vous n'avez pas le droit de modifier, de transformer ou d'adapter cette création.

Vous pouvez obtenir une copie de la license `Creative Commons <http://creativecommons.org/licenses/by-nd/3.0/deed.fr>`_

Informations sur les auteurs
----------------------------

La documentation a été écrite par les membres de la communauté du projet |project_name|. 

Elle a été mise en place sur la base du travail réalisé par le projet `GeoServer <http://geoserver.org>`_ et repose sur le projet `Sphinx <http://sphinx.pocoo.org/>`_. 

Si vous avez des questions, trouvez des anomalies ou bien avez des améliorations, 
contactez-nous via la liste de diffusion |project_name| à geonetwork-devel@lists.sourceforge.net.

Version francophone
```````````````````

- Benjamin Chartier
- Paul Hasenohr
- François-Xavier Prunayre
- Etienne Taffoureau

Qu'est ce que GeoNetwork ?
--------------------------

GeoNetwork est un système de gestion de données géographiques basé sur les standards.
Il est conçu pour permettre l'accès aux bases de données géoréférencées et aux produits cartographiques 
à partir d'une variété de fournisseurs de données via leur description, également appelée métadonnées. 
Il permet les échanges d'information et le partage entre les organisations et leur public, 
en utilisant les capacités et la puissance de l'Internet. 

Le système fournit à une large communauté d'utilisateurs un accès facile et rapide
aux données et services spatiaux disponibles, ainsi qu'aux cartes thématiques pour 
aider à la découverte d'information et à la prise de décision.
 
L'objectif principal du logiciel est d'accroître la collaboration au sein et entre les organisations 
afin de réduire les doublons, améliorer l'information (cohérence, qualité) pour améliorer 
l'accessibilité d'une grande variété d'informations géographiques avec les informations associées,
organisées et documentées de façon standard et uniforme.


Principales caractéristiques :

- Des recherches locales et distribuées
- Le téléchargement de données, documents, PDF et tout autre contenu
- Une carte interactive qui permet la combinaison des couches diffusées par les services WMS
- L'édition en ligne des métadonnées par un système de modèle
- Le moissonnage et la synchronisation des métadonnées entre catalogues distribués
- Groupes et gestion des utilisateurs
- Une interface multilingue


Histoire et évolution
---------------------

Le premier prototype du catalogue GeoNetwork a été développé par la FAO en 2001 
pour archiver et publier les données géographiques produites dans l'organisation. 
Ce prototype a été bâti sur les expériences au sein et en dehors de la FAO. 
Il a utilisé le contenu des métadonnées disponibles dans les systèmes existants 
en le transformant en ce qui n'était alors qu'un projet de
norme sur les métadonnées, l'ISO 19115. Plus tard, une autre agence de l'ONU, le Programme
Alimentaire Mondial (PAM) a rejoint le projet et a contribué à la première version du logiciel qui 
a été publié en 2003. Le système était basé sur le DIS (Draft International Standard) de l'ISO19115
et intégré le module InterMap pour la carte interactive. 
Les recherches distribuées était possible en utilisant la norme Z39.50.
A ce moment, il a été décidé de distribuer GeoNetwork en tant que logiciel libre afin de 
permettre à l'ensemble de la communauté géospatiale aux utilisateurs de bénéficier des
résultats de développement et de contribuer à l'avancement du projet.


Conjointement avec l'UNEP, la FAO a élaboré une deuxième version
en 2004. Cette nouvelle version permet aux utilisateurs de travailler avec plusieurs normes de métadonnées (ISO
19115, FGDC et Dublin Core) de manière transparente. Elle a également permis la mise en 
place de mécanisme de moissonnage et l'amélioration de la fiabilité lors de
recherches dans plusieurs catalogues.


Ensuite, les standards ISO ISO19139 pour les métadonnées de données et ISO19119 pour les métadonnées services ont été ajouté.
GeoNetwork a été l'implémentation  de référence pour le protocol OGC CSW 2.0.2 profile ISO.
Pour améliorer les échanges, de multiples protocoles de moissonnage sont disponibles : OAI-PMH, ESRI ArcSDE, 
CSW, Z39.50, OGC WxS, WFS, Système de fichier, Serveur WebDav, `Thredds <http://www.unidata.ucar.edu/projects/THREDDS/>`_.


Depuis 2009, des travaux ont également permis à GeoNetwork de prendre en compte les recommandations de la
`directive INSPIRE <http://eur-lex.europa.eu/JOHtml.do?uri=OJ:L:2007:108:SOM:EN:HTML>`_ 
en mettant en place des mécanismes avancés de validation, la saisie de métadonnées
en mode multilingue, la gestion des thésaurus au format SKOS tel que GEMET ou AGROVOC.


GeoNetwork est le résultat du travail de nombreux contributeurs avec le soutient
entre autres, des agences des nations unies (FAO, OCHA, CSI-GCRAI, UNEP, ...), 
l'Agence spatiale européenne (ESA), le CSIRO, le BRGM, Swisstopo, GeoNovum, ...



Qu'est ce que GeoSource ?
-------------------------

Initié en 2006 par la Direction Générale de la Modernisation de l'État, Geosource est un outil 
simple de catalogage des données et services à références spatiales, 
implémentant le profil français de la norme EN-ISO 19115:2005, et sa déclinaison en XML (ISO 19139).
Il est dérivé du projet GeoNetwork.

Le code source est accessible sur `github.com <http://github.com/geosource-catalogue/>`_.


GeoNetwork et sa communauté Open Source
---------------------------------------

La communauté des utilisateurs et des développeurs du logiciel GeoNetwork a augmenté
de façon spectaculaire depuis la sortie de la version 2.0 en Décembre 2005.
À l'heure actuelle, les listes de diffusion des utilisateurs et développeurs comptent
respectivement plus de 640 et 340 abonnés. L'abonnement à ces listes est ouvert à tous. 

`Les archives des listes de diffusion <http://osgeo-org.1803224.n2.nabble.com/GeoNetwork-opensource-f2013073.html>`_ 
constituent une source importante d'information. Les membres fournissent des informations, des traductions, 
de nouvelles fonctionnalités, des rapports de bugs, des correctifs et des
instructions pour le projet dans son ensemble. Bâtir une communauté d'utilisateurs et
de développeurs est l'un des plus grands défis pour un projet opensource. Ce
processus repose sur la participation active et les interactions entre ses
membres. Elle s'appuie également sur la confiance et le fonctionnement de manière transparente,
en définissant les objectifs généraux, les priorités et les orientations à long terme
du projet. Un certain nombre de mesures ont été prises par l'équipe du projet afin de
faciliter ce processus.


Conseil consultatif (Advisory Board) et Comité de pilotage (PSC)
----------------------------------------------------------------

La création d'un Conseil consultatif de |project_name| a été mis en place lors de
l'atelier de 2006 à Rome. Un plan de travail est présenté et discuté annuellement;


En 2006, le Conseil consultatif du projet a décidé de proposer le
projet |project_name| à l'incubation de l'
`Open Source Geospatial Foundation (OSGeo) <http://www.osgeo.org>`_.
Aujourd'hui, |project_name| est un projet de l'OSGeo.


Le Comité de pilotage de |project_name| (PSC) coordonne la direction générale,
les cycles de publication, la documentation pour le projet de |project_name|. 
En outre, la PSC s'occupe de l'assistance aux utilisateurs en général, 
il accepte et approuve les correctifs de la communauté |project_name| et
votes sur des questions diverses :

- tout ce qui peut causer des problèmes de compatibilité descendante.
- l'ajout de quantités importantes de nouveau code.
- les modifications de l'API.
- la définition de la gouvernance.
- lors de la sortie de nouvelle version.
- tout ce qui pourrait être sujet à controverse.
- ajouter un nouveau membre à la CFP
- ajouter un nouveau participant au dépôt de code


Le comité de pilotage est actuellement composé des personnes suivantes :

- Andrea Carboni
- Patrizia Monteduro
- Simon Pigot
- Francois Prunayre
- Emanuele Tajariol
- Jeroen Ticheler
- Archie Warnock


Contributeurs anciens et actuels
--------------------------------

Les développeurs actifs sur le trunk sont les suivants :

- Mathieu Coudert
- Heikki Doeleman
- Jose Garcia
- Jesse Eichar
- Roberto Giaccio
- Simon Pigot
- Francois Prunayre	
- Emanuele Tajariol	
- Jeroen Ticheler
- Archie Warnock



`D'autres contributeurs <http://trac.osgeo.org/geonetwork/wiki/committer_list>`_ sont également actifs dans les bacs à sable du projet.


Plus d'information
------------------

Sites web
`````````

Deux sites Web publics ont été créés :

- un pour les utilisateurs : http://geonetwork-opensource.org
- un pour les développeurs : http://trac.osgeo.org/geonetwork 

Les deux sont maintenus par des membres de confiance de la communauté. Ils offrent l'accès
à la documentation, les rapports de bugs, le suivi, le wiki, .... Une
partie de la communauté se connecte via Internet Relay Chat (IRC) sur le canal ``irc://irc.freenode.net/geonetwork``.
Cependant la majorité des dialogues a lieu sur
`la liste utilisateur anglophone <https://lists.sourceforge.net/mailman/listinfo/geonetwork-users>`_, 
`la liste utilisateur francophone <https://lists.sourceforge.net/mailman/listinfo/geonetwork-users-fr>`_, 
`liste de diffusion espagnole <https://lists.sourceforge.net/lists/listinfo/geonetwork-usuarios-es>`_ et 
`la liste développeur <https://lists.sourceforge.net/mailman/listinfo/geonetwork-devel>`_


Code source
```````````

Le code source est accessible sur `SourceForge.net <http://sourceforge.net/projects/geonetwork>`_ et `github.com <http://github.com/geonetwork/>`_.

Documentation
`````````````


La documentation est écrite dans le format reStructuredText et utilise `Sphinx <http://sphinx.pocoo.org>`_
pour la diffusion dans différents formats (e.g. HTML et
PDF).

