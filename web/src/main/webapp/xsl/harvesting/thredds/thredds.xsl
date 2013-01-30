<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === editPanel -->
	<!-- ============================================================================================= -->

	<xsl:template name="editPanel-thredds">
		<div id="thredds.editPanel">
			<xsl:call-template name="site-thredds"/>
			<div class="dots"/>
			<xsl:call-template name="options-thredds"/>
			<div class="dots"/>
			<xsl:call-template name="content-thredds"/>
			<div class="dots"/>
			<xsl:call-template name="privileges">
				<xsl:with-param name="type" select="'thredds'"/>
			</xsl:call-template>
			<div class="dots"/>
			<xsl:call-template name="categories-thredds"/>
		</div>
	</xsl:template>

	<!-- ============================================================================================= -->

	<xsl:template name="site-thredds">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/site"/></h1>
	
		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/name"/></td>
				<td class="padded"><input id="thredds.name" class="content" type="text" value="" size="30"/></td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/cataUrl"/></td>
				<td class="padded">
					<input id="thredds.cataUrl" class="content" type="text" value="http://" size="30"/>
				</td>
			</tr>
			
			<xsl:call-template name="useAccount">
				<xsl:with-param name="type" select="'thredds'"/>
			</xsl:call-template>
			
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/threddslang"/></td>
				<td class="padded">
					<select id="thredds.lang">
					<!--  TODO loop on languages -->
						<option value="eng">eng</option>
						<option value="fra">fra</option>
					</select>
				</td>
			</tr>
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/strings/theme"/></td>
				<td class="padded">
					<select id="thredds.topic">
					<!--  TODO loop on topic -->
						<option value=""></option>
						<option value="farming">farming</option>
						<option value="biota">biota</option>
						<option value="boundaries">boundaries</option>
						<option value="climatologyMeteorologyAtmosphere">climatologyMeteorologyAtmosphere</option>
						<option value="economy">economy</option>
						<option value="elevation">elevation</option>
						<option value="environment">environment</option>
						<option value="geoscientificInformation">geoscientificInformation</option>
						<option value="health">health</option>
						<option value="imageryBaseMapsEarthCover">imageryBaseMapsEarthCover</option>
						<option value="intelligenceMilitary">intelligenceMilitary</option>
						<option value="location">location</option>
						<option value="inlandWaters">inlandWaters</option>
						<option value="oceans">oceans</option>
						<option value="planningCadastre">planningCadastre</option>
						<option value="society">society</option>
						<option value="structure">structure</option>
						<option value="transportation">transportation</option>
						<option value="utilitiesCommunication">utilitiesCommunication</option>						
					</select>
				</td>
			</tr>

			<tr>
				<td class="padded" colspan="2">
					<input type="checkbox" id="thredds.createServiceMd" name="thredds.createServiceMd" value=""/>
					<label for="thredds.createServiceMd"><xsl:value-of select="/root/gui/harvesting/threddsServiceMd"/></label>
					<br/>
				</td>
			</tr>
			
			<tr>
				<td class="padded" colspan="2" style="width:550px">
					<input type="checkbox" id="thredds.collectionDatasetMd" name="thredds.collectionDatasetMd" value=""/>
					<label for="thredds.collectionDatasetMd"><xsl:value-of select="/root/gui/harvesting/threddsCollectionDataset"/></label><br/>
					
					<div id="collectionDatasetsHarvested" style="margin-left:40px;display:none;border-color:#f00;border-style:solid;border-width:1px">
						<xsl:call-template name="thredds-collection-options"/>
					</div>
					
					<input type="checkbox" id="thredds.atomicDatasetMd" name="thredds.atomicDatasetMd" value=""/><label for="thredds.atomicDatasetMd">
					<xsl:value-of select="/root/gui/harvesting/threddsAtomicDataset"/></label><br/>
					
					<div id="atomicDatasetsHarvested" style="margin-left:40px;display:none;border-color:#f00;border-style:solid;border-width:1px">
						<xsl:call-template name="thredds-atomic-dataset-options"/>
					</div>
					
					<input type="checkbox" id="thredds.createThumbnails" name="thredds.createThumbnails" disable="true" value=""/><label for="thredds.createThumbnails"><xsl:value-of select="/root/gui/harvesting/threddsCreateThumbnails"/></label>
				</td>
			</tr>

			<tr>
				<td class="padded" valign="bottom"><xsl:value-of select="/root/gui/harvesting/icon"/></td>
				<td class="padded">
					<select id="thredds.icon" class="content" name="icon" size="1"/>
					&#xA0;
					<img id="thredds.icon.image" src="" alt="" />
				</td>
			</tr>
			

		</table>
	</xsl:template>
		
	<!-- ============================================================================================= -->
	
	<xsl:template name="thredds-collection-options">
		<input type="checkbox" id="thredds.ignoreHarvestOnCollections" name="thredds.ignoreHarvestOnCollections" value=""/>
		<label for="thredds.ignoreHarvestOnCollections"><xsl:value-of select="/root/gui/harvesting/threddsIgnoreHarvest"/></label><br/>
		<input type="radio" name="thredds.collectionGenerationOption" id="thredds.createDIFCollectionMd" value="default" checked="true"/>
		<label for="thredds.createDIFCollectionMd"><xsl:value-of select="/root/gui/harvesting/threddsCreateDIFMd" /></label><br/>
		
		<div id="collectionDIFMetadataOptions" style="margin-left:30px;">
			<xsl:value-of select="/root/gui/harvesting/threddsOutputSchema"/>
			&#160;
			<select id="thredds.outputSchemaOnCollectionsDIF"/>
			<br/>
		</div>
		
		<input type="radio" name="thredds.collectionGenerationOption" id="thredds.createFragmentsForCollections" value="fragments"/>
		<label for="thredds.createFragmentsForCollections"><xsl:value-of select="/root/gui/harvesting/threddsCreateFragments" /></label><br/>
		
		<div id="collectionFragmentOptions" style="margin-left:30px;display:none;">
			<xsl:value-of select="/root/gui/harvesting/threddsOutputSchema"/>
			&#160;
			<select id="thredds.outputSchemaOnCollectionsFragments"/>
			<br/>

			<div id="collectionFragmentSchemaOptions" style="margin-left:30px;display:none;">
				<xsl:value-of select="/root/gui/harvesting/threddsFragmentStylesheet"/>
				&#160;
				<select id="thredds.collectionFragmentStylesheet" size="1"/><br/>
				<input type="checkbox" name="thredds.createCollectionSubtemplates" id="thredds.createCollectionSubtemplates" value=""/>
				<label for="thredds.createCollectionSubtemplates"><xsl:value-of select="/root/gui/harvesting/threddsCreateSubtemplates"/></label><br/>
				<xsl:value-of select="/root/gui/harvesting/threddsTemplate"/>
				&#160;
				<select id="thredds.collectionMetadataTemplate" size="1"/>
			</div>
		</div>
	</xsl:template>
						
	<!-- ============================================================================================= -->
	
	<xsl:template name="thredds-atomic-dataset-options">
		<input type="checkbox" id="thredds.ignoreHarvestOnAtomics" name="thredds.ignoreHarvestOnAtomics" value=""/>
		<label for="thredds.ignoreHarvestOnAtomics"><xsl:value-of select="/root/gui/harvesting/threddsIgnoreHarvest"/></label><br/>
		<input type="radio" name="thredds.atomicGenerationOption" id="thredds.createDIFAtomicMd" value="default" checked="true"/>
		<label for="thredds.createDIFAtomicMd"><xsl:value-of select="/root/gui/harvesting/threddsCreateDIFMd" /></label><br/>
		
		<div id="atomicDIFMetadataOptions" style="margin-left:30px;">
			<xsl:value-of select="/root/gui/harvesting/threddsOutputSchema"/>
			&#160;
			<select id="thredds.outputSchemaOnAtomicsDIF"/>
		</div>
		
		<input type="radio" name="thredds.atomicGenerationOption" id="thredds.createFragmentsForAtomics" value="fragments"/>
		<label for="thredds.createFragmentsForAtomics"><xsl:value-of select="/root/gui/harvesting/threddsCreateFragments" /></label><br/>
		
		<div id="atomicFragmentOptions" style="margin-left:30px;display:none;">
			<xsl:value-of select="/root/gui/harvesting/threddsOutputSchema"/>
			&#160;
			<select id="thredds.outputSchemaOnAtomicsFragments"/>
			<br/>

			<div id="atomicFragmentSchemaOptions" style="margin-left:30px;display:none;">
				<xsl:value-of select="/root/gui/harvesting/threddsFragmentStylesheet"/>
				&#160;
				<select id="thredds.atomicFragmentStylesheet" size="1"/><br/>
				<input type="checkbox" name="thredds.createAtomicSubtemplates" id="thredds.createAtomicSubtemplates" value=""/>
				<label for="thredds.createAtomicSubtemplates"><xsl:value-of select="/root/gui/harvesting/threddsCreateSubtemplates"/></label><br/>
				<xsl:value-of select="/root/gui/harvesting/threddsTemplate"/>
				&#160;
				<select id="thredds.atomicMetadataTemplate" size="1"/>
			</div>
		</div>

		<input type="checkbox" id="thredds.modifiedOnly" name="thredds.modifiedOnly" value=""/>
		<label for="thredds.modifiedOnly"><xsl:value-of select="/root/gui/harvesting/threddsModifiedOnly"/></label><br/>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
	<xsl:template name="options-thredds">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>
		<xsl:call-template name="schedule-widget">
			<xsl:with-param name="type">thredds</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="content-thredds">
	<div style="display:none;"> <!-- UNUSED -->
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/content"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/importxslt"/></td>
				<td class="padded">
					&#160;
					<select id="thredds.importxslt" class="content" name="importxslt" size="1"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/validate"/></td>
				<td class="padded"><input id="thredds.validate" type="checkbox" value=""/></td>
			</tr>
		</table>
	</div>
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="categories-thredds">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/serviceCategory"/></h1>
		
		<select id="thredds.categories" class="content"/>

		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/datasetCategory"/></h1>
		
		<select id="thredds.datasetCategory" class="content"/>

	</xsl:template>
</xsl:stylesheet>
