package org.fao.geonet.kernel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import jeeves.exceptions.JeevesException;
import jeeves.interfaces.Schedule;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ScheduleContext;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Xml;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geocat;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.jdom.Element;
import org.jdom.filter.Filter;
import org.joda.time.DateTime;

public class UnpublishInvalidMetadataJob implements Schedule, Service {

    static final String AUTOMATED_ENTITY = "Automated";

    AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {

    }

    @Override
    public void exec(ScheduleContext context) throws Exception {
        if (new DateTime().getHourOfDay() == 1) {
            GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
            Dbms dbms = (Dbms) context.getResourceManager().openDirect(Geonet.Res.MAIN_DB);
            try {
                performJob(gc, dbms);
            } finally {
                context.getResourceManager().close(Geonet.Res.MAIN_DB, dbms);
            }
        }

    }

    @Override
    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        
        try {
            performJob(gc, dbms);
            return new Element("status").setText("true");
        } catch (Throwable e) {
            return new Element("status").setText("false");
        }
    }

    // --------------------------------------------------------------------------------

    private void performJob(GeonetContext gc, Dbms dbms) throws SQLException, Exception {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Unpublish Job is already running");
        }
        try {
            Integer keepDuration = gc.getSettingManager().getValueAsInt("system/publish_tracking_duration");
            if (keepDuration == null) {
                keepDuration = 100;
            }

            // clean up expired changes
            dbms.execute("DELETE FROM publish_tracking where changedate < current_date-" + Math.max(1, keepDuration));

            List<MetadataRecord> metadataids = lookUpMetadataIds(dbms);

            DataManager dataManager = gc.getDataManager();
            dataManager.startIndexGroup();
            try {
                for (MetadataRecord metadataRecord : metadataids) {
                    String id = "" + metadataRecord.id;
                    try {
                        Record newTodayRecord = validate(gc, metadataRecord, dbms, dataManager);
                        if (newTodayRecord != null) {
                            newTodayRecord.insertInto(dbms);
                        }
                        dataManager.indexMetadataGroup(dbms, id, false, null, false);
                    } catch (Exception e) {
                        String error = Xml.getString(JeevesException.toElement(e));
                        Log.error(Geonet.INDEX_ENGINE, "Error during Validation/Unpublish process of metadata " + id + ".  Exception: "
                                + error);
                    }
                }
            } finally {
                dataManager.endIndexGroup();
            }
        } finally {
            running.set(false);
        }
    }

    private Record validate(GeonetContext gc, MetadataRecord metadataRecord, Dbms dbms, DataManager dataManager) throws Exception {
        String id = "" + metadataRecord.id;
        Element md = gc.getXmlSerializer().select(dbms, "metadata", id, null);
        String schema = gc.getSchemamanager().autodetectSchema(md);
        Record todayRecord = null;
        boolean published = isPublished(id, dbms);
        
        if (published) {
            Element report = dataManager.doValidate(null, dbms, schema, id, md, "eng", false).one();
            Pair<String,String> failureReport = failureReason(report);
            String failureRule = failureReport.one();
            String failureReasons = failureReport.two();
            if (!failureRule.isEmpty()) {
                boolean validated = false;
                String entity = AUTOMATED_ENTITY;
                published = false;
                todayRecord = new Record(metadataRecord.uuid, Validity.fromBoolean(validated), published, entity, failureRule, failureReasons);
                dbms.execute("DELETE FROM operationallowed WHERE metadataid = ? and (groupid = 1 or groupid = -1)", metadataRecord.id);
            }
        }

        return todayRecord;
    }

    public static boolean isPublished(String id, Dbms dbms) throws SQLException {
        @SuppressWarnings("rawtypes")
        List children = dbms.select(
                "SELECT metadataid FROM operationallowed where metadataid=" + id + " and (groupid = 1 or groupid = -1) and operationid = 0").getChildren("record");
        return !children.isEmpty();
    }

    private Pair<String, String> failureReason(Element report) {

        @SuppressWarnings("unchecked")
        Iterator<Element> reports = report.getDescendants(new ReportFinder());

        StringBuilder rules = new StringBuilder();
        StringBuilder failures = new StringBuilder();
        while (reports.hasNext()) {
            report = reports.next();
            if(report.getName().equals("xsderrors")) {
                processXsdError(report, rules, failures);
            } else {
                processSchematronError(report, rules, failures);
            }
        }

        return Pair.read(rules.toString(), failures.toString());
    }

    private void processXsdError(Element report, StringBuilder rules, StringBuilder failures) {
        String reportType = "Xsd Error";
        @SuppressWarnings("unchecked")
        Iterator<Element> errors = report.getDescendants(new ErrorFinder());
        if (errors.hasNext()) {
            if (rules.length() > 0)
                rules.append("\n");
            rules.append(reportType);
        }
        
        while (errors.hasNext()) {
            if (failures.length() > 0)
                failures.append('\n');
            
            Element error = errors.next();
            failures.append (error.getChildText("message", Edit.NAMESPACE));
            failures.append (" xpath[");
            failures.append (error.getChildText("xpath", Edit.NAMESPACE));
            failures.append ("]");
        }
    }

    private void processSchematronError(Element report, StringBuilder rules, StringBuilder failures) {
        String reportType = report.getAttributeValue("rule", Edit.NAMESPACE);
        reportType = reportType == null ? "No name for rule" : reportType;
        if (!reportType.equals(Geocat.INSPIRE_SCHEMATRON_ID)) {
            @SuppressWarnings("unchecked")
            Iterator<Element> errors = report.getDescendants(new ErrorFinder());
            if (errors.hasNext()) {
                if (rules.length() > 0)
                    rules.append("\n");
                rules.append(reportType);
            }
            
            while (errors.hasNext()) {
                if (failures.length() > 0)
                    failures.append('\n');
                
                Element error = errors.next();
                failures.append (error.getAttributeValue("test"));
                failures.append (" xpath[");
                failures.append (error.getAttributeValue("location"));
                failures.append ("]");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<MetadataRecord> lookUpMetadataIds(Dbms dbms) throws SQLException {
        Element results = dbms.select("select id,uuid from metadata where istemplate='n' and isharvested='n'");

        List<MetadataRecord> recordMap = new ArrayList<MetadataRecord>();

        for (Element result : (Collection<Element>) results.getChildren("record")) {
            MetadataRecord record = new MetadataRecord(result);
            recordMap.add(record);
        }

        return recordMap;
    }

    @SuppressWarnings("unchecked")
    static List<Record> values(Dbms dbms, int startOffset, int endOffset) throws Exception {
        Element results = dbms.select("SELECT * from publish_tracking where changedate > current_date-" + startOffset
                + " and changedate <= current_date - " + endOffset);

        List<Record> recordMap = new ArrayList<Record>();

        @SuppressWarnings("rawtypes")
        List children = results.getChildren("record");
        for (Element result : (Collection<Element>) children) {
            Record record = new Record(result);
            recordMap.add(record);
        }

        return recordMap;
    }

    public static enum Validity {
        VALID('y'), INVALID('n'), UNKNOWN('?');
        
        final char dbCode;
        
        private Validity(char dbCode) {
            this.dbCode = dbCode;
        }
        
        public static Validity fromBoolean(boolean validated) {
            return validated ? VALID : INVALID;
        }

        public static Validity parse(char code) {
            for (Validity v : values()) {
                if(v.dbCode == code) return v;
            }
            return UNKNOWN;
        }
    }
    public static class Record {
        final String uuid;
        final String entity;
        final Validity validated;
        final boolean published;
        final String failureRule;
        final String failureReasons;
        final String changeDate;
        final String changeTime;

        Record(Element record) {
            this.uuid = record.getChildTextTrim("uuid");
            this.entity = record.getChildTextTrim("entity");
            this.validated = Validity.parse(record.getChildTextTrim("validated").charAt(0));
            this.published = Boolean.parseBoolean(record.getChildTextTrim("published"));
            this.failureRule = record.getChildTextTrim("failurerule");
            this.failureReasons = record.getChildTextTrim("failurereasons");
            this.changeDate =  record.getChildTextTrim("changedate");
            this.changeTime =  record.getChildTextTrim("changetime");
        }

        public Record(String uuid, Validity validated, boolean published, String entity, String failureRule, String failureReasons) {
            this.uuid = uuid;
            this.entity = entity;
            this.validated = validated;
            this.published = published;
            this.failureRule = failureRule;
            this.failureReasons = failureReasons;
            this.changeDate = null;
            this.changeTime = null;
        }

        public void insertInto(Dbms dbms) throws SQLException {
            dbms.execute("INSERT INTO publish_tracking (uuid, entity, validated, published, failurerule, failurereasons) VALUES (?,?,?,?,?,?)", uuid,
                    entity, String.valueOf(validated.dbCode), codeForDatabase(published), failureRule.replace('"', '\''), failureReasons.replace('"', '\''));
        }

        private String codeForDatabase(boolean value) {
            return value ? "y" : "n";
        }

        public Element toElement() {
            return new Element("record").addContent(new Element("uuid").setText("" + uuid)).addContent(new Element("entity").setText(entity))
                    .addContent(new Element("validated").setText("" + validated))
                    .addContent(new Element("published").setText("" + published))
                    .addContent(new Element("changedate").setText(changeDate))
                    .addContent(new Element("changetime").setText(changeTime))
                    .addContent(new Element("failurerule").setText(failureRule))
                    .addContent(new Element("failurereasons").setText(failureReasons));
        }
    }

    static class MetadataRecord {
        final int id;
        final String uuid;
        final String title;
        final String owner;
        final String changedate;

        MetadataRecord(Element record) {
            this.id = Integer.parseInt(record.getChildTextTrim("id"));
            this.uuid = record.getChildTextTrim("uuid");
            this.title = record.getChildTextTrim("title");
            this.owner = record.getChildTextTrim("owner");
            this.changedate = record.getChildTextTrim("changedate");
        }
    }

    static class ErrorFinder implements Filter {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean matches(Object obj) {
            if (obj instanceof Element) {
                Element element = (Element) obj;
                String name = element.getName();
                if (name.equals("error")) {
                    return true;
                } else if (name.equals("failed-assert")) {
                    return true;
                }
            }
            return false;
        }
    }

    static class ReportFinder implements Filter {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean matches(Object obj) {
            if (obj instanceof Element) {
                Element element = (Element) obj;
                String name = element.getName();
                if (name.equals("report") || name.equals("xsderrors")) {
                    return true;
                }
            }
            return false;
        }
    }
}
