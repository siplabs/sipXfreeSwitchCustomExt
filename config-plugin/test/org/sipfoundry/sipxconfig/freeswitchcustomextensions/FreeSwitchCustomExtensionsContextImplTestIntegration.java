/*
 * Copyright (C) 2013 SibTelCom, JSC., certain elements licensed under a Contributor Agreement.
 * Author: Konstantin S. Vishnivetsky
 * E-mail: info@siplabs.ru
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
*/

package org.sipfoundry.sipxconfig.freeswitchcustomextensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.sipfoundry.sipxconfig.test.IntegrationTestCase;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class FreeSwitchCustomExtensionsContextImplTestIntegration extends IntegrationTestCase {
    private FreeSwitchCustomExtensionsContext m_freeSwitchCustomExtensionsContext;
    private CoreContext m_coreContext;

    @Override
    protected ConfigurableApplicationContext createApplicationContext(String[] locations) {
        List<String> jars = new ArrayList<String>();
        jars.add("classpath:/org/sipfoundry/sipxconfig/system.beans.xml");
        jars.add("classpath:/sipxplugin.beans.xml");
        jars.add("classpath*:/org/sipfoundry/sipxconfig/*/**/*.beans.xml");        
        return new ClassPathXmlApplicationContext(jars.toArray(new String[0]));
    }

    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
        clear();
    }

    @Override
    protected void onSetUpInTransaction() throws Exception {
        super.onSetUpInTransaction();
//        loadDataSetXml("callqueue/CallQueueSeed.xml");
    }

// Utility methods
    public void setFreeSwitchCustomExtensionsContext(FreeSwitchCustomExtensionsContext context) {
        m_freeSwitchCustomExtensionsContext = context;
    }
/*
// Test methods for FreeSwitchCustomExtension
    public void testNewFreeSwitchCustomExtension() throws Exception {
        FreeSwitchCustomExtension freeSwitchCustomExtension = m_freeSwitchCustomExtensionContext.newFreeSwitchCustomExtension();
        callQueue.setName("Queue 10");
        callQueue.setExtension("8110");
        m_callQueueContext.saveCallQueue(callQueue);
        commit();
        // table should have additional row now - 8 = 4 static CallQueues + 1 dynamic CallQueue + 3 CallQueueCommands
        assertEquals(8, countRowsInTable("freeswitch_extension"));
    }

    public void testLoadCallQueue() throws Exception {
        CallQueue callQueue = m_callQueueContext.loadCallQueue(new Integer(300001));
        assertEquals("Queue 1", callQueue.getName());
        assertEquals("8101", callQueue.getDid());
    }

    public void testGetCallQueues() throws Exception {
        Collection<CallQueue> callQueues = m_callQueueContext.getCallQueues();
        assertEquals(4, callQueues.size());
    }

    public void testRemoveCallQueues() throws Exception {
        Collection<Integer> callQueueIds = new HashSet<Integer>(Arrays.asList(new Integer(300004)));
        m_callQueueContext.deleteCallQueues(callQueueIds);
        commit();
        // table should have less rows now - 6
        assertEquals(6, countRowsInTable("freeswitch_extension"));
    }
*/
/*
    public void testDuplicateCallQueues() throws Exception {
        Collection<Integer> callQueueIds = new HashSet<Integer>(Arrays.asList(new Integer(300001), new Integer(300002), new Integer(300003)));
        m_callQueueContext.duplicateCallQueues(callQueueIds);
        commit();
        // table should have additional row now - 10
        assertEquals(10, countRowsInTable("freeswitch_extension"));
        Collection<CallQueue> callQueues = m_callQueueContext.getCallQueues();
        assertEquals(7, callQueues.size());
        ArrayList<CallQueue> a = new ArrayList(callQueues);
        compareClonedQueue(a.get(0), a.get(4));
        compareClonedQueue(a.get(1), a.get(5));
        compareClonedQueue(a.get(2), a.get(6));
    }
    // Utility methods - all private

    // Method is recursive - this is normal, we need to follow settings tree
    private void compareSettingsForCloned(Setting o, Setting c) {
        // Compare Settings count in root element for orginal and clone object
        if (o.isLeaf()) {
            assertEquals(o.getValue(), c.getValue());
        } else {
            assertEquals(o.getValues().size(), c.getValues().size());
            for (Setting oS : o.getValues()) {
                Setting cS = c.getSetting(oS.getName());
                assertEquals(true, null != cS);
                compareSettingsForCloned(oS, cS);
            }
        }
    }

    private void compareTiersForClonedAgent(CallQueueAgent o, CallQueueAgent c) {
        assertEquals(null, c.getExtension());
        // Compare original and cloned CallQeuueAgent tiers count
        assertEquals(o.getTiers().getTiers().size(), c.getTiers().getTiers().size());
        ArrayList<CallQueueTier> oTiers = new ArrayList(o.getTiers().getTiers());
        ArrayList<CallQueueTier> cTiers = new ArrayList(c.getTiers().getTiers());
        for (Integer i = 0 ; i < o.getTiers().getTiers().size(); i++) {
            CallQueueTier ot = oTiers.get(i);
            CallQueueTier ct = cTiers.get(i);
            assertEquals(ot.getCallQueueId(), ct.getCallQueueId());
            assertEquals(ot.getPosition(), ct.getPosition());
            assertEquals(ot.getLevel(), ct.getLevel());
        }
        compareSettingsForCloned(o.getSettings(), c.getSettings());
    }

    private void compareClonedQueue(CallQueue o, CallQueue c) {
        assertEquals(null, c.getDid());
        compareSettingsForCloned(o.getSettings(), c.getSettings());
    }
*/
}
