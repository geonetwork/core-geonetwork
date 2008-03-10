<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>

<table class="info">
  <tbody>
    <tr>
      <td class="label"></td>
      <td class="datum">      
        <bean:message key="text.admin"/>
      </td>      
    </tr>  
    <tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.locks"/>">
			<bean:message key="label.locks"/>:
		</span>
      </td>
      <td class="datum">
        <bean:write name="DATA" property="lockCount"/>
      </td>      
    </tr>
    <tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.connections"/>">
			<bean:message key="label.connections"/>:
		</span>
      </td>
      <td class="datum">
        <bean:write name="DATA" property="connectionCount"/>
      </td>      
    </tr>
    <tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.memory"/>">
			<bean:message key="label.memory"/>:
		</span>
      </td>
      <td class="datum">
        <%= Runtime.getRuntime().freeMemory()/1024 %>K
      </td>      
    </tr>
    <tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.jvm"/>">
			<bean:message key="label.jvm"/>:
		</span>
      </td>
      <td class="datum">
        <%= System.getProperty("java.vendor") %> : <%= System.getProperty("java.version") %>
      </td>      
    </tr>
    <tr>
      <td class="label">
		<span class="help" title="<bean:message key="help.jai"/>">
			<bean:message key="label.jai"/>:
		</span>
      </td>
      <td class="datum">
        <%= ClassLoader.getSystemClassLoader().getResource("javax/media/jai/buildVersion") != null %>
      </td>      
    </tr>
    <tr>
    <td class="label">
		<span class="help" title="<bean:message key="help.jai.memory.capacity"/>">
			<bean:message key="label.jai.memory.capacity"/>:
		</span>
    </td>
    <td class="datum">
      <%= request.getAttribute("JAI_MEM_CAPACITY") %>K
    </td>      
	</tr>
    <tr>
    <td class="label">
		<span class="help" title="<bean:message key="help.jai.memory.used"/>">
			<bean:message key="label.jai.memory.used"/>:
		</span>
    </td>
    <td class="datum">
      <%= request.getAttribute("JAI_MEM_USED") %>K
    </td>      
	</tr>
    <tr>
    <td class="label">
		<span class="help" title="<bean:message key="help.jai.memory.threshold"/>">
			<bean:message key="label.jai.memory.threshold"/>:
		</span>
    </td>
    <td class="datum">
    	<%= request.getAttribute("JAI_MEM_THRESHOLD") %>%
    </td>      
	</tr>
    <tr>
    <td class="label">
		<span class="help" title="<bean:message key="help.jai.tile.threads"/>">
			<bean:message key="label.jai.tile.threads"/>:
		</span>
    </td>
    <td class="datum">
    	<%= request.getAttribute("JAI_TILE_THREADS") %>
    </td>      
	</tr>
    <tr>
    <td class="label">
		<span class="help" title="<bean:message key="help.jai.tile.priority"/>">
			<bean:message key="label.jai.tile.priority"/>:
		</span>
    </td>
    <td class="datum">
    	<%= request.getAttribute("JAI_TILE_PRIORITY") %> (1 - Min, 5 - Normal; 10 - Max)
    </td>      
	</tr>
  </tbody>
</table>