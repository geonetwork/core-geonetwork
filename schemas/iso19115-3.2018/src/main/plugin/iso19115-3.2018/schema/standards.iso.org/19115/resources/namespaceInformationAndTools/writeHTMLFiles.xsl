<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0">
  <xd:doc xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl" scope="stylesheet">
    <xd:desc>
      <xd:p>
        <xd:b>Title: Write Standard ISO Namespace Description Files (namespace/index.html)</xd:b>
      </xd:p>
      <xd:p><xd:b>Version:</xd:b>0.1</xd:p>
      <xd:p><xd:b>Created on:</xd:b>February 27, 2013</xd:p>
      <xd:p><xd:b>Modified on:</xd:b> January 4, 2014</xd:p>
      <xd:p><xd:b>Modified on:</xd:b> January 4, 2019 for migration of schemas to schemas.isotc211.org</xd:p>
      <xd:p><xd:b>Author:</xd:b>ted.habermann@gmail.com</xd:p>
      <xd:p>This stylesheets reads ISONamespaceInformation.xml and writes standard namespace description files into a filesystem with the ISO namespace structure.</xd:p>
      <xd:p>It assumes a schema directory hierarchy like schemaRootDirectory/standard/version/namespace/version/namespace.xsd</xd:p>
      <xd:p>and writes index.html files into the namespace directories (schemaRootDirectory/standard/version/namespace/version/index.html)</xd:p>
    </xd:desc>
  </xd:doc>
  <!-- Parameter schemaRootDirectory:
    This is the root of the schema directories.
    Example: /Users/tedhabermann/GitRepositories/ISOTC211-XML/XML/schemas.isotc211.org
  -->
  <xsl:param name="schemaRootDirectory"/>
  <!-- Parameter standard:
    This is a space delimited list of the schemaStandardNumbers to be included in the output.
    Namespaces whose schemaStandardNumber is in this list will be included in the output.
    Example: 19115-3 19157-2 19110 19111 19135
  -->
  <xsl:param name="standard"/>
  <!-- Parameter workingVersionDate
    This is the date associated with a working version of the schema. It is in the format /YYYY-MM-DD
    NOTE THE SLASH INCLUDED BEFORE THE DATE
    Example: /2014-07-11
  -->
  <xsl:param name="workingVersionDate"/>
  <xsl:variable name="TransformName" select="'writeHTMLFiles'"/>
  <xsl:variable name="TransformVersion" select="'2019-01-04'"/>
  <xsl:key name="namespaceTitleLookup" match="namespace" use="prefix"/>
  <xsl:output method="html"/>
  <xsl:strip-space elements="*"/>
  <xsl:template match="/">
    <!-- The parameter standard is a string that includes the numbers of the standards that will be output. For example '19115-2 19115-3'  -->
    <xsl:for-each select="//namespace[contains($standard,schemaStandardNumber)]">
      <xsl:variable name="currentNamespace" select="."/>
      <xsl:variable name="schemaDirectory" select="concat($schemaRootDirectory,'/',replace(schemaStandardNumber,'-','/-'),'/',prefix,'/',version,$workingVersionDate,'/')"/>
      <xsl:variable name="schemaFile" select="concat($schemaRootDirectory,'/',replace(schemaStandardNumber,'-','/-'),'/',prefix,'/',version,$workingVersionDate,'/',prefix,'.xsd')"/>
      <xsl:variable name="namespaceVersion" select="concat(prefix,' ',version)"/>
      <xsl:variable name="upperCasePrefix" select="upper-case(prefix)"/>
      <xsl:variable name="namespaceVersionTitle" select="concat($upperCasePrefix,' ',version)"/>
      <xsl:variable name="namespaceURL" select="concat(location,'/',replace(schemaStandardNumber,'-','/-'),'/',prefix,'/',version)"/>
      <xsl:variable name="outfile" select="concat($schemaRootDirectory,'/',replace(schemaStandardNumber,'-','/-'),'/',prefix,'/',version,$workingVersionDate,'/index.html')"/>
      <xsl:variable name="xmlFilesSelect" select="concat($schemaDirectory, '?select=*.xml')"/>
      <xsl:value-of select="concat('Schema: ',$schemaFile,', Output:', $outfile)"/>
      <br/>
      <xsl:result-document href="{$outfile}">
        <html>
          <head>
            <title>
              <xsl:value-of select="concat(title,' (',$upperCasePrefix,') Version: ',version)"/>
            </title>
          </head>
          <body>
            <h1>
              <xsl:value-of select="concat(title,' (',$upperCasePrefix,') Version: ',version)"/>
            </h1>
            <xsl:element name="img">
              <xsl:attribute name="src" select="concat(prefix,'.png')"/>
            </xsl:element>
            <h2>Description</h2>
            <p><xsl:value-of select="$namespaceVersionTitle"/> is an XML Schema implementation derived from ISO <xsl:value-of select="concat('ISO ',conceptualStandardNumber,', ',conceptualStandardTitle, if (exists(paragraphNumber)) then concat(', Clause ',paragraphNumber) else '')"/>. <xsl:if test="scope!=''"> It includes <xsl:value-of
                    select="concat(lower-case(substring(scope,1,1)),substring(scope,2))"/>
            </xsl:if> The XML schema was encoded using the rules described in <xsl:value-of select="encodingRules"/>.</p>
            <xsl:if test="count(collection(iri-to-uri($xmlFilesSelect)))">
              <h2>Sample XML files for <xsl:value-of select="$namespaceVersion"/></h2>
              <xsl:for-each select="collection(iri-to-uri($xmlFilesSelect))">
                <xsl:variable name="fileName" select="tokenize(document-uri(.), '/')[last()]"/>
                <xsl:if test="$fileName!='codelists.xml'">
                  <xsl:element name="a">
                    <xsl:attribute name="href" select="$fileName"/>
                    <xsl:value-of select="$fileName"/>
                  </xsl:element>
                  <xsl:text> </xsl:text>
                </xsl:if>
              </xsl:for-each>
            </xsl:if>
            <xsl:if test="count(collection(iri-to-uri(concat($schemaDirectory, '?select=codelists.*'))))">
              <h2>CodeLists for <xsl:value-of select="$namespaceVersion"/></h2>
              <xsl:for-each select="collection(iri-to-uri(concat($schemaDirectory, '?select=codelists.*')))">
                <xsl:variable name="fileName" select="tokenize(document-uri(.), '/')[last()]"/>
                <xsl:element name="a">
                  <xsl:attribute name="href" select="$fileName"/>
                  <xsl:value-of select="$fileName"/>
                </xsl:element>
                <xsl:text> </xsl:text>
              </xsl:for-each>
            </xsl:if>
            <h2>XML Namespace for <xsl:value-of select="$namespaceVersion"/></h2>
            <p>The namespace URI for <xsl:value-of select="$namespaceVersion"/> is <b><xsl:value-of select="$namespaceURL"/></b>.</p>
            <h2>XML Schema for <xsl:value-of select="$namespaceVersion"/></h2>
            <p><b><xsl:element name="a">
              <xsl:attribute name="href" select="concat(prefix,'.xsd')"/>
              <xsl:value-of select="concat(prefix,'.xsd')"/>
            </xsl:element></b> is the XML Schema document to be referenced by XML documents containing XML elements in the <xsl:value-of select="$namespaceVersion"/> namespace or by XML Schema documents importing the <xsl:value-of select="$namespaceVersion"/> namespace. This XML schema includes (indirectly) all the implemented concepts of the <xsl:value-of select="prefix"/> namespace, but it
              does not contain the declaration of any types.</p>
            <p>
              <i>NOTE: The XML Schema for <xsl:value-of select="$namespaceVersion"/> are available <xsl:element name="a">
                <xsl:attribute name="href" select="concat(prefix,'.zip')"/>here</xsl:element>. A zip archive including all the XML Schema Implementations defined in ISO/TS 19115-3 and related standards is also <xsl:element name="a">
                <xsl:attribute name="href" select="'http://schemas.isotc211.org/19115/19115AllNamespaces.zip'"/>available</xsl:element>.</i>
            </p>
            <xsl:variable name="otherSchemaList" as="xs:string*">
              <xsl:for-each select="document($schemaFile)/*/xs:include">
                <xsl:sequence select="@schemaLocation"/>
              </xsl:for-each>
            </xsl:variable>
            <xsl:if test="count($otherSchemaList) > 0">
              <h2>Related XML Schema for <xsl:value-of select="$namespaceVersion"/></h2>
              <xsl:variable name="currentRoot" select="/"/>
              <xsl:for-each select="$otherSchemaList">
                <p><b><xsl:element name="a">
                  <xsl:attribute name="href" select="."/>
                  <xsl:value-of select="."/>
                </xsl:element></b> implements the UML conceptual schema defined in <xsl:value-of select="concat('ISO ',$currentNamespace/conceptualStandardNumber,', ',$currentNamespace/conceptualStandardTitle, if (exists($currentNamespace/paragraphNumber)) then concat(', Clause ',$currentNamespace/paragraphNumber) else '')"/>. It was created using the encoding rules defined in ISO 19118, ISO
                  19139, and the implementation approach described in ISO 19115-3 and contains the following classes (codeLists are bold): <xsl:variable name="otherSchemaFile" select="concat($schemaRootDirectory,'/',replace($currentNamespace/schemaStandardNumber,'-','/-'),'/',$currentNamespace/prefix,'/',$currentNamespace/version,'/',$workingVersionDate,'/',.)"/>
                  <xsl:for-each select="document($otherSchemaFile)/*/xs:element">
                    <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                    <xsl:if test="position()=last() and position()!=1"><xsl:text>and </xsl:text></xsl:if>
                    <!-- Make code lists bold -->
                    <xsl:choose>
                      <xsl:when test="ends-with(@name,'Code')">
                        <b><xsl:value-of select="@name"/></b>
                      </xsl:when>
                      <xsl:otherwise><xsl:value-of select="@name"/></xsl:otherwise>
                    </xsl:choose>
                  </xsl:for-each>
                </p>
              </xsl:for-each>
            </xsl:if>
            <xsl:variable name="xsdFilesSelect" select="concat($schemaDirectory, '?select=*.xsd')"/>
            <xsl:variable name="completeNamespacePrefixList" as="xs:string*">
              <xsl:for-each select="collection(iri-to-uri($xsdFilesSelect))">
                <xsl:for-each select="/*/xs:import">
                  <xsl:variable name="pathTokens" as="xs:string+" select="tokenize(@schemaLocation,'/')"/>
                  <xsl:variable name="numberOfTokens" select="count($pathTokens)"/>
                  <xsl:sequence select="substring-before(tokenize(@schemaLocation,'/')[$numberOfTokens],'.')"/>
                  <!--<xsl:sequence select="tokenize(@namespace,'/')[5]"/>-->
                </xsl:for-each>
              </xsl:for-each>
            </xsl:variable>
            <xsl:variable name="otherNamespacePrefixList" as="xs:string*">
              <xsl:for-each select="collection(iri-to-uri($xsdFilesSelect))">
                <xsl:for-each select="/*/xs:import">
                  <xsl:choose>
                    <xsl:when test="contains(@namespace,'gml')">
                      <xsl:sequence select="'gml'"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:variable name="pathTokens" as="xs:string+" select="tokenize(@schemaLocation,'/')"/>
                      <xsl:variable name="numberOfTokens" select="count($pathTokens)"/>
                      <xsl:sequence select="substring-before(tokenize(@schemaLocation,'/')[$numberOfTokens],'.')"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:for-each>
              </xsl:for-each>
            </xsl:variable>
            <xsl:variable name="otherNamespaceList" as="xs:string*">
              <xsl:for-each select="collection(iri-to-uri($xsdFilesSelect))">
                <xsl:for-each select="/*/xs:import">
                  <xsl:sequence select="@namespace"/>
                </xsl:for-each></xsl:for-each>
            </xsl:variable>
            <xsl:variable name="otherNamespaceLocationList" as="xs:string*">
              <xsl:for-each select="collection(iri-to-uri($xsdFilesSelect))">
                <xsl:for-each select="/*/xs:import">
                  <xsl:sequence select="@schemaLocation"/>
                </xsl:for-each>
              </xsl:for-each>
            </xsl:variable>
            <xsl:choose>
              <xsl:when test="count($otherNamespacePrefixList)">
                <h2>Related XML Namespaces for <xsl:value-of select="$namespaceVersion"/></h2> The <xsl:value-of select="$namespaceVersion"/> namespace imports these other namespaces: <xsl:variable name="currentRoot" select="/"/>
                <table border="1" cellpadding="3" cellspacing="3">
                  <tr>
                    <th>Name</th><th>Standard Prefix</th><th>Namespace Location</th><th>Schema Location</th></tr>
                  <!--<xsl:variable name="distinctNamespacePrefixList" select="distinct-values($otherNamespacePrefixList)"/>-->
                  <xsl:for-each select="distinct-values($otherNamespacePrefixList)">
                    <xsl:sort select="."/>
                    <xsl:variable name="sequencePosition" select="subsequence(index-of($otherNamespacePrefixList,.),1,1)"/>
                    <tr>
                      <td><xsl:value-of select="key('namespaceTitleLookup',.,$currentRoot)/title"/></td>
                      <td><xsl:value-of select="."/></td>
                      <td><xsl:element name="a">
                        <xsl:attribute name="href" select="subsequence($otherNamespaceList,$sequencePosition,1)"/>
                        <xsl:value-of select="subsequence($otherNamespaceList,$sequencePosition,1)"/>
                      </xsl:element></td>
                      <td><xsl:value-of select="subsequence($otherNamespaceLocationList,$sequencePosition,1)"/></td>
                    </tr>
                  </xsl:for-each>
                </table>
              </xsl:when>
              <xsl:otherwise>
                <h2>No Related XML Namespaces for <xsl:value-of select="$namespaceVersion"/></h2>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="count(collection(iri-to-uri(concat($schemaDirectory, '?select=*.sch'))))">
              <h2>Schematron Validation Rules for <xsl:value-of select="$namespaceVersion"/></h2> Schematron rules for validating instance documents of the <xsl:value-of select="$namespaceVersion"/> namespace are in <xsl:element name="a"><xsl:attribute name="href" select="concat(prefix,'.sch')"/><xsl:value-of select="concat(prefix,'.sch')"/></xsl:element>. Other schematron rule sets that are
              required for a complete validation are: <xsl:variable name="currentRoot" select="/"/>
              <xsl:for-each select="$otherNamespacePrefixList">
                <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                <xsl:if test="position()=last() and position()!=1"><xsl:text>and </xsl:text></xsl:if>
                <xsl:value-of select="concat(.,'.sch')"/>
              </xsl:for-each></xsl:if>
            <h2>Working Versions</h2> When revisions to these schema become necessary, they will be managed in the <a href="https://github.com/ISO-TC211/XML">ISO TC211 Git Repository</a>. <hr/>
            <p><font size="small" face="italic"><xsl:value-of select="concat('Written by ',$TransformName,' Version: ',$TransformVersion, ' at ',current-dateTime())"/></font></p>
          </body>
        </html>
      </xsl:result-document>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
