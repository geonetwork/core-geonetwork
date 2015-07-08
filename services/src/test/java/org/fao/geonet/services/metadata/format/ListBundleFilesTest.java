package org.fao.geonet.services.metadata.format;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.nio.file.Path;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.util.ReflectionUtils;

public class ListBundleFilesTest {

    @Test
    public void testIsEditibleFileType() {
        Path p = Mockito.mock(Path.class);
        Mockito.when(p.getFileName()).thenReturn(p);
        // extensions = {"properties", "xml", "xsl", "css", "js"};
        Mockito.when(p.toString()).thenReturn("file.properties", "manifest.xml", "view.xsl", "custom.css", "custom.js", "README");
        
        ListBundleFiles lst = new ListBundleFiles();
        Method prvMeth = ReflectionUtils.findMethod(lst.getClass(), "isEditibleFileType", Path.class);
        prvMeth.setAccessible(true);
        
        Boolean[] rets = new Boolean[6];
        for (int i = 0; i < 6; ++i) {
            rets[i] = (Boolean) ReflectionUtils.invokeMethod(prvMeth, lst, p);
            
        }
        assertTrue("isEditibleFileType(\"file.properties\"): Expected true, false returned", rets[0]);
        assertTrue("isEditibleFileType(\"manifest.xml\"): Expected true, false returned", rets[1]);
        assertTrue("isEditibleFileType(\"view.xsl\"): Expected true, false returned", rets[2]);
        assertTrue("isEditibleFileType(\"custom.css\"): Expected true, false returned", rets[3]);
        assertTrue("isEditibleFileType(\"custom.js\"): Expected true, false returned", rets[4]);
        assertTrue("isEditibleFileType(\"README\"): Expected false, true returned", rets[5]);
        
    }

}
