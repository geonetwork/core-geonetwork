<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:pav="http://purl.org/pav/"
                xmlns:dct="http://purl.org/dc/terms/"
                exclude-result-prefixes="#all">

  <!-- TODO: either we only use the associated records in the current record
             either we get all associated records using the API?
             we may also have limitation depending on user privileges.-->

  <xsl:variable name="isoAssociatedTypesToDcatCommonNames"
                as="node()*">
    <entry associationType="crossReference" initiativeType="collection">dct:isPartOf</entry>
    <entry associationType="partOfSeamlessDatabase">dct:isPartOf</entry>
    <entry associationType="series">dct:isPartOf</entry>
    <!-- TO DISCUSS: Should we use dcat:inSeries instead of isPartOf -->
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
      <xsl:apply-templates mode="rdf-object-ref-attribute"
                           select="*/mri:metadataReference|*/mri:aggregateDataSetIdentifier">
        <xsl:with-param name="isAbout" select="false()"/>
      </xsl:apply-templates>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
