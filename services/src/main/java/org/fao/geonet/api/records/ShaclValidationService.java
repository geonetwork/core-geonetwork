package org.fao.geonet.api.records;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Resource;
import jeeves.server.context.ServiceContext;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.fao.geonet.api.records.formatters.FormatType;
import org.fao.geonet.api.records.formatters.FormatterApi;
import org.fao.geonet.api.records.formatters.FormatterWidth;
import org.fao.geonet.api.records.formatters.cache.Key;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.MetadataValidationId;
import org.fao.geonet.domain.MetadataValidationStatus;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class ShaclValidationService {

    private static final Map<String, Lang> OUTPUT_MAP = Map.of(
        MediaType.APPLICATION_JSON_VALUE, Lang.JSONLD,
        "application/ld+json", Lang.JSONLD,
        MediaType.APPLICATION_XML_VALUE, Lang.RDFXML,
        "application/rdf+xml", Lang.RDFXML,
        "text/turtle", Lang.TURTLE,
        "text/n3", Lang.N3,
        "application/n-triples", Lang.NTRIPLES
    );

    @Resource(name = "shaclValidatorTestsuites")
    private Map<String, String[]> testsuites;

    @Autowired
    MetadataValidationRepository metadataValidationRepository;

    @Autowired
    private IMetadataIndexer metadataIndexer;

    @Autowired
    private GeonetworkDataDirectory dataDirectory;

    public String convertMetadataToRdf(AbstractMetadata metadata, String formatter, ServiceContext context) {
        try {
            Key key = new Key(metadata.getId(), "eng", FormatType.xml, formatter, true, FormatterWidth._100);
            byte[] data = new FormatterApi().new FormatMetadata(context, key, null).call().data;
            return new String(data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Error loading metadata: " + e.getMessage();
        }
    }

    public List<String> getShaclValidationFiles() {
        Path shaclRulesFolder = dataDirectory.getConfigDir().resolve("shacl");
        try (Stream<Path> paths = Files.walk(shaclRulesFolder)) {
            return paths.filter(path -> path.toString().endsWith(".ttl"))
                .map(path -> shaclRulesFolder.relativize(path).toString())
                .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<String> getShaclValidationTestsuites() {
        return testsuites.keySet().stream()
            .collect(Collectors.toList());
    }

    public String validate(String formatter, AbstractMetadata metadata,
                           String testsuite, List<String> shaclShapes,
                           ServiceContext context,
                           String outputFormat, boolean isSavingValidationStatus) {
        String rdfToValidate = convertMetadataToRdf(metadata, formatter, context);

        shaclShapes = getShaclShapes(testsuite, shaclShapes);
        Shapes shapes = parseShapesFromFiles(shaclShapes);
        Reasoner reasoner = configureReasoner(shapes.getImports());

        Model dataModel = ModelFactory.createDefaultModel();
        try (StringReader reader = new StringReader(rdfToValidate)) {
            RDFDataMgr.read(dataModel, reader, null, Lang.RDFXML);
        } catch (Exception e) {
            return buildStatusResponse("Document is not valid RDF/XML: " + e.getMessage(), false);
        }

        long violationCount = 0;
        String validationReportKey = buildValidationReportKey(formatter, testsuite, shaclShapes);

        Model infModel = ModelFactory.createInfModel(reasoner, dataModel);
        ValidationReport report = ShaclValidator.get().validate(shapes, infModel.getGraph());

        if (!report.conforms()) {
            violationCount = report.getEntries().stream()
                .filter(e -> e.severity().level().getURI().equals("http://www.w3.org/ns/shacl#Violation"))
                .count();

            if (isSavingValidationStatus) {
                saveValidationStatus(metadata, validationReportKey, violationCount);
            }

            ShLib.printReport(report);
            StringWriter writer = new StringWriter();
            RDFDataMgr.write(writer, report.getModel(), OUTPUT_MAP.getOrDefault(outputFormat, Lang.RDFXML));
            return writer.toString();
        }

        if (isSavingValidationStatus) {
            saveValidationStatus(metadata, validationReportKey, violationCount);
        }

        return buildStatusResponse(String.format("Document in format %s is valid according to testsuite %s.", formatter, testsuite), true);
    }

    private List<String> getShaclShapes(String testsuite, List<String> shaclShapes) {
        List<String> testSuiteShapes = testsuite == null ? List.of() : List.of(testsuites.get(testsuite));
        if (!testSuiteShapes.isEmpty()){
            shaclShapes = testSuiteShapes;
        }
        return shaclShapes;
    }

    private Shapes parseShapesFromFiles(List<String> shaclFiles) {
        MultiUnion shapesGraph = new MultiUnion();
        for (String shaclFile : shaclFiles) {
            Path shaclPath = dataDirectory.getConfigDir().resolve("shacl").resolve(shaclFile);
            if (!Files.exists(shaclPath)) {
                throw new IllegalArgumentException("SHACL shape file not found: " + shaclPath);
            }
            shapesGraph.addGraph(RDFDataMgr.loadGraph(shaclPath.toString()));
        }
        return Shapes.parse(shapesGraph);
    }

    private Reasoner configureReasoner(Collection<Node> imports) {
        Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
        Model combinedOntologyModel = ModelFactory.createDefaultModel();

        for (Node importedShape : imports) {
            Model model = ModelFactory.createOntologyModel();
            combinedOntologyModel.add(model.read(importedShape.getURI().toString()));
        }
        return reasoner.bindSchema(combinedOntologyModel);
    }

    private static String buildStatusResponse(String message, boolean isValid) {
        return String.format("{\"valid\": %s, \"message\": \"%s\"}", isValid, message);
    }

    private static String buildValidationReportKey(String formatter, String testsuite, List<String> shaclShapes) {
        return String.format("shacl-%s-%s",
            formatter,
            StringUtils.isNotEmpty(testsuite) ? testsuite :
                // Use a hash of the shapes to create a unique key
                // Shorten to 8 first characters
                DigestUtils.sha256Hex(shaclShapes.stream().sorted().collect(Collectors.joining())).substring(0, 8)
        );
    }

    private void saveValidationStatus(AbstractMetadata metadata, String validationReportKey, long violationCount) {
        MetadataValidation validation = new MetadataValidation().setId(new MetadataValidationId(metadata.getId(), validationReportKey))
            .setStatus(violationCount > 0 ? MetadataValidationStatus.INVALID : MetadataValidationStatus.VALID)
            .setRequired(true)
            .setNumTests(1)
            .setNumFailures((int) violationCount);
        metadataValidationRepository.save(validation);

        try {
            metadataIndexer.indexMetadata(List.of(metadata.getId() + ""));
        } catch (Exception e) {
            Log.debug(Geonet.DATA_MANAGER, "Exception while indexing metadata after SHACL validation: {}", e.getMessage(), e);
        }
    }
}
