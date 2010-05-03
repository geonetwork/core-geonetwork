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
			<xsl:call-template name="privileges-thredds"/>
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
			<!-- TODO : Do we have to set up an account for thredds secured ? -->
			<tr style="display:none;">
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/useAccount"/></td>
				<td class="padded"><input id="thredds.useAccount" type="checkbox"/></td>
			</tr>

			<tr style="display:none;">
				<td/>
				<td>
					<table id="thredds.account">
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/username"/></td>
							<td class="padded"><input id="thredds.username" class="content" type="text" value="" size="20"/></td>
						</tr>
		
						<tr>
							<td class="padded"><xsl:value-of select="/root/gui/harvesting/password"/></td>
							<td class="padded"><input id="thredds.password" class="content" type="password" value="" size="20"/></td>
						</tr>
					</table>
				</td>
			</tr>			
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
					<label for="thredds.createServiceMd"><xsl:value-of select="/root/gui/harvesting/threddsServiceMdOnly19119"/></label>
					<br/>
				</td>
			</tr>
			
			<tr>
				<td class="padded" colspan="2" style="width:550px">
					<input type="checkbox" id="thredds.collectionDatasetMd" name="thredds.collectionDatasetMd" value=""/>
					<label for="thredds.collectionDatasetMd"><xsl:value-of select="/root/gui/harvesting/threddsCollectionDataset19115"/></label><br/>
					
					<div id="collectionDatasetsHarvested" style="margin-left:40px;display:none;border-color:#f00;border-style:solid;border-width:1px">
						<xsl:call-template name="thredds-collection-options"/>
					</div>
					
					<input type="checkbox" id="thredds.atomicDatasetMd" name="thredds.atomicDatasetMd" value=""/><label for="thredds.atomicDatasetMd">
					<xsl:value-of select="/root/gui/harvesting/threddsAtomicDataset19115"/></label><br/>
					
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
		<label for="thredds.ignoreHarvestOnCollections"><xsl:value-of select="/root/gui/harvesting/threddsIgnoreHarvestOnCollections"/></label><br/>
		<input type="radio" name="thredds.collectionGenerationOption" id="thredds.createDefaultCollectionMd" value="default" checked="true"/>
		<label for="thredds.createDefaultCollectionMd"><xsl:value-of select="/root/gui/harvesting/threddsCreateDefaultMd" /></label><br/>
		
		<div id="collectionDefaultMetadataOptions" style="margin-left:30px;">
			<xsl:value-of select="/root/gui/harvesting/threddsOSCollections"/>
			&#160;
			<select id="thredds.outputSchemaOnCollections">
				<option value="iso19139">iso19115/19139</option>
				<option value="iso19139.mcp">Marine Community Profile of ISO19115/19139</option>
			</select><br/>
		</div>
		
		<input type="radio" name="thredds.collectionGenerationOption" id="thredds.createFragmentsForCollections" value="fragments"/>
		<label for="thredds.createFragmentsForCollections"><xsl:value-of select="/root/gui/harvesting/threddsCreateFragments" /></label><br/>
		
		<div id="collectionFragmentOptions" style="margin-left:30px;display:none;">
			<xsl:value-of select="/root/gui/harvesting/threddsFragmentStylesheetForCollections"/>
			&#160;
			<select id="thredds.collectionFragmentStylesheet" size="1"/><br/>
			<input type="checkbox" name="thredds.createCollectionSubtemplates" id="thredds.createCollectionSubtemplates" value=""/>
			<label for="thredds.createCollectionSubtemplates"><xsl:value-of select="/root/gui/harvesting/threddsCreateSubtemplates"/></label><br/>
			<xsl:value-of select="/root/gui/harvesting/template"/>
			&#160;
			<select id="thredds.collectionMetadataTemplate" size="1"/>
		</div>
	</xsl:template>
						
	<!-- ============================================================================================= -->
	
	<xsl:template name="thredds-atomic-dataset-options">
		<input type="checkbox" id="thredds.ignoreHarvestOnAtomics" name="thredds.ignoreHarvestOnAtomics" value=""/>
		<label for="thredds.ignoreHarvestOnAtomics"><xsl:value-of select="/root/gui/harvesting/threddsIgnoreHarvestOnAtomics"/></label><br/>
		<input type="radio" name="thredds.atomicGenerationOption" id="thredds.createDefaultAtomicMd" value="default" checked="true"/>
		<label for="thredds.createDefaultAtomicMd"><xsl:value-of select="/root/gui/harvesting/threddsCreateDefaultMd" /></label><br/>
		
		<div id="atomicDefaultMetadataOptions" style="margin-left:30px;">
			<xsl:value-of select="/root/gui/harvesting/threddsOSAtomics"/>
			&#160;
			<select id="thredds.outputSchemaOnAtomics">
				<option value="iso19139">iso19115/19139</option>
				<option value="iso19139.mcp">Marine Community Profile of ISO19115/19139</option>
			</select><br/>
		</div>
		
		<input type="radio" name="thredds.atomicGenerationOption" id="thredds.createFragmentsForAtomics" value="fragments"/>
		<label for="thredds.createFragmentsForAtomics"><xsl:value-of select="/root/gui/harvesting/threddsCreateFragments" /></label><br/>
		
		<div id="atomicFragmentOptions" style="margin-left:30px;display:none;">
			<xsl:value-of select="/root/gui/harvesting/threddsFragmentStylesheetForAtomics"/>
			&#160;
			<select id="thredds.atomicFragmentStylesheet" size="1"/><br/>
			<input type="checkbox" name="thredds.createAtomicSubtemplates" id="thredds.createAtomicSubtemplates" value=""/>
			<label for="thredds.createAtomicSubtemplates"><xsl:value-of select="/root/gui/harvesting/threddsCreateSubtemplates"/></label><br/>
			<xsl:value-of select="/root/gui/harvesting/template"/>
			&#160;
			<select id="thredds.atomicMetadataTemplate" size="1"/>
		</div>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
	<xsl:template name="options-thredds">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/options"/></h1>

		<table border="0">
			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/every"/></td>
				<td class="padded">
					<input id="thredds.every.days"  class="content" type="text" size="2"/> :
					<input id="thredds.every.hours" class="content" type="text" size="2"/> :
					<input id="thredds.every.mins"  class="content" type="text" size="2"/>
					&#160;
					<xsl:value-of select="/root/gui/harvesting/everySpec"/>
				</td>
			</tr>

			<tr>
				<td class="padded"><xsl:value-of select="/root/gui/harvesting/oneRun"/></td>
				<td class="padded"><input id="thredds.oneRunOnly" type="checkbox" value=""/></td>
			</tr>			
		</table>
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

	<xsl:template name="privileges-thredds">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/privileges"/></h1>
		
		<table>
			<tr>
				<td class="padded" valign="top"><xsl:value-of select="/root/gui/harvesting/groups"/></td>
				<td class="padded"><select id="thredds.groups" class="content" size="8" multiple="on"/></td>					
				<td class="padded" valign="top">
					<div align="center">
						<button id="thredds.addGroups" class="content" onclick="harvesting.thredds.addGroupRow()">
							<xsl:value-of select="/root/gui/harvesting/add"/>
						</button>
					</div>
				</td>					
			</tr>
		</table>
		
		<table id="thredds.privileges">
			<tr>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/group"/></b></th>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/oper/op[@id='0']"/></b></th>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/oper/op[@id='5']"/></b></th>
				<th class="padded"><b><xsl:value-of select="/root/gui/harvesting/oper/op[@id='6']"/></b></th>
				<th/>
			</tr>
		</table>
		
	</xsl:template>
	
	<!-- ============================================================================================= -->

	<xsl:template name="categories-thredds">
		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/serviceCategory"/></h1>
		
		<select id="thredds.categories" class="content"/>

		<h1 align="left"><xsl:value-of select="/root/gui/harvesting/datasetCategory"/></h1>
		
		<select id="thredds.datasetCategory" class="content"/>

	</xsl:template>
	
	<!-- ============================================================================================= -->
		
</xsl:stylesheet>
