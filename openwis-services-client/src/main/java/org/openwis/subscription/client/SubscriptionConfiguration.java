package org.openwis.subscription.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class SubscriptionConfiguration {

        @Bean
        public Jaxb2Marshaller marshaller() {
            Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
            marshaller.setContextPath("org.openwis.subscription.client");
            return marshaller;
        }

        @Bean
        public SubscriptionClient productMetadataClient() {
            SubscriptionClient client = new SubscriptionClient();
            client.setDefaultUri("http://localhost:8088/mockCacheIndexServiceSoapBinding");
            Jaxb2Marshaller marshaller = marshaller();
            client.setMarshaller(marshaller);
            client.setUnmarshaller(marshaller);
            return client;
        }
}
