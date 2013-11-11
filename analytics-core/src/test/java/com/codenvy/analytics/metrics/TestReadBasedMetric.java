/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */


package com.codenvy.analytics.metrics;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.pig.scripts.EventType;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestReadBasedMetric extends BaseTest {

    private Map<String, String> params;
    private TestMetric          metric;


    @BeforeClass
    public void init() throws IOException {
        params = Utils.newContext();
        metric = new TestMetric();

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1@gmail.com")
                        .withDate("2013-01-01")
                        .withTime("10:00:00")
                        .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws1", "user1@yahoo.com")
                        .withDate("2013-01-01")
                        .withTime("10:00:01")
                        .build());
        File log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130101");
        Parameters.TO_DATE.put(params, "20130101");
        Parameters.USER.put(params, Parameters.USER_TYPES.REGISTERED.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.PERSISTENT.name());
        Parameters.EVENT.put(params, EventType.TENANT_CREATED.toString());
        Parameters.METRIC.put(params, "testreadbasedmetric");
        Parameters.LOG.put(params, log.getAbsolutePath());

        PigServer.execute(ScriptType.NUMBER_OF_EVENTS, params);

        events = new ArrayList<>();
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user1@gmail.com")
                        .withDate("2013-01-02")
                        .withTime("10:00:00")
                        .build());
        events.add(Event.Builder.createTenantCreatedEvent("ws2", "user1@yahoo.com")
                        .withDate("2013-01-02")
                        .withTime("10:00:01")
                        .build());
        log = LogGenerator.generateLog(events);

        Parameters.FROM_DATE.put(params, "20130102");
        Parameters.TO_DATE.put(params, "20130102");
        Parameters.LOG.put(params, log.getAbsolutePath());

        PigServer.execute(ScriptType.NUMBER_OF_EVENTS, params);
    }

    @Test
    public void testEvaluateValue() throws Exception {
        TestMetric spyMetric = spy(metric);
        doReturn(new LongValueData(10)).when(spyMetric).loadValue(anyMap());

        assertEquals(spyMetric.getValue(Utils.newContext()), new LongValueData(10L));
    }

    @Test
    public void testSingleDateFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130101");

        assertEquals(metric.getValue(context), new LongValueData(2L));
    }

    @Test
    public void testDatePeriodFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130102");

        assertEquals(metric.getValue(context), new LongValueData(4L));
    }

    @Test
    public void testSingleUserFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricFilter.USER.put(context, "user1@gmail.com");

        assertEquals(metric.getValue(context), new LongValueData(2L));
    }

    @Test
    public void testDoubleUserFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricFilter.USER.put(context, "user1@gmail.com,user1@yahoo.com");

        assertEquals(metric.getValue(context), new LongValueData(4L));
    }

    @Test
    public void testSeveralFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        MetricFilter.USER.put(context, "user1@gmail.com");
        MetricFilter.WS.put(context, "ws2");

        assertEquals(metric.getValue(context), new LongValueData(1L));
    }

    @Test
    public void testSingleUserFilterAndDatePeriod() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130102");
        MetricFilter.USER.put(context, "user1@gmail.com");

        assertEquals(metric.getValue(context), new LongValueData(2L));
    }

    @Test
    public void testComplexFilter() throws Exception {
        Map<String, String> context = Utils.newContext();
        Parameters.FROM_DATE.put(context, "20130101");
        Parameters.TO_DATE.put(context, "20130102");
        MetricFilter.USER.put(context, "user1@gmail.com,user1@yahoo.com");
        MetricFilter.WS.put(context, "ws2");

        assertEquals(metric.getValue(context), new LongValueData(2L));
    }

    public class TestMetric extends ReadBasedMetric {

        private TestMetric() {
            super("TestReadBasedMetric");
        }

        @Override
        public Class<? extends ValueData> getValueDataClass() {
            return LongValueData.class;
        }

        @Override
        public Set<Parameters> getParams() {
            return Collections.emptySet();
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
