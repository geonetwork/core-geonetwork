# Translation

Translations are listed alongside english markdown:

* `example.md`
* `example.fr.md`

Using ***pandoc*** to convert to `html`, and then using the [Deepl REST API](http://deepl.com).

1. The script is run from your ``core-genetwork/docs`` folder:

   ```
   cd docs
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

4. Provide environmental variable with Deepl authentication key:

   ```
   export DEEPL_AUTH="xxxxxxxx-xxx-...-xxxxx:fx"
   ```

5. Translate using pandoc and deepl:

   ```
   python3 -m translate french docs/example.md
   ```

See ``python3 -m translate --help`` for more options.

You are welcome to use  google translate, ChatGPT, or Deepl directly - keeping in mind markdown formatting may be lost.

Please see the writing guide for what mkdocs functionality is supported.

## sphinx-build notes

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
   cd core-geonetwork/docs
   python3 -m translate index manual/docs/ manual/docs/**/*.rst
   ```

3. Convert a single file:
   
   ```
   cd core-geonetwork/docs
   python3 -m translate rst manual/docs/contributing/doing-a-release.rst
   ```

4. Bulk convert files in a folder:
   
   ```
   cd core-geonetwork/docs
   python3 -m translate rst docs/**/*.rst
   ```

5. The ``.gitignore`` has been setup to ignore:
   
   * ``.rst``
   * ``.tmp.md``
   * ``conf.py``
   
   To clean up ``rst`` files when you are done:
   
   ```
   find . -type f -regex ".*\.rst" 
   ```
   
   And then remove:
   ```
   find . -type f -regex ".*\.rst" -delete 
   ```


### Manual review required

The process is not 100%, here are common problems to find during review:

* Broken reference or anchor links

* Indention of nested lists in ``rst`` is often incorrect, resulting in restarted number or block quotes.
  
  Correct the RST and re-covert
  
* Random ``{.title-ref}`` snippets is a general indication to simplify the rst and recovert
