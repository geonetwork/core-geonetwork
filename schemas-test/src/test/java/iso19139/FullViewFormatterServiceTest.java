package iso19139;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

/**
 * @author Jesse on 10/17/2014.
 */
public class FullViewFormatterServiceTest extends FullViewFormatterTest {

    protected List<String> excludes() {
        return Lists.newArrayList(
                "> gmd:MD_Metadata > gmd:identificationInfo > gmd:MD_DataIdentification > gmd:citation > gmd:CI_Citation > gmd:title > " +
                "gco:PT_FreeText > gco:textGroup > gmd:LocalisedCharacterString > Text"
        );
    }

    @Override
    protected File getTestMetadataFile() {
        final String mdFile = FullViewFormatterServiceTest.class.getResource("/iso19139/example-service.xml").getFile();
        return new File(mdFile);
    }
}
