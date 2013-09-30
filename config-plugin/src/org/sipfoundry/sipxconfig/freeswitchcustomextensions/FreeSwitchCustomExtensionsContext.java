/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */

package org.sipfoundry.sipxconfig.freeswitchcustomextensions;

import java.util.Collection;
import java.util.List;

import org.sipfoundry.sipxconfig.alias.AliasOwner;
import org.sipfoundry.sipxconfig.cfgmgt.ConfigProvider;
import org.sipfoundry.sipxconfig.cfgmgt.PostConfigListener;
import org.sipfoundry.sipxconfig.common.ReplicableProvider;
import org.sipfoundry.sipxconfig.feature.LocationFeature;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchExtensionProvider;

public interface FreeSwitchCustomExtensionsContext extends FreeswitchExtensionProvider, AliasOwner, ReplicableProvider,
        ConfigProvider, PostConfigListener {

    public static final String CUSTOM_EXTENSIONS_FEATURE_ID = FreeSwitchCustomExtensionsContext.CUSTOM_EXTENSION;
    public static final LocationFeature FEATURE = new LocationFeature(CUSTOM_EXTENSIONS_FEATURE_ID);
    public static final String CUSTOM_EXTENSION = "freeswitchcustomextensions";

    public static final String CONTEXT_BEAN_NAME = "FreeSwitchCustomExtensionsContext";

    /* FreeSwitchExtensionProveder API */

    void saveExtension(FreeSwitchCustomExtension extension);

    void deleteExtension(FreeSwitchCustomExtension extension);

    FreeSwitchCustomExtension getExtensionById(Integer extensionId);

    FreeSwitchCustomExtension getExtensionByName(String extensionName);

    List<FreeSwitchCustomExtension> getFreeswitchExtensions();

    /* Settings API */

    public FreeSwitchCustomExtensionsSettings getSettings();

    public void saveSettings(FreeSwitchCustomExtensionsSettings settings);

    /* FreeSwitchCustomExtension API */
    FreeSwitchCustomExtension loadFreeSwitchCustomExtension(Integer id);

    void saveFreeSwitchCustomExtension(FreeSwitchCustomExtension extension);

    FreeSwitchCustomExtension newFreeSwitchCustomExtension();

    void duplicateFreeSwitchCustomExtensions(Collection<Integer> ids);

    void deleteFreeSwitchCustomExtensions(Collection<Integer> ids);

    Collection<FreeSwitchCustomExtension> getFreeSwitchCustomExtensions();
}
