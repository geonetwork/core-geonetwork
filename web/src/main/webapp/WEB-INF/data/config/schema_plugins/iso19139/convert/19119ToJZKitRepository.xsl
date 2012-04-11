<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
						xmlns:gco="http://www.isotc211.org/2005/gco"
						xmlns:srv="http://www.isotc211.org/2005/srv"
						xmlns:gmd="http://www.isotc211.org/2005/gmd"
						exclude-result-prefixes="gmd gco srv">

	<xsl:output method="xml" indent="yes"/>

	<xsl:template match="/gmd:MD_Metadata">

		<xsl:variable name="recordType" select="*/srv:SV_ServiceIdentification/srv:serviceType/gco:LocalName"/>
		<xsl:if test="$recordType!='Z3950' and $recordType!='ISO 23950 gateway'">
			<error>Incompatible</error>
		</xsl:if>

		<xsl:for-each select="*/srv:SV_ServiceIdentification">

			<xsl:variable name="code" select="../../gmd:fileIdentifier/gco:CharacterString"/>
			<xsl:variable name="serviceName" select="normalize-space(gmd:citation//gmd:title/gco:CharacterString)"/>
			<xsl:variable name="preference" select="normalize-space(substring-after(srv:containsOperations//srv:connectPoint//gmd:linkage/gmd:URL,'z3950://'))"/>

			<xsl:variable name="hostPart" select="substring-before($preference,'/')"/>

			<xsl:comment>Entry for <xsl:value-of select="$serviceName"/></xsl:comment>

			<Repository className="org.jzkit.search.provider.z3950.Z3950Origin" code="{$code}" serviceName="{$serviceName}">
				<Preferences>
					<Preference name="defaultRecordSyntax">xml</Preference>
					<Preference name="defaultElementSetName">s</Preference>
					<Preference name="host"><xsl:value-of select="substring-before($hostPart,':')"/></Preference>
					<Preference name="port"><xsl:value-of select="substring-after($hostPart,':')"/></Preference>
					<Preference name="smallSetElementSetName">F</Preference>
      		<Preference name="charsetEncoding">UTF-8</Preference>
      		<Preference name="useReferenceId">negotiate</Preference>
    		</Preferences>
    		<RecordArchetypes>
      		<Archetype name="F">xml::f</Archetype>
      		<Archetype name="H">html::f</Archetype>
    		</RecordArchetypes>

    		<Collections>
					<xsl:for-each select="srv:containsOperations//srv:connectPoint//gmd:linkage/gmd:URL">
						<xsl:variable name="code" select="substring-after(.,'z3950://')"/>
						<Collection code="{substring-after($code,'/')}" name="{$serviceName}" localId="{substring-after($code,'/')}" profile="geo"/>
					</xsl:for-each>
				</Collections>
			</Repository>
		</xsl:for-each>
	</xsl:template>

</xsl:stylesheet>
