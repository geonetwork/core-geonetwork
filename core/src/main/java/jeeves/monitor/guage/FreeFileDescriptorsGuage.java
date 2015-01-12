package jeeves.monitor.guage;

import com.sun.management.UnixOperatingSystemMXBean;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricsRegistry;
import jeeves.monitor.MetricsFactory;
import jeeves.server.JeevesEngine;
import jeeves.server.context.ServiceContext;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * Returns the number of free file descriptors or -1 if unable to calculate.  (only works on unix based OSs).
 * 
 * @author jeichar
 */
public class FreeFileDescriptorsGuage implements MetricsFactory<Gauge<Long>> {

    @Override
    public Gauge<Long> create(MetricsRegistry metricsRegistry, final ServiceContext context) {
        return metricsRegistry.newGauge(JeevesEngine.class, "Free files descriptors" ,new Gauge<Long>() {
            @Override
            public Long value () {
                try {
                    OperatingSystemMXBean osMbean = ManagementFactory.getOperatingSystemMXBean();
                    if(osMbean instanceof UnixOperatingSystemMXBean) {
                        UnixOperatingSystemMXBean unixMXBean = (UnixOperatingSystemMXBean) osMbean;
                        return unixMXBean.getMaxFileDescriptorCount() - unixMXBean.getOpenFileDescriptorCount();
                    }
                    return -1L;
                } catch (Exception e) {
                    return -1L;
                }
            }
        });
    }

}
