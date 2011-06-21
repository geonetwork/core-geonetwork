<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
	
	<xsl:import href="modal.xsl"/>

	<!--
	page content
	-->
	<xsl:template name="content">
		<xsl:call-template name="formLayout">
			<xsl:with-param name="title" select="/root/gui/strings/children"/>
			<xsl:with-param name="content">

				<div id="children" class="modalbox content" style="float:left;width:50%">
					<input type="hidden" id="parentUuid" name="parentUuid" value="{/root/request/parentUuid}"/>
					<input type="hidden" id="id" name="id" value="{/root/response/id}"/>
					<input type="hidden" id="schema" name="schema" value="{/root/request/schema}"/>
					<input type="hidden" id="childrenIds" name="childrenIds" value="{/root/request/childrenIds}"/>
						
					<span>
						<input type="radio" name="updateMode" id="replace" value="replace" checked="checked"/>
						<label for="replace"><xsl:value-of select="/root/gui/strings/replaceMode"/></label>
						&#160;
						<input type="radio" name="updateMode" value="add" id="add"/>
						<label for="add"><xsl:value-of select="/root/gui/strings/addMode"/></label>
					</span>
					<br/>
					<br/>
					
					<ul>
						<li class="arrow">
							<input type="checkbox" name="gmd-contact" id="contact" />
							<label for="contact">
								<xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name='gmd:contact']/label"/>
							</label>
						</li>
						<li class="arrow">
							<input type="checkbox" name="gmd:identificationInfo" id="identificationInfo" disabled="disabled"/>
							<label for="identificationInfo"> <!-- class="identificationInfo" -->
							  <xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name='gmd:identificationInfo']/label"/>
							</label>
							<ul>
								<li class="arrow">
									<input type="checkbox" name="gmd-pointOfContact" id="pointOfContact" />
									<label for="pointOfContact">
									  <xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name='gmd:pointOfContact']/label"/>
									</label>
								</li>
								<li class="arrow">
									<input type="checkbox" name="gmd-descriptiveKeywords" id="descriptiveKeywords" />
									<label for="descriptiveKeywords">
									  <xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name='gmd:descriptiveKeywords']/label"/>
									</label>
								</li>
								<li class="arrow">
									<input type="checkbox" name="gmd-extent" id="extent" />
									<label for="extent">
									  <xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name='gmd:extent']/label"/>
									</label>
								</li>
							</ul>
						</li>
						<li class="arrow">
							<input type="checkbox" name="gmd-metadataMaintenance" id="metadataMaintenance" />
							<label for="metadataMaintenance">
							  <xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name='gmd:metadataMaintenance']/label"/>
							</label>
						</li>
						<li class="arrow">
							<input type="checkbox" name="gmd-metadataConstraints" id="metadataConstraints" />
							<label for="metadataConstraints">
							  <xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name='gmd:metadataConstraints']/label"/>
							</label>
						</li>
						<li class="arrow">
							<input type="checkbox" name="gmd-spatialRepresentationInfo" id="spatialRepresentationInfo" />
							<label for="spatialRepresentationInfo">
							  <xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name='gmd:spatialRepresentationInfo']/label"/>
							</label>
						</li>
						<li class="arrow">
							<input type="checkbox" name="gmd-referenceSystemInfo" id="referenceSystemInfo" />
							<label for="referenceSystemInfo">
							  <xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name='gmd:referenceSystemInfo']/label"/>
							</label>
						</li>
						<li class="arrow">
							<input type="checkbox" name="gmd-distributionInfo" id="distributionInfo" />
							<label for="distributionInfo">
							  <xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name='gmd:distributionInfo']/label"/>
							</label>
						</li>
						<li class="arrow">
							<input type="checkbox" name="gmd-dataQualityInfo" id="dataQualityInfo" />
							<label for="dataQualityInfo">
							  <xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name='gmd:dataQualityInfo']/label"/>
							</label>
						</li>
						<li class="arrow">
							<input type="checkbox" name="gmd-applicationSchemaInfo" id="applicationSchemaInfo" />
							<label for="applicationSchemaInfo">
							  <xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name='gmd:applicationSchemaInfo']/label"/>
							</label>
						</li>
						<li class="arrow">
							<input type="checkbox" name="gmd-portrayalCatalogueInfo" id="portrayalCatalogueInfo" />
							<label for="portrayalCatalogueInfo">
							  <xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name='gmd:portrayalCatalogueInfo']/label"/>
							</label>
						</li>
						<li class="arrow">
							<input type="checkbox" name="gmd-contentInfo" id="contentInfo" />
							<label for="contentInfo">
							  <xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name='gmd:contentInfo']/label"/>
							</label>
						</li>
						<li class="arrow">
							<input type="checkbox" name="gmd-metadataExtensionInfo" id="metadataExtensionInfo" />
							<label for="metadataExtensionInfo">
							  <xsl:value-of select="/root/gui/schemas/iso19139/labels/element[@name='gmd:metadataExtensionInfo']/label"/>
                            </label>
                        </li>
					</ul>
					<br/>
					<div align="center">
						<button class="content" onclick="updateChildren('children','metadata.batch.update.children','{/root/gui/strings/updateChildrenFailed}')">
							<xsl:value-of select="/root/gui/strings/updateChildren"/>
						</button>
					</div>
				</div>
				<div style="float:right;width:40%">
					<xsl:copy-of select="/root/gui/strings/updateChildrenHelp"/>
				</div>
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
</xsl:stylesheet>
