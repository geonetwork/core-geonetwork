"""
This module provides services written around pandoc for format translation,
and deepl for language translation services.
"""
# message/cli.py

from typing import Optional

import json
import logging
import os
import shutil
import typer

from pathlib import Path
from typing import List
from typing_extensions import Annotated


import translate.translate
from translate import __app_name__, __version__
from .translate import rst_folder
from .translate import anchor_file
from .translate import init_config
from .translate import init_anchors
from .translate import collect_path
from .translate import collect_paths
from .translate import index_rst
from .translate import convert_rst
from .translate import convert_markdown
from .translate import convert_html
from .translate import deepl_document

app = typer.Typer(help="Translation for mkdocs content")

logger = logging.getLogger(__app_name__)

def _version_callback(value: bool) -> None:
    if value:
        typer.echo(f"{__app_name__} v{__version__}")
        raise typer.Exit()

def _log_callback(log: str) -> None:
    logging_format = '%(levelname)s: %(message)s'
    if not log:
        logging.basicConfig(format=logging_format,level=logging.INFO)
    elif 'DEBUG' == log.upper():
        logging.basicConfig(format=logging_format,level=logging.DEBUG)
    elif 'WARNING' == log.upper():
        logging.basicConfig(format=logging_format,level=logging.WARNING)
    elif 'INFO' == log.upper():
        logging.basicConfig(format=logging_format,level=logging.INFO)
    elif 'ERROR' == log.upper():
        logging.basicConfig(format=logging_format,level=logging.ERROR)
    elif 'CRITICAL' == log.upper():
        logging.basicConfig(format=logging_format,level=logging.CRITICAL)
    else:
        logging.config.fileConfig(log)

def _config_callback(config_path: str) -> None:
    init_config(config_path)

@app.command()
def french(
        md_file: Annotated[str, typer.Argument(help="Markdown file path")]
    ):
    """
    Translate markdown file to french using convert, document and markdown steps.
    """
    html_en = convert_markdown(md_file)

    html_fr = html_en[0:-5]+'.fr.html'
    deepl_document(html_en,html_fr)

    translated = convert_html(html_fr)

    folder = os.path.dirname(md_file)

    md_fr = os.path.join(folder,os.path.basename(translated))

    shutil.copy2(translated, md_fr)

    print(md_fr,"\n")

@app.command()
def index(
       test: Optional[str] = typer.Option(
           None,
           "--test",
           help="Test scan a single file, do not update anchors.txt file",
        )
    ):
    """
    Scan rst files collecting doc and ref targets updating anchors.txt index.
    """
    rst_path = translate.translate.rst_folder

    if test:
        index = index_rst(rst_path,test)
        print(index)
        return

    rst_glob = rst_path+"/**/*.rst"
    anchor_path = translate.translate.anchor_file

    collected = collect_path(rst_glob,'rst')
    collected.sort()
    logger.info("Processing "+str(len(collected))+" files")

    index = ''
    for file in collected:
       index += index_rst(rst_path,file)

    anchor_dir = os.path.dirname(anchor_path)
    if not os.path.exists(anchor_dir):
        print("anchors.txt index directory:",anchor_dir)
        os.makedirs(anchor_dir)
    with open(anchor_path,'w') as anchor_file:
        anchor_file.write(index)
    print(anchor_path)

@app.command()
def rst(
        rst_path: Annotated[List[str], typer.Argument(help="path to rst file(s)")] = translate.translate.rst_folder,
    ):
    """
    Convert rst files to markdown using pandoc.

    The rst directives are simplified prior to conversion following our writing guide:
    gui-label, menuselection, file, command

    Manual cleanup required for:
      figure
    """
    init_anchors()

    if not rst_path:
       rst_glob = translate.translate.rst_folder+"/**/*.rst"
       rst_path = [rst_glob]

    for rst_file in collect_paths(rst_path,'rst'):
      md_file = convert_rst(rst_file)
      print(md_file)

@app.command()
def internal_html(
        md_file: Annotated[str, typer.Argument(help="Markdown file path")]
    ):
    """
    Convert markdown file to html using pandoc (some additional simplifications applied).
    This step is used prior to translation.
    """
    file = convert_markdown(md_file)
    print(file,"\n")

@app.command()
def internal_markdown(
        html_file: Annotated[str, typer.Argument(help="HTML file path")]
    ):
    """
    Convert translated html file back to markdown using pandoc.

    Some additional post-processing applied to clean up formatting harmed during
    translation process.
    """
    file = convert_html(html_file)
    print(file,"\n")

@app.command()
def internal_document(
        en_file: Annotated[str, typer.Argument(help="English HTML upload file path")],
        fr_file: Annotated[str, typer.Argument(help="French HTML download file path")]
    ):
    """
    Upload en_file for translation to deepl services, the translation is downloaded to fr_file.
    Some preprocess applied to preserve code blocks.
    Requires DEEPL_AUTH environment variable to access translation services.
    """
    deepl_document(en_file,fr_file)

@app.callback()
def main(
        version: Optional[bool] = typer.Option(
           None,
           "--version",
           "-v",
           help="Use debug logging to trace program execution.",
           callback=_version_callback,
           is_eager=True,
        ),
        log: Optional[str] = typer.Option(
           None,
           "--log",
           help="Use logging to trace program execution (provide logging configuration file, or logging level).",
           callback=_log_callback,
           is_eager=True,
        ),
        config: Optional[str] = typer.Option(
           None,
           "--config",
           help="Provide to config file to override built-in configuration.",
           callback=_config_callback,
           is_eager=True,
        )
) -> None:
    """
    Services written around pandoc for format translation,
    and deepl for language translation services.
    """
    return
