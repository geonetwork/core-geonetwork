package org.fao.geonet.kernel;

import java.util.Comparator;
import java.util.List;

class PrioritizedLangComparator implements Comparator<String> {
        private String defaultLang;
        private List<String> prioritizedList;

        public PrioritizedLangComparator(String defaultLang, List<String> prioritizedList) {
            this.defaultLang = defaultLang;
            this.prioritizedList = prioritizedList;
        }

        public int compare(String lang1, String lang2) {
            int value = val(lang2) - val(lang1);
            if(value == 0) {
                return lang1.compareToIgnoreCase(lang2);
            }
            return value;
        }

        private int val(String lang) {
            if(lang.equalsIgnoreCase(defaultLang)) {
                return 1000;
            }

            int index = prioritizedList.indexOf(lang);
            if(index > 0) {
                return prioritizedList.size() - index;
            }
            return -1;
        }
    }