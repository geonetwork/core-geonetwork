package org.openwis.blacklist.client;

import org.openwis.products.client.ProductMetadataClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class BlacklistConfiguration {

        @Bean
        public Jaxb2Marshaller marshaller() {
            Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
            marshaller.setContextPath("org.openwis.blackist.client");
            return marshaller;
        }

        @Bean
        public BlacklistClient productMetadataClient() {
            BlacklistClient client = new BlacklistClient();
            client.setDefaultUri("http://localhost:8088/mockBlackkistServiceSoapBinding");
            Jaxb2Marshaller marshaller = marshaller();
            client.setMarshaller(marshaller);
            client.setUnmarshaller(marshaller);
            return client;
        }
}
