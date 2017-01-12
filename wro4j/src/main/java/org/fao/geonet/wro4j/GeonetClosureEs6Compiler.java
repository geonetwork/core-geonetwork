package org.fao.geonet.wro4j;

import com.google.javascript.jscomp.*;
import com.google.javascript.jscomp.Compiler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.extensions.processor.support.ObjectPoolHelper;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.Destroyable;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ObjectFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.logging.Level;

/**
 * Custom Geonetwork implementation for importing stylesheets.  Main factor is to make it more
 * forgiving.
 * <p/>
 * User: fgravin Date: 10/01/2017 Time: 3:29 PM
 */
@SupportedResourceType(ResourceType.JS)
public class GeonetClosureEs6Compiler implements ResourcePostProcessor, ResourcePreProcessor, Destroyable {

    public static final String ALIAS = "geonetClosureEs6Compiler";

    private CompilationLevel compilationLevel;
    private ObjectPoolHelper<CompilerOptions> optionsPool;
    @Inject
    private ReadOnlyContext context;
    private String encoding;

    public GeonetClosureEs6Compiler() {
        this(CompilationLevel.SIMPLE_OPTIMIZATIONS);
    }

    public GeonetClosureEs6Compiler(CompilationLevel compilationLevel) {
        Validate.notNull(compilationLevel);
        this.optionsPool = new ObjectPoolHelper(new ObjectFactory() {
            public CompilerOptions create() {
                return GeonetClosureEs6Compiler.this.newCompilerOptions();
            }
        });
        this.compilationLevel = compilationLevel;
    }

    public void process(Resource resource, Reader reader, Writer writer) throws IOException {
        String content = IOUtils.toString(reader);
        CompilerOptions compilerOptions = (CompilerOptions)this.optionsPool.getObject();
        Compiler compiler = this.newCompiler(compilerOptions);

        try {
            String e = resource == null?"wro4j-processed-file.js":resource.getUri();
            SourceFile[] input = new SourceFile[]{SourceFile.fromInputStream(e, new ByteArrayInputStream(content.getBytes(this.getEncoding())))};
            SourceFile[] externs = this.getExterns(resource);
            if(externs == null) {
                externs = new SourceFile[0];
            }

            Result result = null;
            result = compiler.compile(Arrays.asList(externs), Arrays.asList(input), compilerOptions);
            if(!result.success) {
                throw new WroRuntimeException("Compilation has errors: " + Arrays.asList(result.errors));
            }

            writer.write(compiler.toSource());
        } catch (Exception var14) {
            this.onException(var14);
        } finally {
            reader.close();
            writer.close();
            this.optionsPool.returnObject(compilerOptions);
        }

    }

    protected void onException(Exception e) {
        throw WroRuntimeException.wrap(e);
    }

    private String getEncoding() {
        if(this.encoding == null) {
            this.encoding = Context.isContextSet()?this.context.getConfig().getEncoding():"UTF-8";
        }

        return this.encoding;
    }

    private Compiler newCompiler(CompilerOptions compilerOptions) {
        Compiler.setLoggingLevel(Level.SEVERE);
        Compiler compiler = new Compiler();
        this.compilationLevel.setOptionsForCompilationLevel(compilerOptions);
        compiler.disableThreads();
        compiler.initOptions(compilerOptions);
        return compiler;
    }

    public GeonetClosureEs6Compiler setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    protected SourceFile[] getExterns(Resource resource) {
        return new SourceFile[0];
    }


    protected CompilerOptions newCompilerOptions() {
        CompilerOptions options = new CompilerOptions();
        //options.setCodingConvention(new ClosureCodingConvention());
        //options.setOutputCharset(this.getEncoding());
        options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT_2015);
        options.setLanguageOut(CompilerOptions.LanguageMode.ECMASCRIPT5_STRICT);
        options.setWarningLevel(DiagnosticGroups.CHECK_VARIABLES, CheckLevel.WARNING);
        return options;
    }

    public void process(Reader reader, Writer writer) throws IOException {
        this.process((Resource)null, reader, writer);
    }

    public void destroy() throws Exception {
        this.optionsPool.destroy();
    }

}
