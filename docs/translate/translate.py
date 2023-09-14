import deepl
import errno
import glob
import os
import pkgutil
import re
import requests
import subprocess
import yaml

from translate import __app_name__, __version__

# "gfm+definition_lists+fenced_divs+pipe_tables-fenced_code_attributes",
md_extensions_to = 'markdown+definition_lists+fenced_divs+backtick_code_blocks+fenced_code_attributes+pipe_tables-simple_tables'

# "gfm+definition_lists+fenced_divs+pipe_tables",
md_extensions_from = 'markdown+definition_lists+fenced_divs+backtick_code_blocks+fenced_code_attributes+pipe_tables'

def load_auth() -> str:
    """
    Look up DEEPL_AUTH environmental variable for authentication.
    """
    AUTH = os.getenv('DEEPL_AUTH')
    if not AUTH:
       raise ValueError('Environmental variable DEEPL_AUTH required for translate with Deepl REST API')

    return AUTH

def load_config() -> dict:
    """
    Load config.yml application configuration.
    """
    raw = pkgutil.get_data('translate', "config.yml")
    config = yaml.safe_load(raw.decode('utf-8'))

    return config

def load_anchors(anchor_txt:str) -> dict[str,str]:
    """
    load anchors reference of the form:
       reference=/absolut/path/to/file.md#anchor
    """
    if not os.path.exists(anchor_txt):
       raise FileNotFoundError(errno.ENOENT, f"anchors definition file does not exist at location:", anchor_txt)
    anchors = {}
    with open(anchor_txt,'r') as file:
       for line in file:
         if '=' in line:
            (anchor,path) = line.split('=')
            anchors[anchor] = path

    return anchors

def collect_path(path: str, extension: str) -> list[str]:
    """
    Collect all the files with an extension from a path.
    If the path is a single file the extension should match.
    """
    files = []

    if '*' in path:
      for file in glob.glob(path):
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

# administrator-guide/managing-metadata-standards/configure-validation.md
def fix_anchors(anchors: dict[str,str], md_file: str) -> int:
    """
    Use search/replace to identify [reference](reference) links and
    fill in appropriate path to anchor.
    """
    if not os.path.exists(md_file):
       raise FileNotFoundError(errno.ENOENT, f"Markdown file does not exist at location:", md_file)

    with open(md_file, 'r') as file:
        text = file.read()

    count = 0;
    fixed = ''
    for line in text.splitlines():
       match = re.search(r"\[(.+)\]\((.+)\.md\)", line)
       if match:
          text = match.group(1)
          link = match.group(2)
          if text == link:
             path = anchors.get(text)
             print("anchor:",link,"->",path)
             if path:
                line = re.sub(
                   r"\[(.+)\]\((.+)\.md\)",
                   r"[\1]("+path+")",
                   line
                )
                count += 1
                print(line)
       fixed += line

#     with open(md_file,'w') as rst:
#         rst.write(fixed)

    return count

def convert_rst(rst_file: str) -> str:
    """
    Use pandoc to convert rich-structured-text file to markdown file for mkdocs
    :param md_file: Markdown file path
    :return: markdown file path
    """
    if not os.path.exists(rst_file):
       raise FileNotFoundError(errno.ENOENT, f"RST file does not exist at location:", rst_file)

    config = load_config()
    if rst_file[-4:] != '.rst':
       raise FileNotFoundError(errno.ENOENT, f"Rich-structued-text 'rst' extension required:", rst_file)

    # file we are generating
    md_file = rst_file.replace(".txt",".md")
    md_file = md_file.replace(".rst",".md")

    # temp file for processing
    convert_folder = config['convert_folder']
    md_tmp_file = re.sub("^(help|manual)/docs/",convert_folder+'/', rst_file)
    md_tmp_file = md_tmp_file.replace(".txt",".md")
    md_tmp_file = md_tmp_file.replace(".rst",".md")
    md_tmp_file = md_tmp_file.replace(".md",".tmp.md")

    convert_directory = os.path.dirname(md_tmp_file)
    if not os.path.exists(convert_directory):
       print("Conversion directory:",convert_directory)
       os.makedirs(convert_directory)

    rst_prep = re.sub(r"\.md",r".prep.rst", md_tmp_file)

    print("Preprocessing ",rst_file," to ",rst_prep)
    preprocess_rst(rst_file,rst_prep)

    print("Converting ",rst_prep," to ",md_file)

    completed = subprocess.run(["pandoc",
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
        text = _preprocess_rst_toctree(text)

    # gui-label and menuselection represented: **Cancel**
    text = re.sub(
        r":guilabel:`(.*)`",
        r":**\1**",
        text,
        flags=re.MULTILINE
    )
    text = re.sub(
        r":menuselection:`(.*)`",
        r":**\1**",
        text,
        flags=re.MULTILINE
    )

    # command represented: ***mkdir***
    text = re.sub(
        r":command:`(.*)`",
        r":***\1***",
        text,
        flags=re.MULTILINE
    )

    # file path represented: **`file`**
    text = re.sub(
        r":command:`(.*)`",
        r":***\1***",
        text,
        flags=re.MULTILINE
    )

    # kbd represented with +++ by mkdocs
    text = re.sub(
        r":kbd:`(.*)`",
        r":+++\1+++",
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

    with open(rst_prep,'w') as rst:
        rst.write(text)

def _preprocess_rst_toctree(text: str):
   # scan document for toctree directives to process
   toctree = None
   process = ''
   for line in text.splitlines():
       if '.. toctree::' == line:
          # directive started
          toctree = ''
          continue

       if toctree != None:
          if len(line) == 0:
             continue
          if line[0:4] == '   :':
             continue
          if line[0:3] == '   ':
             # processing directive
             link = line[3:-4]
             if link.endswith("/index"):
                label = link[0:-6]
             else:
                label = link

             toctree += f"* `{label} <{link}.md>`__\n"
          else:
             # end directive
             process += toctree
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
        data = markdown.read()

    # process pandoc ::: adominitions to mkdocs representation
    if ':::' in data:
        data = _postprocess_pandoc_fenced_divs(data)


    # fix references into broken links
    data = re.sub(
        r'`(.*) <(.*)>`{\.interpreted-text role="ref"}',
        r'[\1](\2.md)',
        data,
        flags=re.MULTILINE
    )
    data = re.sub(
        r'`(.*)`{\.interpreted-text role="ref"}',
        r'[\1](\1.md)',
        data,
        flags=re.MULTILINE
    )
    # Pandoc escapes characters over-aggressively when writing markdown
    # https://github.com/jgm/pandoc/issues/6259
    # <, >, \, `, *, _, [, ], #
    data = re.sub(
        r"\\<",
        r"<",
        data,
        flags=re.MULTILINE
    )
    data = re.sub(
        r"\\>",
        r">",
        data,
        flags=re.MULTILINE
    )
    data = re.sub(
        r"\\_",
        r"_",
        data,
        flags=re.MULTILINE
    )
    data = re.sub(
        r"\\'",
        r"'",
        data,
        flags=re.MULTILINE
    )
    data = re.sub(
        r'\\"',
        r'"',
        data,
        flags=re.MULTILINE
    )
    data = re.sub(
        r'\\\[',
        r'[',
        data,
        flags=re.MULTILINE
    )
    data = re.sub(
        r'\\\]',
        r']',
        data,
        flags=re.MULTILINE
    )
    with open(md_clean,'w') as markdown:
        markdown.write(data)

def _postprocess_pandoc_fenced_divs(text: str) -> str:
   # scan document for pandoc fenced div info, warnings, ...
   admonition = False
   type = None
   title = None
   note = None
   process = ''
   for line in text.splitlines():
       if line.startswith(':::') and admonition == False:
          # admonition started
          # https://squidfunk.github.io/mkdocs-material/reference/admonitions/

          admonition = True
          type = line[4:]

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
          if type == 'admonition':
             type = 'abstract'

          # sphinx-build directives mapping to fenced blogs
          # https://www.sphinx-doc.org/en/master/usage/restructuredtext/directives.html
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

          log('start:',type,' ',title)
          continue

       if admonition:
          # processing fenced div
          if len(line) == 0:
             continue

          if line.startswith('::: title'):
             # start title processing, next line title
             title = ''
             log("start title")
             continue

          if title == '':
             title = line # title obtained
             log("title",title)
             continue

          if line.startswith(':::') and note == None:
             # start note processing, next content is note
             note = ''
             log("start note")
             continue

          if line.startswith(':::'):
             # processing fenced div
             log("fenced div")
             log("type:",type)
             log("title:",title)
             log("note:",note)

             process += '!!! '+type

             if title != None and title.lower() != type.lower():
               process += ' "' + title + '"'

             process += "\n\n"
             for content in note.splitlines():
                 process += '    '+content+'\n'

             process += "\n"

             admonition = False
             type = None
             title = None
             note = None
             continue

          if note != None:
             note += line + '\n'
             continue

          # unexpected
          log("admonition",admonition)
          log("type",type)
          log("title",title)
          log("note",note)
          log("line",line)
          raise ValueError('unclear what to process')

       else:
          process += line + '\n'

   if admonition:
      # fenced div was at end of file
      raise ValueError('Expected ::: to end fence dive '+type+' '+title+' '+note)

   return process

def log(*args):
   message = ''
#    for value in args:
#       message += str(value) + ' '
#    print(message)

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

    print("Preprocessing ",md_file," to ",md_prep)
    preprocess_markdown(md_file,md_prep)

    print("Converting ",md_prep," to ",html_file)
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


