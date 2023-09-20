"""This module provides pandoc and deepl translation services."""
# message/cli.py

from typing import Optional

import json
import os
import shutil
import typer

from pathlib import Path
from typing import List
from typing_extensions import Annotated


import translate.translate
from translate import __app_name__, __version__
from .translate import collect_paths
from .translate import index_rst
from .translate import load_anchors
from .translate import fix_anchors
from .translate import convert_rst
from .translate import convert_markdown
from .translate import convert_html
from .translate import deepl_document

app = typer.Typer(help="Translation for mkdocs content")

def _version_callback(value: bool) -> None:
    if value:
        typer.echo(f"{__app_name__} v{__version__}")
        raise typer.Exit()

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
        base_path: Annotated[str, typer.Argument(help="base path for references")],
        rst_path: Annotated[List[str], typer.Argument(help="path to rst file(s)")],
        anchor_txt: Optional[str] = typer.Option(
           None,
           "--anchor",
           help="anchors.txt file recording reference locations",
        ),
    ):
    """
    Scan rst files collecting doc and ref targets (supply anchors_txt to write to file).
    """
    if not os.path.exists(base_path):
       raise FileNotFoundError(errno.ENOENT, f"The base_path does not exist at location:", base_path)
    anchor_path = os.path.join(base_path,anchor_txt)

    collected = collect_paths(rst_path,'rst')

    index = ''
    for file in collected:
       index += index_rst(base_path,file)

    if anchor_txt:
        anchor_path = base_path+'/'+anchor_txt
        anchor_dir = os.path.dirname(anchor_path)
        if not os.path.exists(anchor_dir):
           print("RST index directory:",anchor_dir)
           os.makedirs(anchor_dir)

        with open(anchor_path,'w') as anchor_file:
            anchor_file.write(index)
    else:
        print(index)

@app.command()
def fix_references(
        anchor_txt: str, md_path: Annotated[List[str], typer.Argument(help="path to md file(s)")]
    ):
    """
    Using an anchor.txt file of reference=path#anchor to translate [reference](reference) links
    left over from conversion from rst.
    """
    anchors = load_anchors(anchor_txt)

    for md_file in collect_paths(md_path,'md'):
      count = fix_anchors(anchors,md_file)
      print(md_file,"fixed",count)
    print()

@app.command()
def rst(
        rst_path: Annotated[List[str], typer.Argument(help="path to rst file(s)")],
        anchor_txt: Optional[str] = typer.Option(
           None,
           "--anchor",
           help="anchors.txt file providing reference locations",
        ),
    ):
    """
    Convert rst file to markdown using pandoc.

    The rst directives are simplified prior to conversion following our writing guide:
    gui-label, menuselection, file, command

    Manual cleanup required for:
    figure
    """
    if anchor_txt:
       anchors = load_anchors(anchor_txt)

    for rst_file in collect_paths(rst_path,'rst'):
      md_file = convert_rst(rst_file)
      if anchor_txt:
         count = fix_anchors(anchors,md_file)
         if count > 0:
            print("Fixed",count,"anchor references")
      print(md_file,"\n")

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
           help="Show the application's version and exit.",
           callback=_version_callback,
           is_eager=True,
        ),
) -> None:
    return
