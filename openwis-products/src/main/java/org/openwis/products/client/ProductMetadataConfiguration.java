package org.openwis.products.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class ProductMetadataConfiguration {

        @Bean
        public Jaxb2Marshaller marshaller() {
            Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
            marshaller.setContextPath("org.openwis.products.client");
            return marshaller;
        }

        @Bean
        public ProductMetadataClient productMetadataClient() {
            ProductMetadataClient client = new ProductMetadataClient();
            client.setDefaultUri("http://localhost:8088/mockProductMetadataServiceSoapBinding");
            Jaxb2Marshaller marshaller = marshaller();
            client.setMarshaller(marshaller);
            client.setUnmarshaller(marshaller);
            return client;
        }
}
