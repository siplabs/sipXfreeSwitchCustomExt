/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
*/

package org.sipfoundry.sipxconfig.freeswitchcustomextensions;

import java.util.Arrays;
import java.util.Collection;

import org.sipfoundry.sipxconfig.cfgmgt.DeployConfigOnEdit;
import org.sipfoundry.sipxconfig.feature.Feature;
import org.sipfoundry.sipxconfig.setting.PersistableSettings;
import org.sipfoundry.sipxconfig.setting.Setting;

import org.sipfoundry.sipxconfig.freeswitch.FreeswitchFeature;

public class FreeSwitchCustomExtensionsSettings extends PersistableSettings implements DeployConfigOnEdit {
    private static final String BEAN_NAME = "freeSwitchCustomExtensionsSettings";

    @Override
    public Collection<Feature> getAffectedFeaturesOnChange() {
        return Arrays.asList((Feature) FreeSwitchCustomExtensionsContext.FEATURE, (Feature) FreeswitchFeature.FEATURE);
    }

    @Override
    public String getBeanId() {
        return BEAN_NAME;
    }

    @Override
    protected Setting loadSettings() {
        return getModelFilesContext().loadModelFile("sipxfreeswitchcustomextensions/FreeSwitchCustomExtensionsSettings.xml");
    }
}
