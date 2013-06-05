#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
    DocBook to ReST converter
    =========================
    This script probably won't work out of the box. You may need to tweak or
    enhance it and/or manually tweak the output. Anyway, it makes the work
    easier. Send me any patches you have: wojdyr at gmail. 

    ``pydoc db2rst`` shows the list of supported elements.

    Usage: db2rst.py file.xml > file.rst

    :copyright: 2009 by Marcin Wojdyr.
    :license: BSD.
"""

# If this option is True, XML comment are discarded. Otherwise, they are
# converted to ReST comments.
# Note that ReST doesn't support inline comments. XML comments
# are converted to ReST comment blocks, what may break paragraphs.
REMOVE_COMMENTS = False

# id attributes of DocBook elements are translated to ReST labels.
# If this option is False, only labels that are used in links are generated.
WRITE_UNUSED_LABELS = False

import sys
import re
import lxml.etree as ET

# to avoid dupliate error reports
_not_handled_tags = set()

# to remember which id/labels are really needed
_linked_ids = set()

# to avoid duplicate substitutions
_substitutions = set()

# buffer that is flushed after the end of paragraph,
# used for ReST substitutions
_buffer = ""

def _main():
    if len(sys.argv) != 2:
        sys.stderr.write(__doc__)
        sys.exit()
    input_file = sys.argv[1]
    sys.stderr.write("Parsing XML file `%s'...\n" % input_file)
    parser = ET.XMLParser(remove_comments=REMOVE_COMMENTS)
    tree = ET.parse(input_file, parser=parser)
    for elem in tree.getiterator():
        if elem.tag in ("xref", "link"):
            _linked_ids.add(elem.get("linkend"))
    print TreeRoot(tree.getroot()).encode('utf-8')

def _warn(s):
    sys.stderr.write("WARNING: %s\n" % s)

def _supports_only(el, tags):
    "print warning if there are unexpected children"
    for i in el.getchildren():
        if i.tag not in tags:
            _warn("%s/%s skipped." % (el.tag, i.tag))

def _what(el):
    "returns string describing the element, such as <para> or Comment"
    if isinstance(el.tag, basestring):
        return "<%s>" % el.tag
    elif isinstance(el, ET._Comment):
        return "Comment"
    else:
        return str(el)

def _has_only_text(el):
    "print warning if there are any children"
    if el.getchildren():
        _warn("children of %s are skipped: %s" % (_get_path(el),
                              ", ".join(_what(i) for i in el.getchildren())))

def _has_no_text(el):
    "print warning if there is any non-blank text"
    if el.text is not None and not el.text.isspace():
        _warn("skipping text of <%s>: %s" % (_get_path(el), el.text))
    for i in el.getchildren():
        if i.tail is not None and not i.tail.isspace():
            _warn("skipping tail of <%s>: %s" % (_get_path(i), i.tail))

def _conv(el):
    "element to string conversion; usually calls element_name() to do the job"
    if el.tag in globals():
        s = globals()[el.tag](el)
        assert s, "Error: %s -> None\n" % _get_path(el)
        return s
    elif isinstance(el, ET._Comment):
        return Comment(el) if (el.text and not el.text.isspace()) else ""
    else:
        if el.tag not in _not_handled_tags:
            _warn("Don't know how to handle <%s>" % el.tag)
            #_warn(" ... from path: %s" % _get_path(el))
            _not_handled_tags.add(el.tag)
        return _concat(el)

def _no_special_markup(el):
    return _concat(el)

def _remove_indent_and_escape(s):
    "remove indentation from the string s, escape some of the special chars"
    s = "\n".join(i.lstrip().replace("\\", "\\\\") for i in s.splitlines())
    # escape inline mark-up start-string characters (even if there is no
    # end-string, docutils show warning if the start-string is not escaped)
    # TODO: handle also Unicode: ‘ “ ’ « ¡ ¿ as preceding chars
    s = re.sub(r"([\s'\"([{</:-])" # start-string is preceded by one of these
               r"([|*`[])" # the start-string
               r"(\S)",    # start-string is followed by non-whitespace
               r"\1\\\2\3", # insert backslash
               s)
    return s

def _concat(el):
    "concatate .text with children (_conv'ed to text) and their tails"
    s = ""
    id = el.get("id")
    if id is not None and (WRITE_UNUSED_LABELS or id in _linked_ids):
        s += "\n\n.. _%s:\n\n" % id
    if el.text is not None:
        s += _remove_indent_and_escape(el.text)
    for i in el.getchildren():
        s += _conv(i)
        if i.tail is not None:
            if len(s) > 0 and not s[-1].isspace() and i.tail[0] in " \t":
                s += i.tail[0]
            s += _remove_indent_and_escape(i.tail)
    return s

def _concatxml(el):
    "concatate .text with children (_conv'ed to text) and their tails"
    s = ""
    id = el.get("id")
    if id is not None and (WRITE_UNUSED_LABELS or id in _linked_ids):
        s += "\n\n.. _%s:\n\n" % id
    if el.text is not None:
#        s += _remove_indent_and_escape(el.text)
        s += el.text
    for i in el.getchildren():
        s += _conv(i)
        if i.tail is not None:
            if len(s) > 0 and not s[-1].isspace() and i.tail[0] in " \t":
                s += i.tail[0]
#            s += _remove_indent_and_escape(i.tail)
            s += i.tail
    return s

def _original_xml(el):
    return ET.tostring(el, with_tail=False)

def _o_xml(el):
    s = "\n\n" + _concatxml(el)
    global _buffer
    if _buffer:
        s += "\n\n" + _buffer
        _buffer = ""
    return s

def _no_markup(el):
    s = ET.tostring(el, with_tail=False)
    s = re.sub(r"<.+?>", " ", s) # remove tags
    s = re.sub(r"\s+", " ", s) # replace all blanks with single space
    return s

def _get_level(el):
    "return number of ancestors"
    return sum(1 for i in el.iterancestors())

def _get_path(el):
    t = [el] + list(el.iterancestors())
    return "/".join(str(i.tag) for i in reversed(t))

def _make_title(t, level):
    if level == 1:
        return "\n\n" + "=" * len(t) + "\n" + t + "\n" + "=" * len(t)
    char = ["=", "-", "`", "^", "." ]
    return "\n\n" + t + "\n" + char[level-2] * len(t)

def _join_children(el, sep):
    _has_no_text(el)
    return sep.join(_conv(i) for i in el.getchildren())

def _block_separated_with_blank_line(el):
    s = "\n\n" + _concat(el)
    global _buffer
    if _buffer:
        s += "\n\n" + _buffer
        _buffer = ""
    return s

def _indent(el, indent, first_line=None):
    "returns indented block with exactly one blank line at the beginning"
    lines = [" "*indent + i for i in _concat(el).splitlines()
             if i and not i.isspace()]
    if first_line is not None:
        # replace indentation of the first line with prefix `first_line'
        lines[0] = first_line + lines[0][indent:]
    return "\n\n" + "\n".join(lines)

def _normalize_whitespace(s):
    return " ".join(s.split())

###################           DocBook elements        #####################

# special "elements"

def TreeRoot(el):
    output = _conv(el)
    # remove trailing whitespace
    output = re.sub(r"[ \t]+\n", "\n", output)
    # leave only one blank line
    output = re.sub(r"\n{3,}", "\n\n", output)
    return output

def Comment(el):
    return _indent(el, 12, ".. COMMENT: ")


# general inline elements

def emphasis(el):
    return "**%s**" % _concat(el).strip()
phrase = emphasis
citetitle = emphasis

def firstterm(el):
    _has_only_text(el)
    return ":dfn:`%s`" % el.text

acronym = _no_special_markup


# links

def ulink(el):
    url = el.get("url")
    text = _concat(el).strip()
    if text.startswith(".. image::"):
        return "%s\n   :target: %s\n\n" % (text, url)
    elif url == text:
        return text
    else:
        return "`%s <%s>`_" % (text, url)

# TODO:
# put labels where referenced ids are 
# e.g. <appendix id="license"> -> .. _license:\n<appendix>
# if the label is not before title, we need to give explicit title:
# :ref:`Link title <label-name>`
# (in DocBook was: the section called “Variables”)

def xref(el):
    return ":ref:`%s`" % el.get("linkend")

def link(el):
    return ":ref:`%s <%s>`" % (_concat(el).strip(), el.get("linkend"))


# math and media
# the DocBook syntax to embed equations is sick. Usually, (inline)equation is
# a (inline)mediaobject, which is imageobject + textobject

def inlineequation(el):
    _supports_only(el, ("inlinemediaobject",))
    return _concat(el).strip()

def informalequation(el):
    _supports_only(el, ("mediaobject",))
    return _concat(el)

def equation(el):
    _supports_only(el, ("title", "mediaobject"))
    title = el.find("title")
    if title is not None:
        s = "\n\n**%s:**" % _concat(title).strip()
    else:
        s = ""
    for mo in el.findall("mediaobject"):
        s += "\n" + _conv(mo)
    return s

def figure(el):
    _supports_only(el, ("title", "mediaobject", "programlisting"))
    title = el.find("title")
    if title is not None:
        s = "**%s**" % _concat(title).strip()
    else:
        s = ""
    for mo in el.findall("mediaobject"):
        s = _conv(mo) + "\n\n  " + s
    for mo in el.findall("programlisting"):
        s =  "\n\n" + s + "::\n" + _o_xml(mo)
    return s

def mediaobject(el, substitute=False):
    global _substitutions
    _supports_only(el, ("imageobject", "textobject"))
    # i guess the most common case is one imageobject and one (or none)
    alt = ""
    for txto in el.findall("textobject"):
        _supports_only(txto, ("phrase",))
        if alt:
            alt += "; "
        alt += _normalize_whitespace(_concat(txto.find("phrase")))
    symbols = []
    img = ""
    for imgo in el.findall("imageobject"):
        _supports_only(imgo, ("imagedata",))
        fileref = imgo.find("imagedata").get("fileref")
        s = "\n\n.. figure:: %s" % fileref
        if (alt):
            s += "\n   :alt: %s" % alt
        if substitute:
            if fileref not in _substitutions:
                img += s[:4] + " |%s|" % fileref + s[4:] # insert |symbol|
                _substitutions.add(fileref)
            symbols.append(fileref)
        else:
            img += s
    img += "\n\n"
    if substitute:
        return img, symbols
    else:
        return img

def inlinemediaobject(el):
    global _buffer
    subst, symbols = mediaobject(el, substitute=True)
    _buffer += subst
    return "".join("|%s|" % i for i in symbols)

def subscript(el):
    return "\ :sub:`%s`" % _concat(el).strip()

def superscript(el):
    return "\ :sup:`%s`" % _concat(el).strip()


# GUI elements

def menuchoice(el):
    if all(i.tag in ("guimenu", "guimenuitem") for i in el.getchildren()):
        _has_no_text(el)
        return ":menuselection:`%s`" % \
                " --> ".join(i.text for i in el.getchildren())
    else:
        return _concat(el)

def guilabel(el):
    _has_only_text(el)
    return ":guilabel:`%s`" % el.text.strip()
guiicon = guilabel
guimenu = guilabel
guimenuitem = guilabel
mousebutton = _no_special_markup


# system elements

def keycap(el):
    _has_only_text(el)
    return ":kbd:`%s`" % el.text

def application(el):
    _has_only_text(el)
    return ":program:`%s`" % el.text.strip()

def userinput(el):
    return "``%s``" % _concat(el).strip()

systemitem = userinput
prompt = userinput

def filename(el):
    _has_only_text(el)
    return ":file:`%s`" % el.text

def command(el):
    return ":command:`%s`" % _concat(el).strip()

def parameter(el):
    if el.get("class"): # this hack is specific for fityk manual
        return ":option:`%s`" % _concat(el).strip()
    return emphasis(el)

replaceable = emphasis

def cmdsynopsis(el):
    # just remove all markup and remember to change it manually later
    return "\n\nCMDSYN: %s\n" % _no_markup(el)


# programming elements

def function(el):
    #_has_only_text(el)
    #return ":func:`%s`" % _concat(el)
    return "``%s``" % _concat(el).strip()

def constant(el):
    _has_only_text(el)
    #return ":constant:`%s`" % el.text
    return "``%s``" % el.text.strip()

varname = constant


# popular block elements

def title(el):
    # Titles in some elements may be handled from the title's parent.
    t = _concat(el).strip()
    level = _get_level(el)
    parent = el.getparent().tag
    ## title in elements other than the following will trigger assertion
    #if parent in ("book", "chapter", "section", "variablelist", "appendix"):
    return _make_title(t, level)

def screen(el):
    return "\n::\n" + _indent(el, 4) + "\n"

literallayout = screen

def blockquote(el):
    return _indent(el, 4)

book = _no_special_markup
article = _no_special_markup
para = _block_separated_with_blank_line
section = _block_separated_with_blank_line
appendix = _block_separated_with_blank_line
chapter = _block_separated_with_blank_line


# lists

def itemizedlist(el, bullet="-"):
    # ItemizedList ::= (ListItem+)
    s = ""
    for i in el.getchildren():
        s += _indent(i, 2, bullet+" ")
    return s + "\n\n"

def orderedlist(el):
    # OrderedList ::= (ListItem+)
    return itemizedlist(el, bullet="#.")

def simplelist(el):
    # SimpleList ::= (Member+)
    # The simplelist is the most complicated one. There are 3 kinds of 
    # SimpleList: Inline, Horiz and Vert.
    if el.get("type") == "inline":
        return _join_children(el, ", ")
    else:
        # members should be rendered in tabular fashion, with number
        # of columns equal el[columns]
        # but we simply transform it to bullet list
        return itemizedlist(el, bullet="+")

def variablelist(el):
    #VariableList ::= ((Title,TitleAbbrev?)?, VarListEntry+)
    #VarListEntry ::= (Term+,ListItem)
    _supports_only(el, ("title", "varlistentry"))
    s = ""
    title = el.find("title")
    if title is not None:
        s += _conv(title)
    for entry in el.findall("varlistentry"):
        s += "\n\n"
        s += ", ".join(_concat(i).strip() for i in entry.findall("term"))
        s += _indent(entry.find("listitem"), 4)[1:]
    return s


# admonition directives

def note(el):
    return _indent(el, 3, ".. note:: ")
def caution(el):
    return _indent(el, 3, ".. caution:: ")
def important(el):
    return _indent(el, 3, ".. important:: ")
def tip(el):
    return _indent(el, 3, ".. tip:: ")
def warning(el):
    return _indent(el, 3, ".. warning:: ")


# bibliography

def author(el):
    _supports_only(el, ("firstname", "surname"))
    return el.findtext("firstname") + " " + el.findtext("surname")

editor = author

def authorgroup(el):
    return _join_children(el, ", ")

def biblioentry(el):
    _supports_only(el, ("abbrev", "authorgroup", "author", "editor", "title",
                        "publishername", "pubdate", "address"))
    s = "\n"

    abbrev = el.find("abbrev")
    if abbrev is not None:
        _has_only_text(abbrev)
        s += "[%s] " % abbrev.text

    auth = el.find("authorgroup")
    if auth is None:
        auth = el.find("author")
    if auth is not None:
        s += "%s. " % _conv(auth)

    editor = el.find("editor")
    if editor is not None:
        s += "%s. " % _conv(editor)

    title = el.find("title")
    if title is not None:
        _has_only_text(title)
        s += "*%s*. " % title.text.strip()

    address = el.find("address")
    if address is not None:
        _supports_only(address, ("otheraddr",))
        s += "%s " % address.findtext("otheraddr")

    publishername = el.find("publishername")
    if publishername is not None:
        _has_only_text(publishername)
        s += "%s. " % publishername.text

    pubdate = el.find("pubdate")
    if pubdate is not None:
        _has_only_text(pubdate)
        s += "%s. " % pubdate.text
    return s

def bibliography(el):
    _supports_only(el, ("biblioentry",))
    return _make_title("Bibliography", 2) + "\n" + _join_children(el, "\n")



if __name__ == '__main__':
    _main()

