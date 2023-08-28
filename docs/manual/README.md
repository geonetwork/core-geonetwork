## Pandoc Document Conversion

The [translation](../translate) module makes use of document conversion between markdown and html.
As a result some markdown extensions cannot be used:

* ``simple_tables`` cannot be used by github-flavored-markdown, use ``pipe_tables`` instead

Pandoc used for initial conversion from rich-structured-text to markdown:
```
cp -r src/sphinx docs
cd docs
find . -name \*.rst -type f -exec pandoc -o {}.md {} \;
```

Not all sphinx-build directives are supported, you may see junk like:

```
``{.interpreted-text role="guilabel"} **Cancel**
`waterways`{.interpreted-text role="kbd"} `waterways`
```

To prevent this use search/replace to pre-process:

* ``:gui-label:`text` `` changes to `**text**`
* ``:menuselection:`text` `` changes to `**text**`
* ``:kbd:`text` `` changes to ` ``text`` `


The ``translate`` python module can do this for individual files:
```
python3 translate sphinx docs/example.rst
```
