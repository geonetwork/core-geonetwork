package org.fao.geonet.api.users.model;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class UserDtoTest {

    @Test
    public void testDeserialize() throws IOException {

        String payload = "{\"id\":101,\"surname\":\"Doe\",\"emailAddresses\":" +
            "[\"john@doe.com\"],\"addresses\":[{\"id\":100,\"city\":\"\",\"zip\":\"\"," +
            "\"country\":\"\",\"state\":\"\",\"address\":\"\"}],\"organisation\":\"\"," +
            "\"kind\":null,\"profile\":\"Administrator\",\"generic\":false,\"enabled\":true," +
            "\"username\":\"jdoe\",\"name\":\"John Doe\",\"groupsRegisteredUser\":[]," +
            "\"groupsEditor\":[],\"groupsReviewer\":[],\"groupsUserAdmin\":[]}";

        ObjectMapper map = new ObjectMapper();
        UserDto jdoe = map.readValue(payload, UserDto.class);

        assertTrue(jdoe.getUsername().equals("jdoe") &&
            jdoe.isGeneric() == false);

    }
}
