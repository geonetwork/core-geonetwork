<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:geonet="http://www.fao.org/geonetwork"
  version="2.0">
  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
  <!-- XML formatting -->
  <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
  
  <xsl:variable name="xmlHeader">&lt;?xml version="1.0" encoding="UTF-8"?&gt;</xsl:variable>
  
  <!--
    draws an element as xml document
  -->
  <xsl:template mode="xmlDocument" match="*">
    <xsl:param name="edit" select="false()"/>
    <tr><td>
    <xsl:choose>
      <xsl:when test="$edit=true()">
          <textarea class="md xml" name="data">
            <xsl:value-of select="$xmlHeader"/>
            <xsl:text>&#10;</xsl:text>
            <xsl:apply-templates mode="editXMLElement" select="."/>
          </textarea>
      </xsl:when>
      <xsl:otherwise>
          <xsl:value-of select="$xmlHeader"/><br/>
          <xsl:apply-templates mode="showXMLElement" select="."/>
      </xsl:otherwise>
    </xsl:choose>
    </td></tr>
    
  </xsl:template>
  
  <!--
    draws an editable element in xml
  -->
  <xsl:template mode="editXMLElement" match="*">
    <xsl:param name="indent"/>
    <xsl:choose>
      
      <!-- has children -->
      <xsl:when test="*[not(starts-with(name(),'geonet:'))]">
        <xsl:if test="not(contains(name(.),'_ELEMENT'))">
          <xsl:call-template name="editXMLStartTag">
            <xsl:with-param name="indent" select="$indent"/>
          </xsl:call-template>
          <xsl:text>&#10;</xsl:text>
        </xsl:if>
        <xsl:for-each select="*">
          <xsl:apply-templates select="." mode="editXMLElement">
            <xsl:with-param name="indent" select="concat($indent, '&#09;')"/>
          </xsl:apply-templates>
        </xsl:for-each>
        <xsl:if test="not(contains(name(.),'_ELEMENT'))">
          <xsl:call-template name="editEndTag">
            <xsl:with-param name="indent" select="$indent"/>
          </xsl:call-template>
          <xsl:text>&#10;</xsl:text>
        </xsl:if>
      </xsl:when>
      
      <!-- no children but text -->
      <xsl:when test="text()">
        <xsl:if test="not(contains(name(.),'_ELEMENT'))">
          <xsl:call-template name="editXMLStartTag">
            <xsl:with-param name="indent" select="$indent"/>
          </xsl:call-template>
          
          <!-- xml entities should be doubly escaped -->
          <xsl:apply-templates mode="escapeXMLEntities" select="text()"/>
          
          <xsl:call-template name="editEndTag"/>
          <xsl:text>&#10;</xsl:text>
        </xsl:if>
      </xsl:when>
      
      <!-- empty element -->
      <xsl:otherwise>
        <xsl:if test="not(contains(name(.),'_ELEMENT'))">
          <xsl:call-template name="editXMLStartEndTag">
            <xsl:with-param name="indent" select="$indent"/>
          </xsl:call-template>
          <xsl:text>&#10;</xsl:text>
        </xsl:if>
      </xsl:otherwise>
      
    </xsl:choose>
  </xsl:template>
  <!--
    draws the start tag of an editable element
  -->
  <xsl:template name="editXMLStartTag">
    <xsl:param name="indent"/>
    
    <xsl:value-of select="$indent"/>
    <xsl:text>&lt;</xsl:text>
    <xsl:value-of select="name(.)"/>
    <xsl:call-template name="editXMLNamespaces"/>
    <xsl:call-template name="editXMLAttributes"/>
    <xsl:text>&gt;</xsl:text>
  </xsl:template>
  
  <!--
    draws the end tag of an editable element
  -->
  <xsl:template name="editEndTag">
    <xsl:param name="indent"/>
    
    <xsl:value-of select="$indent"/>
    <xsl:text>&lt;/</xsl:text>
    <xsl:value-of select="name(.)"/>
    <xsl:text>&gt;</xsl:text>
  </xsl:template>
  
  <!--
    draws the empty tag of an editable element
  -->
  <xsl:template name="editXMLStartEndTag">
    <xsl:param name="indent"/>
    
    <xsl:value-of select="$indent"/>
    <xsl:text>&lt;</xsl:text>
    <xsl:value-of select="name(.)"/>
    <xsl:call-template name="editXMLNamespaces"/>
    <xsl:call-template name="editXMLAttributes"/>
    <xsl:text>/&gt;</xsl:text>
  </xsl:template>
  
  <!--
    draws attribute of an editable element
  -->
  <xsl:template name="editXMLAttributes">
    <xsl:for-each select="@*">
      <xsl:if test="not(starts-with(name(.),'geonet:'))">
        <xsl:text> </xsl:text>
        <xsl:value-of select="name(.)"/>
        <xsl:text>=</xsl:text>
        <xsl:text>"</xsl:text>
        <xsl:call-template name="replaceString">
          <xsl:with-param name="expr"        select="string()"/>
          <xsl:with-param name="pattern"     select="'&amp;'"/>
          <xsl:with-param name="replacement" select="'&amp;amp;'"/>
        </xsl:call-template>
        <xsl:text>"</xsl:text>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
  
  <!--
    draws namespaces of an editable element
  -->
  <xsl:template name="editXMLNamespaces">
    <xsl:variable name="parent" select=".."/>
    <xsl:for-each select="namespace::*">
      <xsl:if test="not(.=$parent/namespace::*) and name()!='geonet'">
        <xsl:text> xmlns</xsl:text>
        <xsl:if test="name()">
          <xsl:text>:</xsl:text>
          <xsl:value-of select="name()"/>
        </xsl:if>
        <xsl:text>=</xsl:text>
        <xsl:text>"</xsl:text>
        <xsl:value-of select="string()"/>
        <xsl:text>"</xsl:text>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
  
  <!--
    draws an element in xml
  -->
  <xsl:template mode="showXMLElement" match="*">
    <xsl:choose>
      
      <!-- has children -->
      <xsl:when test="count(*)>0">
        <xsl:call-template name="showXMLStartTag"/>
        <dl class="xml">
          <xsl:for-each select="*">
            <dd>
              <xsl:apply-templates select="." mode="showXMLElement"/>
            </dd>
          </xsl:for-each>
        </dl>
        <xsl:call-template name="showEndTag"/>
      </xsl:when>
      
      <!-- no children but text -->
      <xsl:when test="text()">
        <xsl:call-template name="showXMLStartTag"/>
        <span class="xmltagvalue">
          <xsl:value-of select="text()"/>
        </span>
        <xsl:call-template name="showEndTag"/>
      </xsl:when>
      
      <!-- empty element -->
      <xsl:otherwise>
        <xsl:call-template name="showXMLStartEndTag"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!--
    draws the start tag of an element
  -->
  <xsl:template name="showXMLStartTag">
    <span class="xmltag">
      <xsl:text>&lt;</xsl:text>
      <xsl:value-of select="name(.)"/>
      <xsl:call-template name="showXMLNamespaces"/>
      <xsl:call-template name="showXMLAttributes"/>
      <xsl:text>&gt;</xsl:text>
    </span>
  </xsl:template>
  
  <!--
    draws the end tag of an element
  -->
  <xsl:template name="showEndTag">
    <span class="xmltag">
      <xsl:text>&lt;/</xsl:text>
      <xsl:value-of select="name(.)"/>
      <xsl:text>&gt;</xsl:text>
    </span>
  </xsl:template>
  
  <!--
    draws the empty tag of an element
  -->
  <xsl:template name="showXMLStartEndTag">
    <span class="xmltag">
      <xsl:text>&lt;</xsl:text>
      <xsl:value-of select="name(.)"/>
      <xsl:call-template name="showXMLNamespaces"/>
      <xsl:call-template name="showXMLAttributes"/>
      <xsl:text>/&gt;</xsl:text>
    </span>
  </xsl:template>
  
  <!--
    draws attributes of an element
  -->
  <xsl:template name="showXMLAttributes">
    <xsl:for-each select="@*">
      <xsl:if test="not(starts-with(name(.),'geonet:'))">
        <xsl:text> </xsl:text>
        <span class="xmlatt">
        <xsl:value-of select="name(.)"/>
        <xsl:text>=</xsl:text>
        </span>
        <span class="xmlattvalue">
          <xsl:text>"</xsl:text>
          <xsl:value-of select="string()"/>
          <xsl:text>"</xsl:text>
        </span>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
  
  <!--
    draws namespaces of an element
  -->
  <xsl:template name="showXMLNamespaces">
    <xsl:variable name="parent" select=".."/>
    <xsl:for-each select="namespace::*">
      <xsl:if test="not(.=$parent/namespace::*) and name()!='geonet'">
        <xsl:text> xmlns</xsl:text>
        <xsl:if test="name()">
          <xsl:text>:</xsl:text>
          <xsl:value-of select="name()"/>
        </xsl:if>
        <xsl:text>=</xsl:text>
        <span class="xmlns">
          <xsl:text>"</xsl:text>
          <xsl:value-of select="string()"/>
          <xsl:text>"</xsl:text>
        </span>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
  
  <!--
    prevent drawing of geonet:* elements
  -->
  <xsl:template mode="showXMLElement" match="geonet:*"/>
  <xsl:template mode="editXMLElement" match="geonet:*"/>
  
</xsl:stylesheet>