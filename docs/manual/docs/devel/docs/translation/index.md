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

Specific ***pandoc*** extensions are used to match the capabilities of ***mkdocs***.

| mkdocs extension | pandoc extension |
|------------------|------------------|
| tables           | pipe_tables      |

Other differences in markdown requires pre/post processing of markdown and html files. These steps are automated in the ***translate*** python script (check comments for details).

To translate provide environmental variable with Deepl authentication key:

```
cd core-geonetwork/docs
mkdir target
export DEEPL_AUTH="xxxxxxxx-xxx-...-xxxxx:fx"
```

And translate a file:
``` bash
python3 -m translate fr manual/docs/contributing/style-guide.md
```

To test each stage individually:

```
python3 -m translate html manual/docs/contributing/style-guide.md
python3 -m translate document target/contributing/style-guide.html target/contributing/style-guide.fr.html
python3 -m translate markdown target/contributing/style-guide.fr.html

cp target/contributing/style-guide.fr.md manual/docs/contributing/style-guide.fr.md
```

To test markdown / html only:

```
python3 -m translate convert manual/docs/contributing/style-guide.md
python3 -m translate markdown target/contributing/style-guide.html

diff manual/docs/contributing/style-guide.md target/contributing/style-guide.md
```
