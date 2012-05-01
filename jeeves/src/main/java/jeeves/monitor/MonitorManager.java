package jeeves.monitor;

import static jeeves.constants.ConfigFile.Monitors.Child.SERVICE_CONTEXT_COUNTER;
import static jeeves.constants.ConfigFile.Monitors.Child.SERVICE_CONTEXT_GAUGE;
import static jeeves.constants.ConfigFile.Monitors.Child.SERVICE_CONTEXT_HEALTH_CHECK;
import static jeeves.constants.ConfigFile.Monitors.Child.SERVICE_CONTEXT_HISTOGRAM;
import static jeeves.constants.ConfigFile.Monitors.Child.SERVICE_CONTEXT_METER;
import static jeeves.constants.ConfigFile.Monitors.Child.SERVICE_CONTEXT_TIMER;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import jeeves.constants.ConfigFile;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;

import org.apache.log4j.LogManager;
import org.jdom.Element;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.DummyCounter;
import com.yammer.metrics.core.DummyHistogram;
import com.yammer.metrics.core.DummyMeter;
import com.yammer.metrics.core.DummyTimer;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.log4j.InstrumentedAppender;

/**
 * Contains references to the monitor factories to start for each App
 *
 * User: jeichar
 * Date: 3/29/12
 * Time: 3:42 PM
 */
public class MonitorManager {
    public static final String HEALTH_CHECK_REGISTRY = "com.yammer.metrics.reporting.HealthCheckServlet.registry";
    public static final String METRICS_REGISTRY = "com.yammer.metrics.reporting.MetricsServlet.registry";

    ResourceTracker resourceTracker = new ResourceTracker();
    private final List<HealthCheckFactory> serviceContextHealthChecks = new LinkedList<HealthCheckFactory>();
    private final Map<Class<MetricsFactory<Gauge<?>>>, Gauge<?>> serviceContextGauges = new HashMap<Class<MetricsFactory<Gauge<?>>>, Gauge<?>>();
    private final Map<Class<MetricsFactory<Timer>>, Timer> serviceContextTimers = new HashMap<Class<MetricsFactory<Timer>>, Timer>();
    private final Map<Class<MetricsFactory<Counter>>, Counter> serviceContextCounters = new HashMap<Class<MetricsFactory<Counter>>, Counter>();
    private final Map<Class<MetricsFactory<Histogram>>, Histogram> serviceContextHistogram = new HashMap<Class<MetricsFactory<Histogram>>, Histogram>();
    private final Map<Class<MetricsFactory<Meter>>, Meter> serviceContextMeter = new HashMap<Class<MetricsFactory<Meter>>, Meter>();

    private final HealthCheckRegistry healthCheckRegistry;
    private final MetricsRegistry metricsRegistry;
    public MonitorManager(ServletContext context) {
        if (context != null) {
            HealthCheckRegistry tmpHealthCheckRegistry = null;
            MetricsRegistry tmpMetricsRegistry = null;
            if (context != null) {
                tmpHealthCheckRegistry = (HealthCheckRegistry) context.getAttribute(HEALTH_CHECK_REGISTRY);
                tmpMetricsRegistry = (MetricsRegistry) context.getAttribute(METRICS_REGISTRY);
            }

            if (tmpHealthCheckRegistry == null) {
                tmpHealthCheckRegistry = new HealthCheckRegistry();
            }

            if (tmpMetricsRegistry == null) {
                tmpMetricsRegistry = new MetricsRegistry();
            }

            healthCheckRegistry = tmpHealthCheckRegistry;
            context.setAttribute(HEALTH_CHECK_REGISTRY, tmpHealthCheckRegistry);

            metricsRegistry = tmpMetricsRegistry;
            context.setAttribute(METRICS_REGISTRY, tmpMetricsRegistry);
        } else {
            healthCheckRegistry = new HealthCheckRegistry();
            metricsRegistry = new MetricsRegistry();
        }

        LogManager.getRootLogger().addAppender(new InstrumentedAppender(metricsRegistry));
    }

    public void initMonitorsForApp(ServiceContext context) {
        for (HealthCheckFactory healthCheck : serviceContextHealthChecks) {
            Log.info(Log.ENGINE, "Registering health check : "+healthCheck.getClass().getName());
            healthCheckRegistry.register(healthCheck.create(context));
        }
        for (Class<MetricsFactory<Gauge<?>>> factoryClass : serviceContextGauges.keySet()) {
            Log.info(Log.ENGINE, "Instantiating : "+factoryClass.getName());
            Gauge<?> instance = create(factoryClass, context, SERVICE_CONTEXT_GAUGE);
            serviceContextGauges.put(factoryClass, instance);
        }
        for (Class<MetricsFactory<Timer>> factoryClass : serviceContextTimers.keySet()) {
            Log.info(Log.ENGINE, "Instantiating : "+factoryClass.getName());
            Timer instance = create(factoryClass, context, SERVICE_CONTEXT_TIMER);
            serviceContextTimers.put(factoryClass, instance);
        }
        for (Class<MetricsFactory<Counter>> factoryClass : serviceContextCounters.keySet()) {
            Log.info(Log.ENGINE, "Instantiating : "+factoryClass.getName());
            Counter instance = create(factoryClass, context, SERVICE_CONTEXT_COUNTER);
            serviceContextCounters.put(factoryClass, instance);
        }
        for (Class<MetricsFactory<Histogram>> factoryClass : serviceContextHistogram.keySet()) {
            Log.info(Log.ENGINE, "Instantiating : "+factoryClass.getName());
            Histogram instance = create(factoryClass, context, SERVICE_CONTEXT_HISTOGRAM);
            serviceContextHistogram.put(factoryClass, instance);
        }
        for (Class<MetricsFactory<Meter>> factoryClass : serviceContextMeter.keySet()) {
            Log.info(Log.ENGINE, "Instantiating : "+factoryClass.getName());
            Meter instance = create(factoryClass, context, SERVICE_CONTEXT_METER);
            serviceContextMeter.put(factoryClass, instance);
        }
    }

    private <T> T create(Class<MetricsFactory<T>> factoryClass, ServiceContext context, String type) {
        try {
            MetricsFactory<T> instance = factoryClass.newInstance();
            return instance.create(metricsRegistry, context);
        } catch (Exception e) {
            logReflectionError(e,factoryClass.getName(),type);
            return null;
        }
    }

    public void initMonitors(Element monitors) {
        info("Initializing monitors...");

        //--- get schedules root package
        String pack = monitors.getAttributeValue(ConfigFile.Monitors.Attr.PACKAGE);

        // --- scan serviceContextHealthCheck elements
        for (Element check : (List<Element>) monitors.getChildren(SERVICE_CONTEXT_HEALTH_CHECK)) {
            Class<HealthCheckFactory> hcClass = loadClass(check, pack, SERVICE_CONTEXT_HEALTH_CHECK);
            try {
                serviceContextHealthChecks.add(hcClass.newInstance());
            } catch (Exception e) {
                logReflectionError(e, hcClass.getName(), SERVICE_CONTEXT_HEALTH_CHECK);
            }
        }

        for (Element gauge : (List<Element>) monitors.getChildren(SERVICE_CONTEXT_GAUGE)) {
            serviceContextGauges.put(this.<MetricsFactory<Gauge<?>>>loadClass(gauge, pack, SERVICE_CONTEXT_GAUGE), null);
        }
        serviceContextGauges.remove(null);
        for (Element gauge : (List<Element>) monitors.getChildren(SERVICE_CONTEXT_COUNTER)) {
            serviceContextCounters.put(this.<MetricsFactory<Counter>>loadClass(gauge, pack, SERVICE_CONTEXT_GAUGE), null);
        }
        serviceContextCounters.remove(null);
        for (Element gauge : (List<Element>) monitors.getChildren(SERVICE_CONTEXT_TIMER)) {
            serviceContextTimers.put(this.<MetricsFactory<Timer>>loadClass(gauge, pack, SERVICE_CONTEXT_TIMER), null);
        }
        serviceContextTimers.remove(null);
        for (Element gauge : (List<Element>) monitors.getChildren(SERVICE_CONTEXT_HISTOGRAM)) {
            serviceContextHistogram.put(this.<MetricsFactory<Histogram>>loadClass(gauge, pack, SERVICE_CONTEXT_HISTOGRAM), null);
        }
        serviceContextHistogram.remove(null);
        for (Element gauge : (List<Element>) monitors.getChildren(SERVICE_CONTEXT_METER)) {
            serviceContextMeter.put(this.<MetricsFactory<Meter>>loadClass(gauge, pack, SERVICE_CONTEXT_METER), null);
        }
        serviceContextMeter.remove(null);
    }
    private <T> Class<T> loadClass(Element monitor, String pack, String type) {

        String name = monitor .getAttributeValue(ConfigFile.Monitors.Attr.CLASS);

        info("   Adding "+type+": " + name);

        String className = name;
        if(name.startsWith(".")) {
            className = pack + name;
        }
        try {
            return (Class<T>) Class.forName(className);
        } catch (Exception e) {
            logReflectionError(e, className, type);
            return null;
        }
    }

    private void logReflectionError(Exception e, String className, String type) {
        error("Raised exception while registering "+type+". Skipped.");
        error("   Class name  : " + className);
        error("   Exception : " + e);
        error("   Message   : " + e.getMessage());
        error("   Stack     : " + Util.getStackTrace(e));
    }


    private void info(String s) {
        Log.info(Log.MONITOR, s);
    }

    private void error(String s) {
        Log.error(Log.MONITOR, s);
    }
    public Counter getCounter(Class<? extends MetricsFactory<Counter>> type) {
        Counter instance = serviceContextCounters.get(type);
        if (instance == null) {
            return DummyCounter.INSTANCE;
        } else {
            return instance;
        }
    }
    public Timer getTimer(Class<? extends MetricsFactory<Timer>> type) {
        Timer instance = serviceContextTimers.get(type);
        if (instance == null) {
            return DummyTimer.INSTANCE;
        } else {
            return instance;
        }
    }
    public Histogram getHistogram(Class<? extends MetricsFactory<Histogram>> type) {
        Histogram instance = serviceContextHistogram.get(type);
        if (instance == null) {
            return DummyHistogram.INSTANCE;
        } else {
            return instance;
        }
    }
    public Meter getMeter(Class<? extends MetricsFactory<Meter>> type) {
        Meter instance = serviceContextMeter.get(type);
        if (instance == null) {
            return DummyMeter.INSTANCE;
        } else {
            return instance;
        }
    }

	public ResourceTracker getResourceTracker() {
		return resourceTracker;
	}
}
