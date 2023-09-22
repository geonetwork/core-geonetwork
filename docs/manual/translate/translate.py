import deepl
import errno
import glob
import logging
import os
import pkgutil
import re
import requests
import subprocess
import yaml

from translate import __app_name__, __version__

logger = logging.getLogger(__app_name__)

# global configuration setup by cli._config_callback
config = {}
docs_folder = None
rst_folder = None
upload_folder = None
convert_folder = None
download_folder = None
anchor_file = None

anchors = {}
docs_folder = None

md_extensions_to = 'markdown+definition_lists+fenced_divs+backtick_code_blocks+fenced_code_attributes-simple_tables+pipe_tables'
md_extensions_from = 'markdown+definition_lists+fenced_divs+backtick_code_blocks+fenced_code_attributes+pipe_tables'

#
# CLI SUPPORT AND CONFIGURATION
#
def load_auth() -> str:
    """
    Look up DEEPL_AUTH environmental variable for authentication.
    """
    AUTH = os.getenv('DEEPL_AUTH')
    if not AUTH:
       raise ValueError('Environmental variable DEEPL_AUTH required for translate with Deepl REST API')

    return AUTH

def load_config(override_path: str) -> dict:
    """
    Load config.yml application configuration.
    :param override_path: Overide config location, or None to use built-in default configuration
    """
    if override_path:
        # override configuration
        with open(override_path, 'r') as file:
            text = file.read()
            return yaml.safe_load(text)
    else:
        # default configuration
        raw = pkgutil.get_data('translate', "config.yml")
        return yaml.safe_load(raw.decode('utf-8'))

def init_config(override_path: str) -> None:
    """
    Initialize using provided config
    :param override_path: Overide config location, or None to use built-in default configuration
    """
    global config
    global docs_folder
    global upload_folder
    global convert_folder
    global download_folder
    global rst_folder
    global anchor_file

    config = load_config(override_path)

    docs_folder = os.path.join(config['project_folder'],config['docs_folder'])
    upload_folder = os.path.join(config['project_folder'],config['build_folder'],config['upload_folder'])
    convert_folder = os.path.join(config['project_folder'],config['build_folder'],config['convert_folder'])
    download_folder = os.path.join(config['project_folder'],config['build_folder'],config['download_folder'])
    anchor_file = os.path.join(docs_folder,config['anchor_file'])

    rst_folder = docs_folder
    if 'rst_folder' in config:
        rst_folder = config['rst_folder']

    if not os.path.exists(docs_folder):
       raise FileNotFoundError(errno.ENOENT, f"The docs folder does not exist at location:", docs_folder)

    if not os.path.exists(rst_folder):
       raise FileNotFoundError(errno.ENOENT, f"The rst folder does not exist at location:", rst_folder)

    logger.debug('--- start configuration ---')
    logger.debug('  mkdocs: %s',docs_folder)
    logger.debug('  sphinx: %s',rst_folder)
    logger.debug('  upload: %s',upload_folder)
    logger.debug('download: %s',download_folder)
    logger.debug('    docs: %s',docs_folder)
    logger.debug(' anchors: %s',anchor_file)
    logger.debug('--- end configuration ---')
    return

def load_anchors(anchor_txt:str) -> dict[str,str]:
    """
    load anchors reference of the form:
       reference=/absolut/path/to/file.md#anchor
    """
    if not os.path.exists(anchor_txt):
       logger.warning("Anchors definition file not avaialble - to creae run: python3 -m translate index")
       raise FileNotFoundError(errno.ENOENT, f"anchors definition file does not exist at location:", anchor_txt)

    index = {}
    with open(anchor_txt,'r') as file:
       for line in file:
         if '=' in line:
            (anchor,path) = line.split('=')
            index[anchor] = path[0:-1]

    return index

def init_anchors():
    global anchors
    global anchor_file
    anchors = load_anchors(anchor_file)
    logging.debug("anchors loaded:"+str(len(anchors)))

def collect_path(path: str, extension: str) -> list[str]:
    """
    Collect all the files with an extension from a path.
    If the path is a single file the extension should match.
    """
    files = []
    if '*' in path:
      for file in glob.glob(path,recursive = True):
         if file.endswith('.'+extension):
           files.append(file)
    else:
      if path.endswith('.'+extension):
        files.append(path)

    return files

def collect_paths(paths: list[str], extension: str) -> list[str]:
    """
    Collect all the files with an extension from a list of paths.
    If the path is a single file the extension should match.
    """
    files = []

    for path in paths:
       files.extend( collect_path(path,extension) )

    return files

#
# RST INDEX MANAGEMENT AND USE
#
def index_rst(base_path: str, rst_file: str) -> str:
    """
    Scan through rst_file producing doc and ref indexs
    """
    if not os.path.exists(base_path):
       raise FileNotFoundError(errno.ENOENT, f"RST base_path does not exist at location: {base_path}")

    common_path = os.path.commonpath([base_path,rst_file])
    if common_path != base_path:
       raise FileNotFoundError(errno.ENOENT, f"RST base_path '{base_path}' does not contain rst_file: '{rst_file}'")

    with open(rst_file, 'r') as file:
        text = file.read()

    relative_path = rst_file[len(base_path):]
    doc = relative_path
    ref = None
    heading = None
    index = ''

    with open(rst_file, 'r') as file:
        text = file.read()

    lines = text.splitlines()

    for i in range(0,len(lines)):
        line = lines[i]
        if len(line) == 0:
            continue

        if ref:
            heading = scan_heading(i,lines)
            if heading:
                logging.debug(" +- heading:"+heading)
                anchor = ref
                if doc:
                    # reference to doc heading, no need for anchor
                    index += ref + '=' + relative_path + "\n"
                else:
                    index += ref + '=' + relative_path + '#' + ref + "\n"
                index += ref + '.title=' + heading + "\n"
                ref = None

        if doc:
            heading = scan_heading(i,lines)
            if heading:
                logging.debug(" +- page:"+heading)
                index += doc + '=' + relative_path + "\n"
                index += doc + '.title=' + heading + "\n"
                doc = None

        match = re.search(r"^.. _((\w|.|-)*):$", line)
        if match:
            if ref:
                logging.warning("reference "+ref+" defined without a heading, skipped")

            ref = match.group(1)
            logging.debug(" |   ref:"+ref)

    return index

def scan_heading(index: int, lines: list[str] ) -> str:
    """
    Detect and return headline

    @return headline, or None
    """
    # Scan line by line for references and headings
    # # with overline, for parts
    h1 = '#############################################################################################################'
    # * with overline, for chapters
    h2 = '*************************************************************************************************************'
    # =, for sections
    h3 = '============================================================================================================='
    # -, for subsections
    h4 = '-------------------------------------------------------------------------------------------------------------'
    # ^, for subsubsections
    h5 = '^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^'
    # “, for paragraphs
    h6 = '"""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""'
    h7 = "`````````````````````````````````````````````````````````````````````````````````````````````````````````````"
    h8 = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
    if index >= len(lines)-1:
       return None # last line cannot be a heading

    line = lines[index]
    line_length = len(line)
    under = lines[index+1]
    under_length = len(under)

    if under_length < line_length:
       return None # not a heading

    if under == h1[0:under_length] or \
       under == h2[0:under_length] or \
       under == h3[0:under_length] or \
       under == h4[0:under_length] or \
       under == h5[0:under_length] or \
       under == h6[0:under_length] or \
       under == h7[0:under_length] or \
       under == h8[0:under_length]:

       return line

    return None

def _doc_title(rst_path: str, doc_link: str) -> str:
   """
   Create a label title, based on a documentation link (using on anchors.txt index)
   """
   resolved_path = _doc_location(rst_path,doc_link)
   # example:
   #   /install-guide/loading-samples.rst=/install-guide/loading-samples.rst
   #   /install-guide/loading-samples.rst.title=Loading templates and sample data
   title_key = resolved_path+'.rst.title'
   if title_key in anchors:
       return anchors[title_key]
   else:
       label = _label(doc_link)
       logger.warning("broken doc '"+doc_link+"' title:"+label)
       return label

def _doc_location(rst_path: str, doc_link: str) ->  str:
    """
    Determines absolute path location for link, relative to provided path.

    Do not use as is, mkdocs only works with relative links

    :param rst_path: path of rst file providing the documentation link
    :param doc_link: documentation link (may be absolute or relative)
    :return: absolute path, indicating location relative to docs folder. Used for looking up title.
    """
    if doc_link.startswith("/"):
        return doc_link
    else:
        dir = os.path.dirname(rst_path)
        link_path = os.path.join(dir, doc_link)
        location = os.path.relpath(link_path, docs_folder)
        return '/'+location

def _label(link: str) -> str:
   """
   Create a default label, based on a doc link or reference.
   """
   label = link.replace('.rst','')
   label = label.replace('.md','')
   label = label.replace('/index', '')
   label = label.replace('-', ' ')
   label = label.replace('_', ' ')
   label = label.replace('/', ' ')
   label = label.title()

   return label

def _ref_title(reference: str) -> str:
    """
    Determines title for reference (using on anchors.txt index)
    :param reference: reference to document or heading
    :return: title for reference
    """
    title_lookup = reference+".title"

    if title_lookup in anchors:
       return anchors[title_lookup]
    else:
       label = _label(reference)
       logger.warning("broken reference '"+reference+"' title:"+label)
       return label

def _ref_location(reference: str) ->  str:
    """
    Determines absolute path#anchor for reference (using on anchors.txt index)
    :param reference: reference to document or heading
    :return: absolute path, indicating location relative to docs folder. Used to determine a relative path.
    """
    if reference in anchors:
       return anchors[reference]
    else:
       link = reference+"-broken.rst"
       logger.warning("broken reference '"+reference+"' link:"+link)
       return link

def _ref_path(rst_path: str, reference: str) ->  str:
    """
    Generate a relative link for the provided reference.
    """
    logging.debug("ref: "+reference)
    ref_location = _ref_location(reference)

    rst_location = os.path.relpath(os.path.dirname(rst_path),docs_folder)
    if ref_location.startswith("/"):
        ref_location = ref_location[1:]

    logging.debug("--: "+rst_location)
    logging.debug("--> "+ref_location)

    link = os.path.relpath(ref_location,rst_location)

    logging.debug("--> "+link)
    return link


#
# RST PANDOC CONVERSION
#
def convert_rst(rst_file: str) -> str:
    """
    Use pandoc to convert rich-structured-text file to markdown file for mkdocs
    :param md_file: Markdown file path
    :return: markdown file path
    """
    if not os.path.exists(rst_file):
       raise FileNotFoundError(errno.ENOENT, f"RST file does not exist at location:", rst_file)

    if rst_file[-4:] != '.rst':
       raise FileNotFoundError(errno.ENOENT, f"Rich-structued-text 'rst' extension required:", rst_file)

    # file we are generating
    md_file = rst_file.replace(".txt",".md")
    md_file = md_file.replace(".rst",".md")

    # temp file for processing
    md_tmp_file = re.sub("^docs/",convert_folder+'/', rst_file)
    md_tmp_file = md_tmp_file.replace(".txt",".md")
    md_tmp_file = md_tmp_file.replace(".rst",".md")
    md_tmp_file = md_tmp_file.replace(".md",".tmp.md")

    convert_directory = os.path.dirname(md_tmp_file)
    if not os.path.exists(convert_directory):
       logger.info("Creating conversion directory '"+convert_directory+"'")
       os.makedirs(convert_directory)

    rst_prep = re.sub(r"\.md",r".prep.rst", md_tmp_file)

    logging.debug("Preprocessing '"+rst_file+"' to '"+rst_prep+"'")
    preprocess_rst(rst_file,rst_prep)

    logging.debug("Converting '"+rst_prep+"' to '"+md_file+"'")

    completed = subprocess.run(["pandoc",
#      "--verbose",
       "--from", "rst",
       "--to",md_extensions_to,
       "--wrap=none",
       "--eol=lf",
       "-o", md_tmp_file,
       rst_prep
    ])
    if completed.returncode != 0:
        print(completed)

    if not os.path.exists(md_tmp_file):
       raise FileNotFoundError(errno.ENOENT, f"Pandoc did not create md file:", md_tmp_file)

    postprocess_rst_markdown(md_tmp_file, md_file)
    if not os.path.exists(md_file):
      raise FileNotFoundError(errno.ENOENT, f"Did not create postprocessed md file:", md_file)

    return md_file

def preprocess_rst(rst_file:str, rst_prep: str) -> str:
    """
    Pre-process rst files to simplify sphinx-build directives for pandoc conversion
    """
    with open(rst_file, 'r') as file:
        text = file.read()

    # process toc_tree directive into a list of links
    if '.. toctree::' in text:
        text = _preprocess_rst_toctree(rst_file,text)

    if ':doc:' in text:
        text = _preprocess_rst_doc(rst_file,text)

    if ':ref:' in text:
        text = _preprocess_rst_ref(rst_file,text)

    # gui-label and menuselection represented: **Cancel**
    text = re.sub(
        r":guilabel:`(.*)`",
        r":**\1**",
        text,
        flags=re.MULTILINE
    )
    text = re.sub(
        r":menuselection:`(.*)`",
        r"**\1**",
        text,
        flags=re.MULTILINE
    )

    # command represented: ***mkdir***
    text = re.sub(
        r":command:`(.*?)`",
        r"***\1***",
        text,
        flags=re.MULTILINE
    )

    # file path represented: **`file`**
    text = re.sub(
        r":file:`(.*?)`",
        r"**`\1`**",
        text,
        flags=re.MULTILINE
    )

    # kbd represented with +++ by mkdocs
    text = re.sub(
        r":kbd:`(.*?)`",
        r"+++\1+++",
        text,
        flags=re.MULTILINE
    )

    # very simple literals: `some text` should use ``some text``
    text = re.sub(
        r"(\s)`(\w|\s)*([^`])`([^`])",
        r"\1``\2\3``\4",
        text,
        flags=re.MULTILINE
    )
    # rst_epilog stuff from config.py
    text = text.replace("|project_name|","GeoNetwork")
    text = text.replace("|jdbc.properties|",r"**`WEB-INF/config-db/jdbc.properties`**")
    text = text.replace("|config.node.folder|",r"**`WEB-INF/config-db/jdbc.properties`**")
    text = text.replace("|web.xml|",r"**`WEB-INF/web.xml`**")
    text = text.replace("|default.node|",r"`srv`")
    text = text.replace("|default.node.config.file|",r"**`WEB-INF/config-node/srv.xml`**")
    text = text.replace("|default.node|",r"`srv`")
    text = text.replace("|install.homepage|",r"`http://localhost:8080/geonetwork`")

    with open(rst_prep,'w') as rst:
        rst.write(text)

def _preprocess_rst_doc(path: str, text: str) -> str:
   """
   Preprocess rst content replacing doc references with links.
   """
   global anchors


   # doc links processed in order from most to least complicated

   # :doc:`normal <../folder/index.rst>`
   # :doc:`normal <link.rst>`
   # :doc:`normal <link>`
   # `normal <../folder/index.rst>`
   # `normal <link.rst>`
   # `normal <link.rst>`
   text = re.sub(
       r":doc:`(.+?) <(.+?)(\.rst)?>`",
       r"`\1 <\2.rst>`_",
       text,
       flags=re.MULTILINE
   )

   # :doc:`../folder/index.rst`
   # :doc:`simple.rst`
   # `title <../folder/index.rst>`_
   # `title <simple.rst>`_
   document_reference = re.compile(r":doc:`((\w|-|_)*?)(\.rst)?`")
   text = document_reference.sub(
       lambda match: "`"+_doc_title(path, match.group(1))+" <"+match.group(1)+".rst>`_",
       text
   )
   return text

def _preprocess_rst_ref(path: str, text: str) -> str:
   """
   Preprocess rst content replacing ref references with links.
   """
   # ref links processed in order from most to least complicated
   # :ref:`normal <link>`
   named_reference = re.compile(r":ref:`(.*) <((\w|-)*)>`")
   text = named_reference.sub(
       lambda match: "`"+match.group(1)+" <"+_ref_path(path,match.group(2))+">`_",
       text
   )

   # :ref:`simple`
   simple_reference = re.compile(r":ref:`((\w|-)*)\`")
   text = simple_reference.sub(
       lambda match: "`"+_ref_title(match.group(1))+" <"+_ref_path(path,match.group(1))+">`_",
       text
   )

   return text

def _preprocess_rst_toctree(path: str, text: str) -> str:
   """
   scan document for toctree directives to process
   """
   toctree = None
   process = ''
   for line in text.splitlines():
       if '.. toctree::' == line:
          # directive started
          toctree = ''
          continue

       if toctree != None:
          if len(line.strip()) == 0:
             continue
          if line[0:4] == '   :':
             continue
          if line[0:3] == '   ':
             # processing directive
             link = line.strip().replace(".rst","")
             label = _doc_title(path,link)
             toctree += f"* `{label} <{link}.rst>`__\n"
          else:
             # end directive
             process += toctree + '\n'
             process += line + '\n'
             toctree = None
       else:
          process += line + '\n'

   if toctree != None:
      # end directive at end of file
      process += toctree

   return process

def postprocess_rst_markdown(md_file: str, md_clean: str):
    """
    Postprocess pandoc generated markdown for mkdocs use.
    """

    with open(md_file, 'r') as markdown:
        text = markdown.read()

    # process pandoc ::: adominitions to mkdocs representation
    if ':::' in text:
        text = _postprocess_pandoc_fenced_divs(text)


    if "{.title-ref}" in text:
        # some strange thing where `TEXT` is taken to be a wiki link
        text = re.sub(
            r"\[(.*?)\]{\.title-ref}",
            r"``\1``",
            text,
            flags=re.MULTILINE
        )

    # review line by line (skipping fenced code blocks)
    clean = ''
    code = None
    for line in text.splitlines():
       if re.match("^(.*)```", line):
          if code == None:
            code = line + '\n'
          else:
            code += line
            clean += code + '\n'
            code = None
          continue

       # accept code blocks as is
       if code:
          code += line + '\n'
          continue;

       # non-code clean content
       # fix rst#anchor -> md links#anchor
       line = re.sub(
            r"\[(.+?)\]\(((\w|-|/|\.)*)\.rst(#.*?)?\)",
            r"[\1](\2.md\4)",
            line,
            flags=re.MULTILINE
       )

       # Pandoc escapes characters over-aggressively when writing markdown
       # https://github.com/jgm/pandoc/issues/6259
       # <, >, \, `, *, _, [, ], #
       line = line.replace(r'**\`', '**`')
       line = line.replace(r'\`**', '`**')
       line = line.replace(r'\<', '<')
       line = line.replace(r'\>', '>')
       line = line.replace(r'\_', '_')
       line = line.replace(r"\`", "`")
       line = line.replace(r"\'", "'")
       line = line.replace(r'\"', '"')
       line = line.replace(r'\[', '[')
       line = line.replace(r'\]', ']')
       line = line.replace(r'\*', '*')
       line = line.replace(r'\-', '-')
       line = line.replace(r'\|', '|')
       line = line.replace(r'\@', '@')

       clean += line + '\n'

    if code:
       # file ended with a code block
       clean += code

    with open(md_clean,'w') as markdown:
        markdown.write(clean)

def _postprocess_pandoc_fenced_divs(text: str) -> str:
   # scan document for pandoc fenced div info, warnings, ...
   admonition = False
   type = None
   ident = ''
   title = None
   note = None
   process = ''
   for line in text.splitlines():
       match = re.search(r"^(\s*):::\s*(\w*)$", line)
       if match and admonition == False:
          # admonition started
          # https://squidfunk.github.io/mkdocs-material/reference/admonitions/
          admonition = True
          indent = match.group(1)
          type = match.group(2)

          # sphinx-build admonition mappings
          # https://www.sphinx-doc.org/en/master/usage/restructuredtext/basics.html#rst-directives
          if type == 'attention':
             type = 'info'
          if type == 'caution':
             type = 'warning'
          if type == 'danger':
             type = 'danger'
          if type == 'error':
             type = 'failure'
          if type == 'hint':
             type = 'tip'
          if type == 'important':
             type = 'info'
          if type == 'note':
             type = 'note'
          if type == 'tip':
             type = 'tip'
          if type == 'warning':
             type = 'warning'

          # sphinx-build directives mapping to fenced blogs
          # https://www.sphinx-doc.org/en/master/usage/restructuredtext/directives.html
          if type == 'todo':
             type = 'info'
             title = 'Todo'
             note = ''
          if type == 'admonition':
             type = 'abstract'
             title = ''

          if type == 'deprecated':
             type = 'warning'
             title = 'Deprecated'
             note = ''
          if type == 'seealso':
             type = 'info'
             title = 'See Also'
             note = ''
          if type == 'versionadded':
             type = 'info'
             title = 'Version Added'
             note = ''
          if type == 'versionchanged':
             type = 'info'
             title = 'Version Changed'
             note = ''
          if type == 'versionchanged':
             type = 'info'
             title = 'Version Changed'
             note = ''
          log("process:'"+line+"'")
          log('start:',type," title:", title)
          continue

       if admonition:
          # processing fenced div
          log("process:'"+line+"'")

          if match and match.group(2) == 'title':
             # start title processing, next line title
             title = ''
             log("start title")
             continue

          if title == '':
             title = line.strip() # title obtained
             log("title",title)
             if type == 'abstract':
                # .. admonition:: Explore does not use seperate ::: marker between title and note
                note = ''
             continue

          if match and note == None:
             # start note processing, next content is note
             note = ''
             log("start note")
             continue

          if match:
             # processing fenced div
             log("fenced div")
             log("  type:",type)
             log("  title:",title)
             log("  note:",note)

             process += indent+'!!! '+type

             if title != None and title.lower() != type.lower():
               process += ' "' + title + '"'

             process += "\n\n"
             for content in note.splitlines():
                 process += '    '+content+'\n'

             process += "\n"

             admonition = False
             type = None
             ident = ''
             title = None
             note = None
             continue

          if note != None:
             if note == '' and line.strip() == '':
                # skip initial blank line
                continue
             note += line + '\n'
             log("note:"+line)
             continue

          # unexpected
          print("unexpected:")
          print("  admonition",admonition)
          print("  type",type)
          print("  title",title)
          print("  note",note)
          print("  line",line)
          print("  process",len(process))
          print()
          print(process)
          raise ValueError('unclear what to process '+str(type)+" "+str(title))

       else:
          process += line + '\n'

   if admonition:
      # fenced div was at end of file
      print("unexpected:")
      print("  admonition",admonition)
      print("  type",type)
      print("  title",title)
      print("  note",note)
      raise ValueError('Expected ::: to end fence dive '+str(type)+' '+str(title)+' '+str(note))

   return process

def log(*args):
   if False:
      message = ''
      for value in args:
         message += str(value) + ' '
      print(message)

def convert_markdown(md_file: str) -> str:
    """
    Use pandoc to convert markdown file to html file for translation.
    :param md_file: Markdown file path
    :return: html file path
    """
    if not os.path.exists(md_file):
       raise FileNotFoundError(errno.ENOENT, f"Markdown file does not exist at location:", md_file)

    config = load_config()
    if not md_file[-3:] == '.md':
       raise FileNotFoundError(errno.ENOENT, f"Markdown 'md' extension required:", md_file)

    upload_folder = config['upload_folder']

    path = re.sub("^docs/",upload_folder+'/', md_file)
    path = path.replace(".en.md",".en.html")
    path = path.replace(".md",".html")
    html_file = path

    html_dir = os.path.dirname(path)
    if not os.path.exists(html_dir):
       print("Translation directory:",html_dir)
       os.makedirs(html_dir)

    md_prep = re.sub(r"\.html",r".prep.md", path)

    logging.debug("Preprocessing '"+md_file+"' to '"+md_prep+"'")
    preprocess_markdown(md_file,md_prep)

    logging.debug("Converting '"+md_prep+"' to '"+html_file+"'")
    # pandoc --from gfm --to html -o index.en.html index.md
    completed = subprocess.run(["pandoc",
       "--from", md_extensions_from,
       "--to","html",
       "--wrap=none",
       "--eol=lf",
       "-o", html_file,
       md_prep
    ])
    if completed.returncode != 0:
        print(completed)

    if not os.path.exists(html_file):
       raise FileNotFoundError(errno.ENOENT, f"Pandoc did not create html file:", html_file)

    return html_file


def preprocess_markdown(md_file:str, md_prep: str) -> str:
    with open(md_file, 'r') as file:
        text = file.read()

    clean = ''
    code = ''
    admonition = None

    # handle notes as pandoc fenced_divs
    for line in text.splitlines():
        # phase 1: code-block pre-processing
        #
        # cause pandoc #markdown or #text to force to fenced codeblocks (rather than indent)
        if re.match("^```", line):
          if len(code) > 0:
            # print("code-end: '"+line+"'")
            # end code block (so no processing)
            code = ''

          else:
            # print("code-block: '" + line +"'")
            line = re.sub(
                 r"^```(\S+)$",
                 r"```#\1",
                 line,
                 flags=re.MULTILINE
            )
            line = re.sub(
                 r"^```$",
                 r"```#text",
                 line,
                 flags=re.MULTILINE
            )
            code = line[4:]

        # phase 2: process blocks
        if not admonition:
           if '!!! ' in line:
               # print('admonition start  : "'+line+'"')
               admonition = line
           else:
               clean += line + '\n'
        else:
           indent = admonition.index('!!! ')
           padding = admonition[0:indent]

           if len(line) == 0:
               if '\n' in admonition:
                   # print('admonition blank  : "'+line+'"')
                   admonition += line+"\n"
               else:
                   # print('admonition skip   : "'+line+'"')
                   admonition += "\n"
           elif line[0:indent].isspace():
               # print('admonition content: "'+line+'"')
               # use indent level to gather admonition contents
               admonition += padding+line[indent+4:]+'\n'
           else:
               # print('admonition end    : "'+line+'"')
               # outdent admonition completed
               first_newline = admonition.index('\n')
               last_newline = admonition.rindex('\n')

               title = admonition[indent+4:first_newline]
               contents = admonition[first_newline:last_newline]

               # output as pandoc fenced_divs
               clean += padding+"::: "+title
               clean += contents
               clean += padding+":::\n\n"

               # remember to output line that breaks indent level
               admonition = None

               clean += line + '\n'

    with open(md_prep,'w') as markdown:
        markdown.write(clean)

def convert_html(html_file: str) -> str:
    """
    Use pandoc to convert markdown file to html file for translation.
    :param html_file: HTML file path
    :return: md file path
    """
    if not os.path.exists(html_file):
       raise FileNotFoundError(errno.ENOENT, f"HTML file does not exist at location:", html_file)

    if not html_file[-5:] == '.html':
       raise FileNotFoundError(errno.ENOENT, f"HTML '.html' extension required:", html_file)

    # prep html file for conversion
    html_tmp_file = html_file[0:-5] + '.tmp.html'

    preprocess_html(html_file, html_tmp_file)
    if not os.path.exists(html_tmp_file):
       raise FileNotFoundError(errno.ENOENT, f"Did not create preprocessed html file:", html_tmp_file)

    if html_file[:-8] == '.fr.html':
       md_file = html_file[0:-8] + '.fr.md'
    if html_file[:-5] == '.html':
       md_file = html_file[0:-8] + '.md'
    else:
       md_file = html_file[0:-5] + '.md'

    md_tmp_file = md_file[0:-3]+".tmp.md"

    completed = subprocess.run(["pandoc",
       "--from","html",
       "--to", md_extensions_to,
       "--wrap=none",
       "--eol=lf",
       "-o", md_tmp_file,
       html_tmp_file
    ])
    print(completed)

    if not os.path.exists(md_tmp_file):
       raise FileNotFoundError(errno.ENOENT, f"Pandoc did not create temporary md file:", tmp_file)

    postprocess_markdown(md_tmp_file, md_file)
    if not os.path.exists(md_file):
       raise FileNotFoundError(errno.ENOENT, f"Did not create postprocessed md file:", md_file)

    return md_file

def preprocess_html(html_file: str, html_clean: str):
    with open(html_file, 'r') as html:
        data = html.read()

    # Fix image captions
    #
    #     ![Search field](img/search.png) *Champ de recherche*
    #
    # Clean:
    #
    #    ![Search field](img/search.png)
    #    *Champ de recherche*
    #
    clean = re.sub(
        r'^<p>:: : note ',
        r'<div class="note">\n<p>',
        data,
        flags=re.MULTILINE
    )
    clean = re.sub(
        r'^<p>:: :</p>',
        r'</div>',
        clean,
        flags=re.MULTILINE
    )
    # Fix deepl not respecting <pre><code> blogs using CDATA
    clean = re.sub(
        r'<code><!\[CDATA\[',
        r'<code>',
        clean,
        flags=re.MULTILINE
    )
    clean = re.sub(
        r'\]\]></code>',
        r'</code>',
        clean,
        flags=re.MULTILINE
    )
    with open(html_clean,'w') as html:
        html.write(clean)

def postprocess_markdown(md_file: str, md_clean: str):
    with open(md_file, 'r') as markdown:
        data = markdown.read()

    # Fix image captions
    #
    #     ![Search field](img/search.png) *Champ de recherche*
    #
    # Clean:
    #
    #    ![Search field](img/search.png)
    #    *Champ de recherche*
    #
#     data = re.sub(
#         r"^(\s*)\!\[(.*)\]\((.*)\)\s\*(.*)\*$",
#         r"\1![\2](\3)\1*\4*",
#         data,
#         flags=re.MULTILINE
#     )
    # fix icons
    data = re.sub(
        r":(fontawesome-\S*)\s:",
        r":\1:",
        data,
        flags=re.MULTILINE
    )
    # fix fence text blocks
    data = re.sub(
        r'^``` #text$',
        r'```',
        data,
        flags=re.MULTILINE
    )
    # fix fenced code blocks
    data = re.sub(
        r'^``` #(.+)$',
        r'```\1',
        data,
        flags=re.MULTILINE
    )
    with open(md_clean,'w') as markdown:
        markdown.write(data)

def deepl_document(en_html:str, fr_html:str):
    """
    Submit english html file to deepl for translation.
    :param en_html: English html file
    :param fr_html: French html file
    :return: status
    """

    if not os.path.exists(en_html):
       raise FileNotFoundError(errno.ENOENT, f"HTML file does not exist at location:", en_html)

    config = load_config()
    AUTH = load_auth()

    # prep html file for conversion
    translate_tmp_file = en_html[0:-5] + '.tmp.html'
    print("Preprocssing",en_html,"to",translate_tmp_file)

    preprocess_translate(en_html, translate_tmp_file)

    translator = deepl.Translator(AUTH)

    try:
        # Using translate_document_from_filepath() with file paths
        translator.translate_document_from_filepath(
            translate_tmp_file,
            fr_html,
            source_lang='EN',
            target_lang="FR",
            formality="more"
        )

    except deepl.DocumentTranslationException as error:
        # If an error occurs during document translation after the document was
        # already uploaded, a DocumentTranslationException is raised. The
        # document_handle property contains the document handle that may be used to
        # later retrieve the document from the server, or contact DeepL support.
        doc_id = error.document_handle.id
        doc_key = error.document_handle.key
        print(f"Error after uploading ${error}, id: ${doc_id} key: ${doc_key}")

    except deepl.DeepLException as error:
        # Errors during upload raise a DeepLException
        print(error)

    if not os.path.exists(fr_html):
       raise FileNotFoundError(errno.ENOENT, f"Deepl did not create md file:", fr_html)

    return

def preprocess_translate(html_file: str, html_clean: str):
    with open(html_file, 'r') as html:
        data = html.read()

    # Fix deepl not respecting <pre><code> blogs using CDATA
    data = re.sub(
        r'<code>',
        r'<code><![CDATA[',
        data,
        flags=re.MULTILINE
    )
    data = re.sub(
        r'</code>',
        r']]></code>',
        data,
        flags=re.MULTILINE
    )
    with open(html_clean,'w') as html:
        html.write(data)

# def deepl_translate(html_file: str) -> dict:
#     """
#     Submit html_file to deepl for translation.
#     :param html_file: HTML file path
#     :return: json response from deepl api
#     """
#     if not os.path.exists(html_file):
#        raise FileNotFoundError(errno.ENOENT, f"HTML file does not exist at location:", html_file)
#
#     config = load_config()
#     AUTH = load_auth()
#
#     translator = deepl.Translator(AUTH)
#
#     response = requests.post(
#        config['deepl_base_url']+'/v2/document',
#        data={
#           'source_lang':'EN',
#           'target_lang':'FR',
#           'tag_handling':'xml',
#           'formality':'prefer_more',
#           'ignore_tags':'code',
#        },
#        files={
#           'file': open(html_file,'rt')
#        },
#        headers={
#           'Authorization': f"DeepL-Auth-Key {AUTH}"}
#     )
#     status = response.json()
#     return status
#
# def deepl_status(document_id: str, document_key: str) -> dict:
#     """
#     Submit document_id to deepl for translation.
#     :param document_id: ID provided from document upload
#     :param document_key: Encryption key provided from document uploaded
#     :return: json status from deepl api
#     """
#     config = load_config()
#     AUTH = load_auth()
#
#     response = requests.post(
#        config['deepl_base_url']+'/v2/document/'+document_id,
#        data={'document_key':document_key,},
#        headers={
#           'Authorization': f"DeepL-Auth-Key {AUTH}",
#           'Content-Type': 'application/json'},
#     )
#     status = response.json()
#     return status


