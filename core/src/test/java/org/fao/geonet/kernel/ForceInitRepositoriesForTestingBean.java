package org.fao.geonet.kernel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Accesses all Spring-Data Repositories in order to initialize them because they set a transaction's
 * rollbackOnly to true which can cause an exception during tests if it happens at the wrong time.
 *
 * Created by Jesse on 1/24/14.
 */
public class ForceInitRepositoriesForTestingBean {
    @Autowired
    ApplicationContext context;

    @PostConstruct
    public void initAllRepos() {
        context.getBeansOfType(JpaRepository.class);
    }
}
