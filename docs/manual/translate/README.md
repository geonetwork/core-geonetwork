# Translation

A translate script is provided to facilitate working with pandoc and deepl translation services.

1. The script is run the ``core-genetwork/docs/manual`` folder:

   ```
   cd core-genetwork/docs/manual
   ```

2. Install script requirements, and check it runs:

   ```
   pip3 install -r translate/requirements.txt
   python3 -m translate
   ```

3. The script makes use of ``target`` folder for scratch files.

   ```
   mkdir target
   ```

This script requires ***pandoc*** be installed:

Ubuntu:
```bash
apt-get install pandoc
```

macOS:
``` bash
brew install pandoc
```

References:

* https://pandoc.org/installing.html

## Language Translation

Translations are listed alongside english markdown:

* `example.md`
* `example.fr.md`

Using ***pandoc*** to convert to `html`, and then using the [Deepl REST API](http://deepl.com).

4. Provide environmental variable with Deepl authentication key:

   ```
   export DEEPL_AUTH="xxxxxxxx-xxx-...-xxxxx:fx"
   ```

5. Translate a document to french using pandoc and deepl:

   ```
   python3 -m translate french docs/help/index.md
   ```
   
6. To translate several documents in a folder:

   ```
   python3 -m translate french docs/overview/*.md
   ```
   
   Deepl charges by the character so bulk translation not advisable.

See ``python3 -m translate french --help`` for more options.

You are welcome to use  google translate, ChatGPT, or Deepl directly - keeping in mind markdown formatting may be lost.

Please see the writing guide for what mkdocs functionality is supported.

## Format conversion from sphinx-build rst files

1. Copy everything over (so all the images and so on are present)
   
   ```
   cd core-geonetwork/docs
   copy -r manuals/source manual/doc
   ```
   
   Some problems, like tables, are easier to fix in the rst files before conversion.
   To copy any changes back.
   ```
   cd core-geonetwork/docs/manual/doc
   find . -name '*.rst' | cpio -pdm  ../../manuals/source
   ```

2. To index references in rst files into `docs/anchors.txt`:

   ```
   cd core-geonetwork/docs/manual
   python3 -m translate index
   ```

3. To bulk convert all content from ``rst`` to ``md``:
   
   ```
   cd core-geonetwork/docs/manual
   python3 -m translate rst docs/contributing/doing-a-release.rst
   ```

3. Convert a single file:
   
   ```
   cd core-geonetwork/docs/manual
   python3 -m translate rst docs/contributing/doing-a-release.rst
   ```

4. Bulk convert files in a folder:
   
   ```
   cd core-geonetwork/docs/manual
   python3 -m translate rst docs/introduction/**/*.rst
   ```

5. The ``.gitignore`` has been setup to ignore:
   
   * ``.rst``
   * ``.tmp.md``
   * ``conf.py``
   
   To clean up ``rst`` files when you are done:
   ```
   find . -type f -regex ".*\.rst" -delete 
   ```

### Manual review required

The process is not 100%, here are common problems to find during review:

* Broken reference or anchor links

* Indention of nested lists in ``rst`` is often incorrect, resulting in restarted number or block quotes.
  
  Correct the RST and re-covert
  
* Random ``{.title-ref}`` snippets is a general indication to simplify the rst and recovert
