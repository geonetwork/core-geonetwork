package jeeves.guiservices.session;

import java.util.ArrayList;
import java.util.Collection;

import jeeves.server.ProfileManager;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class JeevesUser implements UserDetails {
	private static final long serialVersionUID = -215010489062730571L;
	private String username;
	private String email;
	private String name;
	private String surname;
	private String profile;
	private String address;
	private String city;
	private String state;
	private String zip;
	private String country;
	private String organisation;
	private String password;
	private String kind;
	private String id;
	private transient ProfileManager profileManager;

	public JeevesUser(ProfileManager profileManager){
		this.profileManager = profileManager;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		String profile = getProfile();
		ArrayList<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
		if (profile != null) {
			for (String p : profileManager.getProfilesSet(profile)) {
				auths.add(new SimpleGrantedAuthority(p));
			}
		}
		return auths;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	@Override
	public String getUsername() {
		return username;
	}

	public JeevesUser setUsername(String username) {
		if (username==null) username = "Guest";
		this.username = username;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public JeevesUser setEmail(String email) {
		if (email==null) email = "";
		this.email = email;
		return this;
	}

	public String getName() {
		return name;
	}

	public JeevesUser setName(String name) {
		if (name==null) name = "Guest";
		this.name = name;
		return this;
	}

	public String getSurname() {
		return surname;
	}

	public JeevesUser setSurname(String surname) {
		if (surname==null) surname = "";
		this.surname = surname;
		return this;
	}

	public String getProfile() {
		return profile;
	}

	public JeevesUser setProfile(String profile) {
		if (profile==null) profile = "Guest";
		this.profile = profile;
		return this;
	}

	public String getAddress() {
		return address;
	}

	public JeevesUser setAddress(String address) {
		if (address==null) address = "";
		this.address = address;
		return this;
	}

	public String getCity() {
		return city;
	}

	public JeevesUser setCity(String city) {
		if (city==null) city = "";
		this.city = city;
		return this;
	}

	public String getState() {
		return state;
	}

	public JeevesUser setState(String state) {
		if (state==null) state = "";
		this.state = state;
		return this;
	}

	public String getZip() {
		return zip;
	}

	public JeevesUser setZip(String zip) {
		if (zip==null) zip = "";
		this.zip = zip;
		return this;
	}

	public String getCountry() {
		return country;
	}

	public JeevesUser setCountry(String country) {
		if (country==null) country = "";
		this.country = country;
		return this;
	}

	public String getOrganisation() {
		return organisation;
	}

	public JeevesUser setOrganisation(String organisation) {
		if (organisation==null) organisation = "";
		this.organisation = organisation;
		return this;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public JeevesUser setPassword(String password) {
		if (password==null) password = "";
		this.password = password;
		return this;
	}

	public String getKind() {
		return kind;
	}

	public JeevesUser setKind(String kind) {
		if (kind==null) kind = "";
		this.kind = kind;
		return this;
	}

	public String getId() {
		return id;
	}

	public JeevesUser setId(String id) {
		if (id==null) id = "-1";
		this.id = id;
		return this;
	}

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
                (id!=null? id:"") +
                ":" +
                (username!=null?username:"")
                +"]";
    }
}