package jeeves.guiservices.session;

import java.util.Collection;
import java.util.Collections;

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

	public JeevesUser(){}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		String profile = getProfile();
		return Collections.singleton(new SimpleGrantedAuthority("ROLE_"+profile));
	}

	@Override
	public boolean isAccountNonExpired() {
		return false;
	}

	@Override
	public boolean isAccountNonLocked() {
		return false;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return false;
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
		this.username = username;
		return this;
	}

	public String getEmail() {
		return email;
	}

	public JeevesUser setEmail(String email) {
		this.email = email;
		return this;
	}

	public String getName() {
		return name;
	}

	public JeevesUser setName(String name) {
		this.name = name;
		return this;
	}

	public String getSurname() {
		return surname;
	}

	public JeevesUser setSurname(String surname) {
		this.surname = surname;
		return this;
	}

	public String getProfile() {
		return profile;
	}

	public JeevesUser setProfile(String profile) {
		this.profile = profile;
		return this;
	}

	public String getAddress() {
		return address;
	}

	public JeevesUser setAddress(String address) {
		this.address = address;
		return this;
	}

	public String getCity() {
		return city;
	}

	public JeevesUser setCity(String city) {
		this.city = city;
		return this;
	}

	public String getState() {
		return state;
	}

	public JeevesUser setState(String state) {
		this.state = state;
		return this;
	}

	public String getZip() {
		return zip;
	}

	public JeevesUser setZip(String zip) {
		this.zip = zip;
		return this;
	}

	public String getCountry() {
		return country;
	}

	public JeevesUser setCountry(String country) {
		this.country = country;
		return this;
	}

	public String getOrganisation() {
		return organisation;
	}

	public JeevesUser setOrganisation(String organisation) {
		this.organisation = organisation;
		return this;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public JeevesUser setPassword(String password) {
		this.password = password;
		return this;
	}

	public String getKind() {
		return kind;
	}

	public JeevesUser setKind(String kind) {
		this.kind = kind;
		return this;
	}

	public String getId() {
		return id;
	}

	public JeevesUser setId(String id) {
		this.id = id;
		return this;
	}
	

}