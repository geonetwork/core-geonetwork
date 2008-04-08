<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xalan="http://xml.apache.org/xalan">


    <xsl:variable name="langs">
        <langs>
            <!-- TODO : add a master lang param to be able to compare to other lang than en <en master="true"/> -->
            <fr/>
            <es/>
            <cn/>
            <ar/>
        </langs>
    </xsl:variable>

	<xsl:template match="/">
		<h1>Test i18n</h1>
		<p>This service should help GeoNetwork opensource developpers to have up to date localized files.
		Even if there's no tranlsation, all terms should be added to all localized files (even if no translation 
		are available). If new terms appear, you could ask to the genetwork-users@sourceforge.net for
		some contribution on that. <b>Try to reduce the number of red squares in that page ;).</b> Thanks.</p>
		<i>
			"en" is master language. Each element of english files are
			compared to the others
		</i>
		<p>
		A draft XML document to use to update localized files is available <a href="test.i18n.xml">here</a>.
		</p>
		<table>
			<tr>
				<td bgcolor="green" width="30px"></td>
				<td>ok</td>
			</tr>
			<tr>
				<td bgcolor="red" width="30px"></td>
				<td>missing element</td>
			</tr>
			<tr>
				<td bgcolor="yellow" width="30px"></td>
				<td>
					existing element but translation is equal to "en" value
					(and that could be valid)
				</td>
			</tr>
		</table>

		<table>
			<th>
			     <td width="20px"><b>en</b></td>
			     <xsl:for-each select="xalan:nodeset($langs)/langs/*">
			             <td width="20px"><b><xsl:value-of select="name(.)"/></b></td>
			     </xsl:for-each>
			</th>
			
			<!-- FIXME: This loop over xml loc files is not really elegant :( -->
			<tr>
				<td colspan="5">
					<h1>strings.xml</h1>
				</td>
			</tr>
			<xsl:for-each select="//strings.en/*">
				<tr>
					<xsl:call-template name="checki18n">
						<xsl:with-param name="elem" select="." />
						<xsl:with-param name="file">strings</xsl:with-param>
					</xsl:call-template>
				</tr>
			</xsl:for-each>
				<tr>
					<td colspan="4">
						<h1>about.xml</h1>
					</td>
				</tr>
				<xsl:for-each select="//about.en/*">
					<tr>
						<xsl:call-template name="checki18n">
                            <xsl:with-param name="elem" select="." />
							<xsl:with-param name="file">about</xsl:with-param>
						</xsl:call-template>
					</tr>
				</xsl:for-each>
				<tr>
					<td colspan="4">
						<h1>config.xml</h1>
					</td>
				</tr>
				<xsl:for-each select="//config.en/*">
					<tr>
						<xsl:call-template name="checki18n">
                            <xsl:with-param name="elem" select="." />
							<xsl:with-param name="file">config</xsl:with-param>
						</xsl:call-template>
					</tr>
				</xsl:for-each>
				<tr>
					<td colspan="4">
						<h1>harvesting.xml</h1>
					</td>
				</tr>
				<xsl:for-each select="//harvesting.en/*">
					<tr>
						<xsl:call-template name="checki18n">
                            <xsl:with-param name="elem" select="." />
							<xsl:with-param name="file">harvesting</xsl:with-param>
						</xsl:call-template>
					</tr>
				</xsl:for-each>


		</table>


        <p>
		TODO:
		<ul>
			<li>Check all elements exist somewhere in XSL files</li>
			<li>Check all loc files</li>
            <li>Add a master lang parameter</li>
		</ul>
        </p>
	</xsl:template>



	<xsl:template name="checki18n">
		<xsl:param name="elem"></xsl:param>
        <xsl:param name="file"></xsl:param>
		
        <xsl:variable name="tag" select="name($elem)" />
		<xsl:variable name="string" select="$elem/." />
        <xsl:variable name="value" select="$elem/@value" />
        <xsl:variable name="id" select="$elem/@id" />
        <xsl:variable name="type" select="$elem/@type" />

		
		<td>
			<xsl:attribute name="title">
		          <xsl:value-of select="$string" />
		    </xsl:attribute>

			<xsl:value-of select="$tag" />
			<xsl:if test="$value">
				(value:
				<xsl:value-of select="$value" />
				)
			</xsl:if>
			<xsl:if test="$id">
				(id:
				<xsl:value-of select="$id" />
				)
			</xsl:if>
            <xsl:if test="$type">
                (type:
                <xsl:value-of select="$type" />
                )
            </xsl:if>
		</td>              
		<!-- FIXME: Why can't loop on $la ? 
		<xsl:for-each select="xalan:nodeset($langs)/langs/*">
                <td>
                    <xsl:variable name="la"><xsl:value-of select="concat($file, '.', name(.))"/></xsl:variable>
                    <xsl:value-of select="$la"/>
                    <xsl:value-of select="count(xalan:nodeset(//*[name()='strings.fr']/*))"/>
                    
		            <xsl:call-template name="compare">
		                <xsl:with-param name="tag" select="$tag" />
		                <xsl:with-param name="string" select="$string" />
		                <xsl:with-param name="value" select="$value" />
		                <xsl:with-param name="type" select="$type" />
		                <xsl:with-param name="id" select="$id" />
		                <xsl:with-param name="loctag1"
		                    select="xalan:nodeset(//*[name(.)=$la]/*)" />
		                <xsl:with-param name="loctag2"
		                    select="xalan:nodeset(//*[name(.)=$la]/*/*)" />
		            </xsl:call-template>
		        </td>
        </xsl:for-each> -->
        <!-- FIXME: This loop over languages is not really elegant too :( -->
        <td bgcolor="green"></td>
		<td>
			<xsl:call-template name="compare">
				<xsl:with-param name="tag" select="$tag" />
				<xsl:with-param name="string" select="$string" />
				<xsl:with-param name="value" select="$value" />
                <xsl:with-param name="type" select="$type" />
				<xsl:with-param name="id" select="$id" />
				<xsl:with-param name="loctag1"
					select="xalan:nodeset(//*[name(.)=concat($file, '.fr')]/*)" />
				<xsl:with-param name="loctag2"
					select="xalan:nodeset(//*[name(.)=concat($file, '.fr')]/*/*)" />
			</xsl:call-template>
		</td>
		<td>
			<xsl:call-template name="compare">
				<xsl:with-param name="tag" select="$tag" />
				<xsl:with-param name="string" select="$string" />
				<xsl:with-param name="value" select="$value" />
                <xsl:with-param name="type" select="$type" />
				<xsl:with-param name="id" select="$id" />
				<xsl:with-param name="loctag1"
					select="xalan:nodeset(//*[name(.)=concat($file, '.es')]/*)" />
				<xsl:with-param name="loctag2"
					select="xalan:nodeset(//*[name(.)=concat($file, '.es')]/*/*)" />
			</xsl:call-template>
		</td>
		<td>
			<xsl:call-template name="compare">
				<xsl:with-param name="tag" select="$tag" />
				<xsl:with-param name="string" select="$string" />
				<xsl:with-param name="value" select="$value" />
                <xsl:with-param name="type" select="$type" />
				<xsl:with-param name="id" select="$id" />
				<xsl:with-param name="loctag1"
					select="xalan:nodeset(//*[name(.)=concat($file, '.cn')]/*)" />
				<xsl:with-param name="loctag2"
					select="xalan:nodeset(//*[name(.)=concat($file, '.cn')])/*/*" />
			</xsl:call-template>
		</td>
		<td>
			<xsl:call-template name="compare">
				<xsl:with-param name="tag" select="$tag" />
				<xsl:with-param name="string" select="$string" />
				<xsl:with-param name="value" select="$value" />
                <xsl:with-param name="type" select="$type" />
				<xsl:with-param name="id" select="$id" />
				<xsl:with-param name="loctag1"
					select="xalan:nodeset(//*[name(.)=concat($file, '.ar')])/*" />
				<xsl:with-param name="loctag2"
					select="xalan:nodeset(//*[name(.)=concat($file, '.ar')])/*/*" />
			</xsl:call-template>
		</td> 
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
						<xsl:attribute name="bgcolor">green</xsl:attribute>
					</xsl:when>
					<xsl:when
						test="count($loctag1[name(.)=$tag and (@type=$type or @value=$value or @id=$id)])=1">
						<xsl:attribute name="bgcolor">green</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="bgcolor">red</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="$string=''"><!-- Empty tag -->
					   <xsl:attribute name="bgcolor">green</xsl:attribute>
                    </xsl:when>
                    <xsl:when test="count($loctag2[name(.)=$tag])=1">
						<xsl:choose>
							<xsl:when
								test="$loctag2[name(.)=$tag]/. = $string">
								<xsl:attribute name="bgcolor">yellow</xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="bgcolor">green</xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:when test="count($loctag1[name(.)=$tag])=1">
						<xsl:choose>
							<xsl:when
								test="$loctag1[name(.)=$tag]/. = $string">
								<xsl:attribute name="bgcolor">yellow</xsl:attribute>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="bgcolor">green</xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="bgcolor">red</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


</xsl:stylesheet>

