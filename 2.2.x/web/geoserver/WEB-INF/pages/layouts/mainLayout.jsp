<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/tags/struts-tiles" prefix="tiles" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<tiles:importAttribute scope="request"/>

<bean:define id="key"><tiles:getAsString name='key'/></bean:define>
<bean:define id="keyLabel"><tiles:getAsString name='key'/>.label</bean:define>
<bean:define id="keyTitle"><tiles:getAsString name='key'/>.title</bean:define>
<bean:define id="keyShort"><tiles:getAsString name='key'/>.short</bean:define>
<bean:define id="keyWords"><tiles:getAsString name='key'/>.words</bean:define>
<bean:define id="layer"><tiles:getAsString name='layer'/></bean:define>

<%@page import="org.vfny.geoserver.global.GeoServer"%>
<%@page import="org.vfny.geoserver.util.Requests"%>
<html:html locale="true" xhtml="true">
  <head>
    <title>
      <bean:message key="geoserver.logo"/>
      <bean:message key="<%= keyTitle %>"/>
    </title>
    <meta content="text/html; charset=iso-8859-1" http-equiv="content-type"/>
    <meta content="text/css" http-equiv="content-style-type"/>  
    <meta name="description"
          content="<bean:message key="<%= keyShort %>"/>">
    <meta name="keywords"
          content="(GeoServer) (GIS) (Geographic Information Systems) <bean:message key="<%= keyWords %>"/>"/>
    <meta name="author" content="Dave Blasby, Chris Holmes, Brent Owens, Justin Deoliveira, Jody Garnett, Richard Gould, David Zwiers"/>
    
    <tiles:insert attribute="javascript"/>
  	
  	<script language="JavaScript">
		<!--
		// This is used for URL parsing to check for any spaces that will cause invalid XML
		// Currently it is used in DataConfigDataStoresEditor.jsp
		function checkspaces(form)
		{
			for(var i=0; i<form.elements.length; i++)
			{
				if(form.elements[i].value.match("file:"))
				{
					var badchar = " ";	// look for the space character
					if (form.elements[i].value.indexOf(badchar) > -1) 
					{
						alert("Spaces are not allowed in the filename or path.");
						form.elements[i].focus();
						form.elements[i].select();
						return false;
					}
				}
			}// end for
			return true;
		}
	function onClean() {
	   var iFrameBody = document.getElementById("demoResponse").contentWindow.document.body;
	   var url = document.getElementById("url").value;
	   var body = document.getElementById("body").value;
	   
	   // we need to escape & and other simbols that the browser parsed for us, and make
	   // them &amp; again...
	   var div = document.createElement('div');
	   var text = document.createTextNode(body);
	   div.appendChild(text);
	   body = div.innerHTML;
	   
	   iFrameBody.innerHTML = "<form action='http://<%=request.getServerName()%>:<%=request.getServerPort()%><%=request.getContextPath()%>/TestWfsPost' method='POST'>\n" + 
	                          "<input type='hidden' name='url' value='" + url + "'/>\n" +
	                          "<textarea style='visibility:hidden' name='body' />" + body + "</textarea>\n" + 
	                          "<input type='hidden' value='submit'/>\n" +
	                          "</form>";
	   var form = iFrameBody.firstChild;
	   form.submit();
	}
	function loadResults() {
	   document.getElementById("demoResponse").src = "about:blank";
	   setTimeout('onClean()', 10);
	};
	function resize_iframe()
	{
	    if(!document.getElementById("demoResponse"))
	      return;
	
		var height=window.innerWidth;//Firefox
		if (document.body.clientHeight)
		{
			height=document.body.clientHeight;//IE
		}
		//resize the iframe according to the size of the
		//window (all these should be on the same line)
		document.getElementById("demoResponse").style.height=parseInt(height-
	 	findPos(document.getElementById("demoResponse"))[1] - 8)+"px";
	}
	
	function findPos(obj) {
		var curleft = curtop = 0;
		if (obj.offsetParent) {
			curleft = obj.offsetLeft
			curtop = obj.offsetTop
			while (obj = obj.offsetParent) {
				curleft += obj.offsetLeft
				curtop += obj.offsetTop
			}
		}
		return [curleft,curtop];
	}

	// this will resize the iframe every
	// time you change the size of the window.
	window.onresize=resize_iframe; 

		-->
	</script>
  	
    <style type="text/css">
      <!-- @import url("<html:rewrite forward='style'/>"); -->
    </style>
  
    <link type="image/gif" href="<html:rewrite forward='icon'/>" rel="icon"/>
    <link href="<html:rewrite forward='favicon'/>" rel="SHORTCUT ICON"/>
    <% 
	    GeoServer gs = (GeoServer) getServletContext().getAttribute(GeoServer.WEB_CONTAINER_KEY);
        String baseUrl = Requests.getBaseJspUrl(request, gs);
    %>
     <base href="<%=baseUrl%>"/> 
    <!-- <html:base/> -->
  </head>
  <body>
  
<!-- Security Check (for non application layers -->
<logic:notEqual name="layer" value="application">  
  <logic:notPresent name="GEOSERVER.USER">
    <logic:redirect forward="login" />
  </logic:notPresent>
</logic:notEqual>
    
<table class="page" height="100%">
  <tbody>
	<tr class="header" height="1%">
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
	
    <tr>
      <td class="sidebar">
        <tiles:insert attribute="status"/>	
        <tiles:insert attribute="configActions"/>
        <tiles:insert attribute="actionator"/>
        <tiles:insert attribute="messages"/>
      </td>
      <td style="width: 1em">
      </td>      
      <td style="vertical-align: top;"
          rowspan="1" colspan="2">
            
        <table class="main">
          <tbody>
            <tr class="bar">
              <td class="locator">
                <tiles:insert attribute="locator"/>
              </td>
              <td class="loginStatus">
                <span class="loginStatus">                  
<logic:present name="GEOSERVER.USER">
                    <html:link forward="logout">
				      <bean:message key="label.logout"/>
			        </html:link>
</logic:present>                  
<logic:notPresent name="GEOSERVER.USER">
                    <html:link forward="login">
                      <bean:message key="label.login"/>
                    </html:link>
</logic:notPresent>                  
                </span>
              </td>
            </tr>
          	<tr>
              <td class="<tiles:getAsString name='layer'/>"
                  rowspan="1" colspan="2">
                <table class="title">
                  <tbody>
                    <tr>
                      <td class="menu">
                        <tiles:insert attribute="menuator"/>
                      </td>
                      <td class="title">
                        <h1 class="title">
                          <bean:message key="<%= keyTitle %>"/>
                        </h1>
                        <p class="abstract">
                          <bean:message key="<%= keyShort %>"/>
                        </p>
                      </td>
                      <td class="icon"></td>
                    </tr>    
                  </tbody>
                </table>
                <tiles:insert attribute="body"/>
              </td>
	        </tr>
          </tbody>
	    </table>
      </td>
	</tr>
  </tbody>
</table>

</body>
</html:html>