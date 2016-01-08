package org.fao.geonet.services.metadata.resources;

/**
 * Created by francois on 31/12/15.
 */
public enum ResourceType {
    PUBLIC("public"), PRIVATE("private");

    String value;
    ResourceType(String value) {
        this.value = value;
    }

    public static ResourceType parse(String value) {
      for (ResourceType resourceType : ResourceType.values()) {
          if (resourceType.toString().equals(value)) {
              return resourceType;
          }
      }
      return null;
    }

    public String toString() {
        return this.value;
    }
}
