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
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchExtension;

public interface CustomFreeswitchExtensionsContext extends FreeswitchExtensionProvider, AliasOwner, ReplicableProvider,
        ConfigProvider, PostConfigListener {

    public static final String CUSTOM_EXTENSIONS_FEATURE_ID = CustomFreeswitchExtensionsContext.CUSTOM_EXTENSION;
    public static final LocationFeature FEATURE = new LocationFeature(CUSTOM_EXTENSIONS_FEATURE_ID);
    public static final String CUSTOM_EXTENSION = "freeswitchcustomextensions";

    public static final String CONTEXT_BEAN_NAME = "CustomFreeswitchExtensionsContext";

    /* CustomFreeswitchExtensionProveder API */

    CustomFreeswitchExtension newFreeswitchExtension();

    void saveFreeswitchExtension(CustomFreeswitchExtension extension);

    void deleteFreeswitchExtension(CustomFreeswitchExtension extension);

    void deleteFreeswitchExtensions(Collection<Integer> ids);

    CustomFreeswitchExtension getFreeswitchExtensionById(Integer extensionId);

    CustomFreeswitchExtension getFreeswitchExtensionByName(String extensionName);

    List<CustomFreeswitchExtension> getFreeswitchExtensions();

    /* Settings API */

    public CustomFreeswitchExtensionsSettings getSettings();

    public void saveSettings(CustomFreeswitchExtensionsSettings settings);

}
