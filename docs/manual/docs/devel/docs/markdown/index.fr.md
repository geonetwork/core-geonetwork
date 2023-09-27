# Documentation

## Guide de rédaction {#writing-guide}

GeoCat a fourni un [guide d\'écriture](https://geocat.github.io/geocat-themes/) pour la documentation sphinx. Bien que les conventions d\'écriture doivent être respectées, l\'adaptation des directives sphinx au formatage markdown nécessite un certain travail.

Lors de la conversion en markdown, nous pouvons nous concentrer uniquement sur l\'aspect visuel, en convertissant de nombreuses directives sphinx en leur équivalent visuel le plus proche :

| Markdown                 | Directive Sphinx                       |
|--------------------------|----------------------------------------|
| `**strong**`             | gui-label, menuselection               |
| `` `monospace` ``        | saisie de texte, sélection d\'éléments |
| `*emphasis*`             | chiffre (légende)                      |
| `***strong-emphasis***`  | commande                               |
| `` `monospace-strong` `` | fichier                                |

Veuillez noter que les conventions ci-dessus sont importantes pour la cohérence et sont requises par le processus de traduction.

### Composants de l\'interface utilisateur {#user-interface-components}

Utilisez `**strong**` pour nommer les composants de l\'interface utilisateur à des fins d\'interaction (appuyer pour les boutons, cliquer pour les liens).

Avant-première :

> Accédez à la page **Couches de données** et cliquez sur **Ajouter** pour créer une nouvelle couche.

Markdown :

```markdown
Navigate to **Data Layers** page,
and press **Add** to create a new layer.
```

Texte riche et structuré :

```rst
Navigate to :menuselection:`Data Layers` page,
and press :guilabel:`Add`` to create a new layer.
```

### Données de l\'utilisateur {#user-input}

Utiliser `` `item` `` pour les données fournies par l\'utilisateur, ou les éléments d\'une liste ou d\'un arbre: :

Avant-première :

> Sélectionnez `Basemap` couche.

Markdown :

```markdown
Select `Basemap` layer.
```

Texte riche et structuré :

```
Select ``Basemap`` layer.
```

Utiliser `` `text` `` pour la saisie du texte fourni par l\'utilisateur :

Avant-première :

> Utilisez le champ de *recherche pour* saisir `Ocean*`.

Markdown :

```markdown
Use the *Search* field enter `Ocean*`.
```

Texte riche et structuré :

```
Use the :guilabel:`Serach` field to enter :kbd:`Ocean*`.
```

Utilisez `++key++` pour les touches du clavier.

Avant-première :

> Appuyez sur ++Control-s++ pour effectuer une recherche.

Markdown :

```markdown
Press ++control+s++ to search.
```

Texte riche et structuré :

```
Press :key:``Control-s`` to search.
```

Utilisez une liste de définitions pour documenter la saisie des données. Les noms des champs utilisent strong car ils nomment un élément de l\'interface utilisateur. Les valeurs des champs à saisir utilisent monspace car il s\'agit d\'une entrée utilisateur à saisir.

Avant-première :

1.  Pour vous connecter en tant qu\'administrateur du GeoServer en utilisant le mot de passe par défaut :

    **Utilisateur**

    :   `admin`

    **Mot de passe**

    :   `geoserver`

    **Souvenez-vous de moi**

    :   Non vérifié

    **Connexion** presse.

Markdown : listes de définitions

```markdown
1.  To login as the GeoServer administrator using the default password:

    **User**

    :   `admin`

    **Password**

    :   `geoserver`

    **Remeber me**

    :   Unchecked

    Press **Login**.
```

Texte structuré riche : liste-tableau

```
#. To login as the GeoServer administrator using the default password:

   .. list-table::
      :widths: 30 70
      :width: 100%
      :stub-columns: 1

      * - User:
        - :kbd:`admin`
      * - Password:
        - :kbd:`geoserver`
      * - Remember me
        - Unchecked
   
   Press :guilabel:`Login`.
```

### Applications, commandes et outils {#applications-commands-and-tools}

Utilisez les **caractères gras** et *italiques* pour les noms propres d\'applications, de commandes, d\'outils et de produits.

Avant-première :

Lancez ***pgAdmin*** et connectez-vous à la base de données `tutorial`.

Markdown :

```markdown
Launch ***pgAdmin*** and connect to the databsae `tutorial`.
```

Texte riche et structuré :

```
Launch :command:`pgAdmin` and connect to the ``tutorial`` database.
```

### Fichiers {#files}

Utilisez le **gras** **monospace** pour les fichiers et les dossiers :

Aperçu Voir le fichier de configuration **`WEB-INF/config-security/config-security-ldap.xml`** pour plus de détails

Markdown :

```markdown
See configuration file
**`WEB-INF/config-security/config-security-ldap.xml`**
for details
```

Texte riche et structuré :

```
See configuration
file :file:`WEB-INF/config-security/config-security-ldap.xml`
for details
```

### Liens et références {#links-and-references}

Types de liens spécifiques :

Référence à une autre section du document (une certaine attention est requise pour faire référence à un titre spécifique) :

Les éditeurs ont la possibilité de [gérer les](index.md) enregistrements.

```
Editors have option to :ref:`manage <Publish records>` records.
Editors have option to [manage](../editor/publish/index.md#publish-records) records.
```

Téléchargement de fichiers d\'échantillons :

Exemple :

Télécharger le schéma [**`example.xsd`**](files/example.xsd).

```
Download schema :download:`example.xsd <files/example.xsd>`.
Download schema [**`example.xsd`**](files/example.xsd).
```

### Icônes, images et figures {#icons-images-and-figures}

Material for markdown dispose d\'une prise en charge étendue des icônes. Pour la plupart des éléments de l\'interface utilisateur, il est possible d\'utiliser directement l\'icône appropriée dans Markdown :

```markdown
1.  Press the *Validate :fontawesome-solid-check:* button at the top of the page.
```

Ajoutez les icônes du cusotm à **`overrides/.icons/geocat`**:

```markdown
Thank you from the GeoCat team!
:geocat-logo:
```

Les figures sont traitées par convention, en ajoutant un texte en relief après chaque image et en faisant confiance aux règles CSS pour assurer une présentation cohérente :

```markdown
![](img/begin_date.png)
*Value is required for Begin Date*
```

Les images brutes ne sont pas utilisées très souvent :

```markdown
![](img/geocat-logo.png)
```

### Tableaux {#tables}

La documentation n\'utilise que des tables de pipes (supportées par ***mkdocs*** et ***pandoc***) :

En tête / en queue `|`:

| Première tête      | Deuxième en-tête   | Troisième en-tête  |
|--------------------|--------------------|--------------------|
| Cellule de contenu | Cellule de contenu | Cellule de contenu |
| Cellule de contenu | Cellule de contenu | Cellule de contenu |

Alignement des colonnes à l\'aide de `:`

| Première tête | Deuxième en-tête | Troisième en-tête |
|:--------------|:----------------:|------------------:|
| Gauche        |      Centre      |             Droit |
| Gauche        |      Centre      |             Droit |

## Traduction {#translation}

La traduction utilise ***pandoc*** pour convertir en `html` pour la conversion par ***Deepl***.

Des extensions spécifiques de ***pandoc*** sont utilisées pour s\'adapter aux capacités de ***mkdocs***.

| extension mkdocs | extension pandoc |
|------------------|------------------|
| tables           | tables_pipe      |

D\'autres différences dans le markdown nécessitent un traitement avant/après des fichiers markdown et html. Ces étapes sont automatisées dans le script python ***translate*** (consultez les commentaires pour plus de détails).

Pour traduire une variable environnementale en clé d\'authentification Deepl :

```
export DEEPL_AUTH="xxxxxxxx-xxx-...-xxxxx:fx"
```

Pour tester chaque étape individuellement :

```
python3 -m translate html docs/devel/docs/docs.md
python3 -m translate document target/translate/devel/docs/docs.html target/translate/devel/docs/docs.fr.html
python3 -m translate markdown target/translate/devel/docs/docs.fr.html

cp target/translate/devel/docs/docs.fr.md docs/devel/docs/docs.fr.md
```

Pour tester les formats markdown / html uniquement :

```
python3 -m translate convert docs/devel/docs/docs.md
python3 -m translate markdown target/translate/devel/docs/docs.html

diff  docs/devel/docs/docs.md target/translate/devel/docs/docs.md 
```
