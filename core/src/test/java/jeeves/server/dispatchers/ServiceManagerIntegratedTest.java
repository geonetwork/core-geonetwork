package jeeves.server.dispatchers;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.server.local.LocalServiceRequest;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.HarvesterSetting;
import org.fao.geonet.repository.HarvesterSettingRepository;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the ServiceManager handles errors etc... correctly.
 * <p/>
 * Created by Jesse on 3/11/14.
 */
public class ServiceManagerIntegratedTest extends AbstractCoreIntegrationTest {
    @Autowired
    ServiceManager _serviceManager;


    private static final String SERVICE_NAME = "mockService";
    private MockService _service;
    @Autowired
    private HarvesterSettingRepository _harvesterSettingRepo;

    @Before
    public void addService() throws Exception {
        Element serviceElem = new Element("service");
        serviceElem.setAttribute("name", SERVICE_NAME);
        serviceElem.addContent(new Element("class").setAttribute("name", MockService.class.getName()));

        ServiceInfo serviceInfo = _serviceManager.addService(MockService.class.getPackage().getName(), serviceElem);
        this._service = (MockService) serviceInfo.getServices().get(0);

    }

    @Test
    public void testDispatchRequireTransaction() throws Exception {
        long count = _harvesterSettingRepo.count();
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                LocalServiceRequest req = LocalServiceRequest.create("http://localhost/geonetwork/srv/eng/" + SERVICE_NAME);
                UserSession userSession = new UserSession();

                _service.setService(new ServiceFunction() {
                    @Override
                    public Element exec(Element params, ServiceContext context) throws Exception {
                        saveParentAndChildHarvesterSetting(context);

                        return new Element("ok");
                    }
                });

                _serviceManager.dispatch(req, userSession);

                final String resultString = req.getResultString();

                assertTrue(resultString.contains("ok"));
            }
        });

        assertEquals(count + 2, _harvesterSettingRepo.count());
    }


    @Test
    public void testRollbackOnError() throws Exception {
        long count = _harvesterSettingRepo.count();
        TransactionlessTesting.get().run(new TestTask() {
            @Override
            public void run() throws Exception {
                LocalServiceRequest req = LocalServiceRequest.create("http://localhost/geonetwork/srv/eng/" + SERVICE_NAME);
                UserSession userSession = new UserSession();

                _service.setService(new ServiceFunction() {
                    @Override
                    public Element exec(Element params, ServiceContext context) throws Exception {
                        saveParentAndChildHarvesterSetting(context);
                        throw new Exception("test exception");
                    }
                });

                _serviceManager.dispatch(req, userSession);

                final String resultString = req.getResultString();

                assertTrue(resultString.contains("error"));
            }
        });
        assertEquals(count, _harvesterSettingRepo.count());
    }

    private static void saveParentAndChildHarvesterSetting(ServiceContext context) {
        final String name = UUID.randomUUID().toString();

        final HarvesterSettingRepository settingRepository = context.getBean(HarvesterSettingRepository.class);
        settingRepository.save(new HarvesterSetting().setName(name).setValue("value"));

        final HarvesterSetting parent = settingRepository.findOneByPath(name);
        final HarvesterSetting child = new HarvesterSetting().setValue("childValue").setName("childName")
                .setParent(parent);

        settingRepository.save(child);
    }
}
