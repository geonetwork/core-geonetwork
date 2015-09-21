package org.openwis.cacheindex.client;

import org.openwis.blacklist.client.BlacklistClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class CacheIndexConfiguration {

        @Bean
        public Jaxb2Marshaller marshaller() {
            Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
            marshaller.setContextPath("org.openwis.cacheindex.client");
            return marshaller;
        }

        @Bean
        public CacheIndexClient productMetadataClient() {
            CacheIndexClient client = new CacheIndexClient();
            client.setDefaultUri("http://localhost:8088/mockCacheIndexServiceSoapBinding");
            Jaxb2Marshaller marshaller = marshaller();
            client.setMarshaller(marshaller);
            client.setUnmarshaller(marshaller);
            return client;
        }
}
