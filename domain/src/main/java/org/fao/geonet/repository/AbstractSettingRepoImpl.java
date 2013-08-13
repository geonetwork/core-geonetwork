package org.fao.geonet.repository;

import static org.fao.geonet.repository.SettingRepository.ID_PREFIX;
import static org.fao.geonet.repository.SettingRepository.SEPARATOR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import org.fao.geonet.domain.AbstractSetting;
import org.fao.geonet.domain.AbstractSetting_;

public abstract class AbstractSettingRepoImpl<T extends AbstractSetting<?>> implements AbstractSettingRepoCustom<T> {

    @PersistenceContext
    private EntityManager _entityManager;
    private Class<T> _settingClass;
    @SuppressWarnings("rawtypes")
    private SingularAttribute<AbstractSetting, Object> _parentExpression;

    /**
     * Constructor.
     *
     * @param settingClass the type of T (the concrete settings class).
     */
    public AbstractSettingRepoImpl(Class<T> settingClass, @SuppressWarnings("rawtypes") SingularAttribute<AbstractSetting, Object> parentExpression) {
        this._settingClass = settingClass;
        this._parentExpression = parentExpression;
    }

    @Override
    public List<T> findByPath(String pathToSetting) {
        StringTokenizer stringTokenizer = new StringTokenizer(pathToSetting, SEPARATOR);
    
        int countTokens = stringTokenizer.countTokens();
        List<String> pathSegments = new ArrayList<String>(countTokens);
        while (stringTokenizer.hasMoreTokens()) {
            String child = stringTokenizer.nextToken();
    
            if (child.startsWith(ID_PREFIX)) {
                pathSegments.clear();
                pathSegments.add(child);
            } else {
                pathSegments.add(child);
            }
        }
    
        List<T> currentSettings = null;
        String firstSegment = pathSegments.get(0);
        if (firstSegment.startsWith(ID_PREFIX)) {
            int id = Integer.parseInt(firstSegment.substring(ID_PREFIX.length()));
            T setting = _entityManager.find(_settingClass, id);
            if (setting == null) {
                currentSettings = Collections.emptyList();
                pathSegments=Collections.emptyList();
            } else {
                currentSettings = Collections.singletonList(setting);
                pathSegments = pathSegments.subList(1, pathSegments.size());
            }
        } else {
            currentSettings = findRoots();
        }

        for (String childName : pathSegments) {
            List<T> oldSettings = currentSettings;
            currentSettings = new LinkedList<T>();
            for (T setting : oldSettings) {
                List<T> children = findChildrenByName(setting.getId(), childName);
                currentSettings.addAll(children);
            }
        }
        return currentSettings;
    }

    @Override
    public T findOneByPath(String pathToSetting) {
        List<T> settings = findByPath(pathToSetting);
        if (settings.isEmpty()) {
            return null;
        } else {
            return settings.get(0);
        }
    }

    @Override
    public List<T> findRoots() {
        CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(_settingClass);
        
        Root<T> root = query.from(_settingClass);
        query.where(criteriaBuilder.isNull(root.get(_parentExpression)));
        
        return _entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<T> findAllChildren(int parentid) {
        CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(_settingClass);
        
        Root<T> root = query.from(_settingClass);
        query.where(criteriaBuilder.equal(root.get(_parentExpression), parentid));
        
        return _entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<T> findChildrenByName(int parentid, String name) {
        CriteriaBuilder criteriaBuilder = _entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = criteriaBuilder.createQuery(_settingClass);
        
        Root<T> root = query.from(_settingClass);
        Predicate equalParentId = criteriaBuilder.equal(root.get(_parentExpression), parentid);
        Predicate equalName = criteriaBuilder.equal(root.get(AbstractSetting_.name), name);
        query.where(criteriaBuilder.and(equalParentId, equalName));
        
        return _entityManager.createQuery(query).getResultList();
    }

}