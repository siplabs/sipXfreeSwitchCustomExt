/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
*/

package org.sipfoundry.sipxconfig.web.plugin;

import java.util.Collection;

import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.IPage;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.annotations.ComponentClass;
import org.apache.tapestry.annotations.InitialValue;
import org.apache.tapestry.annotations.InjectObject;
import org.apache.tapestry.annotations.InjectPage;
import org.apache.tapestry.annotations.Persist;
import org.sipfoundry.sipxconfig.freeswitchcustomextensions.CustomFreeswitchExtension;
import org.sipfoundry.sipxconfig.freeswitchcustomextensions.CustomFreeswitchExtensionsContext;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.components.SelectMap;

@ComponentClass
public abstract class CustomFreeswitchExtensionsPanel extends BaseComponent {

    @Persist
    @InitialValue(value = "literal:extensions")
    public abstract String getTab();

    @InjectObject("spring:customFreeswitchExtensionsContext")
    public abstract CustomFreeswitchExtensionsContext getCustomFreeswitchExtensionsContext();

    @InjectObject("spring:coreContext")
    public abstract CoreContext getCoreContext();

    @InjectPage(CustomFreeswitchExtensionsEditExtension.PAGE)
    public abstract CustomFreeswitchExtensionsEditExtension getEditExtensionPage();

    public abstract CustomFreeswitchExtension getCurrentRow();

    public abstract void setCurrentRow(CustomFreeswitchExtension ext);

    public abstract Collection<Integer> getRowsToDuplicate();

    public abstract Collection<Integer> getRowsToDelete();

    @InitialValue(value = "new org.sipfoundry.sipxconfig.components.SelectMap()")
    public abstract SelectMap getSelections();

   private IPage addeditPage(IRequestCycle cycle, Integer id) {
        CustomFreeswitchExtensionsEditExtension page = (CustomFreeswitchExtensionsEditExtension) cycle.getPage(CustomFreeswitchExtensionsEditExtension.PAGE);
        page.setObject(null);
        page.setObjectId(id);
        return page;
   }

    public IPage add(IRequestCycle cycle) {
        return addeditPage(cycle, null);
    }

    public IPage edit(IRequestCycle cycle, Integer id) {
        return addeditPage(cycle, id);
    }
/*
    public void duplicate() {
        Collection<Integer> selectedRows = getRowsToDuplicate();
        if (null != selectedRows) {
            getCustomFreeswitchExtensionsContext().duplicateCustomFreeswitchExtensions(selectedRows);
        }
    }
*/
    public void delete() {
        Collection<Integer> selectedRows = getRowsToDelete();
        if (null != selectedRows) {
            getCustomFreeswitchExtensionsContext().deleteFreeswitchExtensions(selectedRows);
        }
    }
}
