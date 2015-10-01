package org.fao.geonet.services.openwis.product;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ProductMetadataNotFoundException extends RuntimeException {
  //
}
