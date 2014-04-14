//=============================================================================
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

package org.fao.geonet.kernel.mef;

import com.google.common.base.Optional;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.BadFormatEx;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.Util;
import org.fao.geonet.utils.Xml;

import org.apache.commons.io.IOUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.exceptions.UnAuthorizedException;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.*;
import org.fao.oaipmh.exceptions.BadArgumentException;
import org.jdom.Element;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Importer {
	public static List<String> doImport(final Element params, final ServiceContext context,
                                        final File mefFile, final String stylePath) throws Exception {
		final DataManager dm = context.getBean(DataManager.class);

		// Load preferred schema and set to iso19139 by default
        String preferredSchema = context.getBean(ServiceConfig.class).getMandatoryValue("preferredSchema");
        if (preferredSchema == null) {
            preferredSchema = "iso19139";
        }

        final List<String> metadataIdMap = new ArrayList<String>();
		final List<Element> md = new ArrayList<Element>();
		final List<Element> fc = new ArrayList<Element>();

		// Try to define MEF version from mef file not from parameter
		String fileType = Util.getParam(params, "file_type", "mef");
		if (fileType.equals("mef")) {
			MEFLib.Version version = MEFLib.getMEFVersion(mefFile);
			if (version != null && version.equals(MEFLib.Version.V2)) {
                fileType = "mef2";
            }
		}

		IVisitor visitor;

		if (fileType.equals("single"))
			visitor = new XmlVisitor();
		else if (fileType.equals("mef"))
			visitor = new MEFVisitor();
		else if (fileType.equals("mef2"))
			visitor = new MEF2Visitor();
		else
			throw new BadArgumentException("Bad file type parameter.");

		// --- import metadata from MEF, Xml, ZIP files
        final String finalPreferredSchema = preferredSchema;
        MEFLib.visit(mefFile, visitor, new IMEFVisitor() {

			public void handleMetadata(Element metadata, int index)
					throws Exception {
                if(Log.isDebugEnabled(Geonet.MEF))
                    Log.debug(Geonet.MEF, "Collecting metadata:\n" + Xml.getString(metadata));
				md.add(index, metadata);
			}

			public void handleMetadataFiles(File[] Files, Element info, int index)
					throws Exception {
								String infoSchema = "_none_";
								if (info != null && info.getContentSize() != 0) {
									Element general = info.getChild("general");
									if (general != null && general.getContentSize() != 0) {
										if (general.getChildText("schema") != null) {
											infoSchema = general.getChildText("schema");
										}
									}
								}

                String lastUnknownMetadataFolderName=null;
                if(Log.isDebugEnabled(Geonet.MEF))
                    Log.debug(Geonet.MEF, "Multiple metadata files");

                if(Log.isDebugEnabled(Geonet.MEF))
                	Log.debug(Geonet.MEF, "info.xml says schema should be "+infoSchema);


				Element metadataValidForImport;

                Map<String,Pair<String,Element>> mdFiles = new HashMap<String,Pair<String,Element>>();
                for (File file : Files) {
                    if (file != null && !file.isDirectory()) {
                        Element metadata = Xml.loadFile(file);
                        try {
                            String metadataSchema = dm.autodetectSchema(metadata, null);
                            // If local node doesn't know metadata
                            // schema try to load next xml file.
                            if (metadataSchema == null) {
                                continue;
                            }

														String currFile = "Found metadata file " + file.getParentFile().getParentFile().getName() + File.separator + file.getParentFile().getName() + File.separator + file.getName();
														mdFiles.put(metadataSchema,Pair.read(currFile,metadata));

                        } catch (NoSchemaMatchesException e) {
                            // Important folder name to identify metadata should be ../../
                            lastUnknownMetadataFolderName=file.getParentFile().getParentFile().getName() + File.separator + file.getParentFile().getName() + File.separator;
                            Log.debug(Geonet.MEF, "No schema match for "
                                + lastUnknownMetadataFolderName + file.getName() 
                                + ".");
                        }
                    }
                }

								if (mdFiles.size() == 0) {
									throw new BadFormatEx("No valid metadata file found" + ((lastUnknownMetadataFolderName==null)?"":(" in " + lastUnknownMetadataFolderName)) + ".");
								}

                // 1st: Select metadata with schema in info file
								Pair<String,Element> mdInform = mdFiles.get(infoSchema);
								if (mdInform != null) {
									if (Log.isDebugEnabled(Geonet.MEF)) {
										Log.debug(Geonet.MEF, mdInform.one()
											+ " with info.xml schema (" + infoSchema + ").");
 									}
									metadataValidForImport = mdInform.two();
									handleMetadata(metadataValidForImport, index);
									return;
								}

								// 2nd: Select metadata with preferredSchema
								mdInform = mdFiles.get(finalPreferredSchema);
                if (mdInform != null) {
									if (Log.isDebugEnabled(Geonet.MEF)) {
										Log.debug(Geonet.MEF, mdInform.one()
											+ " with preferred schema (" + finalPreferredSchema + ").");
									}
									metadataValidForImport = mdInform.two();
									handleMetadata(metadataValidForImport, index);
									return;
								} 

								// Lastly: Select the first metadata in the map
								String metadataSchema = (String)mdFiles.keySet().toArray()[0];
								mdInform = mdFiles.get(metadataSchema);
								if (Log.isDebugEnabled(Geonet.MEF)) {
									Log.debug(Geonet.MEF, mdInform.one()
										+ " with known schema (" + metadataSchema + ").");
								}
                metadataValidForImport = mdInform.two();

								// Import valid metadata
								handleMetadata(metadataValidForImport, index);
			}

			// --------------------------------------------------------------------

			public void handleFeatureCat(Element featureCat, int index)
					throws Exception {
				if (featureCat != null) {
                    if(Log.isDebugEnabled(Geonet.MEF))
                        Log.debug(Geonet.MEF, "Collecting feature catalog:\n" + Xml.getString(featureCat));
				}
				fc.add(index, featureCat);
			}

			// --------------------------------------------------------------------

			/**
			 * Record is not a template by default. No category attached to
			 * record by default. No stylesheet used by default. If no site
			 * identifier provided, use current node id by default. No
			 * validation by default.
			 * 
			 * If record is a template and not a MEF file always generate a new
			 * UUID.
			 */
			public void handleInfo(Element info, int index) throws Exception {

				String FS = File.separator;

				String uuid = null;
				String createDate = null;
				String changeDate = null;
				String source;
				String sourceName = null;
				// Schema in info.xml is not used here anymore.
				// It is used in handleMetadataFiles as the first option to pick a 
				// metadata file from those in a metadata dir in a MEF2
				// String schema = null;
				final MetadataType isTemplate;
				String rating = null;
				String popularity = null;
				String groupId = null;
				Element categs = null;
				final Element privileges;
				boolean validate = false;

				
				// Apply a stylesheet transformation if requested
				String style = Util.getParam(params, Params.STYLESHEET,
				"_none_");

				if (!style.equals("_none_"))
					md.add(index, Xml.transform(md.get(index), stylePath
							+ FS + style));
				
				
				final Element metadata = md.get(index);
				String schema = dm.autodetectSchema(metadata, null);

				if (schema == null)
					throw new Exception("Unknown schema");

				// Handle non MEF files insertion
				if (info.getChildren().size() == 0) {
                    source = Util.getParam(params, Params.SITE_ID, context.getBean(SettingManager.class).getSiteId());
					isTemplate = MetadataType.lookup(Util.getParam(params, Params.TEMPLATE, "n"));

					String category = Util
							.getParam(params, Params.CATEGORY, "");
					if (!category.equals("")) {
						categs = new Element("categories");
						categs.addContent((new Element("category"))
								.setAttribute("name", category));
					}

					groupId = Util.getParam(params, Params.GROUP);
					privileges = new Element("group");
					privileges.addContent(new Element("operation")
							.setAttribute("name", "view"));
					privileges.addContent(new Element("operation")
							.setAttribute("name", "editing"));
					privileges.addContent(new Element("operation")
							.setAttribute("name", "download"));
					privileges.addContent(new Element("operation")
							.setAttribute("name", "notify"));
					privileges.addContent(new Element("operation")
							.setAttribute("name", "dynamic"));
					privileges.addContent(new Element("operation")
							.setAttribute("name", "featured"));

					if (isTemplate == MetadataType.METADATA) {
                        // Get the Metadata uuid if it's not a template.
                        uuid = dm.extractUUID(schema, md.get(index));
					} else if (isTemplate== MetadataType.SUB_TEMPLATE) {
					    // Get subtemplate uuid if defined in @uuid at root
                        uuid = md.get(index).getAttributeValue("uuid");
					}
					validate = Util.getParam(params, Params.VALIDATE, "off")
							.equals("on");

				} else {
                    if(Log.isDebugEnabled(Geonet.MEF))
                        Log.debug(Geonet.MEF, "Collecting info file:\n" + Xml.getString(info));
					
					categs = info.getChild("categories");
					privileges = info.getChild("privileges");

					Element general = info.getChild("general");

					uuid = general.getChildText("uuid");
					createDate = general.getChildText("createDate");
					changeDate = general.getChildText("changeDate");
					// If "assign" checkbox is set to true, we assign the metadata to the current catalog siteID/siteName
					boolean assign = Util.getParam(params, "assign", "off")
							.equals("on");
					if (assign) {
                        if(Log.isDebugEnabled(Geonet.MEF))
                            Log.debug(Geonet.MEF, "Assign to local catalog");
                        source = context.getBean(SettingManager.class).getSiteId();
					} else {	
						// --- If siteId is not set, set to current node
                        source = Util.getParam(general, Params.SITE_ID, context.getBean(SettingManager.class).getSiteId());
						sourceName = general.getChildText("siteName");

                        if(Log.isDebugEnabled(Geonet.MEF))
                            Log.debug(Geonet.MEF, "Assign to catalog: " + source);
					}
					isTemplate = general.getChildText("isTemplate").equals("true") ? MetadataType.TEMPLATE : MetadataType.METADATA;
					rating = general.getChildText("rating");
					popularity = general.getChildText("popularity");
				}

				if (validate) {
					// Validate xsd and schematron
					DataManager.validateMetadata(schema, metadata, context);
                }

				String uuidAction = Util.getParam(params, Params.UUID_ACTION,
						Params.NOTHING);

				importRecord(uuid, uuidAction, md, schema, index,
						source, sourceName, context, metadataIdMap, createDate,
						changeDate, groupId, isTemplate);

				if (fc.size() != 0 && fc.get(index) != null) {
					// UUID is set as @uuid in root element
					uuid = UUID.randomUUID().toString();

					fc.add(index, dm.setUUID("iso19110", uuid, fc.get(index)));

                    //
                    // insert metadata
                    //
                    int userid = context.getUserSession().getUserIdAsInt();
                    String group = null, docType = null, title = null, category = null;
                    boolean ufo = false, indexImmediate = false;
                    String fcId = dm.insertMetadata(context, "iso19110", fc.get(index), uuid,
                            userid, group, source, isTemplate.codeString, docType, category, createDate, changeDate, ufo, indexImmediate);

                    if(Log.isDebugEnabled(Geonet.MEF))
                        Log.debug(Geonet.MEF, "Adding Feature catalog with uuid: " + uuid);

					// Create database relation between metadata and feature
					// catalog
					String mdId = metadataIdMap.get(index);

                    final MetadataRelationRepository relationRepository = context.getBean(MetadataRelationRepository.class);
                    final MetadataRelation relation = new MetadataRelation();
                    relation.setId(new MetadataRelationId(Integer.valueOf(mdId), Integer.valueOf(fcId)));

                    relationRepository.save(relation);

					metadataIdMap.add(fcId);
					// TODO : privileges not handled for feature catalog ...
				}

				final int iMetadataId = Integer.valueOf(metadataIdMap.get(index));

                final String finalPopularity = popularity;
                final String finalRating = rating;
                final Element finalCategs = categs;
                final String finalGroupId = groupId;
                context.getBean(MetadataRepository.class).update(iMetadataId, new Updater<Metadata>() {
                    @Override
                    public void apply(@Nonnull final Metadata metadata) {
                        final MetadataDataInfo dataInfo = metadata.getDataInfo();
                        if (finalPopularity != null) {
                            dataInfo.setPopularity(Integer.valueOf(finalPopularity));
                        }
                        if (finalRating != null) {
                            dataInfo.setRating(Integer.valueOf(finalRating));
                        }
                        dataInfo.setType(isTemplate);
                        metadata.getHarvestInfo().setHarvested(false);

                        addCategoriesToMetadata(metadata, finalCategs, context);

                        if (finalGroupId == null) {
                            Group ownerGroup = addPrivileges(context, dm, iMetadataId, privileges);
                            if (ownerGroup != null) {
                                metadata.getSourceInfo().setGroupOwner(ownerGroup.getId());
                            }
                        } else {
                            final OperationAllowedRepository allowedRepository = context.getBean(OperationAllowedRepository.class);
                            final Set<OperationAllowed> allowedSet = addOperations(context, dm, privileges, iMetadataId,
                                    Integer.valueOf(finalGroupId));
                            allowedRepository.save(allowedSet);
                        }



                    }
                });


                String pubDir = Lib.resource.getDir(context, "public", metadataIdMap
						.get(index));
                String priDir = Lib.resource.getDir(context, "private", metadataIdMap
                        .get(index));

                IO.mkdirs(new File(pubDir), "MEF Importer public resources directory for metadata "+metadataIdMap);
                IO.mkdirs(new File(priDir), "MEF Importer private resources directory for metadata "+metadataIdMap);

                dm.indexMetadata(metadataIdMap.get(index), false);
			}

			// --------------------------------------------------------------------

        public void handlePublicFile(String file, String changeDate,
					InputStream is, int index) throws IOException {
                if(Log.isDebugEnabled(Geonet.MEF)) {
                    Log.debug(Geonet.MEF, "Adding public file with name=" + file);
                }
				saveFile(context, metadataIdMap.get(index), "public", file, changeDate, is);
			}

			// --------------------------------------------------------------------

			public void handlePrivateFile(String file, String changeDate,
					InputStream is, int index) throws IOException {
                if(Log.isDebugEnabled(Geonet.MEF)) Log.debug(Geonet.MEF, "Adding private file with name=" + file);
				saveFile(context, metadataIdMap.get(index), "private", file, changeDate,
						is);
			}

		});

		return metadataIdMap;
	}

    public static void addCategoriesToMetadata(Metadata metadata, Element finalCategs, ServiceContext context) {
        if (finalCategs != null) {
            final MetadataCategoryRepository categoryRepository = context.getBean(MetadataCategoryRepository.class);
            for (Object cat : finalCategs.getChildren()) {
                Element categoryEl = (Element) cat;
                String catName = categoryEl.getAttributeValue("name");
                final MetadataCategory oneByName = categoryRepository.findOneByName(catName);

                if (oneByName == null) {
                    if(Log.isDebugEnabled(Geonet.MEF)) {
                        Log.debug(Geonet.MEF, " - Skipping non-existent category : " + catName);
                    }
                } else {
                    // --- metadata category exists locally
                    if(Log.isDebugEnabled(Geonet.MEF)) {
                        Log.debug(Geonet.MEF, " - Setting category : " + catName);
                    }
                    metadata.getCategories().add(oneByName);
                }
            }
        }
    }

    public static void importRecord(String uuid,
                                    String uuidAction, List<Element> md, String schema, int index,
                                    String source, String sourceName, ServiceContext context,
                                    List<String> id, String createDate, String changeDate,
                                    String groupId, MetadataType isTemplate) throws Exception {

		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager dm = gc.getBean(DataManager.class);
		

		if (uuid == null || uuid.equals("")
				|| uuidAction.equals(Params.GENERATE_UUID)) {
			String newuuid = UUID.randomUUID().toString();
			source = null;

			Log
					.debug(Geonet.MEF, "Replacing UUID " + uuid + " with "
							+ newuuid);
			uuid = newuuid;

			// --- set uuid inside metadata
			md.add(index, dm.setUUID(schema, uuid, md.get(index)));
		} else {
			if (sourceName == null)
				sourceName = "???";

			if (source == null || source.trim().length() == 0)
				throw new Exception(
						"Missing siteId parameter from info.xml file");

			// --- only update sources table if source is not current site 
            if (!source.equals(gc.getBean(SettingManager.class).getSiteId())) {
                Source source1 = new Source(source, sourceName, true);
                context.getBean(SourceRepository.class).save(source1);
            }
		}

		try {
			if (dm.existsMetadataUuid(uuid) && !uuidAction.equals(Params.NOTHING)) {
                // user has privileges to replace the existing metadata
                if(dm.getAccessManager().canEdit(context, dm.getMetadataId(uuid))) {
                    dm.deleteMetadata(context, dm.getMetadataId(uuid));
                    if(Log.isDebugEnabled(Geonet.MEF))
                        Log.debug(Geonet.MEF, "Deleting existing metadata with UUID : " + uuid);
                }
                // user does not hav privileges to replace the existing metadata
                else {
                    throw new UnAuthorizedException("User has no privilege to replace existing metadata", null);
                }
			}
		}
        catch (Exception e) {
            throw new Exception(" Existing metadata with UUID " + uuid + " could not be deleted. Error is: " + e.getMessage());
		}



        if(Log.isDebugEnabled(Geonet.MEF))
            Log.debug(Geonet.MEF, "Adding metadata with uuid:" + uuid);
		
        //
        // insert metadata
        //
        int userid = context.getUserSession().getUserIdAsInt();
        String docType = null, title = null, category = null;
        boolean ufo = false, indexImmediate = false;
        id.add(index,
                dm.insertMetadata(context, schema, md.get(index), uuid,
                userid, groupId, source, isTemplate.codeString, docType, category, createDate, changeDate, ufo, indexImmediate));

	}

	// --------------------------------------------------------------------------

	private static void saveFile(ServiceContext context, String id,
			String access, String file, String changeDate, InputStream is)
			throws IOException {
		String dir = Lib.resource.getDir(context, access, id);

		File outFile = new File(dir, file);
		FileOutputStream os=null;
		try {
            os = new FileOutputStream(outFile);
    		BinaryFile.copy(is, os);
    		IO.setLastModified(outFile, new ISODate(changeDate).getTimeInSeconds() * 1000, Geonet.MEF);
		} finally {
		    IOUtils.closeQuietly(os);
		}
	}

	/**
	 * Add privileges according to information file.
	 * 
	 * @param context
	 * @param dm
	 * @param metadataId
	 * @param privil
	 * @throws Exception
	 */
	private static Group addPrivileges(final ServiceContext context, final DataManager dm, final int metadataId,
                                       final Element privil) {

        final GroupRepository groupRepository = context.getBean(GroupRepository.class);
        @SuppressWarnings("unchecked")
        List<Element> list = privil.getChildren("group");

        Group owner = null;
        Set<OperationAllowed> opAllowedToAdd = new HashSet<OperationAllowed>();
        List<Group> groupsToAdd = new ArrayList<Group>();

		for (Element group : list) {
			String grpName = group.getAttributeValue("name");
			boolean groupOwner = group.getAttributeValue("groupOwner") != null;
			Group groupEntity = groupRepository.findByName(grpName);

			if (groupEntity == null) {
                if(Log.isDebugEnabled(Geonet.MEF)) {
                    Log.debug(Geonet.MEF, " - Skipping non-existent group : " + grpName);
                }
			} else {
				// --- metadata group exists locally
                if(Log.isDebugEnabled(Geonet.MEF)) {
                    Log.debug(Geonet.MEF, " - Setting privileges for group : " + grpName);
                }

                groupsToAdd.add(groupEntity);
                opAllowedToAdd.addAll(addOperations(context, dm, group, metadataId, groupEntity.getId()));
				if (groupOwner) {
                    if (Log.isDebugEnabled(Geonet.MEF)) {
                        Log.debug(Geonet.MEF, grpName + " set as group Owner ");
                    }
                    owner = groupEntity;
				}
			}
		}

        return owner;
	}

	/**
	 * Add operations according to information file.
	 * 
	 * @param context
	 * @param dm
	 * @param group
	 * @param metadataId
	 * @param grpId
	 * @throws Exception
	 */
	private static Set<OperationAllowed> addOperations(final ServiceContext context, final DataManager dm, final Element group,
                                                       final int metadataId, final int grpId) {
		@SuppressWarnings("unchecked")
        List<Element> operations = group.getChildren("operation");

        Set<OperationAllowed> toAdd = new HashSet<OperationAllowed>();
        for (Element operation : operations) {
            String opName = operation.getAttributeValue("name");

            int opId = dm.getAccessManager().getPrivilegeId(opName);

            if (opId == -1) {
                if(Log.isDebugEnabled(Geonet.MEF)) {
                    Log.debug(Geonet.MEF, "   Skipping --> " + opName);
                }
            } else {
                // --- operation exists locally

                if(Log.isDebugEnabled(Geonet.MEF)) {
                    Log.debug(Geonet.MEF, "   Adding --> " + opName);
                }
                Optional<OperationAllowed> opAllowed = dm.getOperationAllowedToAdd(context, metadataId, grpId, opId);
                if (opAllowed.isPresent()) {
                    toAdd.add(opAllowed.get());
                }
            }
        }

        return toAdd;
	}

}

// =============================================================================

