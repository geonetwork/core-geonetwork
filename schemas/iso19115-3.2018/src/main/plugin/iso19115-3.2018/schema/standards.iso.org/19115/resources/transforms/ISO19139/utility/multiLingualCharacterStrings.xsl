<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gcoold="http://www.isotc211.org/2005/gco" xmlns:srvold="http://www.isotc211.org/2005/srv" xmlns:gml="http://www.opengis.net/gml"
    xmlns:cat="http://standards.iso.org/iso/19115/-3/cat/1.0" xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/1.0" xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
    xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0" xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0" xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.0"
    xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/1.0" xmlns:mas="http://standards.iso.org/iso/19115/-3/mas/1.0" xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
    xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0" xmlns:mda="http://standards.iso.org/iso/19115/-3/mda/1.0" xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/1.0"
    xmlns:mdt="http://standards.iso.org/iso/19115/-3/mdt/1.0" xmlns:mex="http://standards.iso.org/iso/19115/-3/mex/1.0" xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/1.0"
    xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/1.0" xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0" xmlns:mpc="http://standards.iso.org/iso/19115/-3/mpc/1.0"
    xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/1.0" xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0" xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
    xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0" xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/1.0" xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
    xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0" xmlns:gml32="http://www.opengis.net/gml/3.2" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    exclude-result-prefixes="#all">
    <xd:doc xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl" scope="stylesheet">
        <xd:desc>
            <xd:p> These utility templates transform CodeLists and CharacterStrings from ISO 19139 into ISO 19115-3.</xd:p>
            <xd:p>Version August 8, 2015</xd:p>
            <xd:p><xd:b>Author:</xd:b>thabermann@hdfgroup.org</xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template name="writeCharacterStringElement">
        <!-- Parameters
        elementName = the name of the element (with namespace prefix) that contains the codelist, i.e. cit:name
        nodeWithStringToWrite = the path of the node that contains the character string to be written
        -->
        <xsl:param name="elementName"/>
        <xsl:param name="nodeWithStringToWrite"/>
        <xsl:variable name="isMultilingual" select="count($nodeWithStringToWrite/gmd:PT_FreeText) > 0"/>
        <!-- 
            The hasCharacterString variable was generalized to include situations where substitutions are
            being used gor gco:CharacterString, e.g. gmx:FileName.
        -->
        <xsl:variable name="hasChildNode" select="count($nodeWithStringToWrite/*) = 1"/>
        <xsl:if test="$nodeWithStringToWrite">
            <xsl:element name="{$elementName}">
                <!-- Deal with attributes (may be in the old gco namespace -->
                <xsl:apply-templates select="$nodeWithStringToWrite/@*[name() != 'xsi:type']"
                                     mode="from19139to19115-3"/>
                <xsl:if test="$isMultilingual">
                    <xsl:attribute name="xsi:type" select="'lan:PT_FreeText_PropertyType'"/>
                </xsl:if>
                <xsl:if test="$hasChildNode">
                    <!-- 
                            This could be any substitution for gco:CharacterString.
                            Get correct namespace and preserve name for substitutions
                        -->
                    <xsl:for-each select="$nodeWithStringToWrite/*">
                        <xsl:variable name="nameSpacePrefix">
                            <xsl:call-template name="getNamespacePrefix"/>
                        </xsl:variable>
                        <xsl:element name="{concat($nameSpacePrefix, ':',local-name())}">
                            <xsl:value-of select="."/>
                        </xsl:element>
                    </xsl:for-each>
                </xsl:if>
                <xsl:if test="$isMultilingual">
                    <xsl:apply-templates select="$nodeWithStringToWrite/gmd:PT_FreeText"/>
                </xsl:if>
            </xsl:element>
        </xsl:if>
    </xsl:template>
    <xsl:template name="writeCodelistElement">
        <xsl:param name="elementName"/>
        <xsl:param name="codeListName"/>
        <xsl:param name="codeListValue"/>
        <!-- The correct codeList Location goes here -->
        <xsl:variable name="codeListLocation" select="'codeListLocation'"/>
        <xsl:if test="string-length($codeListValue) > 0">
            <xsl:element name="{$elementName}">
                <xsl:element name="{$codeListName}">
                    <xsl:attribute name="codeList">
                        <xsl:value-of select="$codeListLocation"/>
                        <xsl:value-of select="'#'"/>
                        <xsl:value-of select="substring-after($codeListName,':')"/>
                    </xsl:attribute>
                    <xsl:attribute name="codeListValue">
                        <!-- the anyValidURI value is used for testing with paths -->
                        <!--<xsl:value-of select="'anyValidURI'"/>-->
                        <!-- commented out for testing -->
                        <xsl:value-of select="$codeListValue"/>
                    </xsl:attribute>
                    <xsl:value-of select="$codeListValue"/>
                </xsl:element>
            </xsl:element>
            <!--<xsl:if test="@*">
                <xsl:element name="{$elementName}">
                    <xsl:apply-templates select="@*"/>
                </xsl:element>
            </xsl:if>-->
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
