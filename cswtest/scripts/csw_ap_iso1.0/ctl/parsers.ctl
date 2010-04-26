<?xml version="1.0" encoding="UTF-8"?>
<ctl:package
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:ctl="http://www.occamlab.com/ctl"
   xmlns:parsers="http://www.occamlab.com/te/parsers"
   xmlns:p="http://teamengine.sourceforge.net/parsers">

  <!-- Sample usage:
       <ctl:call-test name="ctl:SchematronValidatingParser">
         <ctl:with-param name="doc" select="$response"/>
         <ctl:with-param name="schema">sch/csw/2.0.1/CSWCapabilities.sch</ctl:with-param>
         <ctl:with-param name="phase">DefaultPhase</ctl:with-param>
       </ctl:call-test>
       -->
<!--   <ctl:test name="ctl:SchematronValidatingParser"> -->
<!--     <ctl:param name="doc"/> -->
<!--     <ctl:param name="schema"/> -->
<!--     <ctl:param name="phase"/> -->
<!--     <ctl:assertion> -->
<!--       Validate against Schematron schema {$schema} (phase: {$phase}). -->
<!--     </ctl:assertion> -->
<!--     <ctl:code> -->
<!--       <xsl:variable name="isValid"> -->
<!--         <ctl:call-function name="ctl:CallSchematronValidatingParser"> -->
<!--           <ctl:with-param name="doc"><xsl:copy-of select="$doc"/></ctl:with-param> -->
<!--           <ctl:with-param name="schema" select="string($schema)"/> -->
<!--           <ctl:with-param name="phase" select="string($phase)"/> -->
<!--         </ctl:call-function> -->
<!--       </xsl:variable> -->
<!--       <xsl:if test="$isValid='false'"> -->
<!--         <ctl:fail/> -->
<!--       </xsl:if> -->
<!--     </ctl:code> -->
<!--   </ctl:test> -->
<!--   <ctl:function name="ctl:CallSchematronValidatingParser"> -->
<!--     <ctl:param name="doc"/> -->
<!--     <ctl:param name="schema"/> -->
<!--     <ctl:param name="phase"/> -->
<!--     <ctl:description>Invokes the Schematon validator.</ctl:description> -->
<!--     <ctl:java class="com.occamlab.te.parsers.SchematronValidatingParser" -->
<!--               method="checkSchematronRules" -->
<!--               initialized="true"/> -->
<!--   </ctl:function> -->

  <!-- Sample usage:
       <ctl:call-test name="ctl:XMLValidatingParser">
         <ctl:with-param name="doc"><xsl:copy-of select="$response"/></ctl:with-param>
         <ctl:with-param name="instruction">
           <parsers:schemas>
             <parsers:schema type="resource">xsd/ogc/csw/2.0.1/csw-2.0.1.xsd</parsers:schema>
           </parsers:schemas>
         </ctl:with-param>
       </ctl:call-test>
       -->
  <ctl:test name="ctl:XMLValidatingParser">
    <ctl:param name="doc"/>
    <ctl:param name="instruction"/>
    <ctl:assertion>
      Validates the XML instance against the set of XML Schemas specified
      using the $instruction parameter.
    </ctl:assertion>
    <ctl:code>
      <xsl:variable name="isValid">
        <ctl:call-function name="ctl:CallXMLValidatingParser">
          <ctl:with-param name="doc"><xsl:copy-of select="$doc"/></ctl:with-param>
          <ctl:with-param name="instruction"><xsl:copy-of select="$instruction"/></ctl:with-param>
        </ctl:call-function>
      </xsl:variable>
      <xsl:if test="not(boolean($isValid))">
        <ctl:fail/>
      </xsl:if>
    </ctl:code>
  </ctl:test>
  <ctl:function name="ctl:CallXMLValidatingParser">
    <ctl:param name="doc"/>
    <ctl:param name="instruction"/>
    <ctl:description>Invokes the XML Schema validator.</ctl:description>
    <ctl:java class="com.occamlab.te.parsers.XMLValidatingParser"
              method="checkXMLRules"
              initialized="true"/>
  </ctl:function>

  <!-- Sample usage:
       <ctl:call-test name="ctl:XMLValidatingParser.CSW">
         <ctl:with-param name="doc"><xsl:copy-of select="$response//content/*"/></ctl:with-param>
       </ctl:call-test>
       -->
<!--   <ctl:test name="ctl:XMLValidatingParser.CSW"> -->
<!--     <ctl:param name="doc"/> -->
<!--     <ctl:assertion> -->
<!--       Validates a given XML document against the CSW 2.0.1 schema set. -->
<!--     </ctl:assertion> -->
<!--     <ctl:code> -->
<!--       <ctl:call-test name="ctl:XMLValidatingParser"> -->
<!--         <ctl:with-param name="doc"><xsl:copy-of select="$doc"/></ctl:with-param> -->
<!--         <ctl:with-param name="instruction"> -->
<!--           <parsers:schemas> -->
<!--             <parsers:schema type="resource">xsd/ogc/csw/2.0.1/csw-2.0.1.xsd</parsers:schema> -->
<!--           </parsers:schemas> -->
<!--         </ctl:with-param> -->
<!--       </ctl:call-test> -->
<!--     </ctl:code> -->
<!--   </ctl:test> -->

<!--   <ctl:parser name="p:XMLValidatingParser.CSW"> -->
<!--     <ctl:java class="com.occamlab.te.parsers.XMLValidatingParser" -->
<!--               method="parse" -->
<!--               initialized="true"> -->
<!--       <ctl:with-param name="schemas_links"> -->
<!--         <parsers:schemas> -->
<!--           <parsers:schemas> -->
<!--             <parsers:schema type="resource">xsd/ogc/csw/2.0.1/csw-2.0.1.xsd</parsers:schema> -->
<!--           </parsers:schemas> -->
<!--         </parsers:schemas> -->
<!--       </ctl:with-param> -->
<!--     </ctl:java> -->
<!--   </ctl:parser> -->

<!--   <ctl:parser name="p:XMLValidatingParser.OWS"> -->
<!--     <ctl:java class="com.occamlab.te.parsers.XMLValidatingParser" method="parse" initialized="true"> -->
<!--       <ctl:with-param name="schemas_links"> -->
<!--         <parsers:schemas> -->
<!--           <parsers:schema type="resource">xsd/ogc/ows/1.0.0/ows-1.0.0.xsd</parsers:schema> -->
<!--         </parsers:schemas> -->
<!--       </ctl:with-param> -->
<!--     </ctl:java> -->
<!--   </ctl:parser> -->

<!--   <ctl:parser name="p:XMLValidatingParser.XMLSchema"> -->
<!--     <ctl:java class="com.occamlab.te.parsers.XMLValidatingParser" method="parse" initialized="true"> -->
<!--       <ctl:with-param name="schemas_links"> -->
<!--         <parsers:schemas> -->
<!--           <parsers:schema type="resource">xsd/w3c/xmlschema/1.0/XMLSchema.xsd</parsers:schema> -->
<!--         </parsers:schemas> -->
<!--       </ctl:with-param> -->
<!--     </ctl:java> -->
<!--   </ctl:parser> -->

  <!-- Sample usage:
       <p:SchematronValidatingParser>
         <parsers:schemas>
           <parsers:schema type="resource" phase="Default">sch/csw/2.0.1/CSWCapabilities.sch</parsers:schema>
         </parsers:schemas>
       </p:SchematronValidatingParser>
       -->
<!--   <ctl:parser name="p:SchematronValidatingParser"> -->
<!--     <ctl:param name="schema_link"/> -->
<!--     <ctl:java class="com.occamlab.te.parsers.SchematronValidatingParser" -->
<!--               method="parse" -->
<!--               initialized="true"/> -->
<!--   </ctl:parser> -->

  <!-- Sample usage:
       <p:SchematronValidatingParser.CSWCapabilities />
       -->
<!--   <ctl:parser name="p:SchematronValidatingParser.CSWCapabilities"> -->
<!--     <ctl:java class="com.occamlab.te.parsers.SchematronValidatingParser" -->
<!--               method="parse" -->
<!--               initialized="true"> -->
<!--       <ctl:with-param name="schema_link"> -->
<!--         <parsers:schemas> -->
<!--           <parsers:schema type="resource" phase="DefaultPhase">sch/csw/2.0.1/Capabilities.sch</parsers:schema> -->
<!--         </parsers:schemas> -->
<!--       </ctl:with-param> -->
<!--     </ctl:java> -->
<!--   </ctl:parser> -->

</ctl:package>
