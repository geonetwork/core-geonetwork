package org.fao.geonet.services.metadata.schema;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Schematron;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.repository.SchematronRepository;
import org.fao.geonet.repository.Updater;
import org.jdom.Element;

import javax.annotation.Nonnull;

/**
 *  Load, edit, delete {@link org.fao.geonet.domain.Schematron} entities.
 *
 * Created by Jesse on 2/7/14.
 */
public class SchematronService extends AbstractSchematronService {

    static final String PARAM_DISPLAY_PRIORITY = "displaypriority";

    @Override
    protected Element list(Element params, ServiceContext context) throws Exception {
        String id = Util.getParam(params, Params.ID, null);
        final SchematronRepository repository = context.getBean(SchematronRepository.class);

        Element result;
        if (id == null) {
            result = repository.findAllAsXml();
        } else {
            final Schematron one = repository.findOne(Integer.parseInt(id));
            if (one == null) {
                throw new BadParameterEx(Params.ID, id);
            }
            result = new Element("schematron").addContent(one.asXml());
        }

        result.setName("schematron");
        return result;
    }

    @Override
    protected boolean exists(Element params, ServiceContext context) throws Exception {
        String id = Util.getParam(params, Params.ID);
        return context.getBean(SchematronRepository.class).exists(Integer.parseInt(id));
    }

    @Override
    protected Element edit(Element params, ServiceContext context) throws Exception {
        String id = Util.getParam(params, Params.ID);
        final int displayPriority = Integer.parseInt(Util.getParam(params, PARAM_DISPLAY_PRIORITY));

        context.getBean(SchematronRepository.class).update(Integer.parseInt(id), new Updater<Schematron>() {
            @Override
            public void apply(@Nonnull Schematron entity) {
                entity.setDisplayPriority(displayPriority);
            }
        });

        return new Element("ok");
    }

    @Override
    protected Element delete(Element params, ServiceContext context) throws Exception {
        throw new UnsupportedOperationException("Cannot yet delete existing schematrons");
    }

    @Override
    protected Element add(Element params, ServiceContext context) throws Exception {
        throw new UnsupportedOperationException("Cannot yet add new schematrons");
    }
}
