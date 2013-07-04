package org.fao.geonet.guiservices.metadata;

import jeeves.exceptions.OperationNotAllowedEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.DataManager;
import org.jdom.Element;

import java.util.List;

/**
 * Retrieves the metadata owned by a user. Depending on user profile:
 *
 *   - Administrator: returns all metadata
 *   - Reviewer, UserAdmin: returns all metadata owned by the user's groups
 *   - Editor: return metadata owned by the user
 *   - Other profiles: throw exception OperationNotAllowedEx
 *
 * Service parameters:
 *
 *   - sortBySelect: allowed values (date, popularity, rating) (optional parameter)
 *
 */
public class GetByOwner implements Service {

    private static final String SORT_BY       = "sortBy";

    private Element _response;

    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(String appPath, ServiceConfig config) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

        String query = null;

        String ownerId = context.getUserSession().getUserId();
        String userProfile = context.getUserSession().getProfile();

        if (userProfile == null) {
           throw new OperationNotAllowedEx("Unauthorized user attempted to list editable metadata ");
        }

				boolean useOwnerId = true;

        // if the user is an admin, return all metadata
        if(userProfile.equals(Geonet.Profile.ADMINISTRATOR)) {
            query = "SELECT id FROM Metadata WHERE isHarvested='n'" ;
						useOwnerId = false;
        }
        // if the user is a reviewer, return all metadata of the user's groups
        else if(userProfile.equals(Geonet.Profile.REVIEWER) || userProfile.equals(Geonet.Profile.USER_ADMIN)) {
            query = "SELECT id FROM Metadata "+
                    "WHERE groupOwner IN "+
										"(SELECT groupId FROM UserGroups WHERE userId=? "+
                    "AND isHarvested='n')" ;
        }
        // if the user is an editor, return metadata owned by this user
        else if(userProfile.equals(Geonet.Profile.EDITOR) ) {
            query = "SELECT id FROM Metadata WHERE owner=?"+
                        " AND isHarvested='n'" ;
        } else {
            throw new OperationNotAllowedEx("Unauthorized user " + ownerId + " attempted to list editable metadata ");
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
					result = dbms.select(query, Integer.valueOf(ownerId));
				} else {
					result = dbms.select(query);
				}
        _response = new Element("response");

        @SuppressWarnings("unchecked")
        List<Element> resultElems = result.getChildren();
        for (Element rec : resultElems) {
            String  id = rec.getChildText("id");
            boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
            Element md = gc.getBean(DataManager.class).getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);
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
