# Translation

The ``mkdocs-static-i18n`` plugin is setup based on suffix, with **`index.md`** is the default English, and **``index.fr.md``** used for French:

``` text
| index.md
| index.fr.md
+ img/
  | figure.png
  + figure.fr.png
```

The mkdocs-material-theme language chooser is configured by this plugin allowing the selection of language at runtime.

Reference:

* [mkdocs-static-i18n](https://ultrabug.github.io/mkdocs-static-i18n/)

## Deepl Translation

Translation uses ***pandoc*** to convert to `html` for conversion by ***Deepl***.

Specific ***pandoc*** extensions are used to match the capabilities of ***mkdocs***:

| markdown extension   | pandoc extension       |
|----------------------|------------------------|
| tables               | pipe_tables            |
| pymdownx.keys        |                        |
| pymdownx.superfences | backtick_code_blocks   |
| admonition           | fenced_divs            |

The differences differences in markdown requires pre/post processing of markdown and html files. These steps are automated in the ***translate*** python script (check comments for details). Supporting additional ***mkdocs*** features requires updating this script.

1. Install [mkdocs_translate](https://github.com/jodygarnett/translate) python script:
   
   ```
   pip3 install git+https://github.com/jodygarnett/translate.git
   ```

2. To translate provide environmental variable with Deepl authentication key:

   ```
   cd core-geonetwork/docs
   mkdir target
   export DEEPL_AUTH="xxxxxxxx-xxx-...-xxxxx:fx"
   ```

3. And translate a file:

   ``` bash
   mkdocs_translate french manual/docs/contributing/style-guide.md
   ```
   
4. To test each stage individually:

   ```
   mkdocs_translate internal-html manual/docs/contributing/style-guide.md
   mkdocs_translate internal-document target/contributing/style-guide.html target/contributing/style-guide.fr.html
   mkdocs_translate internal-markdown target/contributing/style-guide.fr.html

   cp target/contributing/style-guide.fr.md manual/docs/contributing/style-guide.fr.md
   ```

5. To test markdown / html round trip:

   ```
   mkdocs_translate convert manual/docs/contributing/style-guide.md
   mkdocs_translate markdown target/contributing/style-guide.html

   diff manual/docs/contributing/style-guide.md target/contributing/style-guide.md
   ```
