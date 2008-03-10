<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<table border=0 width=100%>
	
	<html:form action="/config/wms/renderingSubmit" focus="svgRenderer">
	
	<tr><td align="right">
		<span class="help" title="<bean:message key="help.wms.svgRenderer"/>">
			<bean:message key="label.wms.svgRenderer"/>:
		</span>
		</td>
		<td colspan=2>
			<html:radio name="wmsRenderingForm" property="svgRenderer" value="Simple"><bean:message key="label.wms.svgSimple"/></html:radio>
		</td></tr>
	<tr><td align="right">
		</td>
		<td colspan=2>
			<html:radio name="wmsRenderingForm" property="svgRenderer" value="Batik"><bean:message key="label.wms.svgBatik"/></html:radio> 	
		</td></tr>
	<tr><td align="right">
		</td>
		<td colspan=2>
			<html:checkbox name="wmsRenderingForm" property="svgAntiAlias"><bean:message key="label.wms.svgAntiAlias"/></html:checkbox> 	
		</td></tr>
	<tr><td align="right">
		<span class="help" title="<bean:message key="help.wms.allowInterpolation"/>">
			<bean:message key="label.wms.allowInterpolation"/>:
		</span>
		</td>
		<td colspan=2>
			<html:radio name="wmsRenderingForm" property="allowInterpolation" value="Nearest"><bean:message key="label.wms.allowInterpolation.nearest"/></html:radio>
		</td></tr>
	<tr><td align="right">
		</td>
		<td colspan=2>
			<html:radio name="wmsRenderingForm" property="allowInterpolation" value="Bilinear"><bean:message key="label.wms.allowInterpolation.bilinear"/></html:radio>
		</td></tr>
	<tr><td align="right">
		</td>
		<td colspan=2>
			<html:radio name="wmsRenderingForm" property="allowInterpolation" value="Bicubic"><bean:message key="label.wms.allowInterpolation.bicubic"/></html:radio>
		</td></tr>

	<tr><td align="right">&nbsp;</td><td>
		<html:submit>
			<bean:message key="label.submit"/>
		</html:submit>
		
		<html:reset>
			<bean:message key="label.reset"/>
		</html:reset>
	</td></tr>
	</html:form>
	
</table>