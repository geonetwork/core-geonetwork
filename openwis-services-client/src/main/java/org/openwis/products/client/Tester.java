package org.openwis.products.client;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by jose on 17/09/15.
 */
public class Tester {

    public static void main(String[] args) {
        ApplicationContext context =
                new AnnotationConfigApplicationContext(ProductMetadataConfiguration.class);
        ProductMetadataClient client = context.getBean(ProductMetadataClient.class);

        ProductMetadata product = new ProductMetadata();
        product.setDataPolicy("policy");
        product.setFed(true);
        product.setFileExtension("png");

        long id = client.createProductMetadata(product);

        System.out.println("Create product metadata: " + id);

    }
}
