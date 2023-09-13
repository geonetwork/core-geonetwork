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
   copy -r manuals/source manual/doc
   ```

2. Convert file by file:
   
   ```
   python3 -m translate rst docs/contributing/doing-a-release.rst
   ```

3. Bulk convert files in a folder:
   
   ```
   python3 -m translate rst docs/contributing/*.rst
   ```

4. The ``.gitignore`` has been setup to ignore:
   
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

5. Optional: to assist with anchors:

   ```
   cd docs
   grep --include=\*.rst -rnw . -e "^.. _.*:$" > anchors.txt
   ```

   Grep search and replace (I used an editor):

   * ``^\./`` -->  `` ``
   * ``^([\w\-/\.\d_]+)\.rst:\d*\:\.\.\s+_([\w\-\d_\s\./]+):$`` --> ``\2=/\1.md#\2```
   ```
   
   sed?
   ```
   sed -i '' -e 's;^\./;;' anchors.txt
   sed -i '' -e 's;^([\w\-/\.\d_]+)\.rst:\d*\:\.\.\s+_([\w\-\d_\s\./]+):$;\2=/\1.md#\2;' anchors.txt
   ```

### manual review required

Pandoc conversion from rst to md:

* ``toc:`` not supported, requires manual editing (if that is even appropriate)
* notes, warnings, info
* anchors

