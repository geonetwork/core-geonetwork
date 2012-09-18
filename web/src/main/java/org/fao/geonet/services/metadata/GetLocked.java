package org.fao.geonet.services.metadata;

import jeeves.exceptions.OperationNotAllowedEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;

import java.util.Iterator;

/**
 * Retrieves the locked metadata unlockable by the user. Depending on user profile:
 *
 *   - Administrator: returns all locked metadata
 *   - Reviewer, UserAdmin: returns all locked metadata with edit privilege for at least one of the user's groups
 *   - Editor: return locked metadata owned by the user UNLESS 'symbolicLocking' is enabled, then return results as if Reviewer
 *   - Other profiles: throw exception OperationNotAllowedEx
 *
 * Service parameters:
 *
 *   - sortBySelect: allowed values (date, popularity, rating) (optional parameter)
 *
 */
public class GetLocked implements Service {

    private static final String SORT_BY       = "sortBy";

    private Element _response;


    public void init(String appPath, ServiceConfig config) throws Exception {}

    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager settingManager = gc.getSettingManager();
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

        String query = null;

        String ownerId = context.getUserSession().getUserId();
        String userProfile = context.getUserSession().getProfile();

        if (userProfile == null) {
           throw new OperationNotAllowedEx("Unauthorized user " + ownerId + " attempted to list locked metadata ");
        }

        boolean useOwnerId = true;

        // if the user is an admin, return all metadata
        if(userProfile.equals(Geonet.Profile.ADMINISTRATOR)) {
            query = "SELECT id FROM Metadata WHERE isLocked='y'" ;
            useOwnerId = false;
        }
        // if the user is a reviewer, return all metadata that has edit permission for the user's groups
        else if(userProfile.equals(Geonet.Profile.REVIEWER) || userProfile.equals(Geonet.Profile.USER_ADMIN)) {
            query = "SELECT metadataid as id FROM OperationAllowed oa " +
                    "LEFT JOIN metadata m ON oa.metadataid = m.id " +
                    "WHERE oa.operationid = '2' AND m.isLocked='y' AND oa.groupid IN (SELECT groupid FROM UserGroups WHERE userid=?)";
        }
        // if the user is an editor
        else if(userProfile.equals(Geonet.Profile.EDITOR) ) {
            boolean symbolicLocking = settingManager.getValueAsBool("system/symbolicLocking/enable");
            // return all metadata that has edit permission for the user's groups
            if(symbolicLocking) {
                query = "SELECT metadataid as id FROM OperationAllowed oa " +
                        "LEFT JOIN metadata m ON oa.metadataid = m.id " +
                        "WHERE oa.operationid = '2' AND m.isLocked='y' AND oa.groupid IN (SELECT groupid FROM UserGroups WHERE userid=?)";
            }
            // return metadata owned by this user
            else {
                query = "SELECT id FROM Metadata WHERE owner=?"+
                        " AND isLocked='y'" ;
            }
        }
        else {
            throw new OperationNotAllowedEx("Unauthorized user " + ownerId + " attempted to list locked metadata ");
        }

        // Sorting
        String sortBy = sortByParameter(params);

        if(sortBy.equals("date")) {
            query += " ORDER BY changeDate DESC";
        }
        else if(sortBy.equals("popularity")) {
            query += " ORDER BY popularity DESC";
        }
        else if(sortBy.equals("rating")) {
            query += " ORDER BY rating DESC";
        }

        Element result;
        if (useOwnerId) {
            result = dbms.select(query, ownerId);
        }
        else {
            result = dbms.select(query);
        }
        _response = new Element("response");

        for (Iterator iter = result.getChildren().iterator(); iter.hasNext();) {
            Element rec = (Element)iter.next();
            String  id = rec.getChildText("id");
            boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
            //Element md = gc.getDataManager().getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);
            Element md = gc.getDataManager().getMetadataFromWorkspace(context, id, forEditing, withValidationErrors, keepXlinkAttributes, true);
            _response.addContent(md);
        }

        Element currentSortBySelect = new Element(SORT_BY);
        currentSortBySelect.setText(sortBy);
        _response.addContent(currentSortBySelect);
        Element response = (Element) _response.clone();
        return response;
    }

    private String sortByParameter(Element params) {
		Element sortByEl = params.getChild(SORT_BY);
		String sortBy = null;
		if (sortByEl == null) {
			sortBy = "date";
		}
        else {
			sortBy = sortByEl.getText();
		}
        return sortBy;
    }

}
