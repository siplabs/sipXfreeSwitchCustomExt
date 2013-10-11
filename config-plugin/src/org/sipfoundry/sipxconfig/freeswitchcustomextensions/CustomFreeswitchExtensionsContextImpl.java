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
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchExtension;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchAction;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchCondition;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchFeature;
import org.sipfoundry.sipxconfig.setting.BeanWithSettingsDao;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.support.DataAccessUtils;

public class CustomFreeswitchExtensionsContextImpl extends SipxHibernateDaoSupport implements CustomFreeswitchExtensionsContext, BeanFactoryAware,
        FeatureProvider {

    private static final String QUERY_CUSTOM_EXTENSIONS_WITH_NAMES = "customFreeswitchExtensionWithName";
    private static final String QUERY_PARAM_VALUE = "value";
    private static final String QUERY_PARAM_EXTENSION_ID = "customfreeswitchextensionid";
    private static final String COPY_OF = "Copy of";
    private static final String COPIED = "(Copied)";

    private static final String ALIAS = "alias";
    private static final String EXTENSION = "extension";
    private static final String DID = "did";
    private static final String EXTENSION_NAME = CUSTOM_EXTENSION;

    private BeanFactory m_beanFactory;
    private AliasManager m_aliasManager;
    private FeatureManager m_featureManager;
    private BeanWithSettingsDao<CustomFreeswitchExtensionsSettings> m_settingsDao;
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

    public void setSettingsDao(BeanWithSettingsDao<CustomFreeswitchExtensionsSettings> settingsDao) {
        m_settingsDao = settingsDao;
    }

    public CustomFreeswitchExtensionsSettings getSettings() {
        return m_settingsDao.findOrCreateOne();
    }

    public void saveSettings(CustomFreeswitchExtensionsSettings settings) {
        m_settingsDao.upsert(settings);
    }

    @Override
    public List<Replicable> getReplicables() {
        if (m_featureManager.isFeatureEnabled(FEATURE)) {
            List<Replicable> replicables = new ArrayList<Replicable>();
            replicables.addAll(getFreeswitchExtensions());
            return replicables;
        }
        return Collections.EMPTY_LIST;
    }

    /* Alias support */
    @Override
    public Collection<BeanId> getBeanIdsOfObjectsWithAlias(String alias) {
        Collection<BeanId> bids = new ArrayList<BeanId>();

        List<CustomFreeswitchExtension> exts = getHibernateTemplate().loadAll(CustomFreeswitchExtension.class);
        for (CustomFreeswitchExtension ext : exts) {
            if (ext.getExtension() != null && (ext.getExtension().equals(alias) || ext.getName().equals(alias))
                    || (ext.getAlias() != null && ext.getAlias().equals(alias))
                    || (ext.getDid() != null && ext.getDid().equals(alias))) {
                bids.add(new BeanId(ext.getId(), CustomFreeswitchExtension.class));
            }
        }
        // Add all beans, having alias(es)
        return bids;
    }

    @Override
    public boolean isAliasInUse(String alias) {
        List<CustomFreeswitchExtension> extensions = getFreeswitchExtensions();
        for (CustomFreeswitchExtension extension : extensions) {
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

    /* ExtensionProvider */

    @Override
    public boolean isEnabled() {
        return m_featureManager.isFeatureEnabled(FEATURE);
    }

    public CustomFreeswitchExtension newFreeswitchExtension() {
        return (CustomFreeswitchExtension) m_beanFactory.getBean(CustomFreeswitchExtension.class);
    }

    public void deleteFreeswitchExtension(CustomFreeswitchExtension extension) {
        getHibernateTemplate().delete(extension);
    }

    public void saveFreeswitchExtension(CustomFreeswitchExtension extension) {
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
    public CustomFreeswitchExtension getFreeswitchExtensionById(Integer extensionId) {
        return getHibernateTemplate().load(CustomFreeswitchExtension.class, extensionId);
    }

    @Override
    public CustomFreeswitchExtension getFreeswitchExtensionByName(String extensionName) {
        List<CustomFreeswitchExtension> extensions = getHibernateTemplate().findByNamedQueryAndNamedParam(
                QUERY_CUSTOM_EXTENSIONS_WITH_NAMES, QUERY_PARAM_VALUE, extensionName);
        return DataAccessUtils.singleResult(extensions);
    }

    @Override
    public List<CustomFreeswitchExtension> getFreeswitchExtensions() {
        return getHibernateTemplate().loadAll(CustomFreeswitchExtension.class);
    }

    private void removeNullActions(CustomFreeswitchExtension extension) { // Should not be Tested
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

    @Override
    public void featureChangePrecommit(FeatureManager manager, FeatureChangeValidator validator) {
        validator.requiredOnSameHost(FEATURE, FreeswitchFeature.FEATURE);
        validator.singleLocationOnly(FEATURE);
    }

    @Override
    public void featureChangePostcommit(FeatureManager manager, FeatureChangeRequest request) {
        if (request.getAllNewlyDisabledFeatures().contains(FEATURE)) {
            for (CustomFreeswitchExtension ext : getFreeswitchExtensions()) {
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
        if (!request.applies(CustomFreeswitchExtensionsContext.FEATURE)) {
            return;
        }

        Set<Location> locations = request.locations(manager);
        List<Location> enabledLocations = manager.getFeatureManager().getLocationsForEnabledFeature(FEATURE);
        for (Location location : locations) {
            File dir = manager.getLocationDataDirectory(location);
            boolean enabled = enabledLocations.contains(location);
            ConfigUtils.enableCfengineClass(dir, "sipxcustomfse.cfdat", enabled, "sipxcustomfse");
        }
    }

    @Override
    public void postReplicate(ConfigManager manager, ConfigRequest request) {
    }
}
