# Markdown

These markdown conventions are carefully constructed for consistent representation of user interface elements, files, data and field input.


| Markdown                 | Directive                   |
|--------------------------|-----------------------------|
| `**strong**`             | gui label, menu selection   |
| `` `monospace` ``        | text input, item selection  |
| `*emphasis*`             | figure (caption)            |
| `***strong-emphasis***`  | application, command        |
| **`monospace-strong`**   | file                        |

!!! note

    The above conventions are important for consistency and are required by the translation process.
    
    As an example we do not wish to translate a search terms, so these are represented as monospace text input.

## User interface components

Use `**strong**` to name user interface components for interaction (press for buttons, click for link).

Preview:

> Navigate to **Data --> Layers** page, and press **Add** to create a new layer.

Markdown:

```markdown
Navigate to **Data --> Layers** page,
and press **Add** to create a new layer.
```

Rich structured text:

```rst
Navigate to :menuselection:`Data Layers` page,
and press :guilabel:`Add`` to create a new layer.
```

## User input

Use `` `item` `` for user supplied input, or item in a list or tree::

Preview:

> Select `Basemap` layer.

Markdown:

```markdown
Select `Basemap` layer.
```

Rich structured text:

```
Select ``Basemap`` layer.
```

Use `` `text` `` for user supplied text input:

Preview:

> Use the *Search* field enter `Ocean*`.

Markdown:

```markdown
Use the *Search* field enter `Ocean*`.
```

Rich structured text:

```
Use the :guilabel:`Serach` field to enter :kbd:`Ocean*`.
```

Use `++key++` for keyboard keys.

Preview:

> Press ++control+s++ to search.

Markdown:

```markdown
Press ++control+s++ to search.
```

Rich structured text:

```
Press :key:``Control-s`` to search.
```

Use definition list to document data entry. The field names use strong as they name a user interface element. Field values to input uses monspace as user input to type in.

Preview:

1.  To login as the GeoServer administrator using the default password:

    **User**

    :   `admin`

    **Password**

    :   `geoserver`

    **Remeber me**

    :   Unchecked

    Press **Login**.

Markdown: definition lists

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

Rich structured text: list-table

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

## Applications, commands and tools

Use **bold** and *italics* for proper names of applications, commands, tools, and products.

Preview:

Launch ***pgAdmin*** and connect to the databsae `tutorial`.

Markdown:

```markdown
Launch ***pgAdmin*** and connect to the databsae `tutorial`.
```

Rich structured text:

```
Launch :command:`pgAdmin` and connect to the ``tutorial`` database.
```

## Files

Use **bold** **monospace** for files and folders:

Preview See configuration file **`WEB-INF/config-security/config-security-ldap.xml`** for details

Markdown:

```markdown
See configuration file
**`WEB-INF/config-security/config-security-ldap.xml`**
for details
```

Rich structured text:

```
See configuration
file :file:`WEB-INF/config-security/config-security-ldap.xml`
for details
```

## Links and references

Specific kinds of links:

Reference to other section of the document (some care is required to reference a specific heading):

Editors have option to [manage](index.md) records.

```
Editors have option to :ref:`manage <Publish records>` records.
Editors have option to [manage](../editor/publish/index.md#publish-records) records.
```

Download of sample files:

Example:

> Download schema [**`example.xsd`**](files/example.xsd).

```
Download schema :download:`example.xsd <files/example.xsd>`.
Download schema [**`example.xsd`**](files/example.xsd).
```

## Icons, Images and Figures

Material for markdown has extensive icon support, for most user interface elements we can directly make use of the appropriate icon in Markdown:

```markdown
1.  Press the *Validate :fontawesome-solid-check:* button at the top of the page.
```

Add cusotm icons to **`overrides/.icons/geocat`**:

```markdown
Thank you from the GeoCat team!
:geocat-logo:
```

Figures are handled by convention, adding emphasized text after each image, and trust CSS rules to provide a consistent presentation:

```markdown
![](img/begin_date.png)
*Value is required for Begin Date*
```

Raw images are not used very often:

```markdown
![](img/geocat-logo.png)
```

## Tables

Documentation uses pipe-tables only (supported by both ***mkdocs*** and ***pandoc***):

Leading / tailing `|`:

| First Header | Second Header | Third Header |
|--------------|---------------|--------------|
| Content Cell | Content Cell  | Content Cell |
| Content Cell | Content Cell  | Content Cell |

Column alignment using `:`

| First Header | Second Header | Third Header |
|:-------------|:-------------:|-------------:|
| Left         |    Center     |        Right |
| Left         |    Center     |        Right |

## Inline

Here is a snippet to include markdown files inline, requires opening tag ``{%`` and closing tag ``%}``:

``` markdown
{ %
   include-markdown './version-4.2.4.md'
   heading-offset=3
% }
```

Use a glob pattern to inline many files, shown with option to adjusting header level:

``` markdown
{ %
   include-markdown './version-4.0.*.md'
   exclude = './version-4.2.4.md'
   heading-offset = 3
% }
```

For including markdown files inline, we have to exclude them from ``mkdocs.yml`` warnings:

``` yaml
plugins:
  - exclude:
      glob:
        - 'overview/change-log/version*'
```

Use ``include`` to include normal files, shown with start and end to capture a snippet, and dedent for appearance:

``` markdown
{ %
      include 'record.xml'
      dedent="true"
      start="<!--start-->"
      end="<!--end-->"
% }
```

Reference:

* [mkdocs-include-markdown-plugin](https://pypi.org/project/mkdocs-include-markdown-plugin/)
* [mkdocs-exclude](https://pypi.org/project/mkdocs-exclude/)
