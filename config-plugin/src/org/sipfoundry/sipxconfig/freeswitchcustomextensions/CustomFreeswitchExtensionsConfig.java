/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
*/

package org.sipfoundry.sipxconfig.freeswitchcustomextensions;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.sipfoundry.sipxconfig.commserver.Location;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchFeature;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchSettings;
import org.sipfoundry.sipxconfig.freeswitch.config.AbstractFreeswitchConfiguration;
import org.springframework.beans.factory.annotation.Required;

public class CustomFreeswitchExtensionsConfig extends AbstractFreeswitchConfiguration {

    private CustomFreeswitchExtensionsContext m_customFreeswitchExtensionsContext;
    private Collection<CustomFreeswitchExtension> m_customFreeswitchExtensions;

    @Required
    public void setCustomFreeswitchExtensionsContext(CustomFreeswitchExtensionsContext context) {
        m_customFreeswitchExtensionsContext = context;
    }

    @Override
    public void write(Writer writer, Location location, FreeswitchSettings settings) throws IOException {
        m_customFreeswitchExtensions = m_customFreeswitchExtensionsContext.getFreeswitchExtensions();
        write(writer);
    }

    void write(Writer writer) throws IOException {
        VelocityContext context = new VelocityContext();
        context.put("customextensions", m_customFreeswitchExtensions);
        write(writer, context);
    }

    @Override
    protected String getFileName() {
        return "dialplan/sipXcfse_context.xml";
    }

    @Override
    protected String getTemplate() {
        return "freeswitch/sipXcfse_context.xml.vm";
    }

}
