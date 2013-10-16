package org.fao.geonet.services.user;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.AbstractCoreTest;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.UserGroupId;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: Jesse
 * Date: 10/16/13
 * Time: 3:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateTest extends AbstractCoreTest {
    private static final String COMPAT_UPDATE_PARAMS = "<request>\n" +
                                                       "  <zip>zip1</zip>\n" +
                                                       "  <groups_RegisteredUser>2</groups_RegisteredUser>\n" +
                                                       "  <state>state1</state>\n" +
                                                       "  <surname>lastname</surname>\n" +
                                                       "  <org>c2c</org>\n" +
                                                       "  <password>password</password>\n" +
                                                       "  <kind>consultant</kind>\n" +
                                                       "  <city>city1</city>\n" +
                                                       "  <country>ca</country>\n" +
                                                       "  <id />\n" +
                                                       "  <operation>%s</operation>\n" +
                                                       "  <username>newuser</username>\n" +
                                                       "  <password2>newuser</password2>\n" +
                                                       "  <groups_Reviewer>2</groups_Reviewer>\n" +
                                                       "  <email>newuser@email.com</email>\n" +
                                                       "  <address>address1</address>\n" +
                                                       "  <groups_UserAdmin>2</groups_UserAdmin>\n" +
                                                       "  <name>firstname</name>\n" +
                                                       "  <groups_Editor>2</groups_Editor>\n" +
                                                       "  <profile>UserAdmin</profile>\n" +
                                                       "</request>";

    @Autowired
    UserRepository _userRepository;
    @Autowired
    UserGroupRepository _userGroupRepository;

    @Test
    public void testExecAddNewUserCompatibilityModeAsAdmin() throws Exception {
        Element request = Xml.loadString(String.format(COMPAT_UPDATE_PARAMS, Params.Operation.NEWUSER), false);

        final Update update = new Update();

        ServiceContext context = createServiceContext();
        loginAsAdmin(context);
        update.exec(request, context);

        assertEquals(2, _userRepository.count());
        List<User> users = _userRepository.findAllByProfile(Profile.UserAdmin);
        assertEquals(1, users.size());

        User user = users.get(0);
        
        assertExpectedUser(user);

        assertNotNull(_userGroupRepository.findOne(new UserGroupId().setGroupId(2).setUserId(user.getId()).setProfile(Profile.Editor)));
        assertNotNull(_userGroupRepository.findOne(new UserGroupId().setGroupId(2).setUserId(user.getId()).setProfile(Profile.RegisteredUser)));
        assertNotNull(_userGroupRepository.findOne(new UserGroupId().setGroupId(2).setUserId(user.getId()).setProfile(Profile.UserAdmin)));
        assertNotNull(_userGroupRepository.findOne(new UserGroupId().setGroupId(2).setUserId(user.getId()).setProfile(Profile.Reviewer)));
        assertNull(_userGroupRepository.findOne(new UserGroupId().setGroupId(2).setUserId(user.getId()).setProfile(Profile.Administrator)));

    }

    private void assertExpectedUser(User user) {
        assertEquals(1, user.getAddresses().size());
        assertEquals("zip1", user.getPrimaryAddress().getZip());
        assertEquals("address1", user.getPrimaryAddress().getAddress());
        assertEquals("city1", user.getPrimaryAddress().getCity());
        assertEquals("ca", user.getPrimaryAddress().getCountry());
        assertEquals("state1", user.getPrimaryAddress().getState());

        assertEquals(1, user.getEmailAddresses().size());
        assertEquals("newuser@email.com", user.getEmail());

        assertEquals("consultant", user.getKind());
        assertEquals("firstname", user.getName());
        assertEquals("lastname", user.getSurname());
        assertEquals("c2c", user.getOrganisation());
        assertEquals("password", user.getPassword());
        assertEquals("newuser", user.getUsername());
        assertEquals(Profile.UserAdmin, user.getProfile());
    }
}
