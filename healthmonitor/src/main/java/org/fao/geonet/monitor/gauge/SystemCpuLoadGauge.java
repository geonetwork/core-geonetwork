package org.fao.geonet.monitor.gauge;

/**
 * Gauge that gets the number of connections that the ResourceProvider reports as idle.  If unable to access
 * information or if the number is null (Like in case of JNDI) Integer.MIN_VALUE will be reported
 *
 * User: jeichar
 * Date: 4/5/12
 * Time: 4:29 PM
 */
public class SystemCpuLoadGauge extends AbstractOSMxBeanGauge<Double> {
    public SystemCpuLoadGauge() {
        super("System_CPU_Load");
    }

    @Override
    protected Double getDefaultValue() {
        return -1.0;
    }

    @Override
    protected Double getValue(com.sun.management.OperatingSystemMXBean operatingSystemMXBean) {
        return operatingSystemMXBean.getSystemCpuLoad();
    }
}
