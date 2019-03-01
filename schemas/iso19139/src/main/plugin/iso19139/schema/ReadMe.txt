19139 XSchemas TS RC (2006 May 4)
-----------------------------------------------

OGC GML 3.2.0 version => ISO 19136 XSchemas DIS (2005 november)
 -- http://www.isotc211.org/2005/

These schemas from ISO 19139 version 2005-DIS (Draft International Standard)
dated 2006 May 4. For the sake of convenience, GML 3.2 XML schemas (version
19136 DIS - 2005 november) are (temporarily) provided with the 19139 set of
schemas. They were retrieved from http://www.isotc211.org/2005/ . Once these
schemas are finalized they will become OGC GML 3.2.1 and ISO/TS 19136.

2012-07-21  Kevin Stegemoller
  * WARNING XLink change is NOT BACKWARD COMPATIBLE.
  * Changed OGC XLink (xlink:simpleLink) to W3C XLink (xlink:simpleAttrs)
  per an approved TC and PC motion during the Dec. 2011 Brussels meeting.
  This involved changing the XLink schemaLocation 
  from http://schemas.opengis.net/xlink/1.0.0/xlinks.xsd
  to http://www.w3.org/1999/xlink.xsd
  as well as the attributes xlink:simpleLink to xlink:simpleAttrs.
  See http://www.opengeospatial.org/blog/1597

2010-01-29  Kevin Stegemoller
  Changes made to these ISO 19139 schemas by OGC:
  * changed xlink references from relative to absolute
    so they use schemas.opengis.net/xlink/1.0.0/xlinks.xsd (REMOVED 2012-07-21).
  * update relative schema imports to absolute URLs (OGC 06-135r7 s#15)
  
2007-08-14  Kevin Stegemoller
  Changes made to these ISO 19139 schemas by OGC:
  * changed xlink references from ../xlink/xlinks.xsd to ../../../../xlink/1.0.0/xlinks.xsd
    so they use schemas.opengis.net/xlink/1.0.0/xlinks.xsd (REMOVED 2012-07-21).
  * removed xlinks directory and schema
  * replaced 19139-GML_readme.txt with this document.

