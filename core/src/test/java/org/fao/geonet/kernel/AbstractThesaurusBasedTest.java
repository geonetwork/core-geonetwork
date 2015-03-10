package org.fao.geonet.kernel;

import org.fao.geonet.kernel.search.keyword.KeywordRelation;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.utils.IO;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.openrdf.sesame.Sesame;
import org.openrdf.sesame.config.ConfigurationException;
import org.openrdf.sesame.config.RepositoryConfig;
import org.openrdf.sesame.config.SailConfig;
import org.openrdf.sesame.constants.RDFFormat;
import org.openrdf.sesame.repository.local.LocalRepository;

import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractThesaurusBasedTest {
	protected static final String THESAURUS_KEYWORD_NS = "http://abstract.thesaurus.test#";
    protected static final IsoLanguagesMapper isoLangMapper = new IsoLanguagesMapper() {
		{
			_isoLanguagesMap639.put("en", "eng");
			_isoLanguagesMap639.put("de", "ger");
			_isoLanguagesMap639.put("fr", "fre");
			_isoLanguagesMap639.put("it", "ita");
		}
	};
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    protected Path thesaurusFile;
    protected Thesaurus thesaurus;
    
    protected final static int keywords = 1000;
    protected final String[] languages = {"eng","ger","fre","ita"};
    private final boolean readonly;

    public AbstractThesaurusBasedTest(boolean readonly) {
        this.readonly = readonly;
    }
    
    @Before
    public void beforeTest() throws Exception {
        createTestThesaurus();

        if(!readonly) {
            final String className = getClass().getSimpleName() + ".class";
            Path template = thesaurusFile;
            
            // Now make copy for this test
            this.thesaurusFile = this.folder.getRoot().toPath().resolve(getClass().getSimpleName()+"TestThesaurus.rdf");

            Files.copy(template, thesaurusFile);
            this.thesaurus = new Thesaurus(isoLangMapper, thesaurusFile.getFileName().toString(), "test", "test",
                    thesaurusFile, "http://concept");
        }
        this.thesaurus.initRepository();
    }

    @After
    public void afterTest() throws Exception {
        thesaurus.getRepository().shutDown();
        if(!readonly) {
            Files.deleteIfExists(this.thesaurusFile);
        }
    }
    
    private void createTestThesaurus() throws Exception {
        final String className = AbstractThesaurusBasedTest.class.getSimpleName() + ".class";
        Path directory = IO.toPath(AbstractThesaurusBasedTest.class.getResource(className).toURI()).getParent();

        this.thesaurusFile = directory.resolve("testThesaurus.rdf");
        this.thesaurus = new Thesaurus(isoLangMapper, thesaurusFile.getFileName().toString(), null, null, "test", "test",
                thesaurusFile, "http://concept", true);
        this.thesaurus.initRepository();
    }

	private void populateThesaurus() throws Exception {
	    populateThesaurus(this.thesaurus, keywords, THESAURUS_KEYWORD_NS, "testValue", "testNote", languages);
	}
	/**
	 * Generate a thesaurus with the provided number of words etc...
	 */
    protected void populateThesaurus(Thesaurus thesaurus, int words, String namespace, String valueStem, String noteStem, String... languages) throws Exception {
        String east = "0";
        String west = "10";
        String south = "5";
        String north = "15";

        System.out.println("Generating a test RDF file:"+thesaurus.getFile()+".  This might take several minutes.");
    	long lastUpdateTime = System.currentTimeMillis();
    	System.out.print(0);    	
        for(int i = 0; i < words; i++) {
        	float percent = ((float)i)/words * 100;
        	if(System.currentTimeMillis() - lastUpdateTime > 5000) {
        		System.out.print(Math.round(percent));
        		lastUpdateTime = System.currentTimeMillis();
        	}
        	if(i % 10 == 1) {
        		System.out.print(".");
        	}
            
        	String code = namespace+i;
            KeywordBean keyword = new KeywordBean(isoLangMapper).setUriCode(code);
            // 1/2 of elements will have bounds
            if(i % 2 == 0) {
                keyword.setCoordEast(east)
                    .setCoordNorth(north)
                    .setCoordSouth(south)
                    .setCoordWest(west);
            }
            for(String lang : languages) {
				String prefLab = createExampleLabel(i, valueStem, lang);
				String note = createExampleNote(i, noteStem, lang);
				
				keyword.setValue(prefLab, lang).setDefinition(note, lang);
            }
            thesaurus.addElement(keyword);
            if(i > 0 && i % 20 == 0) {
                thesaurus.addRelation(code,KeywordRelation.NARROWER, namespace+(i-5));
            }
            if(i > 0 && i % 25 == 0) {
                thesaurus.addRelation(code,KeywordRelation.RELATED, namespace+(i-3));
            }
        }
        
        System.out.println("100");
    }

    protected String createExampleNote(int i, String note, String lang) {
        return i+"_"+note+"_"+lang;
    }

    protected String createExampleLabel(int i, String value, String lang) {
        return i+"_"+value+"_"+lang;
    }

    protected String createExampleNote(int i, String lang) {
        return i+"_testNote_"+lang;
    }

    protected String createExampleLabel(int i, String lang) {
        return i+"_testValue_"+lang;
    }

}
