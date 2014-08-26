package org.fao.geonet.services.register;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.exceptions.MissingParameterEx;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;

public class SelfRegisterTest extends AbstractServiceIntegrationTest {

	final SelfRegister sfController = new SelfRegister();

	@Test
    public void selfRegisterTest() throws Exception {
		ServiceContext svcCtx = createServiceContext();


		Element params = createParams(Pair.write("surname", "john"),
							Pair.write("name", "Doe"),
							Pair.write("email", "root@localhost"),
							Pair.write("profile", Profile.RegisteredUser));
		
		
		
		Element ret = sfController.exec(params, svcCtx);
		
		assertTrue(ret.getAttribute("surname").getValue().equals("john"));
		assertTrue(ret.getAttribute("name").getValue().equals("Doe"));
		assertTrue(ret.getAttribute("email").getValue().equals("root@localhost"));
		assertTrue(ret.getAttribute("username").getValue().equals("root@localhost"));
		
	}

	@Test
    public void badParametersSelfRegisterTest() throws Exception {
		ServiceContext svcCtx = createServiceContext();


		Element params = createParams(
							Pair.write("notExpectedParameter", "NotExpectedValue")
							);
		
		
		try {
		  sfController.exec(params, svcCtx);
		} catch (Throwable e) {
		  assertTrue(e instanceof MissingParameterEx);
		}
		
	}

	@Test
    public void highProfileSelfRegisterTest() throws Exception {
		ServiceContext svcCtx = createServiceContext();


		Element params = createParams(Pair.write("surname", "john"),
							Pair.write("name", "Doe"),
							Pair.write("email", "root@localhost"),
							Pair.write("profile", Profile.Administrator));
		
		
		
		Element ret = sfController.exec(params, svcCtx);
		
		assertTrue(ret.getAttribute("surname").getValue().equals("john"));
		assertTrue(ret.getAttribute("name").getValue().equals("Doe"));
		assertTrue(ret.getAttribute("email").getValue().equals("root@localhost"));
		assertTrue(ret.getAttribute("username").getValue().equals("root@localhost"));
				
		// Checks that the user has the  expected requested profile

		// TODO: I am not sure this is the expected behaviour: allowing people to create
		// even temporary account as administrator, just noticing the admin catalogue
		// might be a dangerous behaviour.

		final UserRepository userRepository = svcCtx.getBean(UserRepository.class);
		User newUsr = userRepository.findOneByEmail("root@localhost");
        
		assertTrue (newUsr.getProfile() == Profile.Administrator);
		
	}

	
}
