package org.fao.geonet.jms.message.versioning;


import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.jms.ClusterConfig;
import org.fao.geonet.jms.ClusterException;
import org.fao.geonet.jms.message.MessageHandler;
import org.fao.geonet.kernel.SvnManager;
import org.jdom.Element;

/**
 * @author jose garcia
 */
public class MetadataVersioningMessageHandler implements MessageHandler {

    private ServiceContext context;

    public MetadataVersioningMessageHandler(ServiceContext context) {
        this.context = context;
    }

    public void process(String message) throws ClusterException {
        Log.debug(Geonet.CLUSTER, "MetadataVersioningMessageHandler processing message '" + message + "'");

        MetadataVersioningMessage mdVersioningMessage = new MetadataVersioningMessage();
        mdVersioningMessage = mdVersioningMessage.decode(message);
        // message was sent by this GN instance itself; ignore
        if(mdVersioningMessage.getSenderClientID().equals(ClusterConfig.getClientID())) {
            Log.debug(Geonet.CLUSTER, "MetadataVersioningMessageHandler ignoring message from self");
        }

        // message was sent by another GN instance
        else {
            Dbms dbms = null;
            try {
                GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

                SvnManager svnManager = gc.getSvnManager();
                dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
                String idMd;

                switch (mdVersioningMessage.getAction()) {
                    case VERSION_MD:
                        Element md = Xml.loadString(mdVersioningMessage.getMetadataContent(), false);
                        idMd = (String) mdVersioningMessage.getIds().toArray()[0];
                        
                        svnManager.createMetadataDirWithouSendingTopic(idMd, md, mdVersioningMessage.getMetadataLogMessage());
                        break;

                    case ADD_HISTORY:
                        svnManager.commitWithoutSendingTopic(dbms, mdVersioningMessage.getIds(),
                                mdVersioningMessage.getMetadataLogMessage());
                        break;

                    case DELETE_MD:
                        idMd = (String) mdVersioningMessage.getIds().toArray()[0];

                        svnManager.deleteDirWithoutSendingTopic(idMd, mdVersioningMessage.getMetadataLogMessage());
                        break;
                }

            }
            catch(Exception x) {
                Log.error(Geonet.CLUSTER, "Error processing metadata versioning message: " + x.getMessage());
                x.printStackTrace();
                throw new ClusterException(x.getMessage(), x);
            } finally {
                try {
                    // Close dbms connection
                    if (dbms != null) context.getResourceManager().close(Geonet.Res.MAIN_DB, dbms);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}

