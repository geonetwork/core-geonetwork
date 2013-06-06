.. _introduction:
.. include:: ../substitutions.txt

Introduction aux métadonnées
============================

Qu'est ce qu'une métadonnée ?
-------------------------------

Les métadonnées sont généralement définies comme "données sur les données" ou "information sur les données".
Les métadonnées sont une liste structurée d'information qui décrivent les données ou les services
(incluant les données numériques ou non) stockés dans les systèmes d'information.
Les métadonnées peuvent contenir une brève description sur le contenu, les objectifs,
la qualité et la localisation de la donnée ainsi que les informations relatives à sa création.


Les standards pour les métadonnées géographiques
------------------------------------------------

L'utilisation de standards permet aux utilisateurs d'avoir une terminologie
commune permettant la réalisation de recherche efficace pour la découverte des données
dans les catalogues. Les métadonnées reposant sur les standards
permettent d'avoir un même niveau d'information et d'éviter la perte
de connaissance sur les données.

Les principaux standards sont les suivants :

- Dublin Core : "un schéma de métadonnées générique qui permet de décrire des ressources numériques ou physiques et d’établir des relations avec d'autres ressources. Il comprend officiellement 15 éléments de description" [#]_

- ISO 19139 pour les métadonnées de données

- ISO 19119 pour les métadonnées de services

- ISO 19110 pour la description des catalogues d'attributs

- FGDC, le standard de métadonnée adopté par les Etats-Unis / Federal Geographic Data Committee


.. TODO : décrire plus longuement ces standards


|project_name| supporte également d'autres standards : :ref:`supported_format`.

Les données géographiques sont souvent produites par des organisations ou des indépendants
et peuvent répondre aux besoins de différents types d'utilisateurs (opérateurs SIG,
analyse d'image, politiques, ...). Une documentation adéquate sur les données
aide à mieux définir la pertinence de ces informations pour la production, l'utilisation
et la mise à jour.

L'ISO définit en détail comment décrire les ressources dans le domaine de l'information
géographique tel que les données ou les services. Ce standard précise les descripteurs
obligatoires et conditionels. Il s'applique aux séries de données, aux données, aux
objets géographiques ainsi qu'à leurs propriétés. Bien que
l'ISO 19115:2003 ai été conçu pour les données numériques, ces principes peuvent
être étendus à d'autres types de ressources tels que les cartes, les graphiques,
les documents ou les données non géographiques.

Le format d'échange de l'ISO19115:2003 est XML. |project_name|
utilise ISO Technical Specification 19139/119 Geographic information - Metadata -
XML schema implementation pour l'encodage XML de l'ISO19115.


Les profils de métadonnées
---------------------------

|project_name| supporte plusieurs standards de métadonnées. Ces standards peuvent prendre la forme
de modèles de métadonnées qu'il est possible de créer via l'éditeur.
En utilisant la vue avancée de l'éditeur, tous les éléments sont potentiellement accessibles
à l'utilisateur.

Le support d'extensions ou de nouveaux standards spécifiques peut également être mis en place
par des développeurs via le mécanisme de standard enfichable.

Les standards actuellement supportés sont listés en annexe : :ref:`supported_format`.

Ces standards sont utilisés dans les projets Bluenet, geocat.ch et GéoSource.

.. TODO : Donner des exemples de profils et pointer vers le chapitre "Ajouter un profil". Citer par exemple les profils INSPIRE.


.. [#] http://fr.wikipedia.org/wiki/Dublin_Core
