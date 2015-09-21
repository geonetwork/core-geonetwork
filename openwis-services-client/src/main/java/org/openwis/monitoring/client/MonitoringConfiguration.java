package org.openwis.monitoring.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class MonitoringConfiguration {

        @Bean
        public Jaxb2Marshaller marshaller() {
            Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
            marshaller.setContextPath("org.openwis.monitoring.client");
            return marshaller;
        }

        @Bean
        public MonitoringClient productMetadataClient() {
            MonitoringClient client = new MonitoringClient();
            client.setDefaultUri("http://localhost:8088/mockBlackkistServiceSoapBinding");
            Jaxb2Marshaller marshaller = marshaller();
            client.setMarshaller(marshaller);
            client.setUnmarshaller(marshaller);
            return client;
        }
}
