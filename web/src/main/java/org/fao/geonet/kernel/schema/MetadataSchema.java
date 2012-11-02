//==============================================================================
//===
//===   MetadataSchema
//===
//==============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.schema;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jeeves.utils.Log;
import jeeves.utils.Xml;

import org.fao.geonet.constants.Geonet;
import org.jdom.Element;
import org.jdom.Namespace;

//==============================================================================

public class MetadataSchema
{
	private static final String XSL_FILE_EXTENSION = ".xsl";
    private static final String SCH_FILE_EXTENSION = ".sch";
    private Map<String,List<String>> hmElements = new HashMap<String,List<String>>();
	private Map<String,List<List>> hmRestric  = new HashMap<String,List<List>>();
	private Map<String, MetadataType> hmTypes    = new HashMap<String, MetadataType>();
	private Map<String, List> hmSubs		 = new HashMap<String, List>();
	private Map<String, String> hmSubsLink = new HashMap<String, String>();
	private Map<String,Namespace> hmNameSpaces = new HashMap<String,Namespace>();
	private Map<String,Namespace> hmPrefixes = new HashMap<String,Namespace>();
	private String	schemaName;
	private String	schemaDir;
	private String	primeNS;
	private String[] schematronRules;
	private boolean canEdit = false;

	private static final String SCHEMATRON_RULE_FILE_PREFIX = "schematron-rules";

	private List<Element> rootAppInfoElements;

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	MetadataSchema() {
		schemaName = "UNKNOWN";
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public boolean canEdit()
	{
		return canEdit;
	}

	//---------------------------------------------------------------------------

	public void setCanEdit(boolean canEdit)
	{
		this.canEdit = canEdit;
	}

	//---------------------------------------------------------------------------

	public void setName(String inName)
	{
		schemaName = inName;
	}

	//---------------------------------------------------------------------------
	
	public String getName()
	{
		return schemaName;
	}

	/**
	 * Get schema directory
	 * 
	 * @return
	 */
	public String getSchemaDir() {
		return schemaDir;
	}

	/**
	 * Set schema directory
	 * 
	 * @param schemaDir
	 */
	public void setSchemaDir(String schemaDir) {
		this.schemaDir = schemaDir;
	}
	
	//---------------------------------------------------------------------------

	public void setPrimeNS(String theNS)
	{
		primeNS = theNS;
	}

	//---------------------------------------------------------------------------
	
	public String getPrimeNS()
	{
		return primeNS;
	}

	//---------------------------------------------------------------------------
	
	public MetadataType getTypeInfo(String type)
	{
		Logger.log();
		if (hmTypes.get(type) == null) return new MetadataType();
		else return hmTypes.get(type);
	}

	//---------------------------------------------------------------------------

	public String getElementType(String elem,String parent) throws Exception
	{
		// two cases here - if we have just one element (or a substitute) with 
		// this name then return its type

	  Logger.log();
		List<String> childType = hmElements.get(elem);
		if (childType == null) {
			// Check and see whether we can substitute another element from the
			// substitution link 
			String oldelem = elem;
			elem = hmSubsLink.get(elem);
	  	Logger.log();
			childType = hmElements.get(elem);
			if (childType == null) { 
			    Log.warning(Geonet.SCHEMA_MANAGER, "ERROR: Mismatch between schema and xml: No type for 'element' : "
			                    + oldelem + " with parent " + parent + ". Returning xs:string");
			    return "xs:string";
			}
		}
		if (childType.size() == 1) return childType.get(0);

		Logger.log();
		// OTHERWISE get the type by examining the parent:
		// for each parent with that name parent
		// 1. retrieve its mdt 
		List<String> exType = hmElements.get(parent);
		if (exType == null) return "xs:string";
        for (String type : exType) {
            // 2. search that mdt for the element names elem
            MetadataType mdt = getTypeInfo(type);
            for (int k = 0; k < mdt.getElementCount(); k++) {
                String elemTest = mdt.getElementAt(k);
                // 3. return the type name of that element
                if (elem.equals(elemTest)) {
                    return mdt.getElementTypeAt(k);
                }
            }
        }

		Logger.log();
		return null;
	}

	//---------------------------------------------------------------------------
	/** A simple type is a type that has no children and no attributes (but can
	  * have restrictions on its value)
	  */

	public boolean isSimpleElement(String elem,String parent) throws Exception
	{
		String type = getElementType(elem,parent);
        return type != null && !hmTypes.containsKey(type);
	}

	//---------------------------------------------------------------------------

    //---------------------------------------------------------------------------

	public ArrayList getElementValues(String elem,String parent) throws Exception
	{

		String type = getElementType(elem,parent);
		String restricName = elem;
		if (type != null) restricName = restricName+"+"+type;

		// two cases here - if we have just one element with this name 
		// then return its values
		List<List> childValues = hmRestric.get(restricName);
		if (childValues == null) return null;
		if (childValues.size() == 1) return (ArrayList)childValues.get(0);

		// OTHERWISE we don't know what to do so return the first one anyway! This
		// should not happen....
		Logger.log();
		return (ArrayList)childValues.get(0);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Package protected API methods
	//---
	//---------------------------------------------------------------------------

	void addElement(String name, String type, List alValues, List alSubs, String subLink)
	{
		// first just add the subs - because these are for global elements we 
		// never have a clash because global elements are all in the same scope
		// and are thus unique
		if (alSubs != null && alSubs.size() > 0) hmSubs.put(name,alSubs);
		if (subLink != null && subLink.length() > 0) hmSubsLink.put(name,subLink);

		List<String> exType = hmElements.get(name);

		// it's already there but the type has been added already
		if (exType != null && exType.contains(type)) return; 

		// it's already there but doesn't have this type 
		if (exType != null && !(exType.contains(type))) { 
			Logger.log();


		// it's not there so add a new list
		} else {
			hmElements.put(name, exType = new ArrayList<String>());
		}
		exType.add(type);

		String restricName = name;
		if (type != null) restricName = name+"+"+type;

		// it's already there
		List<List> exValues = hmRestric.get(restricName);
		if (exValues != null) {
			Logger.log();

		// it's not there so add a new list of lists
		} else {
			hmRestric .put(restricName, exValues = new ArrayList<List>());
		}
		exValues.add(alValues);
	}

	//---------------------------------------------------------------------------

	public void addType(String name, MetadataType mdt)
	{
		mdt.setName(name);
		hmTypes.put(name, mdt);
	}

	//---------------------------------------------------------------------------

	public void addNS(String targetNSPrefix, String targetNSUri)
	{

		Namespace ns = Namespace.getNamespace(targetNSPrefix, targetNSUri);
		hmNameSpaces.put(targetNSPrefix, ns);
		hmPrefixes.put(targetNSUri, ns);
	}

	//---------------------------------------------------------------------------

	public String getNS(String targetNSPrefix)
	{
		Namespace ns = hmNameSpaces.get(targetNSPrefix);
		if (ns != null) {
			return ns.getURI();
		} else {
			return null;
		}
	}

	//---------------------------------------------------------------------------

	public String getPrefix(String theNSUri)
	{
		Namespace ns = hmPrefixes.get(theNSUri);
		if (ns != null) {
			return ns.getPrefix();
		} else {
			return null;
		}
	}

	//---------------------------------------------------------------------------

	public List<Namespace> getSchemaNS()
	{
		return new ArrayList<Namespace>(hmPrefixes.values());
	}

	public void buildchematronRules(String basePath) {
        String schematronResourceDir = basePath + "WEB-INF" 
                + File.separator + "classes" + File.separator + "schematron" + File.separator ;
        String schemaSchematronDir = schemaDir + File.separator + "schematron";
        String schematronCompilationFile = schematronResourceDir + "iso_svrl_for_xslt2.xsl";
        
        if(Log.isDebugEnabled(Geonet.SCHEMA_MANAGER)) {
            Log.debug(Geonet.SCHEMA_MANAGER, "     Schematron compilation for schema " + schemaName);
            Log.debug(Geonet.SCHEMA_MANAGER, "          - compiling with " + schematronCompilationFile);
            Log.debug(Geonet.SCHEMA_MANAGER, "          - rules location is " + schemaSchematronDir);
        }
        
        File schematronFolder = new File(schemaSchematronDir);
        if (schematronFolder.exists()) {
            String rules[] = schematronFolder.list(new SchematronReportRulesSCHFilter());
            for (String rule : rules) {
                if(Log.isDebugEnabled(Geonet.SCHEMA_MANAGER)) {
                    Log.debug(Geonet.SCHEMA_MANAGER, "                - rule " + rule);
                }
                
                // Compile all schematron rules
                FileOutputStream schematronXsl;
                String schematronXslFilePath = schemaSchematronDir 
                        + File.separator + rule.replaceAll(SCH_FILE_EXTENSION, XSL_FILE_EXTENSION);
                try {
                    schematronXsl = new FileOutputStream(schematronXslFilePath);
                    Element schematronRule = Xml.loadFile(schemaSchematronDir 
                            + File.separator + rule);
                    Xml.transform(schematronRule, schematronCompilationFile, schematronXsl);
                } catch (FileNotFoundException e) {
                    Log.error(Geonet.SCHEMA_MANAGER, "     Schematron rule file not found " + schematronXslFilePath 
                            + ". Error is " + e.getMessage());
                } catch (Exception e) {
                    Log.error(Geonet.SCHEMA_MANAGER, "     Schematron rule compilation failed for " + schematronXslFilePath 
                            + ". Error is " + e.getMessage());
                }
            }
        }
    }
	
	/**
	 * Compile and register all schematron rules available for current schema.
	 * Schematron rules files are in schema schematron directory
	 * and start with "schematron-rules" prefix.
	 * 
	 * @return
	 */
	public void loadSchematronRules(String basePath) {
	    // Compile schema schematron rules
	    buildchematronRules(basePath);
	    
		String saSchemas[] = new File(schemaDir + File.separator + "schematron").list(new SchematronReportRulesFilter());
		setSchematronRules(saSchemas);
	}

	/**
	 * Schematron rules filename is like "schematron-rules-iso.xsl
	 * 
	 */
	private class SchematronReportRulesFilter implements FilenameFilter {
		public boolean accept(File directory, String filename) {
            return filename.startsWith(SCHEMATRON_RULE_FILE_PREFIX)
                    && filename.endsWith(XSL_FILE_EXTENSION);
        }
	}
	private class SchematronReportRulesSCHFilter implements FilenameFilter {
        public boolean accept(File directory, String filename) {
            return filename.startsWith(SCHEMATRON_RULE_FILE_PREFIX)
                    && filename.endsWith(SCH_FILE_EXTENSION);
        }
    }
	/**
	 * Return the list of schematron rules to applied for this schema
	 * @return
	 */
	public String[] getSchematronRules() {
		return schematronRules;
	}

	private void setSchematronRules(String[] schematronRules) {
		this.schematronRules = schematronRules;
	}

	// -- this info for profile detection methods

	public void setRootAppInfoElements(List<Element> rootAppInfoElements) {
		this.rootAppInfoElements = rootAppInfoElements;
	}
	
	public List<Element> getSchemaAppInfoElements() {
		return rootAppInfoElements;
	}
}

//==============================================================================

