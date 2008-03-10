<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<logic:notPresent name="org.apache.struts.action.MESSAGE" scope="application">
  <span class="error">
    ERROR:  Application resources not loaded -- check servlet container
    logs for error messages.
  </span>
</logic:notPresent>

</span>

<p>
<bean:message key="text.welcome1"/>
</p>

<!--p>
<bean:message key="text.welcome2"/>
</p-->

<!--p>
<bean:message key="text.welcome3"/>
</p-->

<p>
<bean:message key="text.welcome5"/>
</p>

<ul>
  <li>
    <a href="http://docs.codehaus.org/display/GEOSDOC/Documentation">
      Documentation
    </a>
  </li>
  <li>
    <a href="http://docs.codehaus.org/display/GEOS/Home">
      Wiki
    </a>
  </li>
  <li>
    <a href="http://jira.codehaus.org/secure/BrowseProject.jspa?id=10311">
      Task Tracker
    </a>
   </li>
   <li>
    <a href="http://www.moximedia.com:8080/imf-ows/imf.jsp?site=gs_users">
      User Map
    </a>
  </li>
</ul>

<p>
	<bean:message key="text.visitDemoPage"/>
</p>

	<a href="../../../wcs?service=WCS&request=GetCapabilities">WCS Capabilities</a>
	<br>
	<a href="../../../wfs?service=WFS&request=GetCapabilities">WFS Capabilities</a>
	<br>
	<a href="../../../wms?service=WMS&request=GetCapabilities">WMS Capabilities</a>
	<br><br>
	<a href="../../../srsHelp.do"><bean:message key="label.SRSList"/></a>
<br>
