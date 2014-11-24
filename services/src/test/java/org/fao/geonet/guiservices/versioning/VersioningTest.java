package org.fao.geonet.guiservices.versioning;

import org.apache.commons.io.FileUtils;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.jdom.JDOMException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Unit tests for SVN versioning service
 */
public class VersioningTest {
    private static String resources = AbstractCoreIntegrationTest.getClassFile(VersioningTest.class).getParent();
    private SVNURL tgtURL;
    @Rule
    public TemporaryFolder localRepo = new TemporaryFolder();
    @Rule
    public TemporaryFolder workingFolder = new TemporaryFolder();


    @Before
    public void setUp() throws Exception {
        SVNRepositoryFactoryImpl.setup();
        tgtURL = SVNRepositoryFactory.createLocalRepository(localRepo.getRoot(), true, true);
        FileUtils.forceMkdir(workingFolder.getRoot());
    }


    @Test
    public void testConversion() throws SVNException, IOException {
        SVNClientManager ourClientManager = SVNClientManager.newInstance();
        // checkout working copy
        SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);
        updateClient.doCheckout(tgtURL, workingFolder.getRoot(), SVNRevision.UNDEFINED, SVNRevision.HEAD, SVNDepth.INFINITY, true);
        // add file under version control
        int id1 = 45;
        create(ourClientManager, id1);
        modify(ourClientManager, id1);
        int id2 = 2;
        create(ourClientManager, id2);
        modify(ourClientManager, id2);

        delete(ourClientManager, id1);
        delete(ourClientManager, id2);

        final SVNRepository repository = SVNRepositoryFactory.create(tgtURL);
        MyLogEntryHandler logEntryHandler = new MyLogEntryHandler();
        repository.log(new String[]{""}, 0, -1, true, true, logEntryHandler);
        final List<MetadataAction> metadataActionList = logEntryHandler.getMetadataActionList();
        // only partial assertion of entered data
        for (MetadataAction metadataAction : metadataActionList) {
            Assert.assertEquals("admin", metadataAction.getUsername());
            Assert.assertEquals("0:0:0:0:0:0:0:1", metadataAction.getIp());
        }
        final MetadataAction firstEntry = metadataActionList.get(0);
        Assert.assertEquals("all", firstEntry.getSubject());
        Assert.assertEquals(id1, firstEntry.getId());
        Assert.assertEquals('A', firstEntry.getAction());
        Assert.assertNotNull(firstEntry.getDate());
        final MetadataAction lastEntry = metadataActionList.get(metadataActionList.size() - 1);
        Assert.assertEquals('D', lastEntry.getAction());
        Assert.assertEquals("all", lastEntry.getSubject());

        ourClientManager.dispose();
    }

    @Test
    public void testTitleAdding() throws SVNException, IOException, JDOMException {
        SVNClientManager ourClientManager = SVNClientManager.newInstance();
        // checkout working copy
        SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
        updateClient.setIgnoreExternals(false);
        updateClient.doCheckout(tgtURL, workingFolder.getRoot(), SVNRevision.UNDEFINED, SVNRevision.HEAD, SVNDepth.INFINITY, true);
        // add file under version control
        int id = 1;
        create(ourClientManager, id);
        modify(ourClientManager, id);
        delete(ourClientManager, id);
        final SVNRepository repository = SVNRepositoryFactory.create(tgtURL);
        MyLogEntryHandler logEntryHandler = new MyLogEntryHandler();
        repository.log(new String[]{""}, 0, -1, true, true, logEntryHandler);

        final List<MetadataAction> metadataActions = logEntryHandler.getMetadataActionList();

        Get get = new Get();
        get.addTitles(metadataActions, repository.getLocation().getPath());
        Assert.assertEquals("Template for Vector data in ISO19139 (preferred!)", metadataActions.get(0).getTitle());
        Assert.assertEquals("Modified metadata name", metadataActions.get(1).getTitle());
        Assert.assertEquals("Modified metadata name", metadataActions.get(2).getTitle());
    }

    /**
     * Converts and prints out the svnlogs of the original geonetwork svn repoository.
     * @throws org.tmatesoft.svn.core.SVNException
     * @throws java.io.IOException
     * @throws org.jdom.JDOMException
     */
/*      public void testAddedRealRepo() throws SVNException, IOException, JDOMException {
        final String localRepoLocation = getRootPath() + FS + "src" + FS + "main" + FS + "webapp" + FS + "WEB-INF" + FS + "data" + FS + "data" + FS + "metadata_subversion";
        final SVNURL svnurl = SVNURL.fromFile(new File(localRepoLocation));
        final SVNRepository repository = SVNRepositoryFactory.create(svnurl);
        MyLogEntryHandler logEntryHandler = new MyLogEntryHandler();
        repository.log(new String[]{""}, 0, -1, true, true, logEntryHandler);
        final List<MetadataAction> metadataActions = logEntryHandler.getMetadataActionList();
        Get get = new Get();
        get.addTitles(metadataActions, localRepoLocation);
        for (MetadataAction metadataAction : metadataActions) {
            System.out.println("Revision " + metadataAction.getRevision() + " id " + metadataAction.getId() + " title " + metadataAction.getTitle());
        }
    }*/

    /**
     * Creates metadata in a repository exactly as geonetwork would do it.
     * @param ourClientManager
     * @param id
     * @throws java.io.IOException
     * @throws org.tmatesoft.svn.core.SVNException
     */
    private void create(SVNClientManager ourClientManager, int id) throws IOException, SVNException {
        File dir = new File(workingFolder.getRoot().getPath() + "/" + id + "/");
        FileUtils.forceMkdir(dir);
        // add directory
        ourClientManager.getWCClient().doAdd(dir, false, false, true, SVNDepth.FILES, false, true, true);
        final String dirMsg = "GeoNetwork service: metadata.create GeoNetwork User 1 (Username: admin Name: admin admin) Executed from IP address 0:0:0:0:0:0:0:1 adding directory for metadata " + id;
        // commit directory
        ourClientManager.getCommitClient().doCommit(new File[]{dir}, false, dirMsg, null, null, true, true, SVNDepth.INFINITY);
        //put files in working copy
        final String pfn = "privileges.xml";
        File privileges = new File(resources, pfn);
        final String mfn = "metadata.xml";
        File metadata = new File(resources, mfn);
        final String cfn = "categories.xml";
        File categories = new File(resources, cfn);
        final String ofn = "owner.xml";
        File owner = new File(resources, ofn);
        FileUtils.copyFileToDirectory(privileges, dir);
        FileUtils.copyFileToDirectory(metadata, dir);
        FileUtils.copyFileToDirectory(categories, dir);
        FileUtils.copyFileToDirectory(owner, dir);
        final String dp = dir.getPath();
        File[] files = {new File(dp+"/"+pfn), new File(dp+"/"+mfn), new File(dp+"/"+cfn), new File(dp+"/"+ofn)};
        // add files
        ourClientManager.getWCClient().doAdd(files, false, false, true, SVNDepth.FILES, false, true, false);
        final String fileMsg = "GeoNetwork service: metadata.create GeoNetwork User 1 (Username: admin Name: admin admin) Executed from IP address 0:0:0:0:0:0:0:1 adding initial version of metadata " + id;
        // commit files
        ourClientManager.getCommitClient().doCommit(files, false, fileMsg, null, null, true, true, SVNDepth.INFINITY);
    }

    /**
     * Modifies metadata in a repository exactly as geonetwork would do it.
     * @param ourClientManager
     * @param id
     * @throws java.io.IOException
     * @throws org.tmatesoft.svn.core.SVNException
     */
    private void modify(SVNClientManager ourClientManager, int id) throws IOException, SVNException {
        File dir = new File(workingFolder.getRoot().getPath() + "/" + id + "/");
        final String mfn = "modifiedmetadata.xml";
        File metadata = new File(resources, mfn);
        final File fileToBeCommited = new File(dir.getPath(), "metadata.xml");
        FileUtils.copyFile(metadata, fileToBeCommited);
        String fileMsg = "GeoNetwork service: metadata.update.finish GeoNetwork User 1 (Username: admin Name: admin admin) Executed from IP address 0:0:0:0:0:0:0:1 (committing dbms session jeeves.resources.dbms.Dbms@199d1e1)";
        ourClientManager.getCommitClient().doCommit(new File[]{fileToBeCommited}, false, fileMsg, null, null, true, false, SVNDepth.FILES);
    }

    /**
     * Deletes metadata in a repository exactly as geonetwork would do it.
     * @param ourClientManager
     * @param id
     * @throws java.io.IOException
     * @throws org.tmatesoft.svn.core.SVNException
     */
    private void delete(SVNClientManager ourClientManager, int id) throws IOException, SVNException {
        final File dir = new File(workingFolder.getRoot(), ""+id);
        ourClientManager.getWCClient().doDelete(dir, false, true, false);
        String delMsg = "GeoNetwork service: metadata.batch.delete GeoNetwork User 1 (Username: admin Name: admin admin) Executed from IP address 0:0:0:0:0:0:0:1 deleting directory for metadata " + id;
        ourClientManager.getUpdateClient().doUpdate(workingFolder.getRoot(), SVNRevision.HEAD, SVNDepth.INFINITY, false, false);
        ourClientManager.getCommitClient().doCommit(new File[]{workingFolder.getRoot()}, false, delMsg, null, null, false, false, SVNDepth.INFINITY);
    }
}
