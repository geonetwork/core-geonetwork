package org.fao.geonet.utils;

import name.fraser.neil.plaintext.diff_match_patch;
import org.jdom.Element;

import java.util.LinkedList;

public class Diff {

    public static String diff(String oldVersion, String newVersion, DiffType diffType) {
        diff_match_patch dmp = new diff_match_patch();
        if (DiffType.patch.equals(diffType)) {
            LinkedList<diff_match_patch.Patch> diffs =
                dmp.patch_make(oldVersion, newVersion);
            return diffs.toString();
        } else if (DiffType.diff.equals(diffType)) {
            LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(oldVersion, newVersion);
            return diffs.toString();
        } else {
            LinkedList<diff_match_patch.Diff> diffs = dmp.diff_main(oldVersion, newVersion);
            return dmp.diff_prettyHtml(diffs);
        }
    }
}
