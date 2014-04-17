package org.fao.geonet.services.reports;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.domain.*;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.repository.specification.UserSpecs;
import org.jdom.Element;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

/**
 * Service to return the list of users "active" during a time period.
 *
 * Service parameters:
 *   dateFrom (mandatory)
 *   dateTo   (mandatory)
 *   groups   (optional)
 *
 * Service output:
 * <p/>
 * <response>
 * <record>
 * <username></username>
 * <surname></surname>
 * <name></name>
 * <email></email>
 * <userGroups>Group1/Profile1-Group2/Profile2</userGroups>
 * <lastlogindate></lastlogindate>
 * </record>
 * </response>
 *
 * @author Jose García
 */
public class ReportUsers  implements Service {
    public void init(String appPath, ServiceConfig params) throws Exception {
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context)
            throws Exception {
        // Process parameters
        String beginLoginDate = Util.getParam(params, "dateFrom");
        String endLoginDate = Util.getParam(params, "dateTo");

        beginLoginDate = beginLoginDate + "T00:00:00";
        endLoginDate = endLoginDate + "T23:59:59";

        ISODate beginLoginDateIso = new ISODate(beginLoginDate);
        ISODate endLoginDateIso = new ISODate(endLoginDate);
        Set<Integer> groupList = ReportUtils.groupsForFilter(context, params);

        // Retrieve users
        final Sort sort = new Sort(Sort.Direction.ASC, SortUtils.createPath(User_.lastLoginDate));
        final List<User> records = context.getBean(UserRepository.class).findAll(
                UserSpecs.loginDateBetweenAndByGroups(beginLoginDateIso, endLoginDateIso, groupList), sort);

        // Process metadata results for the report
        Element response = new Element(Jeeves.Elem.RESPONSE);

        for (User user : records) {

            String username= user.getUsername();
            String name= (user.getName() != null?user.getName():"");
            String surname= (user.getSurname() != null?user.getSurname():"");
            String email = (user.getEmail() != null?user.getEmail():"");
            String lastLoginDate = user.getLastLoginDate();
            StringBuilder userGroupsList = new StringBuilder();

            // Retrieve user groups
            List<UserGroup> userGroups = context.getBean(UserGroupRepository.class).
                    findAll(UserGroupSpecs.hasUserId(user.getId()));

            int i = 0;
            for(UserGroup ug : userGroups) {
                Group g =  ug.getGroup();
                String groupName = g.getLabelTranslations().get(context.getLanguage());
                if (groupName == null) groupName = g.getName();

                String groupProfile = ug.getId().getProfile().name();
                if (groupName == null) groupName = g.getName();

                if (i++ > 0) userGroupsList.append("-");
                userGroupsList.append(groupName+ "/" + groupProfile);
            }

            // Build the record element with the information for the report
            Element metadataEl = new Element("record");
            metadataEl.addContent(new Element("username").setText(username))
                    .addContent(new Element("surname").setText(surname))
                    .addContent(new Element("name").setText(name))
                    .addContent(new Element("email").setText(email))
                    .addContent(new Element("userGroups").setText(userGroupsList.toString()))
                    .addContent(new Element("lastlogindate").setText(lastLoginDate));

            response.addContent(metadataEl);
        }
        return response;
    }
}
