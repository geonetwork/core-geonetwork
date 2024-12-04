package org.fao.geonet.session.serialization;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Set;

//https://stackoverflow.com/questions/39507125/changing-return-type-with-json-annotations
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@JsonDeserialize(using = SetDeserializer.class)
public abstract class SetMixin {

    @JsonCreator
    public SetMixin(Set<?> s) {
    }
}
