OGC(r) GML schema ReadMe.txt
======================================================================

OGC(r) Geography Markup Language (GML) Encoding Standard
-----------------------------------------------------------------------

Geography Markup Language is an OGC Standard.

More information may be found at
 http://www.opengeospatial.org/standards/gml

The most current schema are available at http://schemas.opengis.net/ .

-----------------------------------------------------------------------

2016-12-02  Clemens Portele
  + v3.2.2: Corrigendum to GML 3.2.1 for Change Request 12-092. Changes:
                - gml:id in gml:AbstractGMLType has been made optional
                - gml:AbstractRing in substitutionGroup gml:AbstractCurve
                - gml:AbstractShell in substitutionGroup gml:AbstractSurface
            - gml:AbstractRingType extended from base type gml:AbstractCurveType
            - gml:ShellType extended from base type gml:AbstractSurfaceType
            These changes correct inconsistencies with ISO 19107 without breaking the
            validity of instance documents created using the GML 3.2.1 schema. This
            required the change to the gml:id attribute which reverts a change that
            has been made between GML 3.1.1 and GML 3.2.1 and that has been requested
            by several communities, too.

2014-10-03  Simon Cox
  + v3.2.1: Fix spelling of Bezier in gml/3.2.1/gml_32_geometries.rdf .
            No Version Change.

2012-09-10  Simon Cox
  + v3.2.1: add geosparql 1.0.1 ontology gml/3.2.1/gml_32_geometries.rdf
            see OGC 11-052r4 at http://www.opengeospatial.org/standards/geosparql

2012-07-30  Clemens Portele, Claus Nagel
  * v3.0.0 - v3.1.1: Change smil20.xsd to use http://www.w3.org/2001/xml.xsd
  and remove the modified smil/xml-mod.xsd.

2012-07-21  Kevin Stegemoller
  * v2.0.0 - v3.2.1 WARNING XLink change is NOT BACKWARD COMPATIBLE.
  * changed OGC XLink (xlink:simpleLink) to W3C XLink (xlink:simpleAttrs)
  per an approved TC and PC motion during the Dec. 2011 Brussels meeting.
  see http://www.opengeospatial.org/blog/1597
  * implement 11-025: retroactively require/add all leaf documents of an
  XML namespace shall explicitly <include/> the all-components schema
  * v3.3: no changes
  * v3.2.1: added gml.xsd as the all-components document (06-135r11 #14)
  * v3.2.1: archived previous version in gml-3_2_1_1.zip contained in gml-3_2_1.zip
  * v3.2.1: updated xsd:schema/@version to 3.2.1.2 (06-135r7 s#13.4)
  * v3.1.1: updated xsd:schema/@version to 3.1.1.2 (06-135r7 s#13.4)
  * v3.1.1: added gml.xsd as the all-components document (06-135r11 #14)
  * v3.1.0: no changes
  * v3.0.1: updated xsd:schema/@version to 3.0.1.2 (06-135r7 s#13.4)
  * v3.0.0: no changes
  * v2.1.2.x: gml/2.1.2.x schema were removed and archived
  * v2.1.2.0: removed gml/2.1.2.0 and now redirected to 2.1.2
  * v2.1.2.2: removed gml/2.1.2.1 and now redirected to 2.1.2
  * v2.1.2: added gml.xsd as the all-components document (06-135r11 #14)
  * v2.1.2: updated xsd:schema/@version to 2.1.2.2 (06-135r7 s#13.4)
  * v2.1.1: updated xsd:schema/@version to 2.1.1.1 (06-135r7 s#13.4)
  * v2.0.0: updated xsd:schema/@version to 2.0.7.1 (06-135r7 s#13.4)

2012-02-07  Clemens Portele
  * v3.3.0: Posted GML 3.3 schemas from OGC 10-129r1.
	  See http://schemas.opengis.net/gml/3.3/ReadMe.txt

2010-01-28  Kevin Stegemoller
  * v1.0-v3.2: update/verify copyright (06-135r7 s#3.2)
  * v1.0-v3.2: update relative schema imports to absolute URLs (06-135r7 s#15)
  * v1.0-v3.2: updated xsd:schema/@version attribute (06-135r7 s#13.4)
  * v1.0-v3.2: add archives (.zip) files of previous versions
  * v1.0-v3.2: create/update ReadMe.txt (06-135r7 s#17)

2007-09-06  Kevin Stegemoller
  * v3.2.1: Posted GML 3.2.1 (ISO 19136) schemas from OGC 07-036
     Note: The root document of the GML 3.2.1 schema is
       http://schemas.opengis.net/gml/3.2.1/gml.xsd

Note:: GML 2.1.2 will link to the newest version of GML 2.1.2.x .  As
of 5 September 2007, the contents of 2.1.2 contain GML 2.1.2.1 per OGC
06-189.  -- 2007-09-05

2007-08-27  Chris Holmes
  * v2.1.2: update 2.1.2.1 and ReadMe.txt changes
  * v2.1.2.0: Contains previous version of GML 2.1.2 (pre- 5 Sep 2007)
  * v2.1.2.1: Contains Corrigendum 1 for GML 2.1.2 schema fix (OGC 06-189).
  * v2.1.2.1: Corrigendum 1 for GML 2.1.2 schema fix (OGC 06-189) includes:
    + Official schema location is now http://schemas.opengis.net
    + replace xlink import schema location with ../../xlink/1.0.0/xlinks.xsd
    + remove gml/2.1.2/xlinks.xsd (optional, as is now unused).
    + geometry.xsd: fixed so will now validate by conformant processors by:
    + geometry.xsd: moving minOccurs/maxOccurs cardinality indicators from
      <element> declarations to their containing <sequence> elements in the
      context of the GML property pattern. -- SJDC 2006-12-07
    + gml:Coord is suppressed. -- SJDC 2006-12-07

2005-11-22  Arliss Whiteside
  * GML versions 2.0.0 through 3.1.1: The sets of XML Schema Documents for
    OpenGIS GML Versions 2.0.0 through 3.1.1 have been edited to reflect the
    corrigenda to all those OGC documents that is based on the change requests:
    OGC 05-068r1 "Store xlinks.xsd file at a fixed location"
    OGC 05-081r2 "Change to use relative paths"
    OGC 05-105 "Remove description and copyright tags from XML schema documents"

  * Note: check each OGC numbered document for detailed changes.

-----------------------------------------------------------------------

Policies, Procedures, Terms, and Conditions of OGC(r) are available
  http://www.opengeospatial.org/ogc/legal/ .

OGC and OpenGIS are registered trademarks of Open Geospatial Consortium.

Copyright (c) 2012, 2018 Open Geospatial Consortium

-----------------------------------------------------------------------
