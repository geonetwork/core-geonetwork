<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:srv="http://standards.iso.org/iso/19115/-3/srv/2.1"
                xmlns:mds="http://standards.iso.org/iso/19115/-3/mds/2.0"
                xmlns:mcc="http://standards.iso.org/iso/19115/-3/mcc/1.0"
                xmlns:mri="http://standards.iso.org/iso/19115/-3/mri/1.0"
                xmlns:mrs="http://standards.iso.org/iso/19115/-3/mrs/1.0"
                xmlns:mrd="http://standards.iso.org/iso/19115/-3/mrd/1.0"
                xmlns:mdb="http://standards.iso.org/iso/19115/-3/mdb/2.0"
                xmlns:mrl="http://standards.iso.org/iso/19115/-3/mrl/2.0"
                xmlns:mrc="http://standards.iso.org/iso/19115/-3/mrc/2.0"
                xmlns:mmi="http://standards.iso.org/iso/19115/-3/mmi/1.0"
                xmlns:mco="http://standards.iso.org/iso/19115/-3/mco/1.0"
                xmlns:mdq="http://standards.iso.org/iso/19157/-2/mdq/1.0"
                xmlns:msr="http://standards.iso.org/iso/19115/-3/msr/2.0"
                xmlns:lan="http://standards.iso.org/iso/19115/-3/lan/1.0"
                xmlns:gcx="http://standards.iso.org/iso/19115/-3/gcx/1.0"
                xmlns:gex="http://standards.iso.org/iso/19115/-3/gex/1.0"
                xmlns:dqm="http://standards.iso.org/iso/19157/-2/dqm/1.0"
                xmlns:cit="http://standards.iso.org/iso/19115/-3/cit/2.0"
                xmlns:gco="http://standards.iso.org/iso/19115/-3/gco/1.0"
                xmlns:mac="http://standards.iso.org/iso/19115/-3/mac/2.0"
                xmlns:gfc="http://standards.iso.org/iso/19110/gfc/1.1"
                xmlns:gts="http://www.isotc211.org/2005/gts"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:err="http://www.w3.org/2005/xqt-errors"
                xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
                xmlns:gn="http://www.fao.org/geonetwork"
                exclude-result-prefixes="#all">


  <!-- Evaluate an expression. This is schema dependant in order to properly
        set namespaces required for evaluate.

       A node returned by evaluate will lost its context (ancestors).
    -->
  <xsl:function name="gn-fn-metadata:evaluate-iso19115-3.2018">
    <xsl:param name="base" as="node()"/>
    <xsl:param name="in"/>
    <!--
     <xsl:message>in xml <xsl:copy-of select="$base"/></xsl:message>
     <xsl:message>search for <xsl:copy-of select="$in"/></xsl:message>-->

    <!-- saxon:evaluate and xsl:evaluate does not have the same context mechanism.
    TODO-SAXON: Check how to better handle XPath expression
    in edit and view mode. -->
    <xsl:variable name="context" as="node()">
      <root>
        <xsl:copy-of select="$base"/>
      </root>
    </xsl:variable>

    <xsl:try>
      <xsl:evaluate xpath="if (starts-with($in, '/../')) then substring($in, 5)
                           else if (starts-with($in, '..//')) then substring($in, 5)
                           else $in"
                    context-item="$context"/>
      <xsl:catch>
        <xsl:message>Error evaluating <xsl:value-of select="$in"/> in context item <xsl:value-of select="name($base)"/>.
          <xsl:value-of select="$err:description"/></xsl:message>
      </xsl:catch>
    </xsl:try>
  </xsl:function>

  <!-- Evaluate XPath returning a boolean value. -->
  <xsl:function name="gn-fn-metadata:evaluate-iso19115-3.2018-boolean"
                as="xs:boolean">
    <xsl:param name="base" as="node()"/>
    <xsl:param name="in"/>

    <xsl:evaluate xpath="$in" context-item="$base"/>
  </xsl:function>
</xsl:stylesheet>
