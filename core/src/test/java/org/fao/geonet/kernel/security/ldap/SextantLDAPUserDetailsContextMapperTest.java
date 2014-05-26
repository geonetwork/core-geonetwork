package org.fao.geonet.kernel.security.ldap;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.fao.geonet.domain.LDAPUser;
import org.fao.geonet.domain.Profile;
import org.junit.Before;
import org.junit.Test;

public class SextantLDAPUserDetailsContextMapperTest {

	private SextantLDAPUserDetailsContextMapper sxtldapMapper;

    private Map<String, String[]> mapping;
    
	@Before
	public void setUp() {
		mapping = new  HashMap<String, String[]>();
		String[] cn = {"cn", ""};
		String[] mail = {"mail", "data@myorganisation.org"};
		String[] organisation = {"o", "myorg"};
		String[] kind = {"title", "" };
		String[] address = { "", "" };
		String[] privilege = {"listesiteweb", "sample" };
		String[] profile = {"profile", "RegisteredUser" };

		mapping.put("name",         cn);
		mapping.put("mail",         mail);
		mapping.put("organisation", organisation);
		mapping.put("kind",         kind);
		mapping.put("address",      address);
		mapping.put("privilege",    privilege);
		mapping.put("profile",      profile);
		sxtldapMapper = new SextantLDAPUserDetailsContextMapper();
		sxtldapMapper.setPrivilegePattern("SXT5_(.*)_(.*)");
		sxtldapMapper.setGroupIndexInPattern(1);
		sxtldapMapper.setProfilIndexInPattern(2);
		sxtldapMapper.setImportPrivilegesFromLdap(true);
		sxtldapMapper.setMapping(mapping);
	}

	@Test
	public void testSextantLDAPUserDetailsAsAdmin() {
		LDAPUser fakeUser = new LDAPUser("admin");

		Map<String, ArrayList<String>> userInfo = new HashMap<String, ArrayList<String>>();
		ArrayList<String> uidLst = new ArrayList<String>();
		uidLst.add("admin");
		userInfo.put("uid", uidLst);
		ArrayList<String> siteWebs = new ArrayList<String>();
		siteWebs.add("SXT5_IFREMER_Administrator");
		userInfo.put("listesiteweb", siteWebs);
		// setProfilesAndPrivileges(Profile defaultProfile,	String defaultGroup, Map<String, ArrayList<String>> userInfo, LDAPUser userDetails) {
		sxtldapMapper.setProfilesAndPrivileges(Profile.RegisteredUser,"sample", userInfo, fakeUser);
		// o = IFREMER is not set, organization should be == ""
		assertTrue(fakeUser.getOrganisation().equals(""));
		// IFREMER => Administrator
		assertTrue(fakeUser.getPrivileges().get("IFREMER").contains(Profile.Administrator));
		// The user is Administrator
		assertTrue(fakeUser.getProfile() == Profile.Administrator);
	}
	private Map<String, ArrayList<String>> getUserInfo(String uid, String mail, String sn, String cn, String givenName, String listesiteweb){
		Map<String, ArrayList<String>> userInfo = new HashMap<String, ArrayList<String>>();
		ArrayList<String> uidLst = new ArrayList<String>();
		uidLst.add(uid);
		userInfo.put("uid", uidLst);
		
		ArrayList<String> mailLst = new ArrayList<String>();
		mailLst.add(mail);
		userInfo.put("mail", mailLst);
		
		ArrayList<String> snLst = new ArrayList<String>();
		snLst.add(sn);
		userInfo.put("sn", snLst);
		
		ArrayList<String> cnLst = new ArrayList<String>();
		cnLst.add(cn);
		userInfo.put("cn", cnLst);
		
		ArrayList<String> givenNameLst = new ArrayList<String>();
		givenNameLst.add(givenName);
		userInfo.put("givenName", givenNameLst);
		
		ArrayList<String> listesitewebLst = new ArrayList<String>();
		listesitewebLst.add(listesiteweb);
		userInfo.put("listesiteweb", listesitewebLst);
		
		return userInfo;
	}
	@Test
	public void testSextantLdapUserDetailsAsUserAdmin() {
		LDAPUser fakeUser = new LDAPUser("useradmin");
		Map<String, ArrayList<String>> userInfo;

		userInfo = getUserInfo("useradmin", "useradmin@sextant.org", "useradmin", "useradmin", "useradmin", "SXT5_MEDBENTH_UserAdmin");
		sxtldapMapper.setProfilesAndPrivileges(Profile.RegisteredUser,"sample", userInfo, fakeUser);
		assertTrue(fakeUser.getOrganisation().equals(""));
		// MEDBENTH => UserAdmin
		assertTrue(fakeUser.getPrivileges().get("MEDBENTH").contains(Profile.UserAdmin));
		// The user is UserAdmin
		assertTrue(fakeUser.getProfile() == Profile.UserAdmin);
	}

	@Test
	public void testSextantLdapUserDetailsAsReviewer() {
		LDAPUser fakeUser = new LDAPUser("reviewer");
		Map<String, ArrayList<String>> userInfo;

		userInfo = getUserInfo("reviewer", "reviewer@sextant.org", "reviewer", "reviewer", "reviewer", "SXT5_ANOTHERGRP_Reviewer");
		
		ArrayList<String> org = new ArrayList<String>();
		org.add("IFREMER");
		userInfo.put("o", org);
		
		sxtldapMapper.setProfilesAndPrivileges(Profile.RegisteredUser,"sample", userInfo, fakeUser);
		
		// ANOTHERGRP => Reviewer
		assertTrue(fakeUser.getPrivileges().get("ANOTHERGRP").contains(Profile.Reviewer));
		
		// The user is Reviewer
		assertTrue(fakeUser.getProfile() == Profile.Reviewer);
	}

	@Test
	public void testSextantLdapUserDetailsAsEditor() {
		LDAPUser fakeUser = new LDAPUser("editor");
		Map<String, ArrayList<String>> userInfo;
		userInfo = getUserInfo("editor", "editor@sextant.org", "editor", "editor", "editor", "SXT5_ANOTHERGRP_Editor");
		sxtldapMapper.setProfilesAndPrivileges(Profile.RegisteredUser,"sample", userInfo, fakeUser);
		// o = ""
		assertTrue(fakeUser.getOrganisation().equals(""));
		// ANOTHERGRP => Editor
		assertTrue(fakeUser.getPrivileges().get("ANOTHERGRP").contains(Profile.Editor));
		// The user is Editor
		assertTrue(fakeUser.getProfile() == Profile.Editor);
	}

	@Test
	public void testSextantLdapUserDetailsAsRegisteredUser() {	
		LDAPUser fakeUser = new LDAPUser("registereduser");
		Map<String, ArrayList<String>> userInfo;
		userInfo = getUserInfo("registereduser", "registered@sextant.org", "registereduser", "registereduser", "registereduser", "SXT5_ANOTHERGRP_RegisteredUser");
		sxtldapMapper.setProfilesAndPrivileges(Profile.RegisteredUser,"sample", userInfo, fakeUser);
		// o = ""
		assertTrue(fakeUser.getOrganisation().equals(""));
		// ANOTHERGRP => RegisteredUser
		assertTrue(fakeUser.getPrivileges().get("ANOTHERGRP").contains(Profile.RegisteredUser));
		// The user is RegisteredUser
		assertTrue(fakeUser.getProfile() == Profile.RegisteredUser);
	}
	
	@Test
	public void testSextantLdapUserDetailsAsUnknownProfile() {	
		LDAPUser fakeUser = new LDAPUser("unknown");
		Map<String, ArrayList<String>> userInfo;
		userInfo = getUserInfo("unknown", "unknown@sextant.org", "unknown", "unknown", "unknown", "SXT5_ANOTHERGRP_UnknownProfile");
		sxtldapMapper.setProfilesAndPrivileges(Profile.RegisteredUser,"sample", userInfo, fakeUser);
		// o = ""
		assertTrue(fakeUser.getOrganisation().equals(""));
		// The user is RegisteredUser
		assertTrue(fakeUser.getProfile() == Profile.RegisteredUser);
	}

}
