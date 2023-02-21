<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:sr="http://www.w3.org/2005/sparql-results#"
                xmlns:gn-fn-sparql="http://geonetwork-opensource.org/xsl/functions/sparql"
                xmlns:saxon="http://saxon.sf.net/"
                version="2.0" extension-element-prefixes="saxon"
                exclude-result-prefixes="#all">


  <xsl:function name="gn-fn-sparql:getSubject" as="node()*">
    <xsl:param name="context" as="node()"/>
    <xsl:param name="predicate" as="xs:string"/>
    <xsl:param name="object" as="xs:string"/>

    <xsl:copy-of select="$context/sr:result[
                            sr:binding[@name = 'predicate']/sr:uri = $predicate
                            and sr:binding[@name = 'object']/sr:uri = $object]
                            /sr:binding[@name = 'subject']"/>
  </xsl:function>


  <xsl:function name="gn-fn-sparql:getObject" as="node()*">
    <xsl:param name="context" as="node()"/>
    <xsl:param name="predicate" as="xs:string*"/>
    <xsl:param name="subject" as="xs:string"/>

<!--    <xsl:message>For <xsl:value-of select="$subject"/> getObject <xsl:value-of select="$predicate"/></xsl:message>-->
    <xsl:variable name="object"
                  select="$context/sr:result[
                            sr:binding[@name = 'subject']/(sr:uri|sr:bnode) = $subject
                            and sr:binding[@name = 'predicate']/sr:uri = $predicate[1]]
                          /sr:binding[@name = 'object']"/>
    <xsl:choose>
      <xsl:when test="count($predicate) > 1">
        <xsl:if test="$object/sr:bnode">
          <xsl:copy-of select="gn-fn-sparql:getObject($context,
                                $predicate[position() > 1],
                                $object/sr:bnode)/sr:literal"/>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="$object"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
</xsl:stylesheet>
