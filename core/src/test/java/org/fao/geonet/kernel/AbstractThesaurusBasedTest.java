/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel;

import net.jcip.annotations.NotThreadSafe;
import org.fao.geonet.kernel.search.keyword.KeywordRelation;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.utils.IO;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;

@NotThreadSafe  // randomly failing without that
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
    protected final static int keywords = 1000;
    protected final String[] languages = {"eng", "ger", "fre", "ita"};
    private final boolean readonly;
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    protected Path thesaurusFile;
    protected Thesaurus thesaurus;

    public AbstractThesaurusBasedTest(boolean readonly) {
        this.readonly = readonly;
    }

    @Before
    public void beforeTest() throws Exception {
        createTestThesaurus();

        if (!readonly) {
            Path template = this.thesaurusFile;

            // Now make copy for this test
            this.thesaurusFile = locateThesaurus(getClass().getSimpleName() + "TestThesaurus.rdf");

            Files.copy(template, thesaurusFile);
            this.thesaurus = new Thesaurus(isoLangMapper, thesaurusFile.getFileName().toString(), "test", "test",
                thesaurusFile, "http://concept");
        }
        this.thesaurus.initRepository();
    }

	protected Path locateThesaurus(String name) {
		return this.folder.getRoot().toPath().resolve(name);
	}

    @After
    public void afterTest() throws Exception {
        thesaurus.getRepository().shutDown();
        if (!readonly) {
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

    /**
     * Generate a thesaurus with the provided number of words etc...
     */
    protected void populateThesaurus(Thesaurus thesaurus, int words, String namespace, String valueStem, String noteStem, String... languages) throws Exception {
        String east = "0";
        String west = "10";
        String south = "5";
        String north = "15";

        System.out.println("Generating a test RDF file:" + thesaurus.getFile() + ".  This might take several minutes.");
        long lastUpdateTime = System.currentTimeMillis();
        System.out.print(0);
        for (int i = 0; i < words; i++) {
            float percent = ((float) i) / words * 100;
            if (System.currentTimeMillis() - lastUpdateTime > 1000) {
                System.out.print(Math.round(percent));
                lastUpdateTime = System.currentTimeMillis();
            }
            if (i % 10 == 1) {
                System.out.print(".");
            }

            String code = namespace + i;
            KeywordBean keyword = new KeywordBean(isoLangMapper).setUriCode(code);
            // 1/2 of elements will have bounds
            if (i % 2 == 0) {
                keyword.setCoordEast(east)
                    .setCoordNorth(north)
                    .setCoordSouth(south)
                    .setCoordWest(west);
            }
            for (String lang : languages) {
                String prefLab = createExampleLabel(i, valueStem, lang);
                String note = createExampleNote(i, noteStem, lang);

                keyword.setValue(prefLab, lang).setDefinition(note, lang);
            }
            thesaurus.addElement(keyword);
            if (i > 0 && i % 20 == 0) {
                thesaurus.addRelation(code, KeywordRelation.NARROWER, namespace + (i - 5));
            }
            if (i > 0 && i % 25 == 0) {
                thesaurus.addRelation(code, KeywordRelation.RELATED, namespace + (i - 3));
            }
        }

        System.out.println("100");
    }

    protected String createExampleNote(int i, String note, String lang) {
        return i + "_" + note + "_" + lang;
    }

    protected String createExampleLabel(int i, String value, String lang) {
        return i + "_" + value + "_" + lang;
    }

    protected String createExampleNote(int i, String lang) {
        return i + "_testNote_" + lang;
    }

    protected String createExampleLabel(int i, String lang) {
        return i + "_testValue_" + lang;
    }

}
