<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<!---   
          This JSP expect to have a:
          
          <form-bean 
	    name="srsInfoForm" 
	    type="org.apache.struts.action.DynaActionForm">
	    <form-property 
	        name="srsDefinitionList"
	        type="java.lang.String[]" 
	    />
	    <form-property 
	        name="srsIDList" 
	        type="java.lang.String[]" 
	    />
	</form-bean>
	
	   given to it.  The id list is a list of integers (as strings) - these are the EPSG:# codes.
	   The Definition list is the WKT of that EPSG code.
  --->

<!-- ALL THIS STUFF TAKEN FROM MAINLAYOUT.JSP -->
<!------------------------------------------------------------------------>
<html:html locale="true" xhtml="true">
  <head>
    <title>
      <bean:message key="geoserver.logo"/>
      Geoserver
    </title>
    <meta content="text/html; charset=UTF-8" http-equiv="content-type"/>
    <meta content="text/css" http-equiv="content-style-type"/>  
    <meta name="keywords"
          content="(GeoServer) (GIS) (Geographic Information Systems)"/>
    <meta name="author" content="David Blasby"/>
  
    <style type="text/css">
      <!-- @import url("<html:rewrite forward='style'/>"); -->
    </style>
  
    <link type="image/gif" href="<html:rewrite forward='icon'/>" rel="icon"/>
    <link href="<html:rewrite forward='favicon'/>" rel="SHORTCUT ICON"/>
    <html:base/>
  </head>
  <body>
 <table class="page">
  <tbody>
	<tr class="header">
        <td class="gutter">
          <span class="project">
            <a href="<bean:message key="link.geoserver"/>">
              <bean:message key="geoserver.logo"/>
            </a>
          </span>
          <span class="license">
            <a href="<bean:message key="link.license"/>">&copy;</a>
          </span>
		</td>
        <td style="width: 1em">
        </td>
		<td style="vertical-align: bottom; white-space: nowrap;">
          <span class="site">
<logic:notEmpty name="GeoServer" property="title">
              <bean:write name="GeoServer" property="title"/>
</logic:notEmpty>
<logic:empty name="GeoServer" property="title">
              <bean:message key="message.noTitle"/>
</logic:empty>            
          </span>			
		</td>	
		<td style="vertical-align: bottom; white-space: nowrap; text-align: right;">
			<span class="contact">
			   <a href="<bean:message key="label.credits.url"/>"><bean:message key="label.credits"/></a>
			</span>
<logic:notEmpty name="GeoServer" property="contactParty">
            <span class="contact">		
              <bean:message key="label.contact"/>: 	
              <html:link forward="contact">
                <bean:write name="GeoServer" property="contactParty"/>
              </html:link>
            </span>            
</logic:notEmpty>                
        </td>
	</tr>
	</table>
<!------------------------------------------------------------------------>

<h1> <bean:message key="srsList.title"/> </h1>

<!------------------------------------------------------------------------>
<!-- DISPLAY THE LIST OF SRS AND THEIR DEFINITIONS                       ->
<!------------------------------------------------------------------------>


<table border=1 cellspacing="0" cellpadding="2" width=95%>

  <tr><th>EPSG #</th><th><bean:message key="srsList.tableTitle"/></th></tr>
  
  
<!-- This iterator take idx from 0 to however many items there are in the list.
     I use the index to grab the data from the 2 input lists (see above).
     The it_value is ignored.
  -->
 <logic:iterate id="it_value" indexId="idx" name="srsInfoForm" property="srsIDList">
	<tr>
	     <td valign="top" align="right">
	          <bean:write property="<%= "srsIDList[" + idx + "]" %>" name="srsInfoForm"/>
	     </td>
	     <td class="greyedOut2">
	     	<pre><bean:write property="<%= "srsDefinitionList[" + idx + "]" %>" name="srsInfoForm"/></pre>
	     </td>
	</tr>
</logic:iterate>
</table>

</body>
</html:html>