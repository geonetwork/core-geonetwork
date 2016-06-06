//==============================================================================
//===
//===   SchemaLoader
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

import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.repository.SchematronCriteriaGroupRepository;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Resolver;
import org.fao.geonet.utils.ResolverWrapper;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

//==============================================================================

public class SchemaLoader {
    private Element elFirst = null;
    private Map<String, String> hmElements = new HashMap<String, String>();
    private Map<String, ComplexTypeEntry> hmTypes = new HashMap<String, ComplexTypeEntry>();
    private Map<String, List<AttributeEntry>> hmAttrGrp = new HashMap<String, List<AttributeEntry>>();
    private Map<String, AttributeGroupEntry> hmAttrGpEn = new HashMap<String, AttributeGroupEntry>();
    private Map<String, String> hmAbsElems = new HashMap<String, String>();
    private Map<String, ArrayList<ElementEntry>> hmSubsGrp = new HashMap<String, ArrayList<ElementEntry>>();
    private Map<String, String> hmSubsLink = new HashMap<String, String>();
    private Map<String, List<String>> hmSubsNames = new HashMap<String, List<String>>();
    private Map<String, AttributeEntry> hmAttribs = new HashMap<String, AttributeEntry>();
    private Map<String, AttributeEntry> hmAllAttrs = new HashMap<String, AttributeEntry>();
    private Map<String, GroupEntry> hmGroups = new HashMap<String, GroupEntry>();

    private SchemaSubstitutions ssOverRides;

    private String targetNS;
    private String targetNSPrefix;

    /**
     * Restrictions for simple types (element restriction)
     */
    private Map<String, List<String>> hmElemRestr = new HashMap<String, List<String>>();

    /**
     * Restrictions for simple types (type restriction)
     */
    private Map<String, List<String>> hmTypeRestr = new HashMap<String, List<String>>();

    private Map<String, List<String>> hmMemberTypeRestr = new HashMap<String, List<String>>();

    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    public SchemaLoader() {
    }

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    public MetadataSchema load(Path xmlSchemaFile, Path xmlSubstitutionsFile, SchematronRepository schemaRepo,
                               SchematronCriteriaGroupRepository criteriaGroupRepository) throws Exception {
        ssOverRides = new SchemaSubstitutions(xmlSubstitutionsFile);

        if (!Files.exists(xmlSchemaFile))
            return new MetadataSchema(schemaRepo, criteriaGroupRepository);

        //--- PHASE 1 : pre-processing
        //---
        //--- the xml file is parsed and simplified. Xml schema subtrees are
        //--- wrapped in some classes

        List<ElementInfo> alElementFiles = loadFile(xmlSchemaFile, new HashSet<Path>());

        parseElements(alElementFiles);

        //--- PHASE 2 : resolve abstract elements

        for (String o : hmSubsGrp.keySet()) {

            List<ElementEntry> elements = hmSubsGrp.get(o);
            List<String> subsNames = new ArrayList<String>();
            hmSubsNames.put(o, subsNames);
            for (ElementEntry ee : elements) {
                if (ee.type == null) {
                    ee.type = hmAbsElems.get(o);

                    if (ee.type == null) {
                        // If we don't have a type then insert with null and fix
                        // when all elements have been added to hmElements
                        Logger.log();
                    }
                }

                hmElements.put(ee.name, ee.type);
                subsNames.add(ee.name);
            }
        }

        //--- PHASE 3 : get appinfo, add namespaces and elements

        MetadataSchema mds = new MetadataSchema(schemaRepo, criteriaGroupRepository);
        mds.setPrimeNS(elFirst.getAttributeValue("targetNamespace"));


        @SuppressWarnings("unchecked")
        List<Element> annotation = elFirst.getChildren("annotation", Geonet.Namespaces.XSD);

        if (annotation != null) {
            List<Element> allAppInfo = new ArrayList<Element>();

            for (Element currAnnotation : annotation) {
                @SuppressWarnings("unchecked")
                List<Element> currAppInfo = currAnnotation.getChildren("appinfo", Geonet.Namespaces.XSD);

                if (currAppInfo != null) {
                    allAppInfo.addAll(currAppInfo);
                }
            }
            mds.setRootAppInfoElements(allAppInfo);
        }

        for (ElementInfo ei : alElementFiles) {
            mds.addNS(ei.targetNSPrefix, ei.targetNS);
        }

        for (String elem : hmElements.keySet()) {
            String type = hmElements.get(elem);

            // fix any null types by back tracking through substitution links
            // until we get a concrete type or die trying :-)
            if (type == null) {
                Logger.log();
                type = recurseOnSubstitutionLinks(elem);
                if (type == null) {
                    Log.warning(Geonet.SCHEMA_MANAGER, "WARNING: Cannot find type for " + elem + ": assuming string");
                    type = "string";
                } else {
                    Logger.log();
                }
            }

            List<String> elemRestr = hmElemRestr.get(elem);
            List<String> typeRestr = hmTypeRestr.get(type);

            if (elemRestr == null) {
                elemRestr = new ArrayList<String>();
            }

            if (typeRestr != null) {
                elemRestr.addAll(typeRestr);
            }

            List<String> elemSubs = hmSubsNames.get(elem);
            if (elemSubs == null) {
                elemSubs = new ArrayList<String>();
            }
            String elemSubsLink = hmSubsLink.get(elem);
            if (elemSubsLink == null) {
                elemSubsLink = "";
            }
            mds.addElement(elem, type, elemRestr, elemSubs, elemSubsLink);
        }

        //--- PHASE 4 : resolve references in attribute groups

        for (AttributeGroupEntry age : hmAttrGpEn.values()) {
            for (int k = 0; k < age.alAttrs.size(); k++) {
                AttributeEntry attr = age.alAttrs.get(k);
                if (attr.name != null) {
                    hmAllAttrs.put(attr.name, attr);
                }
            }
            ArrayList<AttributeEntry> attrs = resolveNestedAttributeGroups(age);
            hmAttrGrp.put(age.name, attrs);
        }

        //--- PHASE 5 : check attributes to see whether they should be qualified

        Map<String, AttributeEntry> hmAttrChk = new HashMap<String, AttributeEntry>();
        for (AttributeEntry attr : hmAllAttrs.values()) {
            AttributeEntry attrPrev = hmAttrChk.get(attr.unqualifiedName);
            if (attrPrev != null) {
                attr.form = "qualified";
                attrPrev.form = "qualified";
            } else {
                hmAttrChk.put(attr.unqualifiedName, attr);
            }
        }

        //--- PHASE 6 : post-processing
        //---
        //--- resolve type inheritance and elements

        List<ComplexTypeEntry> alTypes = new ArrayList<ComplexTypeEntry>(hmTypes.values());
        for (ListIterator<ComplexTypeEntry> i = alTypes.listIterator(); i.hasNext(); ) {
            ComplexTypeEntry cte = i.next();

            MetadataType mdt = new MetadataType();

            mdt.setOrType(cte.isOrType);

            //--- resolve element and attribute inheritance from complexContent

            if (cte.complexContent != null) {

                if (cte.complexContent.base != null) {

                    //--- add elements
                    cte.alElements = resolveInheritance(cte);

                    //--- add attribs (if any)
                    List<AttributeEntry> complexContentAttribs = resolveAttributeInheritance(cte);
                    for (AttributeEntry ae : complexContentAttribs) {
                        mdt.addAttribute(buildMetadataAttrib(ae));
                    }

                    //--- if the base type is an ortype then we need to make this an
                    //--- or type as well
                    ComplexTypeEntry baseCTE = hmTypes.get(cte.complexContent.base);
                    if (baseCTE.isOrType) {
                        cte.isOrType = true;
                        mdt.setOrType(true);
                        Logger.log();
                    }
                } else {
                    throw new IllegalArgumentException("base not defined for complexContent in " + cte.name);
                }

                //--- resolve attribute inheritance from simpleContent

            } else if (cte.simpleContent != null) {
                List<AttributeEntry> simpleContentAttribs = resolveAttributeInheritanceFromSimpleContent(cte);
                for (AttributeEntry ae : simpleContentAttribs) {
                    mdt.addAttribute(buildMetadataAttrib(ae));
                }

                //--- otherwise process the attributes and attribute groups for this type

            } else {
                for (int j = 0; j < cte.alAttribs.size(); j++) {
                    AttributeEntry ae = cte.alAttribs.get(j);
                    mdt.addAttribute(buildMetadataAttrib(ae));
                }
                for (int k = 0; k < cte.alAttribGroups.size(); k++) {
                    String attribGroup = cte.alAttribGroups.get(k);
                    List<AttributeEntry> al = hmAttrGrp.get(attribGroup);

                    if (al == null)
                        throw new IllegalArgumentException("Attribute group not found : " + attribGroup);

                    for (AttributeEntry ae : al) {
                        mdt.addAttribute(buildMetadataAttrib(ae));
                    }
                }
            }

            //--- now add the elements belonging to this complex type to the mdt

            for (int j = 0; j < cte.alElements.size(); j++) {
                ElementEntry ee = (ElementEntry) cte.alElements.get(j);

// Three situations:
// 1. element is a container element - group, choice or sequence - so recurse
// and get elements from any containers nested inside this container -
// we generate a name to use from the cte.name and element position

                if (ee.groupElem || ee.choiceElem || ee.sequenceElem) {
                    String baseName = cte.name;
                    String extension;
                    ArrayList<ElementEntry> elements;
                    if (ee.choiceElem) {
                        extension = Edit.RootChild.CHOICE;
                        elements = ee.alContainerElems;
                    } else if (ee.groupElem) {
                        extension = Edit.RootChild.GROUP;
                        GroupEntry group = hmGroups.get(ee.ref);
                        elements = group.alElements;
                    } else {
                        extension = Edit.RootChild.SEQUENCE;
                        elements = ee.alContainerElems;
                    }
                    String type = ee.name = baseName + extension + (Integer) j;
                    ArrayList<ComplexTypeEntry> newCtes = createTypeAndResolveNestedContainers(
                        mds, elements,
                        baseName, extension, j);
                    if (newCtes.size() != 0) {
                        for (ComplexTypeEntry newCte : newCtes) {
                            i.add(newCte);
                            i.previous();
                        }
                    }
                    mds.addElement(ee.name, type, new ArrayList<String>(), new ArrayList<String>(), "");
                    mdt.addElementWithType(ee.name, type, ee.min, ee.max);

// 2. element is a reference to a global element so check if abstract or
//    if the type needs to be turned into a choice ie. it has one element
//    which is the head of a substitution group or a new choice type
//    is created for the element or just add it if none of
//    the above
                } else if (ee.ref != null) {
                    boolean choiceType = (cte.alElements.size() == 1);
                    handleRefElement(j, cte.name, choiceType, ee, mdt, mds);


// 3. element is a local element so get type or process local complex/simpleType//    and add to the ListIterator if complex
                } else if (ee.name != null) {
                    ComplexTypeEntry newCte = handleLocalElement(j, cte.name, ee, mdt, mds);
                    if (newCte != null) {
                        i.add(newCte);
                        i.previous();
                    }

                } else {
                    throw new IllegalArgumentException("Unknown element type at position " + j + " in complexType " + cte.name);
                }
            }
            mds.addType(cte.name, mdt);
        }


        // now set the schema to be editable and return
        mds.setCanEdit(true);
        return mds;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Recurse on substitution links until we get a type that we can use
    //---
    //---------------------------------------------------------------------------
    private String recurseOnSubstitutionLinks(String elemName) {
        String elemLinkName = hmSubsLink.get(elemName);
        if (elemLinkName != null) {
            String elemLinkType = hmElements.get(elemLinkName);
            if (elemLinkType != null) return elemLinkType; // found concrete type!
            else return recurseOnSubstitutionLinks(elemLinkName); // keep trying
        }
        return null; // Cannot find a type so return null
    }

    //---------------------------------------------------------------------------
    //---
    //--- Build a local element into the MetadataType and Schema
    //---
    //---------------------------------------------------------------------------
    private ComplexTypeEntry handleLocalElement(Integer elementNr, String baseName, ElementEntry ee, MetadataType mdt, MetadataSchema mds) {

        ComplexTypeEntry cteInt = null;
        ArrayList<String> elemRestr = new ArrayList<String>();

        if (ee.type == null) {
            if (ee.complexType != null) {
                cteInt = ee.complexType;
                ee.type = cteInt.name = ee.name + "HSI" + elementNr +
                    getUnqualifiedName(baseName);
            } else if (ee.simpleType != null) {
                ee.type = "string";
                if (ee.simpleType.alEnum != null) // add enumerations if any
                    elemRestr.addAll(ee.simpleType.alEnum);
            } else {
                Log.warning(Geonet.SCHEMA_MANAGER, "WARNING: Could not find type for " + ee.name + " - assuming string");
                ee.type = "string";
            }
        }

        mds.addElement(ee.name, ee.type, elemRestr, new ArrayList<String>(), "");
        mdt.addElementWithType(ee.name, ee.type, ee.min, ee.max);

        return (cteInt);

    }

    //---------------------------------------------------------------------------
    //---
    //--- Return list of substitutes if we want to override those derived
    //--- from the schema XSDs - this is schema dependent and defined in
    //--- the schema-substitutes files and is used for elements such as
    //--- gco:CharacterString in the iso19139 schema
    //---
    //--- returns null if there are no user defined substitutes,
    //---     OR  an empty list if removal of all schema substitutes is required
    //---     OR  a list of ElementEntry objects to use as substitutes
    //---
    //---------------------------------------------------------------------------
    private ArrayList<ElementEntry> getOverRideSubstitutes(String elementName) {

        ArrayList<ElementEntry> subs = hmSubsGrp.get(elementName);
        List<String> ssOs = ssOverRides.getSubstitutes(elementName);
        if (ssOs != null && subs != null) {
            ArrayList<ElementEntry> results = new ArrayList<ElementEntry>();
            List<String> validSubs = hmSubsNames.get(elementName);
            for (String altSub : ssOs) {
                if (validSubs != null && !validSubs.contains(altSub)) {
                    Log.warning(Geonet.SCHEMA_MANAGER, "WARNING: schema-substitutions.xml specified " + altSub + " for element " + elementName + " but the schema does not define this as a valid substitute");
                }
                for (ElementEntry ee : subs) {
                    if (ee.name.equals(altSub)) {
                        results.add(ee);
                    }
                }
            }
            if (results.size() == 0 && validSubs != null) {
                Log.warning(Geonet.SCHEMA_MANAGER, "WARNING: schema-substitutions.xml has wiped out XSD substitution list for " + elementName);
            }
            return results;
        }
        return null;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Build a reference to a global element into the MetadataType and Schema
    //---
    //---------------------------------------------------------------------------
    private void handleRefElement(Integer elementNr, String baseName, boolean choiceType, ElementEntry ee, MetadataType mdt, MetadataSchema mds) {

        String type = hmElements.get(ee.ref);
        boolean isAbstract = hmAbsElems.containsKey(ee.ref);

        // If we have user specified substitutions then use them otherwise
        // use those from the schema
        boolean doSubs = true;
        ArrayList<ElementEntry> al = getOverRideSubstitutes(ee.ref);
        if (al == null) al = hmSubsGrp.get(ee.ref);
        else doSubs = false;

        if ((al != null && al.size() > 0) || isAbstract) {
            if (choiceType) {
                // The complex type has only one element then make it a choice type if
                // there are concrete elements in the substitution group
                int elementsAdded = assembleChoiceElements(mdt, al, doSubs);
                if (!isAbstract && doSubs) {
                    /*
					 * Control of substitution lists is via the schema-substitutions.xml
					 * file because some profiles do not mandate substitutions of this
					 * kind eg. wmo
					 *
					if (elementsAdded == 1 &&
							(getPrefix(mdt.getElementAt(0)).equals(getProfile(schemaId))) &&
											schemaId.startsWith("iso19139")) {
						Logger.log("Sticking with "+mdt.toString()+" for "+ee.ref);
					} else {
					 *
					 */
                    mdt.addRefElementWithType(ee.ref, type, ee.min, ee.max);
                    elementsAdded++;
					/*}*/
                }
                mdt.setOrType(elementsAdded > 1);
            } else {
                // The complex type has real elements and/or attributes so make a new
                // choice element with type and replace this element with it
                MetadataType mdtc = new MetadataType();
                Integer elementsAdded = assembleChoiceElements(mdtc, al, doSubs);
                if (!isAbstract && doSubs) {
                    mdtc.addRefElementWithType(ee.ref, ee.type, ee.min, ee.max);
                    elementsAdded++;
                }
                mdtc.setOrType(elementsAdded > 1);
                type = ee.ref + Edit.RootChild.CHOICE + elementNr;
                String name = type;
                mds.addType(type, mdtc);
                mds.addElement(name, type, new ArrayList<String>(), new ArrayList<String>(), "");
                mdt.addElementWithType(name, type, ee.min, ee.max);
            }
        } else if (!isAbstract) {
            mdt.addRefElementWithType(ee.ref, type, ee.min, ee.max);
        } else {
            Log.warning(Geonet.SCHEMA_MANAGER, "WARNING: element " + ee.ref + " from " + baseName + " has fallen through the logic (abstract: " + isAbstract + ") - ignoring");
        }
    }

    //---------------------------------------------------------------------------
    //---
    //--- Recurse on attributeGroups to build a list of AttributeEntry objects
    //---
    //---------------------------------------------------------------------------
    private ArrayList<AttributeEntry> resolveNestedAttributeGroups(AttributeGroupEntry age) {
        ArrayList<AttributeEntry> attrs = new ArrayList<AttributeEntry>();

        if (age.alAttrGrps.size() > 0) {
            for (int i = 0; i < age.alAttrGrps.size(); i++) {
                AttributeGroupEntry ageInternal =
                    age.alAttrGrps.get(i);
                AttributeGroupEntry ageRef =
                    hmAttrGpEn.get(ageInternal.ref);
                if (ageRef == null)
                    throw new IllegalArgumentException
                        ("ERROR: cannot find attributeGroup with ref " + ageInternal.ref);
                attrs.addAll(resolveNestedAttributeGroups(ageRef));
            }
        }
        attrs.addAll(age.alAttrs);
        return attrs;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Descend recursively to deal with nested containers
    //---
    //---------------------------------------------------------------------------
    private ArrayList<ComplexTypeEntry> createTypeAndResolveNestedContainers(
        MetadataSchema mds, ArrayList<ElementEntry> al, String baseName,
        String extension, Integer baseNr) {

        ArrayList<ComplexTypeEntry> complexTypes = new ArrayList<ComplexTypeEntry>();

        Integer oldBaseNr = baseNr;
        if (al == null) return complexTypes;
        MetadataType mdt = new MetadataType();
        if (extension.contains(Edit.RootChild.CHOICE)) mdt.setOrType(true);
        for (int k = 0; k < al.size(); k++) {
            ElementEntry ee = al.get(k);
            baseNr++;

            // CHOICE
            if (ee.choiceElem) {
                String newExtension = Edit.RootChild.CHOICE;
                ArrayList<ComplexTypeEntry> newCtes = createTypeAndResolveNestedContainers(mds, ee.alContainerElems, baseName, newExtension, baseNr);
                if (newCtes.size() > 0) complexTypes.addAll(newCtes);
                ee.name = ee.type = baseName + newExtension + baseNr;
                mds.addElement(ee.name, ee.type, new ArrayList<String>(), new ArrayList<String>(), "");
                mdt.addElementWithType(ee.name, ee.type, ee.min, ee.max);

                // GROUP
            } else if (ee.groupElem) {
                String newExtension = Edit.RootChild.GROUP;
                if (ee.ref != null) {
                    GroupEntry group = hmGroups.get(ee.ref);
                    ArrayList<ElementEntry> alGroupElements = group.alElements;
                    ArrayList<ComplexTypeEntry> newCtes = createTypeAndResolveNestedContainers(mds, alGroupElements, baseName, newExtension, baseNr);
                    if (newCtes.size() > 0) complexTypes.addAll(newCtes);
                    ee.name = ee.type = baseName + newExtension + baseNr;
                    mds.addElement(ee.name, ee.type, new ArrayList<String>(), new ArrayList<String>(), "");
                    mdt.addElementWithType(ee.name, ee.type, ee.min, ee.max);
                } else {
                    Log.warning(Geonet.SCHEMA_MANAGER, "WARNING: group element ref is NULL in " + baseName + extension + baseNr);
                }

                // SEQUENCE
            } else if (ee.sequenceElem) {
                String newExtension = Edit.RootChild.SEQUENCE;
                ArrayList<ComplexTypeEntry> newCtes = createTypeAndResolveNestedContainers(mds, ee.alContainerElems, baseName, newExtension, baseNr);
                if (newCtes.size() > 0) complexTypes.addAll(newCtes);
                ee.name = ee.type = baseName + newExtension + baseNr;
                mds.addElement(ee.name, ee.type, new ArrayList<String>(), new ArrayList<String>(), "");
                mdt.addElementWithType(ee.name, ee.type, ee.min, ee.max);

                // ELEMENT
            } else {
                if (ee.name != null) {
                    ComplexTypeEntry newCte = handleLocalElement(k, baseName, ee, mdt, mds);
                    if (newCte != null) complexTypes.add(newCte);
                } else {
                    handleRefElement(k, baseName, false, ee, mdt, mds);
                }
            }
        }
        mds.addType(baseName + extension + oldBaseNr, mdt);
        return complexTypes;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Descend recursively to deal with abstract elements
    //---
    //---------------------------------------------------------------------------
    private int assembleChoiceElements(MetadataType mdt, ArrayList<ElementEntry> al, boolean doSubs) {

        int number = 0;
        if (al == null) return number;
        for (ElementEntry ee : al) {
            if (ee.abstrElem) {
                Integer numberRecursed = assembleChoiceElements(mdt, hmSubsGrp.get(ee.name), doSubs);
                number = number + numberRecursed;
            } else {
                number++;
                mdt.addElementWithType(ee.name, ee.type, ee.min, ee.max);
                // Also add any elements that substitute for this one so that we can
                // complete the list of choices if required
                if (doSubs) {
                    ArrayList<ElementEntry> elemSubs = hmSubsGrp.get(ee.name);
                    if (elemSubs != null) {
                        for (ElementEntry eeSub : elemSubs) {
                            mdt.addElementWithType(eeSub.name, eeSub.type, eeSub.min, eeSub.max);
                            number++;
                        }
                    }
                }
            }
        }
        return number;
    }

    //---------------------------------------------------------------------------
    //---
    //--- PHASE 1 : Schema loading
    //---
    //---------------------------------------------------------------------------

    /**
     * Loads the xml-schema file, removes annotations and resolve imports/includes
     */

    private List<ElementInfo> loadFile(Path xmlSchemaFile, HashSet<Path> loadedFiles) throws Exception {
        loadedFiles.add(xmlSchemaFile.toAbsolutePath().normalize());

        Path path = xmlSchemaFile.getParent();

        //--- load xml-schema
        Log.debug(Geonet.SCHEMA_MANAGER, "Loading schema " + xmlSchemaFile);

        Element elRoot = Xml.loadFile(xmlSchemaFile);
        if (elFirst == null) elFirst = elRoot;

        // change target namespace
        String oldtargetNS = targetNS;
        String oldtargetNSPrefix = targetNSPrefix;
        targetNS = elRoot.getAttributeValue("targetNamespace");
        targetNSPrefix = null;


        if (targetNS != null) {
            for (Object o : elRoot.getAdditionalNamespaces()) {
                Namespace ns = (Namespace) o;
                if (targetNS.equals(ns.getURI())) {
                    targetNSPrefix = ns.getPrefix();
                    break;
                }
            }
            if ("".equals(targetNSPrefix)) targetNSPrefix = null;
        }
        // This is a bug in jdom - seems that if the namespace prefix is xml: and
        // namespace is as shown in the if statement then getAdditionalNamespaces
        // doesn't return the namespaces and we can't get a prefix - this fix gets
        // around that bug
        if ((xmlSchemaFile.toString().contains("xml.xsd") ||
            xmlSchemaFile.toString().contains("xml-mod.xsd")) &&
            targetNS.equals("http://www.w3.org/XML/1998/namespace")) {
            targetNSPrefix = "xml";
        }

        @SuppressWarnings("unchecked")
        List<Element> children = elRoot.getChildren();

        //--- collect elements into an array because we have to add elements
        //--- when we encounter the "import" element

        List<ElementInfo> alElementFiles = new ArrayList<ElementInfo>();

        for (Element elChild : children) {
            String name = elChild.getName();

            if (name.equals("annotation")) {

            } else if (name.equals("import") || name.equals("include")) {
                String schemaLoc = elChild.getAttributeValue("schemaLocation");

                //--- we must try to resolve imports from the web using the
                //--- oasis catalog
                Path scFile;
                if (schemaLoc.startsWith("http:")) {
                    Resolver resolver = ResolverWrapper.getInstance();
                    final String scPath = resolver.getXmlResolver().resolveURI(schemaLoc);

                    if (Log.isDebugEnabled(Geonet.SCHEMA_MANAGER)) {
                        Log.debug(Geonet.SCHEMA_MANAGER,
                            "Cats: " + Arrays.toString(resolver.getXmlResolver().getCatalogList()) +
                                " Resolved " + schemaLoc + " " + scPath);
                    }

                    if (scPath == null) {
                        Log.warning(Geonet.SCHEMA_MANAGER,
                            "Cannot resolve " + schemaLoc + ": will append last component to current path " +
                                "(not sure it will help though!)");
                        int lastSlash = schemaLoc.lastIndexOf('/');
                        scFile = path.resolve(schemaLoc.substring(lastSlash + 1));
                    } else {
                        scFile = IO.toPath(new URI(scPath));
                    }
                } else {
                    scFile = path.resolve(schemaLoc);
                }
                if (!loadedFiles.contains(scFile.toAbsolutePath().normalize())) {
                    alElementFiles.addAll(loadFile(scFile, loadedFiles));
                }
            } else {
                alElementFiles.add(new ElementInfo(elChild, xmlSchemaFile, targetNS, targetNSPrefix));
            }
        }
        // restore target namespace
        targetNS = oldtargetNS;
        targetNSPrefix = oldtargetNSPrefix;

        return alElementFiles;
    }

    //---------------------------------------------------------------------------
    //---
    //--- PHASE 2 : Parse elements building intermediate data structures
    //---
    //---------------------------------------------------------------------------

    private void parseElements(List<ElementInfo> alElementFiles) throws JDOMException {
        //--- clear some structures

        hmElements.clear();
        hmTypes.clear();
        hmAttrGrp.clear();
        hmAbsElems.clear();
        hmSubsGrp.clear();
        hmSubsLink.clear();
        hmElemRestr.clear();
        hmTypeRestr.clear();
        hmAttribs.clear();
        hmAllAttrs.clear();
        hmGroups.clear();

        for (ElementInfo ei : alElementFiles) {
            Element elChild = ei.element;
            String name = elChild.getName();

            if (name.equals("element")) {
                buildGlobalElement(ei);
            } else if (name.equals("complexType")) {
                buildComplexType(ei);
            } else if (name.equals("simpleType")) {
                buildSimpleType(ei);
            } else if (name.equals("attribute")) {
                buildGlobalAttrib(ei);
            } else if (name.equals("group")) {
                buildGlobalGroup(ei);
            } else if (name.equals("attributeGroup")) {
                buildGlobalAttributeGroup(ei);
            } else {
                Logger.log();
            }
        }
    }

    //---------------------------------------------------------------------------

    private void buildGlobalElement(ElementInfo ei) {
        ElementEntry ee = new ElementEntry(ei);

        if (ee.name == null)
            throw new IllegalArgumentException("Name is null for element : " + ee.name);


        if (ee.substGroup != null) {
            ArrayList<ElementEntry> al = hmSubsGrp.get(ee.substGroup);

            if (al == null) {
                al = new ArrayList<ElementEntry>();
                hmSubsGrp.put(ee.substGroup, al);
            }
            al.add(ee);

            String existingSubstitionGroup = hmSubsLink.get(ee.name);
            if (existingSubstitionGroup != null
                && !ee.substGroup.equals(existingSubstitionGroup)) {
                throw new IllegalArgumentException("Substitution link collision" +
                    " for " + ee.name +
                    " link to " + existingSubstitionGroup +
                    ". Already bound to " + ee.substGroup);
            } else {
                hmSubsLink.put(ee.name, ee.substGroup);
            }
        }
        if (ee.abstrElem) {

            String existingType = hmAbsElems.get(ee.name);
            if (existingType != null && !ee.type.equals(existingType)) {
                throw new IllegalArgumentException("Namespace collision" +
                    " for " + ee.name +
                    " type " + existingType +
                    ". Already bound to " + ee.type);
            } else {
                hmAbsElems.put(ee.name, ee.type);
            }
            return;
        }
        if (ee.complexType != null) {
            String type = ee.name + "HSI";
            ee.complexType.name = type;
            ee.type = type;
            if (hmElements.containsKey(ee.name))
                throw new IllegalArgumentException("Namespace collision for : " + ee.name);

            hmElements.put(ee.name, type);
            hmTypes.put(type, ee.complexType);

        } else if (ee.simpleType != null) {
            if (hmElements.containsKey(ee.name))
                throw new IllegalArgumentException("Namespace collision for : " + ee.name);
            ee.type = "string";
            hmElements.put(ee.name, ee.type);
            hmElemRestr.put(ee.name, ee.simpleType.alEnum);

        } else {
            if (ee.type == null && ee.substGroup == null) {
                Log.warning(Geonet.SCHEMA_MANAGER, "WARNING: " + ee.name + " is a global element without a type - assuming a string");
                ee.type = "string";
            }
            hmElements.put(ee.name, ee.type);

        }
        if (ee.name.contains("SensorML")) {
            Logger.log();
        }
    }

    //---------------------------------------------------------------------------

    private void buildComplexType(ElementInfo ei) {
        ComplexTypeEntry ct = new ComplexTypeEntry(ei);

        ComplexTypeEntry existingType = hmTypes.get(ct.name);
        if (existingType != null && !ct.name.equals(existingType.name)) {
            throw new IllegalArgumentException("Namespace collision" +
                " for complex type " + ct.name +
                " type " + existingType.name + "already defined.");
        }
        hmTypes.put(ct.name, ct);
    }

    //---------------------------------------------------------------------------

    private void buildSimpleType(ElementInfo ei) {
        SimpleTypeEntry st = new SimpleTypeEntry(ei);
        if (hmTypeRestr.containsKey(st.name))
            throw new IllegalArgumentException("Namespace collision for : " + st.name);

        hmTypeRestr.put(st.name, st.alEnum);

        if (!hmMemberTypeRestr.containsKey(st.name))
            hmMemberTypeRestr.put(st.name, st.alTypes);
    }

    //---------------------------------------------------------------------------

    private void buildGlobalAttrib(ElementInfo ei) {
        AttributeEntry at = new AttributeEntry(ei);
        if (hmAttribs.containsKey(at.name))
            throw new IllegalArgumentException("Namespace collision for : " + at.name);

        hmAttribs.put(at.name, at);
        hmAllAttrs.put(at.name, at);
    }

    //---------------------------------------------------------------------------

    private void buildGlobalGroup(ElementInfo ei) {
        GroupEntry ge = new GroupEntry(ei);
        if (hmGroups.containsKey(ge.name))
            throw new IllegalArgumentException("Namespace collision for : " + ge.name);

        hmGroups.put(ge.name, ge);
    }

    //---------------------------------------------------------------------------

    private void buildGlobalAttributeGroup(ElementInfo ei) {

        AttributeGroupEntry age = new AttributeGroupEntry(ei);
        if (hmAttrGpEn.containsKey(age.name))
            throw new IllegalArgumentException("Namespace collision for : " + age.name);
        hmAttrGpEn.put(age.name, age);

    }

    //---------------------------------------------------------------------------
    //---
    //--- Add in attributes from complexType with SimpleContent that restricts
    //--- or extends a base type (if any)
    //---
    //---------------------------------------------------------------------------

    private List<AttributeEntry> resolveAttributeInheritanceFromSimpleContent(ComplexTypeEntry cte) {
        List<AttributeEntry> result = new ArrayList<AttributeEntry>();

        if (cte.simpleContent == null) {
            throw new IllegalArgumentException("SimpleContent must be present in base type of the SimpleContent in " + cte.name);
        } else {

            // recurse if we need to follow the base type

            String baseType = cte.simpleContent.base;
            ComplexTypeEntry baseCTE = hmTypes.get(baseType);
            if (baseCTE != null)
                result = new ArrayList<AttributeEntry>(resolveAttributeInheritanceFromSimpleContent(baseCTE));

            // if the base type was a restriction then replace the attributes we got
            // from the restriction with these
            if (cte.simpleContent.restriction) {
                @SuppressWarnings("unchecked")
                List<AttributeEntry> adds = (List<AttributeEntry>) cte.simpleContent.alAttribs.clone();
                for (int i = 0; i < result.size(); i++) {
                    AttributeEntry attrib = (AttributeEntry) result.get(i);
                    for (Object add : adds) {
                        AttributeEntry attribOther = (AttributeEntry) add;
                        boolean eqAttrib = eqAttribs(attribOther, attrib);
                        if (eqAttrib) {
                            result.set(i, attribOther);
                        }
                    }
                }
            }
            // otherwise base type was an extension so add the attributes we got
            // from the extension to these
            else {
                @SuppressWarnings("unchecked")
                List<AttributeEntry> clone = (List<AttributeEntry>) cte.simpleContent.alAttribs.clone();
                result.addAll(clone);
            }

            // No one seems clear on what to do with attributeGroups so treat them
            // as an extension
            if (cte.simpleContent.alAttribGroups != null) {
                for (int k = 0; k < cte.simpleContent.alAttribGroups.size(); k++) {
                    String attribGroup = cte.simpleContent.alAttribGroups.get(k);
                    List<AttributeEntry> al = hmAttrGrp.get(attribGroup);

                    if (al == null)
                        throw new IllegalArgumentException("Attribute group not found : " + attribGroup);

                    for (AttributeEntry anAl : al) {
                        result.add(anAl);
                    }
                }
            }
        }


        return result;
    }

    /**
     * function to test whether two AttributeEntry objects have the same name
     */
    boolean eqAttribs(AttributeEntry attribOther, AttributeEntry attrib) {
        if (attribOther.name != null) {
            if (attrib.name != null) {
                if (attribOther.name.equals(attrib.name)) return true;
            } else {
                if (attribOther.name.equals(attrib.reference)) return true;
            }
        } else {
            if (attrib.name != null) {
                if (attribOther.reference.equals(attrib.name)) return true;
            } else {
                if (attribOther.reference.equals(attrib.reference)) return true;
            }
        }
        return false;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Add in attributes from complexType with ComplexContent that restricts
    //--- or extends a base type (if any)
    //---
    //---------------------------------------------------------------------------

    private List<AttributeEntry> resolveAttributeInheritance(ComplexTypeEntry cte) {

        if (cte.complexContent == null)
            return cte.alAttribs;

        String baseType = cte.complexContent.base;
        ComplexTypeEntry baseCTE = hmTypes.get(baseType);
        if (baseCTE == null)
            throw new IllegalArgumentException("Base type not found for : " + baseType);

        List<AttributeEntry> result = new ArrayList<AttributeEntry>(resolveAttributeInheritance(baseCTE));

        // if the base type was a restriction then replace the attributes we got
        // from the restriction with these

        if (cte.complexContent.restriction) {
            List<AttributeEntry> adds = cte.complexContent.alAttribs;
            for (int i = 0; i < result.size(); i++) {
                AttributeEntry attrib = (AttributeEntry) result.get(i);
                for (AttributeEntry attribOther : adds) {
                    boolean eqAttrib = eqAttribs(attribOther, attrib);
                    if (eqAttrib) {
                        result.set(i, attribOther);
                    }
                }
            }
        }
        // otherwise base type was an extension so add the attributes we got
        // from the extension to these
        else {
            result.addAll(cte.complexContent.alAttribs);
            if (cte.complexContent.alAttribGroups != null) {
                for (int k = 0; k < cte.complexContent.alAttribGroups.size(); k++) {
                    String attribGroup = cte.complexContent.alAttribGroups.get(k);
                    List<AttributeEntry> al = hmAttrGrp.get(attribGroup);
                    if (al == null)
                        throw new IllegalArgumentException("Attribute group not found : " + attribGroup);
                    for (AttributeEntry anAl : al) {
                        result.add(anAl);
                    }
                }
            }
        }

        // No one seems clear on what to do with attributeGroups so treat them
        // as an extension
        if (baseCTE.alAttribGroups != null) {
            for (int k = 0; k < baseCTE.alAttribGroups.size(); k++) {
                String attribGroup = baseCTE.alAttribGroups.get(k);
                List<AttributeEntry> al = hmAttrGrp.get(attribGroup);

                if (al == null)
                    throw new IllegalArgumentException("Attribute group not found : " + attribGroup);

                for (AttributeEntry anAl : al) {
                    result.add(anAl);
                }
            }
        }

        return result;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Add in elements to complexType that come from base type (if any)
    //---
    //---------------------------------------------------------------------------

    private List<ElementEntry> resolveInheritance(ComplexTypeEntry cte) {
        if (cte == null || cte.complexContent == null)
            if (cte != null) {
                return cte.alElements;
            }

        String baseType = null;
        if (cte != null) {
            baseType = cte.complexContent.base;
        }
        ComplexTypeEntry baseCTE = hmTypes.get(baseType);
        if (baseCTE == null)
            throw new IllegalArgumentException("Base type not found for : " + baseType);

        // skip over the elements in the base type of a restricted complex type
        // by ending the recursion
        List<ElementEntry> result = new ArrayList<ElementEntry>();
        if (!cte.complexContent.restriction)
            result = new ArrayList<ElementEntry>(resolveInheritance(baseCTE));

        result.addAll(cte.complexContent.alElements);

        return result;
    }

    //---------------------------------------------------------------------------

    private MetadataAttribute buildMetadataAttrib(AttributeEntry ae) {
        String name = ae.name;
        String ref = ae.reference;
        String value = ae.defValue;
        boolean overRequired = ae.required;

        MetadataAttribute ma = new MetadataAttribute();

        if (ref != null) {
            ae = hmAttribs.get(ref);
            if (ae == null)
                throw new IllegalArgumentException("Reference '" + ref + "' not found for attrib : " + name + ":" + ref);
        }

        if (ref != null && ref.contains(":"))
            ma.name = ref;
        else
            ma.name = ae.unqualifiedName;

        if (value != null)
            ma.defValue = value;
        else
            ma.defValue = ae.defValue;

        ma.required = overRequired;

        // Load simple type entry values
        if (ae.type != null) {
            List<String> values = hmTypeRestr.get(ae.type);
            if (values != null) {
                for (String v : values) {
                    ae.alValues.add(v);
                }
            }
            // Load member types entry values
            List<String> memberTypes = hmMemberTypeRestr.get(ae.type);
            if (memberTypes != null) {
                for (String type : memberTypes) {
                    List<String> memberTypeValues = hmTypeRestr.get((String) type);
                    if (memberTypeValues != null) {
                        for (String v : memberTypeValues) {
                            ma.values.add((String) v);
                        }
                    }
                }
            }
        }


        for (int k = 0; k < ae.alValues.size(); k++) {
            ma.values.add(ae.alValues.get(k));
        }

        return ma;
    }

    //---------------------------------------------------------------------------

    public String getUnqualifiedName(String qname) {
        int pos = qname.indexOf(':');
        if (pos < 0) return qname;
        else return qname.substring(pos + 1);
    }
}

//==============================================================================

class ElementInfo {
    public Element element;
    public Path file;
    public String targetNS;
    public String targetNSPrefix;

    //---------------------------------------------------------------------------

    public ElementInfo(Element e, Path f, String tns, String tnsp) {
        element = e;
        file = f;
        targetNS = tns;
        targetNSPrefix = tnsp;
    }
}

//==============================================================================

