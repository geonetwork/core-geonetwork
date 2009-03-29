19139 XSchemas TS RC (2006 May 4)
-----------------------------------------------

OGC GML 3.2.0 version => ISO 19136 XSchemas DIS (2005 november)
 -- http://www.isotc211.org/2005/

These schemas from ISO 19139 version 2005-DIS (Draft International Standard)
dated 2006 May 4. For the sake of convenience, GML 3.2 XML schemas (version
19136 DIS - 2005 november) are (temporarily) provided with the 19139 set of
schemas. They were retrieved from http://www.isotc211.org/2005/ . Once these
schemas are finalized they will become OGC GML 3.2.1 and ISO/TS 19136.

Changes made to these ISO 19139 schemas by OGC:
  * changed xlink references from ../xlink/xlinks.xsd to ../../../../xlink/1.0.0/xlinks.xsd
    so they use http://schemas.opengis.net/xlink/1.0.0/xlinks.xsd .
    (see W3C XLink 1.0)
  * removed xlinks directory and schema
  * replaced 19139-GML_readme.txt with this document.

In Folder "gml": the GML Schema; the root document of the GML Schema is file
"gml/gml.xsd"

Imported schemas:
- Folder "xlink": the W3C XLink schema (see W3C XLink 1.0)
- iso19139 schemas: the GMD schema and contained schemas (see ISO/TS 19139)

