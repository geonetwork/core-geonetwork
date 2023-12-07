<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:reg="http://standards.iso.org/iso/19115/-3/reg/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:prov="http://www.w3.org/ns/prov#"
                xmlns:dcat="http://www.w3.org/ns/dcat#"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                xmlns:owl="http://www.w3.org/2002/07/owl#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/"
                xmlns:vcard="http://www.w3.org/2006/vcard/ns#"
                xmlns:org="http://www.w3.org/ns/org#"
                xmlns:pav="http://purl.org/pav/"
                xmlns:dct="http://purl.org/dc/terms/"
                exclude-result-prefixes="#all">

  <!-- TODO: either we only use the associated records in the current record
             either we get all associated records using the API? -->

  <xsl:variable name="isoAssociatedTypesToDcatCommonNames"
                as="node()*">
    <entry associationType="crossReference" initiativeType="collection">dct:isPartOf</entry>
    <entry associationType="crossReference">dct:references</entry>
    <entry associationType="isComposedOf">dct:hasPart</entry>
    <!-- <entry associationType="revisionOf">dct:isVersionOf</entry>-->
    <entry associationType="revisionOf">pav:previousVersion</entry>
    <!-- Others are dct:relation -->
  </xsl:variable>

  <!--
  RDF Property:	dcterms:relation (Fallback)
  Definition:	A resource with an unspecified relationship to the cataloged resource.
  Usage note:	dcterms:relation SHOULD be used where the nature of the relationship between a cataloged resource and related resources is not known. A more specific sub-property SHOULD be used if the nature of the relationship of the link is known. The property dcat:distribution SHOULD be used to link from a dcat:Dataset to a representation of the dataset, described as a dcat:Distribution
  See also:	Sub-properties of dcterms:relation in particular dcat:distribution, dcterms:hasPart, (and its sub-properties dcat:catalog, dcat:dataset, dcat:service ), dcterms:isPartOf, dcterms:conformsTo, dcterms:isFormatOf, dcterms:hasFormat, dcterms:isVersionOf, dcterms:hasVersion (and its sub-property dcat:hasVersion ), dcterms:replaces, dcterms:isReplacedBy, dcterms:references, dcterms:isReferencedBy, dcterms:requires, dcterms:isRequiredBy
  _____________________________
  RDF Property:	dcterms:hasPart (Supported)
  Definition:	A related resource that is included either physically or logically in the described resource.
  _____________________________
  RDF Property:	dcterms:isReferencedBy (Not supported)
  Definition:	A related resource, such as a publication, that references, cites, or otherwise points to the cataloged resource.
  Usage note:	In relation to the use case of data citation, when the cataloged resource is a dataset, the dcterms:isReferencedBy property allows to relate the dataset to the resources (such as scholarly publications) that cite or point to the dataset. Multiple dcterms:isReferencedBy properties can be used to indicate the dataset has been referenced by multiple publications, or other resources.
  Usage note:	This property is used to associate a resource with the resource (of type dcat:Resource) in question. For other relations to resources not covered with this property, the more generic property dcat:qualifiedRelation can be used. See also 15. Qualified relations.
  _____________________________
  RDF Property:	dcat:previousVersion (Supported)
  Definition:	The previous version of a resource in a lineage [PAV].
  Equivalent property:	pav:previousVersion
  Sub-property of:	prov:wasRevisionOf
  Usage note:
  This property is meant to be used to specify a version chain, consisting of snapshots of a resource.

  The notion of version used by this property is limited to versions resulting from revisions occurring to a resource as part of its life-cycle. One of the typical cases here is representing the history of the versions of a dataset that have been released over time.
  _____________________________
  RDF Property:	dcterms:replaces (Not supported - same as previous version?)
  Definition:	A related resource that is supplanted, displaced, or superseded by the described resource [DCTERMS].
  Sub-property of:	dcterms:relation
  _____________________________
  RDF Property:	dcat:hasVersion (Not supported)
  Definition:	This resource has a more specific, versioned resource [PAV].
  Equivalent property:	pav:hasVersion
  Sub-property of:	dcterms:hasVersion
  Sub-property of:	prov:generalizationOf
  Usage note:
  This property is intended for relating a non-versioned or abstract resource to several versioned resources, e.g., snapshots [PAV].

  The notion of version used by this property is limited to versions resulting from revisions occurring to a resource as part of its life-cycle. Therefore, its semantics is more specific than its super-property dcterms:hasVersion, which makes use of a broader notion of version, including editions and adaptations.
  _____________________________
  RDF Property:	dcat:hasCurrentVersion (Not supported - could be in a series, the not superseeded record)
  Definition:	This resource has a more specific, versioned resource with equivalent content [PAV].
  Equivalent property:	pav:hasCurrentVersion
  Sub-property of:	pav:hasVersion
  _____________________________
  RDF Property:	dcat:first (Not supported)
  Definition:	The first resource in an ordered collection or series of resources, to which the current resource belongs.
  Sub-property of:	xhv:first

  RDF Property:	dcat:last (Not supported)
  Definition:	The last resource in an ordered collection or series of resources, to which the current resource belongs.
  Sub-property of:	xhv:last

  RDF Property:	dcat:prev (Not supported)
  Definition:	The previous resource (before the current one) in an ordered collection or series of resources.
  Sub-property of:	xhv:prev
  -->
  <xsl:template mode="iso19115-3-to-dcat"
                match="mdb:identificationInfo/*/mri:associatedResource">
    <xsl:variable name="associationType"
                  select="*/mri:associationType/*/@codeListValue"/>
    <xsl:variable name="initiativeType"
                  select="*/mri:initiativeType/*/@codeListValue"/>
    <xsl:variable name="dcTypeForAssociationAndInitiative"
                  as="xs:string?"
                  select="$isoAssociatedTypesToDcatCommonNames[@associationType = $associationType and @initiativeType = $initiativeType]/text()"/>
    <xsl:variable name="dcTypeForAssociation"
                  as="xs:string?"
                  select="$isoAssociatedTypesToDcatCommonNames[@associationType = $associationType and not(@initiativeType)]/text()"/>
    <xsl:variable name="elementType"
                  as="xs:string"
                  select="if ($dcTypeForAssociationAndInitiative)
                          then $dcTypeForAssociationAndInitiative
                          else if ($dcTypeForAssociation)
                          then $dcTypeForAssociation
                          else 'dct:relation'"/>

    <xsl:element name="{$elementType}">
      <xsl:choose>
        <xsl:when test="*/mri:metadataReference/@xlink:href">
          <xsl:attribute name="rdf:about" select="*/mri:metadataReference/@xlink:href"/>
        </xsl:when>
        <xsl:when test="*/mri:metadataReference/@uuidref">
          <!-- TODO: Here we need a URI? -->
          <xsl:attribute name="rdf:about" select="*/mri:metadataReference/@uuidref"/>
        </xsl:when>
        <xsl:otherwise>
          <!-- TODO: Here we need a URI? -->
          <xsl:attribute name="rdf:about" select="*/mri:aggregateDataSetIdentifier/*/mri:code/*/text()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
