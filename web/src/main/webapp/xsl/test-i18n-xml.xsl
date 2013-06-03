<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:exslt="http://exslt.org/common">

	<xsl:output method="xml"/>

	<xsl:variable name="langs">
		<langs>
			<!-- TODO : add a master lang param to be able to compare to other lang than eng <eng master="true"/> -->
			<fre/>
			<spa/>
			<chi/>
			<ara/>
			<ger/>
			<ita/>
			<rus/>
			<dut/>
			<por/>
			<cat/>
			<fin/>
			<nor/>
			<tur/>
			<pol/>
		</langs>
	</xsl:variable>

	<xsl:template match="/">
	                   
		<root>
		    <info>This service produce an XML document containing all missing localized terms in each loc files. 
		    You could use this service in order to have more up to date localized files. 
		    Use copy/paste to update the files and translate the term from english to the other language.</info>
		    <warning>WARNING: This will not produce sub-child elements. It will only produce correct 
		    localized file for the first level of element with no attribute or with an id, value or type attribute.</warning>
		    <strings file="strings.xml">
	           <xsl:variable name="master" select="//strings.eng"/>
	
				<xsl:for-each select="exslt:node-set($langs)/langs/*">
						    
				    <xsl:variable name="l" select="name(.)"/>
					<xsl:element name="{$l}">
                        <xsl:for-each select="$master/*">
						   <xsl:call-template name="checki18n-lang">
								<xsl:with-param name="elem" select="." />
								<xsl:with-param name="lang" select="$l" />
								<xsl:with-param name="file">strings</xsl:with-param>
							</xsl:call-template>
						</xsl:for-each>
					</xsl:element>
				</xsl:for-each>
			</strings> 
			<about file="about.xml">
	           <xsl:variable name="master" select="//about.eng"/>
    
                <xsl:for-each select="exslt:node-set($langs)/langs/*">
                            
                    <xsl:variable name="l" select="name(.)"/>
                    
                    <xsl:element name="{$l}">
                        <xsl:for-each select="$master/*">
                           <xsl:call-template name="checki18n-lang">
                                <xsl:with-param name="elem" select="." />
                                <xsl:with-param name="lang" select="$l" />
                                <xsl:with-param name="file">about</xsl:with-param>
                            </xsl:call-template>
                        </xsl:for-each>
                    </xsl:element>
                </xsl:for-each>
    		</about>
			<config file="config.xml">
                <xsl:variable name="master" select="//config.eng"/>
    
                <xsl:for-each select="exslt:node-set($langs)/langs/*">
                            
                    <xsl:variable name="l" select="name(.)"/>
                    
                    <xsl:element name="{$l}">
                        <xsl:for-each select="$master/*">
                           <xsl:call-template name="checki18n-lang">
                                <xsl:with-param name="elem" select="." />
                                <xsl:with-param name="lang" select="$l" />
                                <xsl:with-param name="file">config</xsl:with-param>
                            </xsl:call-template>
                        </xsl:for-each>
                    </xsl:element>
                </xsl:for-each>
    			</config>
			<harvesting file="harvesting.xml">
                <xsl:variable name="master" select="//harvesting.eng"/>
    
                <xsl:for-each select="exslt:node-set($langs)/langs/*">
                            
                    <xsl:variable name="l" select="name(.)"/>
                    
                    <xsl:element name="{$l}">
                        <xsl:for-each select="$master/*">
                           <xsl:call-template name="checki18n-lang">
                                <xsl:with-param name="elem" select="." />
                                <xsl:with-param name="lang" select="$l" />
                                <xsl:with-param name="file">harvesting</xsl:with-param>
                            </xsl:call-template>
                        </xsl:for-each>
                    </xsl:element>
                </xsl:for-each>
    			</harvesting>
		</root>
	</xsl:template>



	<xsl:template name="checki18n-lang">
		<xsl:param name="elem"></xsl:param>
		<xsl:param name="lang"></xsl:param>
		<xsl:param name="file"></xsl:param>

		<xsl:variable name="tag" select="name($elem)" />
		<xsl:variable name="string" select="$elem/." />
		<xsl:variable name="value" select="$elem/@value" />
		<xsl:variable name="id" select="$elem/@id" />
		<xsl:variable name="type" select="$elem/@type" />

        
        <xsl:variable name="ok">
            <xsl:call-template name="compare">
                <xsl:with-param name="tag" select="$tag" />
                <xsl:with-param name="string" select="$string" />
                <xsl:with-param name="value" select="$value" />
                <xsl:with-param name="type" select="$type" />
                <xsl:with-param name="id" select="$id" />
                <xsl:with-param name="loctag1"
                    select="exslt:node-set(//*[name(.)=concat($file, '.', $lang)]/*)" />
                <xsl:with-param name="loctag2"
                    select="exslt:node-set(//*[name(.)=concat($file, '.', $lang)]/*/*)" />
            </xsl:call-template><!-- TODO add lang param -->
        </xsl:variable>

        <xsl:if test="$ok='red'">
	        <!-- FIXME : this will not take sub-child element -->
	        <xsl:element name="{$tag}">
				<xsl:copy-of select="@*" />
				<xsl:value-of select="$string" />
    		</xsl:element>
        </xsl:if>
	</xsl:template>


	<xsl:template name="compare">
		<xsl:param name="tag"></xsl:param>
		<xsl:param name="string"></xsl:param>
		<xsl:param name="value"></xsl:param>
		<xsl:param name="type"></xsl:param>
		<xsl:param name="id"></xsl:param>
		<xsl:param name="loctag1"></xsl:param>
		<xsl:param name="loctag2"></xsl:param>
		<xsl:choose>
			<xsl:when test="$value or $id or $type">
				<xsl:choose>
					<xsl:when
						test="count($loctag2[name(.)=$tag and (@type=$type or @value=$value or @id=$id)])=1">
						<xsl:text>green</xsl:text>
					</xsl:when>
					<xsl:when
						test="count($loctag1[name(.)=$tag and (@type=$type or @value=$value or @id=$id)])=1">
						<xsl:text>green</xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>red</xsl:text>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="count($loctag2[name(.)=$tag])=1">
						<xsl:choose>
							<xsl:when
								test="$loctag2[name(.)=$tag]/. = $string">
								<xsl:text>yellow</xsl:text>
							</xsl:when>
							<xsl:otherwise>
								<xsl:text>green</xsl:text>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:when test="count($loctag1[name(.)=$tag])=1">
						<xsl:choose>
							<xsl:when
								test="$loctag1[name(.)=$tag]/. = $string">
								<xsl:text>yellow</xsl:text>
							</xsl:when>
							<xsl:otherwise>
								<xsl:text>green</xsl:text>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>red</xsl:text>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


</xsl:stylesheet>