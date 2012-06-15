<xsl:stylesheet version="1.0"
                xmlns="http://www.interlis.ch/INTERLIS2.3"                
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                
     <xsl:param name="group">geocat.ch</xsl:param>
                
    <xsl:template name="header">
        <HEADERSECTION VERSION='2.3' SENDER='{$group}'>
          <MODELS/>
          <ALIAS>
              <ENTRIES FOR="GM03_2Core">
                <TAGENTRY FROM="GM03_2Core.Core" TO="GM03_2Core.Core"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive" TO="GM03_2Core.Core"/>
                <TAGENTRY FROM="GM03_2Core.Core.CharacterString_" TO="GM03_2Core.Core.CharacterString_"/>
                <TAGENTRY FROM="GM03_2Core.Core.CharacterStringLong_" TO="GM03_2Core.Core.CharacterStringLong_"/>
                <TAGENTRY FROM="GM03_2Core.Core.CI_RoleCode_" TO="GM03_2Core.Core.CI_RoleCode_"/>
                <TAGENTRY FROM="GM03_2Core.Core.DateTime_" TO="GM03_2Core.Core.DateTime_"/>
                <TAGENTRY FROM="GM03_2Core.Core.GM_Point_" TO="GM03_2Core.Core.GM_Point_"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_CharacterSetCode_" TO="GM03_2Core.Core.MD_CharacterSetCode_"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_ProgressCode_" TO="GM03_2Core.Core.MD_ProgressCode_"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_ScopeCode_" TO="GM03_2Core.Core.MD_ScopeCode_"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_SpatialRepresentationTypeCode_" TO="GM03_2Core.Core.MD_SpatialRepresentationTypeCode_"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_TopicCategoryCode_" TO="GM03_2Core.Core.MD_TopicCategoryCode_"/>
                <TAGENTRY FROM="GM03_2Core.Core.Real_" TO="GM03_2Core.Core.Real_"/>
                <TAGENTRY FROM="GM03_2Core.Core.URL_" TO="GM03_2Core.Core.URL_"/>
                <TAGENTRY FROM="GM03_2Core.Core.DQ_DataQuality" TO="GM03_2Core.Core.DQ_DataQuality"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_Authority" TO="GM03_2Core.Core.MD_Authority"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_DigitalTransferOptions" TO="GM03_2Core.Core.MD_DigitalTransferOptions"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_DigitalTransferOptions" TO="GM03_2Core.Core.MD_DigitalTransferOptions"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_DigitalTransferOptions" ATTR="unitsOfDistribution"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_DigitalTransferOptions" ATTR="transferSize"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_Distribution" TO="GM03_2Core.Core.MD_Distribution"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_ReferenceSystem" TO="GM03_2Core.Core.MD_ReferenceSystem"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_CRS" TO="GM03_2Core.Core.MD_ReferenceSystem"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_Thesaurus" TO="GM03_2Core.Core.MD_Thesaurus"/>
                <TAGENTRY FROM="GM03_2Core.Core.SC_VerticalDatum" TO="GM03_2Core.Core.SC_VerticalDatum"/>
                <TAGENTRY FROM="GM03_2Core.Core.CI_Address" TO="GM03_2Core.Core.CI_Address"/>
                <TAGENTRY FROM="GM03_2Core.Core.CI_Date" TO="GM03_2Core.Core.CI_Date"/>
                <TAGENTRY FROM="GM03_2Core.Core.CI_Telephone" TO="GM03_2Core.Core.CI_Telephone"/>
                <TAGENTRY FROM="GM03_2Core.Core.DQ_Scope" TO="GM03_2Core.Core.DQ_Scope"/>
                <TAGENTRY FROM="GM03_2Core.Core.EX_VerticalExtent" TO="GM03_2Core.Core.EX_VerticalExtent"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_Format" TO="GM03_2Core.Core.MD_Format"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Format" TO="GM03_2Core.Core.MD_Format"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Format" ATTR="amendmentNumber"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Format" ATTR="specification"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Format" ATTR="fileDecompressionTechnique"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_Metadata" TO="GM03_2Core.Core.MD_Metadata"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_RepresentativeFraction" TO="GM03_2Core.Core.MD_RepresentativeFraction"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_Resolution" TO="GM03_2Core.Core.MD_Resolution"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_ScopeDescription" TO="GM03_2Core.Core.MD_ScopeDescription"/>
                <TAGENTRY FROM="GM03_2Core.Core.PT_Group" TO="GM03_2Core.Core.PT_Group"/>
                <TAGENTRY FROM="GM03_2Core.Core.PT_URLGroup" TO="GM03_2Core.Core.PT_URLGroup"/>
                <TAGENTRY FROM="GM03_2Core.Core.TM_Primitive" TO="GM03_2Core.Core.TM_Primitive"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_DistributiontransferOptions" TO="GM03_2Core.Core.MD_DistributiontransferOptions"/>
                <TAGENTRY FROM="GM03_2Core.Core.EX_BoundingPolygon" TO="GM03_2Core.Core.EX_BoundingPolygon"/>
                <TAGENTRY FROM="GM03_2Core.Core.EX_GeographicBoundingBox" TO="GM03_2Core.Core.EX_GeographicBoundingBox"/>
                <TAGENTRY FROM="GM03_2Core.Core.EX_GeographicDescription" TO="GM03_2Core.Core.EX_GeographicDescription"/>
                <TAGENTRY FROM="GM03_2Core.Core.EX_TemporalExtent" TO="GM03_2Core.Core.EX_TemporalExtent"/>
                <TAGENTRY FROM="GM03_2Core.Core.EX_SpatialTemporalExtent" TO="GM03_2Core.Core.EX_TemporalExtent"/>
                <TAGENTRY FROM="GM03_2Core.Core.PT_FreeText" TO="GM03_2Core.Core.PT_FreeText"/>
                <TAGENTRY FROM="GM03_2Core.Core.PT_FreeURL" TO="GM03_2Core.Core.PT_FreeURL"/>
                <TAGENTRY FROM="GM03_2Core.Core.distributionInfoMD_Metadata" TO="GM03_2Core.Core.distributionInfoMD_Metadata"/>
                <TAGENTRY FROM="GM03_2Core.Core.DQ_ScopelevelDescription" TO="GM03_2Core.Core.DQ_ScopelevelDescription"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_DistributiondistributionFormat" TO="GM03_2Core.Core.MD_DistributiondistributionFormat"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_MetadatadataQualityInfo" TO="GM03_2Core.Core.MD_MetadatadataQualityInfo"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_ResolutionequivalentScale" TO="GM03_2Core.Core.MD_ResolutionequivalentScale"/>
                <TAGENTRY FROM="GM03_2Core.Core.parentIdentifierMD_Metadata" TO="GM03_2Core.Core.parentIdentifierMD_Metadata"/>
                <TAGENTRY FROM="GM03_2Core.Core.referenceSystemInfoMD_Metadata" TO="GM03_2Core.Core.referenceSystemInfoMD_Metadata"/>
                <TAGENTRY FROM="GM03_2Core.Core.scopeDQ_DataQuality" TO="GM03_2Core.Core.scopeDQ_DataQuality"/>
                <TAGENTRY FROM="GM03_2Core.Core.verticalDatumEX_VerticalExtent" TO="GM03_2Core.Core.verticalDatumEX_VerticalExtent"/>
                <TAGENTRY FROM="GM03_2Core.Core.CI_Citation" TO="GM03_2Core.Core.CI_Citation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.CI_Citation" TO="GM03_2Core.Core.CI_Citation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.CI_Citation" ATTR="edition"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.CI_Citation" ATTR="editionDate"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.CI_Citation" ATTR="presentationForm"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.CI_Citation" ATTR="ISBN"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.CI_Citation" ATTR="ISSN"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.CI_Citation" ATTR="alternateTitle"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.CI_Citation" ATTR="collectiveTitle"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.CI_Citation" ATTR="otherCitationDetails"/>
                <TAGENTRY FROM="GM03_2Core.Core.CI_Contact" TO="GM03_2Core.Core.CI_Contact"/>
                <TAGENTRY FROM="GM03_2Core.Core.CI_OnlineResource" TO="GM03_2Core.Core.CI_OnlineResource"/>
                <TAGENTRY FROM="GM03_2Core.Core.CI_ResponsibleParty" TO="GM03_2Core.Core.CI_ResponsibleParty"/>
                <TAGENTRY FROM="GM03_2Core.Core.EX_Extent" TO="GM03_2Core.Core.EX_Extent"/>
                <TAGENTRY FROM="GM03_2Core.Core.EX_SpatialTemporalExtent" TO="GM03_2Core.Core.EX_SpatialTemporalExtent"/>
                <TAGENTRY FROM="GM03_2Core.Core.LI_Lineage" TO="GM03_2Core.Core.LI_Lineage"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_Identifier" TO="GM03_2Core.Core.MD_Identifier"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.RS_Identifier" TO="GM03_2Core.Core.MD_Identifier"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.RS_Identifier" ATTR="codeSpace"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.RS_Identifier" ATTR="version"/>
                <TAGENTRY FROM="GM03_2Core.Core.RS_Identifier" TO="GM03_2Core.Core.MD_Identifier"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_Keywords" TO="GM03_2Core.Core.MD_Keywords"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_DataIdentification" TO="GM03_2Core.Core.MD_DataIdentification"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_DataIdentification" TO="GM03_2Core.Core.MD_DataIdentification"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_DataIdentification" ATTR="ProjectType"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_DataIdentification" ATTR="credit"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_DataIdentification" ATTR="basicGeodataID"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_DataIdentification" ATTR="basicGeodataIDType"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_DataIdentification" ATTR="environmentDescription"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_DataIdentification" ATTR="supplementalInformation"/>
                <TAGENTRY FROM="GM03_2Core.Core.RS_Identifier" TO="GM03_2Core.Core.RS_Identifier"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.RS_Identifier" TO="GM03_2Core.Core.RS_Identifier"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.RS_Identifier" ATTR="codeSpace"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.RS_Identifier" ATTR="version"/>
                <TAGENTRY FROM="GM03_2Core.Core.authorityMD_Identifier" TO="GM03_2Core.Core.authorityMD_Identifier"/>
                <TAGENTRY FROM="GM03_2Core.Core.CI_Citationdate" TO="GM03_2Core.Core.CI_Citationdate"/>
                <TAGENTRY FROM="GM03_2Core.Core.CI_ResponsiblePartyaddress" TO="GM03_2Core.Core.CI_ResponsiblePartyaddress"/>
                <TAGENTRY FROM="GM03_2Core.Core.CI_ResponsiblePartycontactInfo" TO="GM03_2Core.Core.CI_ResponsiblePartycontactInfo"/>
                <TAGENTRY FROM="GM03_2Core.Core.CI_ResponsiblePartyparentinfo" TO="GM03_2Core.Core.CI_ResponsiblePartyparentinfo"/>
                <TAGENTRY FROM="GM03_2Core.Core.CI_ResponsiblePartyphone" TO="GM03_2Core.Core.CI_ResponsiblePartyphone"/>
                <TAGENTRY FROM="GM03_2Core.Core.citationCI_Citation" TO="GM03_2Core.Core.citationCI_Citation"/>
                <TAGENTRY FROM="GM03_2Core.Core.descriptiveKeywordsMD_Identification" TO="GM03_2Core.Core.descriptiveKeywordsMD_Identification"/>
                <TAGENTRY FROM="GM03_2Core.Core.DQ_DataQualitylineage" TO="GM03_2Core.Core.DQ_DataQualitylineage"/>
                <TAGENTRY FROM="GM03_2Core.Core.EX_ExtentgeographicElement" TO="GM03_2Core.Core.EX_ExtentgeographicElement"/>
                <TAGENTRY FROM="GM03_2Core.Core.EX_ExtenttemporalElement" TO="GM03_2Core.Core.EX_ExtenttemporalElement"/>
                <TAGENTRY FROM="GM03_2Core.Core.EX_ExtentverticalElement" TO="GM03_2Core.Core.EX_ExtentverticalElement"/>
                <TAGENTRY FROM="GM03_2Core.Core.EX_GeographicDescriptiongeographicIdentifier" TO="GM03_2Core.Core.EX_GeographicDescriptiongeographicIdentifier"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_DigitalTransferOptionsonLine" TO="GM03_2Core.Core.MD_DigitalTransferOptionsonLine"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_Identificationcitation" TO="GM03_2Core.Core.MD_Identificationcitation"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_IdentificationpointOfContact" TO="GM03_2Core.Core.MD_IdentificationpointOfContact"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_Keywordsthesaurus" TO="GM03_2Core.Core.MD_Keywordsthesaurus"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_Metadatacontact" TO="GM03_2Core.Core.MD_Metadatacontact"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_MetadataidentificationInfo" TO="GM03_2Core.Core.MD_MetadataidentificationInfo"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_Thesauruscitation" TO="GM03_2Core.Core.MD_Thesauruscitation"/>
                <TAGENTRY FROM="GM03_2Core.Core.spatialExtentEX_SpatialTemporalExtent" TO="GM03_2Core.Core.spatialExtentEX_SpatialTemporalExtent"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_DataIdentificationextent" TO="GM03_2Core.Core.MD_DataIdentificationextent"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_DataIdentificationspatialResolution" TO="GM03_2Core.Core.MD_DataIdentificationspatialResolution"/>
                <TAGENTRY FROM="GM03_2Core.Core.MD_ReferenceSystemreferenceSystemIdentifier" TO="GM03_2Core.Core.MD_ReferenceSystemreferenceSystemIdentifier"/>
                <TAGENTRY FROM="GM03_2Core.Core.SC_VerticalDatumdatumID" TO="GM03_2Core.Core.SC_VerticalDatumdatumID"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.CI_PresentationFormCode_"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DCPList_"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.GenericName_"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_MediumFormatCode_"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_RestrictionCode_"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.Record_"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.CI_Series"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.Duration"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.gml_CodeType"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.LI_ProcessStep"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.LI_Source"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ArchiveConcept"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Attribute"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_BrowseGraphic"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_CodeDomain"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_CodeValue"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Constraints"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Distributor"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_EllipsoidParameters"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_HistoryConcept"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_MetadataExtensionInformation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ObliqueLineAzimuth"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ObliqueLinePoint"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_PortrayalCatalogueReference"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ProjectionParameters"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Revision"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Role"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_StandardOrderProcess"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Type"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Usage"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.SV_OperationChainMetadata"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_Scopeextent"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.resourceFormatMD_Identification"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_ConformanceResult"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_QuantitativeResult"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_AggregateInformation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ApplicationSchemaInformation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Association"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Class"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_CoverageDescription"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Dimension"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ExtendedElementInformation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_FeatureCatalogueDescription"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_GeometricObjects"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_GridSpatialRepresentation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_LegalConstraints"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Legislation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Medium"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_RangeDimension"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_SecurityConstraints"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_VectorSpatialRepresentation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.SV_CoupledResource"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.SV_OperationMetadata"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.SV_Parameter"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.anonymousTypeMD_Attribute"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.attributeMD_AbstractClass"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.contentInfoMD_Metadata"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.distributionOrderProcessMD_Distributor"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.distributorTransferOptionsMD_Distributor"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.ellipsoidParametersMD_CRS"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.formatDistributordistributorFormat"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.graphicOverviewMD_Identification"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.LI_ProcessStepprocessor"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.LI_SourcescaleDenominator"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.LI_SourcesourceExtent"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.LI_SourcesourceReferenceSystem"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ArchiveConceptarchiveConceptCitation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_AttributenamedType"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_CRSdatum"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_CRSellipsoid"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_CRSprojection"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Distributiondistributor"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_DistributordistributorContact"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_HistoryConcepthistoryConceptCitation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_MetadataExtensionInfoextensionOnLineResource"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_MetadatametadataConstraints"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_MetadataspatialRepresentationInfo"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_RevisionrevisionContact"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_RevisionrevisionExtent"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_RevisionrevisionScopeDescription"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_RoletoClass"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_UsageuserContactInfo"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.metadataExtensionInfoMD_Metadata"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.obliqueLineAzimuthParameterMD_ProjectionParameters"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.obliqueLinePointParameterMD_ProjectionParameters"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.portrayalCatalogueInfoMD_Metadata"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.processStepLI_Lineage"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.projectionParametersMD_CRS"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.resourceConstraintsMD_Identification"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.resourceSpecificUsageMD_Identification"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.revisionMD_Identification"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.sourceLI_Lineage"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.sourceStepsource"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.subClassbaseClass"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.subDomainbaseDomain"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.subValueMD_CodeValue"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.typeMD_CodeDomain"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.valueMD_Type"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Band"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Georectified"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Georeferenceable"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ImageDescription"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.SV_ServiceIdentification"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.aggregationInfo_MD_Identification"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.applicationSchemaInfoMD_Metadata"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.axisDimensionPropertiesMD_GridSpatialRepresentation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.CI_CitationcitedResponsibleParty"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.CI_Citationidentifier"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.CI_Citationseries"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.classMD_FeatureCatalogueDescription"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.dimensionMD_CoverageDescription"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.domainMD_FeatureCatalogueDescription"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_ConformanceResultspecification"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_ElementevaluationProcedure"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_ElementmeasureIdentification"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.extendedElementInformationMD_MetadataExtensionInformation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.featureCatalogueCitationCI_Citation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.legislationConstraintsMD_LegalConstraints"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.LI_SourcesourceCitation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_AggregateInformationaggregateDataSetIdentifier"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_AggregateInformationaggregateDataSetName"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ApplicationSchemaInformationname"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_DigitalTransferOptionsoffLine"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ExtendedElementInformationsource"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Legislationtitle"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_MetadatalegislationInformation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_PortrayalCatalogueReferenceportrayalCatalogueCitation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_VectorSpatialRepresentationgeometricObjects"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.parametersoperation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.reportDQ_DataQuality"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.resultDQ_Element"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.roleMD_Association"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.SV_OperationChainMetadataoperation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.SV_OperationMetadataconnectPoint"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_AbsoluteExternalPositionalAccuracy"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_AccuracyOfATimeMeasurement"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_CompletenessCommission"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_CompletenessOmission"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_ConceptualConsistency"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_DomainConsistency"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_FormatConsistency"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_GriddedDataPositionalAccuracy"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_NonQuantitativeAttributeAccuracy"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_QuantitativeAttributeAccuracy"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_RelativeInternalPositionalAccuracy"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_TemporalConsistency"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_TemporalValidity"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_ThematicClassificationCorrectness"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.DQ_TopologicalConsistency"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.containsOperationsSV_ServiceIdentification"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.extentSV_ServiceIdentification"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_GeoreferenceableparameterCitation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ImageDescriptionimageQualitiyCode"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ImageDescriptionprocessingLevelCode"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformationarchiveConcept"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformationcontact"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformationhistoryConcept"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformationupdateScopeDescription"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_MetadatametadataMaintenance"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.resourceMaintenanceMD_Identification"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.SV_ServiceIdentificationaccessProperties"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.SV_ServiceIdentificationrestrictions"/>
              </ENTRIES>
              <ENTRIES FOR="GM03_2Comprehensive">
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive" TO="GM03_2Comprehensive.Comprehensive"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.CI_PresentationFormCode_" TO="GM03_2Comprehensive.Comprehensive.CI_PresentationFormCode_"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DCPList_" TO="GM03_2Comprehensive.Comprehensive.DCPList_"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.GenericName_" TO="GM03_2Comprehensive.Comprehensive.GenericName_"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_MediumFormatCode_" TO="GM03_2Comprehensive.Comprehensive.MD_MediumFormatCode_"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_RestrictionCode_" TO="GM03_2Comprehensive.Comprehensive.MD_RestrictionCode_"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.Record_" TO="GM03_2Comprehensive.Comprehensive.Record_"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.CI_Series" TO="GM03_2Comprehensive.Comprehensive.CI_Series"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.Duration" TO="GM03_2Comprehensive.Comprehensive.Duration"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.gml_CodeType" TO="GM03_2Comprehensive.Comprehensive.gml_CodeType"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.LI_ProcessStep" TO="GM03_2Comprehensive.Comprehensive.LI_ProcessStep"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.LI_Source" TO="GM03_2Comprehensive.Comprehensive.LI_Source"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_ArchiveConcept" TO="GM03_2Comprehensive.Comprehensive.MD_ArchiveConcept"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Attribute" TO="GM03_2Comprehensive.Comprehensive.MD_Attribute"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_BrowseGraphic" TO="GM03_2Comprehensive.Comprehensive.MD_BrowseGraphic"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_CodeDomain" TO="GM03_2Comprehensive.Comprehensive.MD_CodeDomain"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_CodeValue" TO="GM03_2Comprehensive.Comprehensive.MD_CodeValue"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Constraints" TO="GM03_2Comprehensive.Comprehensive.MD_Constraints"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_LegalConstraints" TO="GM03_2Comprehensive.Comprehensive.MD_Constraints"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_LegalConstraints" ATTR="accessConstraints"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_LegalConstraints" ATTR="useConstraints"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_LegalConstraints" ATTR="otherConstraints"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_SecurityConstraints" TO="GM03_2Comprehensive.Comprehensive.MD_Constraints"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_SecurityConstraints" ATTR="classification"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_SecurityConstraints" ATTR="classificationSystem"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_SecurityConstraints" ATTR="userNote"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_SecurityConstraints" ATTR="handlingDescription"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_CRS" TO="GM03_2Comprehensive.Comprehensive.MD_CRS"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_DigitalTransferOptions" TO="GM03_2Comprehensive.Comprehensive.MD_DigitalTransferOptions"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Distributor" TO="GM03_2Comprehensive.Comprehensive.MD_Distributor"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_EllipsoidParameters" TO="GM03_2Comprehensive.Comprehensive.MD_EllipsoidParameters"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Format" TO="GM03_2Comprehensive.Comprehensive.MD_Format"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_HistoryConcept" TO="GM03_2Comprehensive.Comprehensive.MD_HistoryConcept"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_MetadataExtensionInformation" TO="GM03_2Comprehensive.Comprehensive.MD_MetadataExtensionInformation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_ObliqueLineAzimuth" TO="GM03_2Comprehensive.Comprehensive.MD_ObliqueLineAzimuth"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_ObliqueLinePoint" TO="GM03_2Comprehensive.Comprehensive.MD_ObliqueLinePoint"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_PortrayalCatalogueReference" TO="GM03_2Comprehensive.Comprehensive.MD_PortrayalCatalogueReference"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_ProjectionParameters" TO="GM03_2Comprehensive.Comprehensive.MD_ProjectionParameters"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Revision" TO="GM03_2Comprehensive.Comprehensive.MD_Revision"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Role" TO="GM03_2Comprehensive.Comprehensive.MD_Role"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_StandardOrderProcess" TO="GM03_2Comprehensive.Comprehensive.MD_StandardOrderProcess"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Type" TO="GM03_2Comprehensive.Comprehensive.MD_Type"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Usage" TO="GM03_2Comprehensive.Comprehensive.MD_Usage"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.RS_Identifier" TO="GM03_2Comprehensive.Comprehensive.RS_Identifier"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.SV_OperationChainMetadata" TO="GM03_2Comprehensive.Comprehensive.SV_OperationChainMetadata"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_Scopeextent" TO="GM03_2Comprehensive.Comprehensive.DQ_Scopeextent"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.resourceFormatMD_Identification" TO="GM03_2Comprehensive.Comprehensive.resourceFormatMD_Identification"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.CI_Citation" TO="GM03_2Comprehensive.Comprehensive.CI_Citation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_ConformanceResult" TO="GM03_2Comprehensive.Comprehensive.DQ_ConformanceResult"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_QuantitativeResult" TO="GM03_2Comprehensive.Comprehensive.DQ_QuantitativeResult"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_AggregateInformation" TO="GM03_2Comprehensive.Comprehensive.MD_AggregateInformation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_ApplicationSchemaInformation" TO="GM03_2Comprehensive.Comprehensive.MD_ApplicationSchemaInformation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Association" TO="GM03_2Comprehensive.Comprehensive.MD_Association"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Class" TO="GM03_2Comprehensive.Comprehensive.MD_Class"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_CoverageDescription" TO="GM03_2Comprehensive.Comprehensive.MD_CoverageDescription"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_ImageDescription" TO="GM03_2Comprehensive.Comprehensive.MD_CoverageDescription"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ImageDescription" ATTR="illuminationElevationAngle"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ImageDescription" ATTR="illuminationAzimuthAngle"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ImageDescription" ATTR="imagingCondition"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ImageDescription" ATTR="cloudCoverPercentage"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ImageDescription" ATTR="compressionGenerationQuantity"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ImageDescription" ATTR="triangulationIndicator"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ImageDescription" ATTR="radiometricCalibrationDataAvailability"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ImageDescription" ATTR="cameraCalibrationInformationAvailability"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ImageDescription" ATTR="filmDistortionInformationAvailability"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_ImageDescription" ATTR="lensDistortionInformationAvailability"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_DataIdentification" TO="GM03_2Comprehensive.Comprehensive.MD_DataIdentification"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Dimension" TO="GM03_2Comprehensive.Comprehensive.MD_Dimension"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_ExtendedElementInformation" TO="GM03_2Comprehensive.Comprehensive.MD_ExtendedElementInformation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_FeatureCatalogueDescription" TO="GM03_2Comprehensive.Comprehensive.MD_FeatureCatalogueDescription"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_GeometricObjects" TO="GM03_2Comprehensive.Comprehensive.MD_GeometricObjects"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_GridSpatialRepresentation" TO="GM03_2Comprehensive.Comprehensive.MD_GridSpatialRepresentation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Georectified" TO="GM03_2Comprehensive.Comprehensive.MD_GridSpatialRepresentation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Georectified" ATTR="checkPointAvailability"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Georectified" ATTR="checkPointDescription"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Georectified" ATTR="cornerPoints"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Georectified" ATTR="centerPoint"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Georectified" ATTR="pointInPixel"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Georectified" ATTR="transformationDimensionDescription"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Georectified" ATTR="transformationDimensionMapping"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Georeferenceable" TO="GM03_2Comprehensive.Comprehensive.MD_GridSpatialRepresentation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Georeferenceable" ATTR="controlPointAvailability"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Georeferenceable" ATTR="orientationParameterAvailability"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Georeferenceable" ATTR="orientationParameterDescription"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Georeferenceable" ATTR="georeferencedParameters"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_LegalConstraints" TO="GM03_2Comprehensive.Comprehensive.MD_LegalConstraints"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Legislation" TO="GM03_2Comprehensive.Comprehensive.MD_Legislation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Medium" TO="GM03_2Comprehensive.Comprehensive.MD_Medium"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_RangeDimension" TO="GM03_2Comprehensive.Comprehensive.MD_RangeDimension"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Band" TO="GM03_2Comprehensive.Comprehensive.MD_RangeDimension"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Band" ATTR="maxValue"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Band" ATTR="minValue"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Band" ATTR="units"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Band" ATTR="peakResponse"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Band" ATTR="bitsPerValue"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Band" ATTR="toneGradation"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Band" ATTR="scaleFactor"/>
                <DELENTRY TAG="GM03_2Comprehensive.Comprehensive.MD_Band" ATTR="offset"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_SecurityConstraints" TO="GM03_2Comprehensive.Comprehensive.MD_SecurityConstraints"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_VectorSpatialRepresentation" TO="GM03_2Comprehensive.Comprehensive.MD_VectorSpatialRepresentation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.SV_CoupledResource" TO="GM03_2Comprehensive.Comprehensive.SV_CoupledResource"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.SV_OperationMetadata" TO="GM03_2Comprehensive.Comprehensive.SV_OperationMetadata"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.SV_Parameter" TO="GM03_2Comprehensive.Comprehensive.SV_Parameter"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.anonymousTypeMD_Attribute" TO="GM03_2Comprehensive.Comprehensive.anonymousTypeMD_Attribute"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.attributeMD_AbstractClass" TO="GM03_2Comprehensive.Comprehensive.attributeMD_AbstractClass"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.contentInfoMD_Metadata" TO="GM03_2Comprehensive.Comprehensive.contentInfoMD_Metadata"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.distributionOrderProcessMD_Distributor" TO="GM03_2Comprehensive.Comprehensive.distributionOrderProcessMD_Distributor"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.distributorTransferOptionsMD_Distributor" TO="GM03_2Comprehensive.Comprehensive.distributorTransferOptionsMD_Distributor"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.ellipsoidParametersMD_CRS" TO="GM03_2Comprehensive.Comprehensive.ellipsoidParametersMD_CRS"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.formatDistributordistributorFormat" TO="GM03_2Comprehensive.Comprehensive.formatDistributordistributorFormat"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.graphicOverviewMD_Identification" TO="GM03_2Comprehensive.Comprehensive.graphicOverviewMD_Identification"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.LI_ProcessStepprocessor" TO="GM03_2Comprehensive.Comprehensive.LI_ProcessStepprocessor"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.LI_SourcescaleDenominator" TO="GM03_2Comprehensive.Comprehensive.LI_SourcescaleDenominator"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.LI_SourcesourceExtent" TO="GM03_2Comprehensive.Comprehensive.LI_SourcesourceExtent"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.LI_SourcesourceReferenceSystem" TO="GM03_2Comprehensive.Comprehensive.LI_SourcesourceReferenceSystem"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_ArchiveConceptarchiveConceptCitation" TO="GM03_2Comprehensive.Comprehensive.MD_ArchiveConceptarchiveConceptCitation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_AttributenamedType" TO="GM03_2Comprehensive.Comprehensive.MD_AttributenamedType"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_CRSdatum" TO="GM03_2Comprehensive.Comprehensive.MD_CRSdatum"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_CRSellipsoid" TO="GM03_2Comprehensive.Comprehensive.MD_CRSellipsoid"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_CRSprojection" TO="GM03_2Comprehensive.Comprehensive.MD_CRSprojection"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Distributiondistributor" TO="GM03_2Comprehensive.Comprehensive.MD_Distributiondistributor"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_DistributordistributorContact" TO="GM03_2Comprehensive.Comprehensive.MD_DistributordistributorContact"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_HistoryConcepthistoryConceptCitation" TO="GM03_2Comprehensive.Comprehensive.MD_HistoryConcepthistoryConceptCitation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_MetadataExtensionInfoextensionOnLineResource" TO="GM03_2Comprehensive.Comprehensive.MD_MetadataExtensionInfoextensionOnLineResource"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_MetadatametadataConstraints" TO="GM03_2Comprehensive.Comprehensive.MD_MetadatametadataConstraints"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_MetadataspatialRepresentationInfo" TO="GM03_2Comprehensive.Comprehensive.MD_MetadataspatialRepresentationInfo"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_RevisionrevisionContact" TO="GM03_2Comprehensive.Comprehensive.MD_RevisionrevisionContact"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_RevisionrevisionExtent" TO="GM03_2Comprehensive.Comprehensive.MD_RevisionrevisionExtent"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_RevisionrevisionScopeDescription" TO="GM03_2Comprehensive.Comprehensive.MD_RevisionrevisionScopeDescription"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_RoletoClass" TO="GM03_2Comprehensive.Comprehensive.MD_RoletoClass"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_UsageuserContactInfo" TO="GM03_2Comprehensive.Comprehensive.MD_UsageuserContactInfo"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.metadataExtensionInfoMD_Metadata" TO="GM03_2Comprehensive.Comprehensive.metadataExtensionInfoMD_Metadata"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.obliqueLineAzimuthParameterMD_ProjectionParameters" TO="GM03_2Comprehensive.Comprehensive.obliqueLineAzimuthParameterMD_ProjectionParameters"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.obliqueLinePointParameterMD_ProjectionParameters" TO="GM03_2Comprehensive.Comprehensive.obliqueLinePointParameterMD_ProjectionParameters"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.portrayalCatalogueInfoMD_Metadata" TO="GM03_2Comprehensive.Comprehensive.portrayalCatalogueInfoMD_Metadata"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.processStepLI_Lineage" TO="GM03_2Comprehensive.Comprehensive.processStepLI_Lineage"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.projectionParametersMD_CRS" TO="GM03_2Comprehensive.Comprehensive.projectionParametersMD_CRS"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.resourceConstraintsMD_Identification" TO="GM03_2Comprehensive.Comprehensive.resourceConstraintsMD_Identification"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.resourceSpecificUsageMD_Identification" TO="GM03_2Comprehensive.Comprehensive.resourceSpecificUsageMD_Identification"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.revisionMD_Identification" TO="GM03_2Comprehensive.Comprehensive.revisionMD_Identification"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.sourceLI_Lineage" TO="GM03_2Comprehensive.Comprehensive.sourceLI_Lineage"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.sourceStepsource" TO="GM03_2Comprehensive.Comprehensive.sourceStepsource"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.subClassbaseClass" TO="GM03_2Comprehensive.Comprehensive.subClassbaseClass"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.subDomainbaseDomain" TO="GM03_2Comprehensive.Comprehensive.subDomainbaseDomain"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.subValueMD_CodeValue" TO="GM03_2Comprehensive.Comprehensive.subValueMD_CodeValue"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.typeMD_CodeDomain" TO="GM03_2Comprehensive.Comprehensive.typeMD_CodeDomain"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.valueMD_Type" TO="GM03_2Comprehensive.Comprehensive.valueMD_Type"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Band" TO="GM03_2Comprehensive.Comprehensive.MD_Band"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Georectified" TO="GM03_2Comprehensive.Comprehensive.MD_Georectified"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Georeferenceable" TO="GM03_2Comprehensive.Comprehensive.MD_Georeferenceable"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_ImageDescription" TO="GM03_2Comprehensive.Comprehensive.MD_ImageDescription"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformation" TO="GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.SV_ServiceIdentification" TO="GM03_2Comprehensive.Comprehensive.SV_ServiceIdentification"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.aggregationInfo_MD_Identification" TO="GM03_2Comprehensive.Comprehensive.aggregationInfo_MD_Identification"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.applicationSchemaInfoMD_Metadata" TO="GM03_2Comprehensive.Comprehensive.applicationSchemaInfoMD_Metadata"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.axisDimensionPropertiesMD_GridSpatialRepresentation" TO="GM03_2Comprehensive.Comprehensive.axisDimensionPropertiesMD_GridSpatialRepresentation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.CI_CitationcitedResponsibleParty" TO="GM03_2Comprehensive.Comprehensive.CI_CitationcitedResponsibleParty"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.CI_Citationidentifier" TO="GM03_2Comprehensive.Comprehensive.CI_Citationidentifier"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.CI_Citationseries" TO="GM03_2Comprehensive.Comprehensive.CI_Citationseries"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.classMD_FeatureCatalogueDescription" TO="GM03_2Comprehensive.Comprehensive.classMD_FeatureCatalogueDescription"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.dimensionMD_CoverageDescription" TO="GM03_2Comprehensive.Comprehensive.dimensionMD_CoverageDescription"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.domainMD_FeatureCatalogueDescription" TO="GM03_2Comprehensive.Comprehensive.domainMD_FeatureCatalogueDescription"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_ConformanceResultspecification" TO="GM03_2Comprehensive.Comprehensive.DQ_ConformanceResultspecification"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_ElementevaluationProcedure" TO="GM03_2Comprehensive.Comprehensive.DQ_ElementevaluationProcedure"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_ElementmeasureIdentification" TO="GM03_2Comprehensive.Comprehensive.DQ_ElementmeasureIdentification"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.extendedElementInformationMD_MetadataExtensionInformation" TO="GM03_2Comprehensive.Comprehensive.extendedElementInformationMD_MetadataExtensionInformation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.featureCatalogueCitationCI_Citation" TO="GM03_2Comprehensive.Comprehensive.featureCatalogueCitationCI_Citation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.legislationConstraintsMD_LegalConstraints" TO="GM03_2Comprehensive.Comprehensive.legislationConstraintsMD_LegalConstraints"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.LI_SourcesourceCitation" TO="GM03_2Comprehensive.Comprehensive.LI_SourcesourceCitation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_AggregateInformationaggregateDataSetIdentifier" TO="GM03_2Comprehensive.Comprehensive.MD_AggregateInformationaggregateDataSetIdentifier"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_AggregateInformationaggregateDataSetName" TO="GM03_2Comprehensive.Comprehensive.MD_AggregateInformationaggregateDataSetName"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_ApplicationSchemaInformationname" TO="GM03_2Comprehensive.Comprehensive.MD_ApplicationSchemaInformationname"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_DigitalTransferOptionsoffLine" TO="GM03_2Comprehensive.Comprehensive.MD_DigitalTransferOptionsoffLine"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_ExtendedElementInformationsource" TO="GM03_2Comprehensive.Comprehensive.MD_ExtendedElementInformationsource"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_Legislationtitle" TO="GM03_2Comprehensive.Comprehensive.MD_Legislationtitle"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_MetadatalegislationInformation" TO="GM03_2Comprehensive.Comprehensive.MD_MetadatalegislationInformation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_PortrayalCatalogueReferenceportrayalCatalogueCitation" TO="GM03_2Comprehensive.Comprehensive.MD_PortrayalCatalogueReferenceportrayalCatalogueCitation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_VectorSpatialRepresentationgeometricObjects" TO="GM03_2Comprehensive.Comprehensive.MD_VectorSpatialRepresentationgeometricObjects"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.parametersoperation" TO="GM03_2Comprehensive.Comprehensive.parametersoperation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.reportDQ_DataQuality" TO="GM03_2Comprehensive.Comprehensive.reportDQ_DataQuality"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.resultDQ_Element" TO="GM03_2Comprehensive.Comprehensive.resultDQ_Element"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.roleMD_Association" TO="GM03_2Comprehensive.Comprehensive.roleMD_Association"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.SV_OperationChainMetadataoperation" TO="GM03_2Comprehensive.Comprehensive.SV_OperationChainMetadataoperation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.SV_OperationMetadataconnectPoint" TO="GM03_2Comprehensive.Comprehensive.SV_OperationMetadataconnectPoint"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_AbsoluteExternalPositionalAccuracy" TO="GM03_2Comprehensive.Comprehensive.DQ_AbsoluteExternalPositionalAccuracy"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_AccuracyOfATimeMeasurement" TO="GM03_2Comprehensive.Comprehensive.DQ_AccuracyOfATimeMeasurement"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_CompletenessCommission" TO="GM03_2Comprehensive.Comprehensive.DQ_CompletenessCommission"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_CompletenessOmission" TO="GM03_2Comprehensive.Comprehensive.DQ_CompletenessOmission"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_ConceptualConsistency" TO="GM03_2Comprehensive.Comprehensive.DQ_ConceptualConsistency"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_DomainConsistency" TO="GM03_2Comprehensive.Comprehensive.DQ_DomainConsistency"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_FormatConsistency" TO="GM03_2Comprehensive.Comprehensive.DQ_FormatConsistency"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_GriddedDataPositionalAccuracy" TO="GM03_2Comprehensive.Comprehensive.DQ_GriddedDataPositionalAccuracy"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_NonQuantitativeAttributeAccuracy" TO="GM03_2Comprehensive.Comprehensive.DQ_NonQuantitativeAttributeAccuracy"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_QuantitativeAttributeAccuracy" TO="GM03_2Comprehensive.Comprehensive.DQ_QuantitativeAttributeAccuracy"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_RelativeInternalPositionalAccuracy" TO="GM03_2Comprehensive.Comprehensive.DQ_RelativeInternalPositionalAccuracy"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_TemporalConsistency" TO="GM03_2Comprehensive.Comprehensive.DQ_TemporalConsistency"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_TemporalValidity" TO="GM03_2Comprehensive.Comprehensive.DQ_TemporalValidity"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_ThematicClassificationCorrectness" TO="GM03_2Comprehensive.Comprehensive.DQ_ThematicClassificationCorrectness"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.DQ_TopologicalConsistency" TO="GM03_2Comprehensive.Comprehensive.DQ_TopologicalConsistency"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.containsOperationsSV_ServiceIdentification" TO="GM03_2Comprehensive.Comprehensive.containsOperationsSV_ServiceIdentification"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.extentSV_ServiceIdentification" TO="GM03_2Comprehensive.Comprehensive.extentSV_ServiceIdentification"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_GeoreferenceableparameterCitation" TO="GM03_2Comprehensive.Comprehensive.MD_GeoreferenceableparameterCitation"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_ImageDescriptionimageQualitiyCode" TO="GM03_2Comprehensive.Comprehensive.MD_ImageDescriptionimageQualitiyCode"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_ImageDescriptionprocessingLevelCode" TO="GM03_2Comprehensive.Comprehensive.MD_ImageDescriptionprocessingLevelCode"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformationarchiveConcept" TO="GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformationarchiveConcept"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformationcontact" TO="GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformationcontact"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformationhistoryConcept" TO="GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformationhistoryConcept"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformationupdateScopeDescription" TO="GM03_2Comprehensive.Comprehensive.MD_MaintenanceInformationupdateScopeDescription"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.MD_MetadatametadataMaintenance" TO="GM03_2Comprehensive.Comprehensive.MD_MetadatametadataMaintenance"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.resourceMaintenanceMD_Identification" TO="GM03_2Comprehensive.Comprehensive.resourceMaintenanceMD_Identification"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.SV_ServiceIdentificationaccessProperties" TO="GM03_2Comprehensive.Comprehensive.SV_ServiceIdentificationaccessProperties"/>
                <TAGENTRY FROM="GM03_2Comprehensive.Comprehensive.SV_ServiceIdentificationrestrictions" TO="GM03_2Comprehensive.Comprehensive.SV_ServiceIdentificationrestrictions"/>
              </ENTRIES>
          </ALIAS>
        </HEADERSECTION>
    </xsl:template>
</xsl:stylesheet>