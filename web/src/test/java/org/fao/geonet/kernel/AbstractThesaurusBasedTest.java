package org.fao.geonet.kernel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.kernel.rdf.QueryBuilder;
import org.fao.geonet.kernel.search.keyword.KeywordRelation;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.junit.After;
import org.junit.Before;
import org.openrdf.sesame.Sesame;
import org.openrdf.sesame.config.ConfigurationException;
import org.openrdf.sesame.config.RepositoryConfig;
import org.openrdf.sesame.config.SailConfig;
import org.openrdf.sesame.constants.RDFFormat;
import org.openrdf.sesame.repository.local.LocalRepository;

public abstract class AbstractThesaurusBasedTest {
	protected static final String THESAURUS_KEYWORD_NS = "http://abstract.thesaurus.test#";
    protected static final IsoLanguagesMapper isoLangMapper = new IsoLanguagesMapper() {
		{
			isoLanguagesMap639.put("en", "eng");
			isoLanguagesMap639.put("de", "ger");
			isoLanguagesMap639.put("fr", "fre");
			isoLanguagesMap639.put("it", "ita");
		}
	};
    protected File thesaurusFile;
    protected Thesaurus thesaurus;
    
    protected final static int keywords = 1000;
    protected final String[] languages = {"eng","ger","fre","ita"};
    private final boolean readonly;

    public AbstractThesaurusBasedTest(boolean readonly) {
        this.readonly = readonly;
    }
    
    @Before
    public void beforeTest() throws Exception {
        generateTestThesaurus();

        if(!readonly) {
            File directory = new File(getClass().getResource(getClass().getSimpleName()+".class").getFile()).getParentFile();
            File template = thesaurusFile;
            
            // Now make copy for this test
            this.thesaurusFile = new File(directory, getClass().getSimpleName()+"TestThesaurus.rdf");
            this.thesaurusFile.deleteOnExit();
            FileChannel to = new FileOutputStream(thesaurusFile).getChannel();
            FileChannel from = new FileInputStream(template).getChannel();
            try {
            	to.transferFrom(from, 0, template.length());
            } finally {
            	IOUtils.closeQuietly(from);
            	IOUtils.closeQuietly(to);
            }
            FileUtils.copyFile(template, thesaurusFile);
            this.thesaurus = new Thesaurus(isoLangMapper, thesaurusFile.getName(), "test", "test", thesaurusFile, "http://concept");
        }
        setRepository(this.thesaurus);
    }

    @After
    public void afterTest() throws Exception {
        thesaurus.getRepository().shutDown();
        if(!readonly) {
            this.thesaurusFile.delete();
        }
    }
    
    private void generateTestThesaurus() throws Exception {
        File directory = new File(AbstractThesaurusBasedTest.class.getResource(AbstractThesaurusBasedTest.class.getSimpleName()+".class").getFile()).getParentFile();

        this.thesaurusFile = new File(directory, "testThesaurus.rdf");
        this.thesaurus = new Thesaurus(isoLangMapper, thesaurusFile.getName(), "test", "test", thesaurusFile, "http://concept", true);
        setRepository(this.thesaurus);
        
        if (thesaurusFile.exists() && thesaurusFile.length() > 0) {
            try {
            	QueryBuilder.builder().selectId().limit(1).build().rawExecute(thesaurus);
            } catch (Exception e) {
                this.thesaurusFile.delete();
                populateThesaurus();
            }
        } else {
            populateThesaurus();
        }
        thesaurus.getRepository().shutDown();
    }

	protected static void setRepository(Thesaurus thesaurus) throws ConfigurationException {
		RepositoryConfig repConfig = new RepositoryConfig(thesaurus.getKey());

        SailConfig syncSail = new SailConfig("org.openrdf.sesame.sailimpl.sync.SyncRdfSchemaRepository");
        SailConfig memSail = new org.openrdf.sesame.sailimpl.memory.RdfSchemaRepositoryConfig(thesaurus.getFile().getAbsolutePath(),
                RDFFormat.RDFXML);
        repConfig.addSail(syncSail);
        repConfig.addSail(memSail);
        repConfig.setWorldReadable(true);
        repConfig.setWorldWriteable(true);

        LocalRepository thesaurusRepository = Sesame.getService().createRepository(repConfig);
        thesaurus.setRepository(thesaurusRepository);
	}
	private void populateThesaurus() throws Exception {
	    populateThesaurus(this.thesaurus, keywords, THESAURUS_KEYWORD_NS, "testValue", "testNote",languages);
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
