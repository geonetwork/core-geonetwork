.. _editor_gui:
.. include:: ../../substitutions.txt

L'interface d'édition
=====================


.. _metadata_edit_mode:

Les vues
--------

Les vues disponibles dans l'éditeur peuvent être configurée (cf. :ref:`how_to_config_edit_mode`).
Elles sont fonctions du standard de métadonnée utilisé. Les vues pour une métadonnée
en ISO sont différentes d'une métadonnée en dublin core.

La description ci-dessous présente les vues pour les métadonnées au format ISO.

La vue par défaut
`````````````````
La vue par défaut présente l'ensemble des champs remplis dans la fiche ou le modèle
utilisé. Elle permet d'avoir une vision simple de la métadonnée par contre il
ne sera pas possible de saisir des éléments non visible. Il faut alors passer
dans un autre mode, en général le mode avancé.


Cette vue est également disponible pour les autres standards.


La vue INSPIRE ou vue découverte
````````````````````````````````
Cette vue a été mise en place en ayant pour objectif d'organiser l'éditeur
tel que présenté dans les `régles d'implémentation sur les métadonnées
de la directive INSPIRE <http://inspire.jrc.ec.europa.eu/index.cfm/pageid/101>`_.

Cette vue se décompose de la manière suivante :

- Identification
- Classification des données ou service
- Mots-clés & thèmes INSPIRE
- Localisation géographique
- Référence temporelle
- Qualité et validité
- Conformité
- Constrainte d'accès et d'utilisation
- Organisations responsable de la réalisation, gestion, maintenance et distribution


La vue ISO
``````````
Les 3 onglets core, minimum et all reprennent les groupes d'information définis
par la norme ISO.


La vue complète
```````````````

Cette vue permet de visualiser et éditer **l'ensemble** des descripteurs 
du standard de la métadonnée. Les onglets correspondent aux grandes sections
de l'ISO.


Cette vue se décompose de la manière suivante :

- Métadonnées
- Identification
- Maintenance
- Contraintes
- Informations spatiales
- Système de référence
- Distribution
- Qualité des données
- Schéma d'application
- Catalogue
- Information sur le contenu
- Information sur les extensions



La vue XML
``````````

La **vue XML** montre l'ensemble du contenu de la
métadonnée dans la structure hiérarchique d'origine; La structure XML est
composée de balises, à chacune des balises doit correspondre une balise fermée. Le contenu est entièrement placé entre les
deux balises:

::

  <gmd:language>
    <gco:CharacterString>eng</gco:CharacterString>
  </gmd:language>



Cependant, l'utilisation de la vue XML requiert une connaissance minimale du
langage XML.

.. figure:: xmlView1.png

   Vue XML




Barre de menu
-------------

.. figure:: toolbar.png
   :scale: 85%
   
   Barre d'outil en mode édition
   
La barre de menu propose les actions suivantes :

- **Type de fiche** : une fiche est un modèle ou une métadonnée
- **Affichage** : une des vues possible pour le standard de la métadonnée en cours d'édition (cf. :ref:`metadata_edit_mode`)
- **Sauver** : sauvegarde la fiche
- **Sauver et valider** : sauvegarde et valide la fiche (cf. :ref:`metadata_validation`)
- **Sauver et fermer** : sauvegarde et ferme l'éditeur
- **Réinitialiser** : réinitialise le formulaire de saisie
- **Annuler** : annule la saisie en cours


La barre des titres de la fenêtre d'édition contient les mêmes options que la fenêtre de consultation (cf. :ref:`window_tools`)


