<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<!-- ============================================================================================= -->
	<!-- === This stylesheet transforms an harvesting node from settings XML to output XML -->
	<!-- ============================================================================================= -->

	<xsl:template match="node">
		<xsl:variable name="site"  select="children/site/children"/>
		<xsl:variable name="opt"   select="children/options/children"/>
		<xsl:variable name="con"   select="children/content/children"/>
		<xsl:variable name="priv"  select="children/privileges/children"/>
		<xsl:variable name="categ" select="children/categories/children"/>
		<xsl:variable name="info"  select="children/info/children"/>
        <xsl:variable name="owner" select="children/owner"/>
        <xsl:variable name="ownerGroup" select="children/ownerGroup"/>
		
		<node id="{@id}" type="{value}">
            <owner><id><xsl:value-of select="$site/ownerId/value" /></id></owner>
            <ownerGroup><id><xsl:value-of select="$site/ownerGroup/value" /></id></ownerGroup>

			<site>
				<name><xsl:value-of select="$site/name/value" /></name>
                <translations>
                    <xsl:for-each select="$site/translations/children/node()">
                        <xsl:element name="{name()}">
                            <xsl:value-of select="."/>
                        </xsl:element>
                    </xsl:for-each>
                </translations>

                <uuid><xsl:value-of select="$site/uuid/value" /></uuid>
				<account>
					<use><xsl:value-of select="$site/useAccount/value" /></use>
					<username><xsl:value-of select="$site/useAccount/children/username/value" /></username>
					<password><xsl:value-of select="$site/useAccount/children/password/value" /></password>
				</account>
				
				<xsl:apply-templates select="$site" mode="site"/>
			</site>
		
			<content>
				<validate><xsl:value-of select="$con/validate/value" /></validate>
				<importxslt><xsl:value-of select="$con/importxslt/value" /></importxslt>
			</content>

			<options>
				<every><xsl:value-of select="$opt/every/value" /></every>
				<oneRunOnly><xsl:value-of select="$opt/oneRunOnly/value" /></oneRunOnly>
				<status><xsl:value-of select="$opt/status/value"/></status>
				
				<xsl:apply-templates select="$opt" mode="options"/>
			</options>
			
			<xsl:apply-templates select="."      mode="searches"/>			
			<xsl:apply-templates select="$priv"  mode="privileges"/>
			<xsl:apply-templates select="$categ" mode="categories"/>
			<xsl:apply-templates select="."      mode="other"/>			
			
			<info>
				<lastRun><xsl:value-of select="$info/lastRun/value" /></lastRun>
			</info>
		</node>
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
	<xsl:template match="*" mode="privileges">
		<privileges>
			<xsl:for-each select="group">
				<group id="{value}">
					<xsl:for-each select="children/operation">
						<xsl:choose>
							<xsl:when test="value = '0'"><operation name="view"/></xsl:when>
							<xsl:when test="value = '1'"><operation name="download"/></xsl:when>
							<xsl:when test="value = '3'"><operation name="notify"/></xsl:when>
							<xsl:when test="value = '5'"><operation name="dynamic"/></xsl:when>
							<xsl:when test="value = '6'"><operation name="featured"/></xsl:when>
						</xsl:choose>
					</xsl:for-each>
				</group>
			</xsl:for-each>
		</privileges>			
	</xsl:template>
	
	<!-- ============================================================================================= -->
	
	<xsl:template match="*" mode="categories">
		<categories>
			<xsl:for-each select="category">
				<category id="{value}"/>
			</xsl:for-each>
		</categories>
	</xsl:template>
	<!-- ============================================================================================= -->
	<!-- === Hooks -->
	<!-- ============================================================================================= -->

	<xsl:template match="*" mode="site"/>
	<xsl:template match="*" mode="options"/>
	<xsl:template match="*" mode="searches"/>
	<xsl:template match="*" mode="other"/>
	
	<!-- ============================================================================================= -->
	
</xsl:stylesheet>
