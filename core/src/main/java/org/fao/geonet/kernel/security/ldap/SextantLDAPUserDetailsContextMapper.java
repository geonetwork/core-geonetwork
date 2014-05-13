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

import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.utils.Log;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.LDAPUser;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.UserGroup;
import org.springframework.context.ApplicationContext;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.common.collect.Multimap;

public class SextantLDAPUserDetailsContextMapper extends
		AbstractLDAPUserDetailsContextMapper {

	private String privilegePattern;
	private Pattern pattern;
	private int groupIndexInPattern;
	private int profilIndexInPattern;

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

	// This does basically the same as
	// AbstractLDAPUserDetailsContextMapper.mapUserFromContext(),
	// but avoids to update the LDAP directory (no saveUser()),
	// and ensures that the group ownership from the LDAP directory
	// is correctly imported.
	@Override
	public UserDetails mapUserFromContext(DirContextOperations userCtx,
			String username, Collection<? extends GrantedAuthority> authorities) {

		Profile defaultProfile;
		if (mapping.get("profile")[1] != null) {
			defaultProfile = Profile.valueOf(mapping.get("profile")[1]);
		} else {
			defaultProfile = Profile.RegisteredUser;
		}
		String defaultGroup = mapping.get("privilege")[1];

		Map<String, ArrayList<String>> userInfo = LDAPUtils
				.convertAttributes(userCtx.getAttributes().getAll());

		LDAPUser userDetails = new LDAPUser(username);

		// Checks if the user already exists
		User user = applicationContext.getBean(UserRepository.class)
				.findOneByUsername(username);

		// Uses the newly created user (from LDAPUser)
		if (user == null) {
			user = userDetails.getUser();
		} else {
			userDetails.setUser(user);
		}
		// Updates the user fields
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
		// Set privileges for the user. If not, privileges are handled
		// in local database
		user.getSecurity().setAuthType(LDAPConstants.LDAP_FLAG);

		if (importPrivilegesFromLdap) {
			setProfilesAndPrivileges(defaultProfile, defaultGroup, userInfo,
					userDetails);
		}

		// Assign default profile if not set by LDAP info or local database
		if (user.getProfile() == null) {
			user.setProfile(defaultProfile);
		}

		// Saves the user
		applicationContext.getBean(UserRepository.class).saveAndFlush(user);
		
		// updates the groups informations
		updateGroupOwnership(userDetails);

		return userDetails;
	}

	private void updateGroupOwnership(LDAPUser u) {
		Multimap<String, Profile> privs = u.getPrivileges();
		// List of known groups for the current user
		List<UserGroup> ugroups = applicationContext.getBean(
				UserGroupRepository.class).findAll(
				UserGroupSpecs.hasUserId(u.getUser().getId()));

		for (String k : privs.keys()) {
			// first, try to find the group
			Group group = applicationContext.getBean(GroupRepository.class)
					.findByName(k);
			// Group not found, creating it ...
			if (group == null) {
				group = new Group();
				group.setName(k);
				applicationContext.getBean(GroupRepository.class).saveAndFlush(
						group);
			}
			if (! ugroups.contains(group)) {
				// Then add a new entry for the user / group
				UserGroup newUg = new UserGroup();
				newUg.setGroup(group);
				newUg.setUser(u.getUser());
				newUg.setProfile((Profile) privs.get(k).toArray()[0]);
				applicationContext.getBean(UserGroupRepository.class)
						.saveAndFlush(newUg);
				// Adds it (in case of multiple listesiteweb entries
				ugroups.add(newUg);
			}
		}
	}

	@Override
	protected void setProfilesAndPrivileges(Profile defaultProfile,
			String defaultGroup, Map<String, ArrayList<String>> userInfo,
			LDAPUser userDetails) {
		// Set privileges for the user. If not, privileges are handled
		// in local database
		if (importPrivilegesFromLdap) {
			if ("".equals(privilegePattern)) {
				// 1. no privilegePattern defined. In that case the user
				// has the same profile for all groups. The list of groups
				// is retrieved from the privilegeAttribute content
				// getUserInfo(userInfo, mapping.get("profile")[0]));

				// Usually only one profile is defined in the profile attribute
				List<String> ldapProfiles = userInfo
						.get(mapping.get("profile")[0]);
				if (ldapProfiles != null) {
					Collections.sort(ldapProfiles);
					for (String profile : ldapProfiles) {
						if (Log.isDebugEnabled(Geonet.LDAP)) {
							Log.debug(
									Geonet.LDAP,
									"  User profile " + profile
											+ " found in attribute "
											+ mapping.get("profile")[0]);
						}
						// Check if profile exist in profile mapping table
						String mappedProfile = profileMapping.get(profile)
								.name();
						if (mappedProfile != null) {
							profile = mappedProfile;
						}

						if (Log.isDebugEnabled(Geonet.LDAP)) {
							Log.debug(Geonet.LDAP, "  Assigning profile "
									+ profile);
						}
						if (Profile.exists(profile)) {
							userDetails.setProfile(Profile.valueOf(profile));
						} else {
							Log.error(Geonet.LDAP, "  Profile " + profile
									+ " does not exist.");
						}
					}
				}

				// If no profile defined, use default profile
				if (userDetails.getProfile() == null) {
					if (Log.isDebugEnabled(Geonet.LDAP)) {
						Log.debug(Geonet.LDAP,
								"  No profile defined in LDAP, using default profile "
										+ defaultProfile);
					}
					userDetails.setProfile(defaultProfile);
				}

				if (userDetails.getProfile() != Profile.Administrator) {
					List<String> ldapGroups = userInfo.get(mapping
							.get("privilege")[0]);
					if (ldapGroups != null) {
						for (String group : ldapGroups) {
							if (Log.isDebugEnabled(Geonet.LDAP)) {
								Log.debug(
										Geonet.LDAP,
										"  Define group privilege for group "
												+ group + " as "
												+ userDetails.getProfile());
							}
							userDetails.addPrivilege(group,
									userDetails.getProfile());
						}
					}

					// Set default privileges
					if (userDetails.getPrivileges().size() == 0
							&& defaultGroup != null) {
						if (Log.isDebugEnabled(Geonet.LDAP)) {
							Log.debug(Geonet.LDAP,
									"  No privilege defined, setting privilege for group "
											+ defaultGroup + " as "
											+ userDetails.getProfile());
						}
						userDetails.addPrivilege(defaultGroup,
								userDetails.getProfile());
					}
				}
			} else {
				// 2. a privilegePattern is defined which define a
				// combination of group and profile pair.
				ArrayList<String> privileges = userInfo.get(mapping
						.get("privilege")[0]);
				// This should come from the LDAP, not from userDetails
				// which might not be completely initialized yet.
				// was: if(userDetails.getOrganisation().equals("IFREMER")) {
				ArrayList<String> orgUsr = userInfo.get(mapping
						.get("organisation")[0]);
				if (orgUsr != null && orgUsr.contains("IFREMER")) {
					userDetails.setOrganisation("IFREMER");

					if (privileges == null) {
						privileges = new ArrayList<String>();
					}
					privileges.add("SXT5_IFREMER_RegisteredUser");
				}
				if (privileges != null) {
					// fixing Mantis issue #18255
					// removing leading / trailing whitespaces on the group
					// names
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

							if (group != null && profile != null
									&& Profile.exists(profile)) {
								if (!group
										.equals(LDAPConstants.ALL_GROUP_INDICATOR)) {
									if (Log.isDebugEnabled(Geonet.LDAP)) {
										Log.debug(Geonet.LDAP,
												"  Adding profile " + profile
														+ " for group " + group);
									}
									userDetails.addPrivilege(group,
											Profile.valueOf(profile));
									profileList.add(profile);
								} else {
									profileList.add(profile);
								}
							} else {
								Log.info(Geonet.LDAP, "LDAP privilege info '"
										+ privilegeDefinition
										+ "' does not match search pattern '"
										+ privilegePattern
										+ "'. Information ignored.");
							}
						}
					}
					String highestUserProfile = getHighestProfile(profileList
							.toArray(new String[0]));
					if (highestUserProfile != null) {
						if (Log.isDebugEnabled(Geonet.LDAP)) {
							Log.debug(Geonet.LDAP, "  Highest user profile is "
									+ highestUserProfile);
						}
						userDetails.setProfile(Profile
								.valueOf(highestUserProfile));
					}
				}
			}

			// Note: in Sextant, this code should never be triggered, because
			// importPrivilegesFromLdap should be true in every configurations.
			// if (! importPrivilegesFromLdap)
		} else {
			// TODO: port the legacy code ?
		}

		// Assign default profile if not set by LDAP info or local database
		if (userDetails.getProfile() == null) {
			userDetails.setProfile(defaultProfile);
		}
		// Check that user profile is defined and fallback to registered user
		// in order to avoid inconsistent JeevesUser creation
		String checkProfile = Profile.valueOf(userDetails.getProfile().name())
				.name();

		if (checkProfile == null) {
			Log.error(Geonet.LDAP, "  User profile " + userDetails.getProfile()
					+ " is not set in Jeeves registered profiles."
					+ " Assigning registered user profile.");
			userDetails.setProfile(Profile.RegisteredUser);
		}

	}

	private String getHighestProfile(String[] array) {
		int highestProfile = 0;

		Map<String, Integer> profiles = new HashMap<String, Integer>();
		String[] revProfiles = { "RegisteredUser", "Editor", "Reviewer",
				"UserAdmin", "Administrator" };

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
