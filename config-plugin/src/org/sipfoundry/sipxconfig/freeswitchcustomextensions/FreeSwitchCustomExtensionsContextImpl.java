/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */

package org.sipfoundry.sipxconfig.freeswitchcustomextensions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Query;
import org.sipfoundry.sipxconfig.alias.AliasManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigManager;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigRequest;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigUtils;
import org.sipfoundry.sipxconfig.common.BeanId;
import org.sipfoundry.sipxconfig.common.ExtensionInUseException;
import org.sipfoundry.sipxconfig.common.NameInUseException;
import org.sipfoundry.sipxconfig.common.Replicable;
import org.sipfoundry.sipxconfig.common.SameExtensionException;
import org.sipfoundry.sipxconfig.common.SipxHibernateDaoSupport;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.commserver.imdb.ReplicationManager;
import org.sipfoundry.sipxconfig.feature.Bundle;
import org.sipfoundry.sipxconfig.feature.FeatureChangeRequest;
import org.sipfoundry.sipxconfig.feature.FeatureChangeValidator;
import org.sipfoundry.sipxconfig.feature.FeatureManager;
import org.sipfoundry.sipxconfig.feature.FeatureProvider;
import org.sipfoundry.sipxconfig.feature.GlobalFeature;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchAction;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchCondition;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchFeature;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.support.DataAccessUtils;

public class FreeSwitchCustomExtensionsContextImpl extends SipxHibernateDaoSupport implements FreeSwitchCustomExtensionsContext, BeanFactoryAware,
        FeatureProvider {

    private static final String QUERY_CUSTOM_EXTENSIONS_WITH_NAMES = "freeSwitchCustomExtensionWithName";
    private static final String QUERY_PARAM_VALUE = "value";
    private static final String QUERY_PARAM_EXTENSION_ID = "freeswitchcustomextensionid";
    private static final String COPY_OF = "Copy of";
    private static final String COPIED = "(Copied)";

    private static final String ALIAS = "alias";
    private static final String EXTENSION = "extension";
    private static final String DID = "did";
    private static final String EXTENSION_NAME = CUSTOM_EXTENSION;

    private BeanFactory m_beanFactory;
    private AliasManager m_aliasManager;
    private FeatureManager m_featureManager;
    private BeanWithSettingsDao<FreeSwitchCustomExtensionsSettings> m_settingsDao;
    private ReplicationManager m_replicationManager;

    public void setBeanFactory(BeanFactory beanFactory) {
        m_beanFactory = beanFactory;
    }

    /* Bean properties */
    @Required
    public void setAliasManager(AliasManager aliasManager) {
        m_aliasManager = aliasManager;
    }

    @Required
    public void setFeatureManager(FeatureManager featureManager) {
        m_featureManager = featureManager;
    }

    @Required
    public void setReplicationManager(ReplicationManager replicationManager) {
        m_replicationManager = replicationManager;
    }

    /* Settings API */

    public void setSettingsDao(BeanWithSettingsDao<FreeSwitchCustomExtensionsSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }

    public FreeSwitchCustomExtensionsSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    public void saveSettings(FreeSwitchCustomExtensionsSettings settings) {
        m_settingsDao.upsert(settings);
    }

    @Override
    public List<Replicable> getReplicables() {
        if (m_featureManager.isFeatureEnabled(FEATURE)) {
            List<Replicable> replicables = new ArrayList<Replicable>();
            replicables.addAll(getFreeSwitchCustomExtensions());
            return replicables;
        }
        return Collections.EMPTY_LIST;
    }

    /* Alias support */
    @Override
    public Collection<BeanId> getBeanIdsOfObjectsWithAlias(String alias) {
        Collection<BeanId> bids = new ArrayList<BeanId>();

        List<FreeSwitchCustomExtension> exts = getHibernateTemplate().loadAll(FreeSwitchCustomExtension.class);
        for (FreeSwitchCustomExtension ext : exts) {
            if (ext.getExtension() != null && (ext.getExtension().equals(alias) || ext.getName().equals(alias))
                    || (ext.getAlias() != null && ext.getAlias().equals(alias))
                    || (ext.getDid() != null && ext.getDid().equals(alias))) {
                bids.add(new BeanId(ext.getId(), FreeSwitchCustomExtension.class));
            }
        }
        // Add all beans, having alias(es)
        return bids;
    }

    @Override
    public boolean isAliasInUse(String alias) {
        List<FreeSwitchCustomExtension> extensions = getFreeswitchExtensions();
        for (FreeSwitchCustomExtension extension : extensions) {
            if (extension.getExtension() != null
                    && (extension.getExtension().equals(alias) || extension.getName().equals(alias))) {
                return true;
            }
            if (extension.getAlias() != null && extension.getAlias().equals(alias)) {
                return true;
            }
            if (extension.getDid() != null && extension.getDid().equals(alias)) {
                return true;
            }
        }
        return false;
    }

    /* FreeSwitchExtensionProvider */
    @Override
    public boolean isEnabled() {
        return m_featureManager.isFeatureEnabled(FEATURE);
    }

    public void deleteExtension(FreeSwitchCustomExtension ext) {
        getHibernateTemplate().delete(ext);
    }

    public void saveExtension(FreeSwitchCustomExtension extension) {
        if (extension.getName() == null) {
            throw new UserException("&null.name");
        }
        if (extension.getExtension() == null) {
            throw new UserException("&null.extension");
        }
        String capturedExt = extension.getCapturedExtension();

        if (!m_aliasManager.canObjectUseAlias(extension, extension.getName())) {
            throw new NameInUseException(EXTENSION_NAME, extension.getName());
        } else if (!m_aliasManager.canObjectUseAlias(extension, capturedExt)) {
            throw new ExtensionInUseException(EXTENSION_NAME, capturedExt);
        } else if (extension.getAlias() != null
                && !m_aliasManager.canObjectUseAlias(extension, extension.getAlias())) {
            throw new ExtensionInUseException(EXTENSION_NAME, extension.getAlias());
        } else if (extension.getAlias() != null && extension.getAlias().equals(extension.getExtension())) {
            throw new SameExtensionException(ALIAS, EXTENSION);
        } else if (extension.getDid() != null && !m_aliasManager.canObjectUseAlias(extension, extension.getDid())) {
            throw new ExtensionInUseException(EXTENSION_NAME, extension.getDid());
        } else if (extension.getDid() != null && extension.getDid().equals(extension.getExtension())) {
            throw new SameExtensionException(DID, EXTENSION);
        } else if (extension.getDid() != null && extension.getAlias() != null
                && extension.getDid().equals(extension.getAlias())) {
            throw new SameExtensionException(ALIAS, DID);
        }
        removeNullActions(extension);
        if (extension.isNew()) {
            getHibernateTemplate().saveOrUpdate(extension);
        } else {
            getHibernateTemplate().merge(extension);
        }
    }

    @Override
    public FreeSwitchCustomExtension getExtensionById(Integer extensionId) {
        return getHibernateTemplate().load(FreeSwitchCustomExtension.class, extensionId);
    }

    @Override
    public FreeSwitchCustomExtension getExtensionByName(String extensionName) {
        List<FreeSwitchCustomExtension> extensions = getHibernateTemplate().findByNamedQueryAndNamedParam(
                QUERY_CUSTOM_EXTENSIONS_WITH_NAMES, QUERY_PARAM_VALUE, extensionName);
        return DataAccessUtils.singleResult(extensions);
    }

    @Override
    public List<FreeSwitchCustomExtension> getFreeswitchExtensions() {
        return getHibernateTemplate().loadAll(FreeSwitchCustomExtension.class);
    }

    private void removeNullActions(FreeSwitchCustomExtension extension) { // Should not be Tested
        if (extension.getConditions() == null) {
            return;
        }
        for (FreeswitchCondition condition : extension.getConditions()) {
            for (FreeswitchAction action : condition.getActions()) {
                if (action != null && action.getApplication() == null) {
                    condition.removeAction(action);
                }
            }
        }
    }

    /* FreeSwitchCustomExtension API */

    public FreeSwitchCustomExtension newFreeSwitchCustomExtension() { // Tested
        FreeSwitchCustomExtension obj = (FreeSwitchCustomExtension) m_beanFactory.getBean(FreeSwitchCustomExtension.class);
        return obj;
    }

    public void saveFreeSwitchCustomExtension(FreeSwitchCustomExtension obj) { // Tested
        saveExtension(obj);
    }

    public FreeSwitchCustomExtension loadFreeSwitchCustomExtension(Integer id) { // Tested
        return (FreeSwitchCustomExtension) getHibernateTemplate().load(FreeSwitchCustomExtension.class, id);
    }

    public void duplicateFreeSwitchCustomExtensions(Collection<Integer> ids) { // Tested
        for (Integer id : ids) {
            FreeSwitchCustomExtension srcExt = (FreeSwitchCustomExtension) getHibernateTemplate().load(FreeSwitchCustomExtension.class, id);
            FreeSwitchCustomExtension newExt = newFreeSwitchCustomExtension();
            // TODO: localize strings
            newExt.setName(COPY_OF + srcExt.getName());
            if (null != srcExt.getDescription()) {
                newExt.setDescription(srcExt.getDescription() + COPIED);
            }
//            srcExt.copySettingsTo(newExt);
            getHibernateTemplate().saveOrUpdate(newExt);
        }
    }

    public void deleteFreeSwitchCustomExtensions(Collection<Integer> ids) { // Tested
        if (ids.isEmpty()) {
            return;
        }
        for (Integer id : ids) {
            FreeSwitchCustomExtension ext = loadFreeSwitchCustomExtension(id);
            String extension = ext.getExtension();
            getHibernateTemplate().delete(ext);
        }
    }

    public Collection<FreeSwitchCustomExtension> getFreeSwitchCustomExtensions() { // Test
        return getHibernateTemplate().loadAll(FreeSwitchCustomExtension.class);
    }

    @Override
    public void featureChangePrecommit(FeatureManager manager, FeatureChangeValidator validator) {
        validator.requiredOnSameHost(FEATURE, FreeswitchFeature.FEATURE);
        validator.singleLocationOnly(FEATURE);
    }

    @Override
    public void featureChangePostcommit(FeatureManager manager, FeatureChangeRequest request) {
        if (request.getAllNewlyDisabledFeatures().contains(FEATURE)) {
            for (FreeSwitchCustomExtension ext : getFreeSwitchCustomExtensions()) {
                m_replicationManager.removeEntity(ext);
            }
        }
    }

    @Override
    public Collection<GlobalFeature> getAvailableGlobalFeatures(FeatureManager featureManager) {
        return null;
    }

    @Override
    public Collection<LocationFeature> getAvailableLocationFeatures(FeatureManager featureManager, Location l) {
        return Collections.singleton(FEATURE);
    }

    @Override
    public void getBundleFeatures(FeatureManager featureManager, Bundle b) {
        if (b == Bundle.CORE_TELEPHONY) {
            b.addFeature(FEATURE);
        }
    }

    @Override
    public void replicate(ConfigManager manager, ConfigRequest request) throws IOException {
        if (!request.applies(FreeSwitchCustomExtensionsContext.FEATURE)) {
            return;
        }

        Set<Location> locations = request.locations(manager);
        List<Location> enabledLocations = manager.getFeatureManager().getLocationsForEnabledFeature(FEATURE);
        for (Location location : locations) {
            File dir = manager.getLocationDataDirectory(location);
            boolean enabled = enabledLocations.contains(location);
            ConfigUtils.enableCfengineClass(dir, "sipxfreeswitchcustomextensions.cfdat", enabled, "freeswitchcustomextensions");
        }
    }

    @Override
    public void postReplicate(ConfigManager manager, ConfigRequest request) throws IOException {
        // reload queues only after reloadxml finished
        if (request.applies(FEATURE) || request.applies(FreeswitchFeature.FEATURE)) {
        }
    }
}
