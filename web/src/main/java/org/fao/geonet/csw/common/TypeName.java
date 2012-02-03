package org.fao.geonet.csw.common;

import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

// TODO this class is not used anywhere
public enum TypeName {
    DATASET("dataset"), DATASET_COLLECTION("datasetcollection"), SERVICE("service"), APPLICATION("application"),

    // TODO heikki: these are the only 2 legal values
    RECORD("csw:Record"), METADATA("gmd:MD_Metadata");

    //------------------------------------------------------------------------

    private TypeName(String typeName) {
        this.typeName = typeName;
    }

    //------------------------------------------------------------------------

    public String toString() {
        return typeName;
    }

    //------------------------------------------------------------------------

    public static TypeName getTypeName(String typeName) {
        if (typeName.equals(DATASET.toString()))
            return DATASET;

        else if (typeName.equals(DATASET_COLLECTION.toString()))
            return DATASET_COLLECTION;

        else if (typeName.equals(SERVICE.toString()))
            return SERVICE;

        else if (typeName.equals(APPLICATION.toString()))
            return APPLICATION;

        else if (typeName.equals(RECORD.toString()))
            return RECORD;

        else if (typeName.equals(METADATA.toString()))
            return METADATA;

        else
            return null;

    }
    //------------------------------------------------------------------------

    public static Set<TypeName> parse(String typeNames) throws InvalidParameterValueEx {
        HashSet<TypeName> hs = new HashSet<TypeName>();

        if (typeNames != null) {
            StringTokenizer st = new StringTokenizer(typeNames, " ");

            while (st.hasMoreTokens()) {
                String typeName = st.nextToken();

                if (typeName.equals(DATASET.toString()))
                    hs.add(DATASET);

                else if (typeName.equals(DATASET_COLLECTION.toString()))
                    hs.add(DATASET_COLLECTION);

                else if (typeName.equals(SERVICE.toString()))
                    hs.add(SERVICE);

                else if (typeName.equals(APPLICATION.toString()))
                    hs.add(APPLICATION);

                else if (typeName.equals(RECORD.toString()))
                    hs.add(RECORD);

                else if (typeName.equals(METADATA.toString()))
                    hs.add(METADATA);

                    // These two are explicitly not allowed as search targets in CSW 2.0.2,
                    // so we throw an exception if the client asks for them
                else if (typeName.equals("csw:BriefRecord"))
                    throw new InvalidParameterValueEx("typeName", typeName);

                else if (typeName.equals("csw:SummaryRecord"))
                    throw new InvalidParameterValueEx("typeName", typeName);

            }
        }

        return hs;
    }

    //------------------------------------------------------------------------

    private String typeName;
}
