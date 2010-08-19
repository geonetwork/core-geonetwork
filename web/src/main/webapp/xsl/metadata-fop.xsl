<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:exslt="http://exslt.org/common"
	exclude-result-prefixes="exslt"
	xmlns:gco="http://www.isotc211.org/2005/gco"
	xmlns:geonet="http://www.fao.org/geonetwork"
	xmlns:fo="http://www.w3.org/1999/XSL/Format">


	<!--
		gui to show a simple element
	-->
	<xsl:template name="simpleElementFop">
		<xsl:param name="title" />
		<xsl:param name="text" />
		<xsl:param name="helpLink" />
		<xsl:param name="addLink" />
		<xsl:param name="removeLink" />
		<xsl:param name="upLink" />
		<xsl:param name="downLink" />
		<xsl:param name="schematronLink" />
		<xsl:param name="schema" />
		<xsl:param name="edit" select="false()" />
		<!-- used as do*ElementAction url anchor to go back to the same position after editing operations -->
		<xsl:param name="anchor">
			<xsl:choose>

				<!-- current node is an element -->
				<xsl:when test="geonet:element/@ref">
					_
					<xsl:value-of select="geonet:element/@ref" />
				</xsl:when>

				<!-- current node is an attribute or a new child: create anchor to parent -->
				<xsl:when test="../geonet:element/@ref">
					_
					<xsl:value-of select="../geonet:element/@ref" />
				</xsl:when>

			</xsl:choose>
		</xsl:param>


		<fo:table-row>
			<fo:table-cell>
				<fo:block>
					<fo:inline font-weight="bold">
						<xsl:value-of select="$title" />
					</fo:inline>
				</fo:block>
			</fo:table-cell>
			<fo:table-cell number-columns-spanned="2">
				<fo:block>
					<xsl:value-of select="$text" />
				</fo:block>
			</fo:table-cell>
		</fo:table-row>
	</xsl:template>


	<!--
		gui to show a complex element
	-->
	<xsl:template name="complexElementFop">
		<xsl:param name="title" />
		<xsl:param name="text" />
		<xsl:param name="content" />
		<xsl:param name="helpLink" />
		<xsl:param name="addLink" />
		<xsl:param name="removeLink" />
		<xsl:param name="upLink" />
		<xsl:param name="downLink" />
		<xsl:param name="schematronLink" />
		<xsl:param name="schema" />
		<xsl:param name="edit" select="false()" />

		<!-- used as do*ElementAction url anchor to go back to the same position after editing operations -->
		<xsl:param name="anchor">
			<xsl:choose>

				<!-- current node is an element -->
				<xsl:when test="geonet:element/@ref">
					_
					<xsl:value-of select="geonet:element/@ref" />
				</xsl:when>

				<!-- current node is a new child: create anchor to parent -->
				<xsl:when test="../geonet:element/@ref">
					_
					<xsl:value-of select="../geonet:element/@ref" />
				</xsl:when>

			</xsl:choose>
		</xsl:param>

		<fo:table-row>
			<fo:table-cell>
				<fo:block>
					<fo:table width="100%" table-layout="fixed">
						<fo:table-column column-width="3cm" />
						<fo:table-column column-width="12cm" />
						<fo:table-column column-width="1cm" />
						<fo:table-body>
							<fo:table-row>
								<fo:table-cell
									number-columns-spanned="3">
									<fo:block
										border-top="2pt solid black">
										<fo:inline font-weight="bold">
											<xsl:text>::</xsl:text>
											<xsl:value-of
												select="$title" />
										</fo:inline>
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
							<fo:table-row>
								<fo:table-cell>
									<fo:block></fo:block>
								</fo:table-cell>
								<fo:table-cell>
									<fo:block>
										<xsl:variable name="n"
											select="exslt:node-set($content)" />
										<xsl:if test="$n/node()">
											<fo:table
												table-layout="fixed" width="100%"
												border-collapse="separate">
												<fo:table-body>
													<xsl:copy-of
														select="$content" />
												</fo:table-body>
											</fo:table>
										</xsl:if>
									</fo:block>
								</fo:table-cell>
							</fo:table-row>
						</fo:table-body>
					</fo:table>
				</fo:block>
			</fo:table-cell>
		</fo:table-row>

	</xsl:template>



	<!--
		metadata result to fop
	-->
	<xsl:template name="fo">
		<xsl:param name="res" />
		<xsl:param name="server" />
		<xsl:param name="gui" />
		<xsl:param name="remote" />

		<xsl:for-each select="$res/*">
		    <xsl:variable name="md">
                <xsl:apply-templates mode="brief" select="."/>
            </xsl:variable>
            <xsl:variable name="metadata" select="exslt:node-set($md)/*[1]"/>

            <xsl:if test="$metadata/geonet:info/id != ''">
			<fo:table-row>
				<fo:table-cell>

				    <xsl:variable name="source" select="string($metadata/geonet:info/source)"/>

                    <fo:block font-weight="bold" font-size="14pt"
						border-top="2pt solid black" padding-top="4pt" margin-top="4pt">
						<fo:external-graphic content-width="35pt">
	                                        <xsl:attribute name="src">
	                            url('<xsl:value-of
	                                                select="concat('http://', $server/host,':', $server/port, $gui/url, '/images/logos/', $source , '.gif')" />')"
	                                </xsl:attribute>
	                    </fo:external-graphic>
                    	<xsl:value-of select="concat(position()-1,' - ',$metadata/title)" />
					</fo:block>
					<fo:block text-align="left" font-style="italic" margin-top="4pt">
						<xsl:value-of select="$gui/strings/uuid" />
						:
            <xsl:value-of select="$metadata/geonet:info/uuid" />
					</fo:block>
					<fo:block text-align="left" margin-top="4pt">
						<xsl:value-of select="$gui/strings/abstract" />
						:
						<xsl:value-of select="$metadata/abstract" />
					</fo:block>
					<!-- keywords -->
					<xsl:if test="$metadata/keyword">
						<fo:block text-align="left"
							margin-top="4pt"
							font-style="italic">
							<xsl:value-of select="$gui/strings/keywords" />
							:
							<xsl:for-each select="$metadata/keyword">
								<xsl:if test="position() &gt; 1">, </xsl:if><xsl:value-of select="." />
							</xsl:for-each>
						</fo:block>
					</xsl:if>
					<xsl:if test="$remote=false()">
						<fo:block text-align="left" margin-top="4pt">
							<xsl:value-of select="$gui/strings/schema" />
							:
            	<xsl:value-of select="$metadata/geonet:info/schema" />
						</fo:block>
					</xsl:if>

					<!-- display metadata url but only if its not a remote result -->
					<fo:block text-align="left" margin-top="4pt">
						<xsl:choose>
							<xsl:when test="$remote=false()">
								|<fo:basic-link text-decoration="underline" color="blue">
									<xsl:attribute name="external-destination">
                                  			url('<xsl:value-of
											select="concat('http://', $server/host,':', $server/port, $gui/locService,'/metadata.show?id=', $metadata/geonet:info/id, '&amp;currTab=simple')" />')
                              			</xsl:attribute>
									<xsl:value-of select="$gui/strings/show" />
								</fo:basic-link>|
							</xsl:when>
							<xsl:otherwise>
								<fo:block text-align="left" font-style="italic">
									<xsl:text>Z3950: </xsl:text><xsl:value-of select="$metadata/geonet:info/server" /><xsl:text> </xsl:text>
								</fo:block>
							</xsl:otherwise>
						</xsl:choose>

						<xsl:if test="$metadata/geonet:info/download='true'">
							<xsl:for-each
								select="$metadata/link[@type='download']">
								<fo:basic-link
									text-decoration="underline" color="blue">
									<xsl:attribute
										name="external-destination">
                                   url('<xsl:value-of
											select="." />')
                               </xsl:attribute>
									<xsl:value-of
										select="$gui/strings/download" />
								</fo:basic-link>|
							</xsl:for-each>
						</xsl:if>

						<xsl:if test="$metadata/geonet:info/dynamic='true'">
                            <xsl:for-each
                                select="$metadata/link[@type='application/vnd.ogc.wms_xml']">
                                <fo:basic-link
                                    text-decoration="underline" color="blue">
                                    <xsl:attribute
                                        name="external-destination">
                                   url('<xsl:value-of
                                            select="@href" />')
                               </xsl:attribute>
                                    <xsl:value-of
                                        select="$gui/strings/interactiveMap" />
                                </fo:basic-link>|
                            </xsl:for-each>
                        </xsl:if>


					</fo:block>
				</fo:table-cell>
				<fo:table-cell>
					<fo:block>
						<xsl:if test="$metadata/image">
						    <xsl:choose>
								<xsl:when
									test="contains($metadata/image ,'://')">
									<fo:external-graphic>
										<xsl:attribute name="src"><xsl:text>url('</xsl:text><xsl:value-of
												select="$metadata/image" /><xsl:text>')"</xsl:text></xsl:attribute>
									</fo:external-graphic>
								</xsl:when>
								<xsl:otherwise>
									<fo:external-graphic>
										<xsl:attribute name="src"><xsl:text>url('</xsl:text><xsl:value-of
												select="concat('http://', $server/host,':', $server/port, $metadata/image)" /><xsl:text>')"</xsl:text></xsl:attribute>
									</fo:external-graphic>
								</xsl:otherwise>
							</xsl:choose>

						</xsl:if>
					</fo:block>
				</fo:table-cell>
			</fo:table-row>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>


	<!--
		main pdf banner
	-->
	<xsl:template name="banner">
		<fo:table table-layout="fixed" width="100%">
			<fo:table-column
				column-width="proportional-column-width(1)" />
			<fo:table-column
				column-width="proportional-column-width(1)" />
			<fo:table-body>
				<fo:table-row>
					<fo:table-cell display-align="left" background-color="#064377">
						<fo:block text-align="left">
							<!-- FIXME -->
							<fo:external-graphic>
								<xsl:attribute name="src">
                                url('<xsl:value-of
										select="concat('http://', //server/host,':', //server/port, /root/gui/url,'/images/header-left.jpg')" />')"
                                    </xsl:attribute>
							</fo:external-graphic>
					   </fo:block>
					</fo:table-cell>
					<fo:table-cell display-align="right" background-color="#064377">
					   <fo:block text-align="right">
                    		<fo:external-graphic>
								<xsl:attribute name="src">
                                url('<xsl:value-of
										select="concat('http://', //server/host,':', //server/port, /root/gui/url,'/images/header-right.gif')" />')"
                                    </xsl:attribute>

							</fo:external-graphic>
						</fo:block>
					</fo:table-cell>
				</fo:table-row>
			</fo:table-body>
		</fo:table>
	</xsl:template>






</xsl:stylesheet>
