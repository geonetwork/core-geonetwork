<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>

<logic:present name="GEOSERVER.USER" property="dataStoreConfig" scope="session">

<html:form action="/config/data/storeSubmit" onsubmit="return checkspaces(this)">
  <table class="info">	
	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.dataStore_id"/>">
			<bean:message key="label.dataStoreID"/>:
		</span>
	  </td>
	  <td class="datum">
		<bean:write name="dataDataStoresEditorForm" property="dataStoreId"/>
	  </td>
	</tr>	
	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.dataStore_enabled"/>">
          <bean:message key="label.enabled"/>:
        </span>
      </td>
	  <td class="datum">
		<html:checkbox property="enabled"/>
	  </td>
	</tr>	
	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.dataStore_nameSpace"/>">
			<bean:message key="label.namespace"/>:
		</span>
      </td>
	  <td class="datum">
		<html:select property="namespaceId">
			<html:options property="namespaces"/>
		</html:select>
	  </td>
	</tr>	
	<tr>
	  <td class="label">
		<span class="help" title="<bean:message key="help.dataStore_description"/>">
			<bean:message key="label.description"/>:
		</span>
      </td>
	  <td class="datum">
		<html:textarea property="description" cols="60" rows="2"/>
	  </td>
	</tr>
<logic:iterate id="param"
               indexId="ctr"
               name="dataDataStoresEditorForm"
               property="paramKeys">
	<logic:notEqual name="dataDataStoresEditorForm"
	                property='<%= "paramKey[" + ctr + "]"%>'
	                value="dbtype">
    	<tr>
		  	<td class="label">
		  		<!-- is this a required field -->
				<logic:equal name="dataDataStoresEditorForm"
							property='<%= "paramRequired[" + ctr + "]"%>'
							value="true">
					<font color="red">*</font>
				</logic:equal>
			
	        	<span class="help"
				      title="<bean:write name="dataDataStoresEditorForm"
				      property='<%= "paramHelp[" + ctr + "]" %>'/>">
					<bean:write name="dataDataStoresEditorForm" property='<%= "paramKey[" + ctr + "]"%>'/>:
				</span>
			</td>
			<td class="datum">
				<!-- if it is not a password field -->
				<logic:notEqual name="dataDataStoresEditorForm"
				    	        property='<%= "paramKey[" + ctr + "]"%>'
						        value="passwd">
				    <!--use the type information to figure out the type of widget to create -->
					<!-- check for boolean, if so provide a drop down of true false -->
					<logic:match name="dataDataStoresEditorForm"
						property='<%= "paramType[" + ctr + "]"%>'
						value="java.lang.Boolean">
						
						<html:select property='<%= "paramValues[" + ctr + "]"%>'>
							<html:option key="" value=""/>
							<html:option key="false" value="false"/>
							<html:option key="true" value="true"/>
						</html:select>
					</logic:match>
					<logic:notMatch name="dataDataStoresEditorForm"
									property='<%= "paramType[" + ctr + "]"%>'
									value="java.lang.Boolean">
						<!-- default to just a text box -->
						<logic:match name="dataDataStoresEditorForm" property='<%= "paramKey[" + ctr + "]"%>' value="url">
							<!-- if the value is empty, put an example string -->
							<logic:equal name="dataDataStoresEditorForm" property='<%= "paramValue[" + ctr + "]"%>' value="">
									 <html:text property='<%= "paramValues[" + ctr + "]"%>' size="60" value="file:data/example.extension"/>
							</logic:equal>
							<!-- if the value is not empty, use the regular value -->
							<logic:notEqual name="dataDataStoresEditorForm" property='<%= "paramValue[" + ctr + "]"%>' value="">
									 <html:text property='<%= "paramValues[" + ctr + "]"%>' size="60"/>
							</logic:notEqual>
						</logic:match>
						<logic:notMatch name="dataDataStoresEditorForm" property='<%= "paramKey[" + ctr + "]"%>' value="url">
									<html:text property='<%= "paramValues[" + ctr + "]"%>' size="60"/>
						</logic:notMatch>
					</logic:notMatch>
				</logic:notEqual>
				
				<logic:equal name="dataDataStoresEditorForm"
				   		     property='<%= "paramKey[" + ctr + "]"%>'
				             value="passwd">
          			<html:password property='<%= "paramValues[" + ctr + "]"%>' size="12"/>
				</logic:equal>
				
	  		</td>
		</tr>
	</logic:notEqual>
</logic:iterate>

	<tr>
	  <td class="label">&nbsp;</td>
	  <td class="datum">
		<html:submit>
			<bean:message key="label.submit"/>
		</html:submit>
		
		<html:reset>
			<bean:message key="label.reset"/>
		</html:reset>
	  </td>
	</tr>						
  </table>
</html:form>
</logic:present>
<br>
&nbsp;&nbsp;<font color="red">*</font> = <bean:message key="config.data.store.editor.requiredField"/>

