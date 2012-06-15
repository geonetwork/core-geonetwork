<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:xalan="http://xml.apache.org/xalan"
    xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
	exclude-result-prefixes="xsl xalan geonet csw">

	<xsl:include href="csw-fop.xsl" />
	<xsl:include href="metadata.xsl" />
	<xsl:include href="utils.xsl" />

	<xsl:template match="/root">
		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
			<fo:layout-master-set>
				<fo:simple-page-master master-name="simpleA4"
					page-height="29.7cm" page-width="21cm" margin-top="1cm"
					margin-bottom="1cm" margin-left="1cm" margin-right="1cm">
					<fo:region-body margin-top="1cm" />
					<fo:region-before extent="1cm" />
				</fo:simple-page-master>


				<fo:page-sequence-master master-name="PSM_Name">
					<fo:single-page-master-reference
						master-reference="simpleA4" />
				</fo:page-sequence-master>
			</fo:layout-master-set>

			<fo:page-sequence master-reference="simpleA4"
				initial-page-number="1">

				<fo:static-content flow-name="xsl-region-before">
					<fo:block text-align="end">
						<xsl:value-of select="format-date(current-date(),'[D01]-[M01]-[Y0001]')"/> -
						<fo:page-number />
						/
						<fo:page-number-citation ref-id="terminator" />
					</fo:block>
				</fo:static-content>

				<fo:flow flow-name="xsl-region-body">

					<!-- Banner level -->
					<xsl:call-template name="banner" />


					<fo:block font-size="10pt">

						<fo:table width="100%" table-layout="fixed">
							<fo:table-column column-width="18cm" />
							<!--<fo:table-column column-width="13cm" />-->
							<!--<fo:table-column column-width="5cm" />-->
							<fo:table-body>
								<fo:table-row height="10mm">
									<fo:table-cell
										display-align="center">
										<fo:block text-align="right">
											<xsl:value-of
												select="/root/gui/strings/resultsMatching" />
											<xsl:value-of
												select="/root/csw:GetRecordsResponse/csw:SearchResults/@numberOfRecordsMatched" />
										</fo:block>
									</fo:table-cell>
								</fo:table-row>

								<xsl:call-template name="fo">
									<xsl:with-param name="res"
										select="/root/csw:GetRecordsResponse/csw:SearchResults" />
									<xsl:with-param name="gui"
										select="/root/gui" />
									<xsl:with-param name="server"
										select="/root/gui/env/server" />
								</xsl:call-template>
							</fo:table-body>
						</fo:table>
					</fo:block>

					<fo:block id="terminator" />
				</fo:flow>
			</fo:page-sequence>
		</fo:root>
	</xsl:template>

	<!-- ============================================================== -->

	<xsl:template match="geonet:info/title" mode="strip" />

	<xsl:template match="@*|node()" mode="strip">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" mode="strip" />
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
