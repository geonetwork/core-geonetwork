package org.fao.geonet.utils;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.jdom.Element;

import java.util.LinkedList;

public class Diff {

    public static String diff(String oldVersion, String newVersion, DiffType diffType) {
        DiffMatchPatch dmp = new DiffMatchPatch();
        if (DiffType.patch.equals(diffType)) {
            LinkedList<DiffMatchPatch.Patch> diffs =
                dmp.patchMake(oldVersion, newVersion);
            return diffs.toString();
        } else if (DiffType.diff.equals(diffType)) {
            LinkedList<DiffMatchPatch.Diff> diffs = dmp.diffMain(oldVersion, newVersion);
            return diffs.toString();
        } else {
            LinkedList<DiffMatchPatch.Diff> diffs = dmp.diffMain(oldVersion, newVersion);
            return dmp.diffPrettyHtml(diffs);
        }
    }
}
