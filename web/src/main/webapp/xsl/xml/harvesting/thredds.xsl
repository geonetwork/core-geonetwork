<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- ==================================================================== -->

	<xsl:import href="common.xsl"/>	

	<!-- ==================================================================== -->
	<!-- === Thredds catalog harvesting node -->
	<!-- ==================================================================== -->

	<xsl:template match="*" mode="site">
		<url><xsl:value-of select="url/value" /></url>
		<icon><xsl:value-of select="icon/value" /></icon>
	</xsl:template>

	<!-- ==================================================================== -->

	<xsl:template match="*" mode="options">
		<lang><xsl:value-of  select="lang/value" /></lang>
		<topic><xsl:value-of  select="topic/value" /></topic>
		<createThumbnails><xsl:value-of  select="createThumbnails/value" /></createThumbnails>
		<createServiceMd><xsl:value-of  select="createServiceMd/value" /></createServiceMd>
		<createCollectionDatasetMd><xsl:value-of  select="createCollectionDatasetMd/value" /></createCollectionDatasetMd>
		<createAtomicDatasetMd><xsl:value-of  select="createAtomicDatasetMd/value" /></createAtomicDatasetMd>
		<ignoreHarvestOnAtomics><xsl:value-of  select="ignoreHarvestOnAtomics/value" /></ignoreHarvestOnAtomics>
		<atomicGeneration><xsl:value-of  select="atomicGeneration/value" /></atomicGeneration>
		<modifiedOnly><xsl:value-of  select="modifiedOnly/value" /></modifiedOnly>
		<atomicFragmentStylesheet><xsl:value-of select="atomicFragmentStylesheet" /></atomicFragmentStylesheet>
		<atomicMetadataTemplate><xsl:value-of select="atomicMetadataTemplate" /></atomicMetadataTemplate>
		<createAtomicSubtemplates><xsl:value-of select="createAtomicSubtemplates" /></createAtomicSubtemplates>
		<outputSchemaOnAtomicsDIF><xsl:value-of  select="outputSchemaOnAtomicsDIF/value" /></outputSchemaOnAtomicsDIF>
		<outputSchemaOnAtomicsFragments><xsl:value-of  select="outputSchemaOnAtomicsFragments/value" /></outputSchemaOnAtomicsFragments>
		<ignoreHarvestOnCollections><xsl:value-of  select="ignoreHarvestOnCollections/value" /></ignoreHarvestOnCollections>
		<collectionGeneration><xsl:value-of  select="collectionGeneration/value" /></collectionGeneration>
		<collectionFragmentStylesheet><xsl:value-of select="collectionFragmentStylesheet" /></collectionFragmentStylesheet>
		<collectionMetadataTemplate><xsl:value-of select="collectionMetadataTemplate" /></collectionMetadataTemplate>
		<createCollectionSubtemplates><xsl:value-of select="createCollectionSubtemplates" /></createCollectionSubtemplates>
		<outputSchemaOnCollectionsDIF><xsl:value-of  select="outputSchemaOnCollectionsDIF/value" /></outputSchemaOnCollectionsDIF>
		<outputSchemaOnCollectionsFragments><xsl:value-of  select="outputSchemaOnCollectionsFragments/value" /></outputSchemaOnCollectionsFragments>
		<datasetCategory><xsl:value-of  select="datasetCategory/value" /></datasetCategory>
	</xsl:template>

	<!-- ==================================================================== -->

	<xsl:template match="*" mode="searches"/>

	<!-- ==================================================================== -->

</xsl:stylesheet>
