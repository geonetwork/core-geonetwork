package org.fao.geonet.kernel.security.ldap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.LDAPUser;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.common.collect.Multimap;

import javax.annotation.PostConstruct;

public class SextantLDAPUserDetailsContextMapper extends
		AbstractLDAPUserDetailsContextMapper {

	private String privilegePattern;
	private Pattern pattern;
	private int groupIndexInPattern;
	private int profilIndexInPattern;

	@Autowired
	private UserGroupRepository userGroupRepository;

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
    private LDAPUtils ldapUtils;

	private Profile defaultProfile = Profile.RegisteredUser;
    // unused anyway ?
    private String defaultGroup = "";

	@PostConstruct
    public void init() {
        String[] strsProf = mapping.get("profile");
        if (strsProf.length > 1) {
            if (strsProf[1] != null) {
                defaultProfile = Profile.valueOf(strsProf[1]);
            }
        }
        String[] privs = mapping.get("privilege");
        if (privs.length > 1) {
            defaultGroup = privs[1];
        }
    }

    public void setPrivilegePattern(String privilegePattern) {
		this.privilegePattern = privilegePattern;
		pattern = Pattern.compile(privilegePattern);
	}

	public void setGroupIndexInPattern(int groupIndexInPattern) {
		this.groupIndexInPattern = groupIndexInPattern;
	}

	public void setProfilIndexInPattern(int profilIndexInPattern) {
		this.profilIndexInPattern = profilIndexInPattern;
	}

    /**
     * Gets the GeoNetwork LDAP user
     * @param username the user identifier as a string
     * @param userCtx a DirContextOperation describing the user from the LDAP
     * @return a LDAPUser object.
     */
	public LDAPUser getGeoNetworkUserFromContext(String username, DirContextOperations userCtx) {
        Map<String, ArrayList<String>> userInfo = ldapUtils
            .convertAttributes(userCtx.getAttributes().getAll());

        LDAPUser userDetails = new LDAPUser(username);
        User user = userDetails.getUser();

        user.setName(getUserInfo(userInfo, "name"))
            .setSurname(getUserInfo(userInfo, "surname"))
            .setOrganisation(getUserInfo(userInfo, "organisation"));
        user.getEmailAddresses().clear();
        user.getEmailAddresses().add(getUserInfo(userInfo, "mail"));
        user.getPrimaryAddress().setAddress(getUserInfo(userInfo, "address"))
            .setState(getUserInfo(userInfo, "state"))
            .setZip(getUserInfo(userInfo, "zip"))
            .setCity(getUserInfo(userInfo, "city"))
            .setCountry(getUserInfo(userInfo, "country"));

        user.getSecurity().setAuthType(LDAPConstants.LDAP_FLAG);

        if (importPrivilegesFromLdap) {
            // Sets privileges for the user. If not, privileges are handled
            // in local database
            setProfilesAndPrivileges(defaultProfile, defaultGroup, userInfo,
                userDetails);
        }


        return userDetails;
    }


    /**
     * This method does basically the same as AbstractLDAPUserDetailsContextMapper.mapUserFromContext(),
     * but avoids to update the LDAP directory (no saveUser() implemented), and ensures that the group
     * ownership from the LDAP directory is correctly imported.
     *
     * @param userCtx
     * @param username
     * @param authorities
     * @return
     */
	@Override
	public UserDetails mapUserFromContext(DirContextOperations userCtx,
			String username, Collection<? extends GrantedAuthority> authorities) {

	    LDAPUser userDetails = getGeoNetworkUserFromContext(username, userCtx);
	    User user = userDetails.getUser();

        // Assign default profile if not set by LDAP info or local database
        if (user.getProfile() == null) {
            user.setProfile(defaultProfile);
        }
		// Saves / updates the user
        saveUser(userDetails);

		// updates the groups informations
		updateGroupOwnership(userDetails);

		return userDetails;
	}

	private void updateGroupOwnership(LDAPUser u) {
		Multimap<String, Profile> privs = u.getPrivileges();

		// removes every groups mappings known for the current user
		userGroupRepository.deleteAll(UserGroupSpecs.hasUserId(u.getUser().getId()));

		for (String k : privs.keys()) {
			// first, try to find the group
			Group group = groupRepository.findByName(k);
			// Group not found, skipping it (Mantis issue #21571)
			if (group == null) {
				continue ;
			}

			// Mantis issue #21684: Elects the highest profile for the given group
			Collection<Profile> cProfs = privs.get(k);
			if (cProfs.isEmpty())
				continue;
			Profile[] profs = cProfs.toArray(new Profile[cProfs.size()]);

			String[] sProfs = new String[profs.length];
			for (int i = 0 ;  i < profs.length ; i++) {
				sProfs[i] = profs[i].name();
			}
			String sProf = getHighestProfile(sProfs);
			if (sProf == null)
				continue;

			Profile prof = Profile.findProfileIgnoreCase(sProf);


			UserGroup newUg = new UserGroup().setGroup(group).setUser(u.getUser()).setProfile(prof);
			userGroupRepository.saveAndFlush(newUg);
			// if Reviewer, then the user is also considered as Editor
			if (prof == Profile.Reviewer) {
				UserGroup newUg2 = new UserGroup().setGroup(group).setUser(u.getUser()).setProfile(Profile.Editor);
				userGroupRepository.saveAndFlush(newUg2);
			}
		}
	}

	@Override
	protected void setProfilesAndPrivileges(Profile defaultProfile, String defaultGroup, Map<String, ArrayList<String>> userInfo, LDAPUser userDetails) {
		// Set privileges for the user. If not, privileges are handled in local
		// database

		// SXT issue #18395
		// We need this info earlier in the code, to avoid filling the usergroups table
		// in case of super admin.
		boolean isSuperAdmin = false;

		// 2. a privilegePattern is defined which define a
		// combination of group and profile pair.
		ArrayList<String> privileges = userInfo.get(mapping.get("privilege")[0]);

		if (privileges == null) {
			privileges = new ArrayList<String>();
		}

		// First pass: if the user is super admin, no need to populate the
		// usergroups table.
		for (String p : privileges) {
			if (p.toUpperCase().equals("SXT5_ALL_ADMINISTRATOR")) {
				isSuperAdmin = true;
				userDetails.setProfile(Profile.Administrator);
			}
			// SXT mantis issue #27843: if user is in listesiteweb generique,
			// update the user
			else if (p.toUpperCase().equals("GENERIQUE")) {
				userDetails.getUser().setGeneric(true);
			}
		}

		if (! isSuperAdmin) {
			if (userDetails.getOrganisation().equals("IFREMER")) {
				// If the user is admin, no need to add the following privilege
				// (SXT issue #18395)
                privileges.add("SXT5_IFREMER_RegisteredUser");
            }
			// Mantis issue #18255
			// removing leading / trailing whitespaces on the group names
			for (int i = 0; i < privileges.size(); i++) {
				privileges.set(i, privileges.get(i).trim());
			}

			Set<String> profileList = new HashSet<String>();

			for (String privilegeDefinition : privileges) {

				Matcher m = pattern.matcher(privilegeDefinition);
				boolean b = m.matches();
				if (b) {
					String group = m.group(groupIndexInPattern);
					String profile = m.group(profilIndexInPattern);

					if (group != null && profile != null && Profile.exists(profile)) {
						if (!group.equals(LDAPConstants.ALL_GROUP_INDICATOR)) {
							if (Log.isDebugEnabled(Geonet.LDAP)) {
								Log.debug(Geonet.LDAP, "  Adding profile " + profile + " for group " + group);
							}
							userDetails.addPrivilege(group, Profile.valueOf(profile));
							profileList.add(profile);
						} else {
							profileList.add(profile);
						}
					} else {
						Log.info(Geonet.LDAP, "LDAP privilege info '" + privilegeDefinition
								+ "' does not match search pattern '" + privilegePattern + "'. Information ignored.");
					}
				}
			}
			String highestUserProfile = getHighestProfile(profileList.toArray(new String[0]));
			if (highestUserProfile != null) {
				if (Log.isDebugEnabled(Geonet.LDAP)) {
					Log.debug(Geonet.LDAP, "  Highest user profile is " + highestUserProfile);
				}
				userDetails.setProfile(Profile.valueOf(highestUserProfile));
			}

			// Assign default profile if not set by LDAP info or local database
			if (userDetails.getProfile() == null) {
				userDetails.setProfile(defaultProfile);
			}
			// Check that user profile is defined and fallback to registered
			// user
			// in order to avoid inconsistent JeevesUser creation
			String checkProfile = Profile.valueOf(userDetails.getProfile().name()).name();

			if (checkProfile == null) {
				Log.error(Geonet.LDAP, "  User profile " + userDetails.getProfile()
						+ " is not set in Jeeves registered profiles." + " Assigning registered user profile.");
				userDetails.setProfile(Profile.RegisteredUser);
			}
		}
	}

	private String getHighestProfile(String[] array) {
		int highestProfile = 0;

		Map<String, Integer> profiles = new HashMap<String, Integer>();
		String[] revProfiles = { "RegisteredUser", "Editor", "Reviewer", "UserAdmin", "Administrator" };

		// We hard-code the profile list to avoid introducing too much
		// complexity in the current code
		// anyway, in case of Sextant, we would use a static list of profiles
		profiles.put("Administrator", 4);
		profiles.put("UserAdmin", 3);
		profiles.put("Reviewer", 2);
		profiles.put("Editor", 1);
		profiles.put("RegisteredUser", 0);

		for (int i = 0; i < array.length; i++) {
			String currentProf = array[i];
			Integer currentProfI = profiles.get(currentProf);
			if ((currentProfI != null) && (currentProfI > highestProfile)) {
				highestProfile = currentProfI;
			}
		}

		return revProfiles[highestProfile];
	}

}
