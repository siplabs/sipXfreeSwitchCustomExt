/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
*/

package org.sipfoundry.sipxconfig.web.plugin;

import org.apache.tapestry.annotations.Bean;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.annotations.InitialValue;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageEvent;

/*sipXecs WEB components API imports */
import org.sipfoundry.sipxconfig.freeswitchcustomextensions.CustomFreeswitchExtensionsContext;
import org.sipfoundry.sipxconfig.freeswitchcustomextensions.CustomFreeswitchExtensionsSettings;
import org.sipfoundry.sipxconfig.components.PageWithCallback;
import org.sipfoundry.sipxconfig.components.SipxValidationDelegate;

public abstract class CustomFreeswitchExtensionsPage extends PageWithCallback implements PageBeginRenderListener {
    public static final String PAGE = "plugin/CustomFreeswitchExtensionsPage";
    private static final String SETTINGS = "settings";

    @Persist
    @InitialValue(value = "literal:extensions")
    public abstract String getTab();

    public abstract void setTab(String id);

    public abstract CustomFreeswitchExtensionsSettings getSettings();

    public abstract void setSettings(CustomFreeswitchExtensionsSettings settings);

    @InjectObject("spring:customFreeswitchExtensionsContext")
    public abstract CustomFreeswitchExtensionsContext getCustomFreeswitchExtensionsContext();

    @Bean
    public abstract SipxValidationDelegate getValidator();

    public void editExtensions() {
        setTab("extensions");
    }

    public void editSettings() {
        setTab(SETTINGS);
    }

    @Override
    public void pageBeginRender(PageEvent event) {
        if (null == getSettings() && getTab().equals(SETTINGS)) {
            setSettings(getCustomFreeswitchExtensionsContext().getSettings());
        }
    }

    public void saveSettings() {
        if (null != getCustomFreeswitchExtensionsContext()) {
            if (null != getSettings()) {
                getCustomFreeswitchExtensionsContext().saveSettings(getSettings());
            }
        }
    }
}
