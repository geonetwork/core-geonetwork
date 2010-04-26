<?xml version="1.0" encoding="UTF-8"?>
<!-- Global functions - these are always included when processing a test suite -->
<ctl:package
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:ctl="http://www.occamlab.com/ctl">
   
	<ctl:function name="ctl:getImageType">
		<ctl:param name="image.uri"/>
		<ctl:description>Returns the image type name (empty string if invalid).</ctl:description>
		<ctl:java class="com.occamlab.te.parsers.ImageParser" 
                  method="getImageType"/>
	</ctl:function>   
   
	<ctl:function name="ctl:getBeginningDateTime">
		<ctl:param name="timestamp"/>
		<ctl:description>
        Returns the time instant (a dateTime value) at which a given time period 
        begins. The period may be expressed as a year, month, or date according 
        to the Gregorian calendar.
        </ctl:description>
		<ctl:java class="com.occamlab.te.util.DateTimeUtils" 
			method="getBeginningInstant" />
	</ctl:function>
   
  <ctl:function name="ctl:addDomAttr">
	  <ctl:param name="doc"/>
	  <ctl:param name="tag.name"/>
	  <ctl:param name="tag.namespace"/>
	  <ctl:param name="attr.name"/>
	  <ctl:param name="attr.value"/>
	  <ctl:description>Adds a given attribute to the nodes of a document, retrieved by the xpath expression.</ctl:description>
	  <ctl:java class="com.occamlab.te.util.DomUtils" 
					method="addDomAttr"/>
  </ctl:function>   
  
  <ctl:function name="ctl:getPathFromString">
	  <ctl:param name="filepath"/>
	  <ctl:description>Extracts the path portion of a filepath (minus the filename).</ctl:description>
	  <ctl:java class="com.occamlab.te.util.StringUtils" 
					method="getPathFromString"/>
  </ctl:function>     
   
        <ctl:function name="ctl:getResourceURL">
                <ctl:param name="resourcepath"/>
                <ctl:description>Returns the URL for the resource at a given resource path.</ctl:description>
                <ctl:java class="com.occamlab.te.util.Misc" method="getResourceURL"/>
        </ctl:function>

    <ctl:function name="ctl:encode">
        <ctl:param name="s">String to encode</ctl:param>
                <ctl:description>Returns the URL encoded form of a string.</ctl:description>
        <ctl:java class="java.net.URLEncoder" method="encode"/>
    </ctl:function>
</ctl:package>
