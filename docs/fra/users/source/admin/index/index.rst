.. include:: ../../substitutions.txt

.. _lucene_index:

Paramètres de l'index
=====================


Gestion de l'index
~~~~~~~~~~~~~~~~~~

A partir de la page d'administration, il est possible de réaliser les actions suivantes :

- Définir les mots vides ou à ignorer lors de l'indexation : Cette option n'est valable que si un GeoNetworkAnalyzer est utilisé
  dans la configuration de l'index (cf :ref:`lucene_index_adv`).
- Regénérer l'index du catalogue
- Optimisation de l'index Lucene
- Recharger la configuration Lucene
- Vider le cache d'XLink et reconstruire l'index

.. figure:: index.png



.. _lucene_index_adv:

Configuration avancée
~~~~~~~~~~~~~~~~~~~~~

Le fichier WEB-INF/config-lucene.xsl permet une configuration fine de l'index du catalogue.

Il est possible de configurer :

- Paramètre de l'index :

 - RAMBufferSizeMB
 
 - MergeFactor
 
 - LuceneVersion
 
- Recherche

 - Calcul du score (**trackDocScores**) : Affecte les performances de recherche
 
 - Possibilité d'améliorer la pertinence des résultats en définition des "Boosters" (eg. RecencyBoostingQuery, stopwords)
 
- Indexation

 - Définition des "Analyzers" pour chaque champs
 
 - Définition des listes de mots à ignorer (stopwords)
 
 - Défintion des champs à découper lors de l'analyze
 
 - Définition des champs numérique



.. figure:: lucene-config.png

Il est possible de modifier à chaud (catalogue opérationnel sans redémarrage) cette configuration, 
puis de la recharger avec le bouton dans l'administration. En général après toute modification de ce fichier,
il est recommandé de regénerer l'index.

