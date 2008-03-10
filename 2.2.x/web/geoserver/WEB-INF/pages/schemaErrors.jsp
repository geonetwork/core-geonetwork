<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<!-- This .jsp expects to have a "StylesEditorForm" bean called "dataStylesEditorForm" available. Inside the bean are: -->
<!--     String[] validationReport -->



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

<!-- Print everything out! -->
          
<h1><bean:message key="sldValididity.title"/></h1>
<a href=<bean:message key="sldValiditity.help.url"/>><bean:message key="sldValiditity.help.text"/></a>
<br>          
<pre>
<logic:iterate id="it_value"  name="dataStylesEditorForm" property="validationReport"><logic:match name="it_value" location="start" value=" "><font color=red><b></logic:match>
<bean:write name="it_value"/><logic:match name="it_value" location="start" value=" "></font></b></logic:match></logic:iterate>
</pre>         
          

<!------------------------------------------------------------------------>
</body>
</html:html>
