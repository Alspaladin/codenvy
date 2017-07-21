/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.selenium.dashboard.organization;

import com.codenvy.organization.shared.dto.OrganizationDto;
import com.codenvy.selenium.core.client.OnpremTestOrganizationServiceClient;
import com.codenvy.selenium.pageobject.dashboard.ConfirmDialog;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import com.codenvy.selenium.pageobject.dashboard.organization.OrganizationListPage;
import com.google.inject.Inject;

import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.eclipse.che.selenium.pageobject.dashboard.NavigationBar.MenuItem.ORGANIZATIONS;
import static com.codenvy.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test validates the bulk deletion of organizations in the list.
 *
 * @author Ann Shumilova
 */
public class BulkOrganizationDeletionTest {
    private static final Logger LOG = LoggerFactory.getLogger(BulkOrganizationDeletionTest.class);

    private List<OrganizationDto> organizations;
    private OrganizationDto       organization1;
    private OrganizationDto       organization2;

    @Inject
    private OrganizationListPage                organizationListPage;
    @Inject
    private NavigationBar                       navigationBar;
    @Inject
    private ConfirmDialog                       confirmDialog;
    @Inject
    private Dashboard                           dashboard;
    @Inject
    private OnpremTestOrganizationServiceClient organizationServiceClient;
    @Inject
    private AdminTestUser                       adminTestUser;

    @BeforeClass
    public void setUp() throws Exception {
        dashboard.open(adminTestUser.getAuthToken());

        String organizationName1 = NameGenerator.generate("organization", 5);
        String organizationName2 = NameGenerator.generate("organization", 5);

        organization1 = organizationServiceClient.createOrganization(organizationName1, adminTestUser.getAuthToken());
        organization2 = organizationServiceClient.createOrganization(organizationName2, adminTestUser.getAuthToken());
        organizations = organizationServiceClient.getOrganizations(adminTestUser.getAuthToken());
    }

    @AfterClass
    public void tearDown() throws Exception {
        organizationServiceClient.deleteOrganizationById(organization1.getId(), adminTestUser.getAuthToken());
        organizationServiceClient.deleteOrganizationById(organization2.getId(), adminTestUser.getAuthToken());
    }

    @Test
    public void testOrganizationBulkDeletion() {
        navigationBar.waitNavigationBar();
        int organizationsCount = organizations.size();

        navigationBar.clickOnMenu(ORGANIZATIONS);
        organizationListPage.waitForOrganizationsToolbar();
        navigationBar.clickOnMenu(ORGANIZATIONS);
        organizationListPage.waitForOrganizationsToolbar();
        organizationListPage.waitForOrganizationsList();
        assertEquals(organizationListPage.getOrganizationListItemCount(), organizationsCount);
        assertTrue(organizationListPage.getValues(NAME).contains(organization1.getName()));
        assertTrue(organizationListPage.getValues(NAME).contains(organization2.getName()));

        // Tests delete:
        assertFalse(organizationListPage.isBulkDeleteVisible());
        organizationListPage.clickCheckbox(organization1.getName());
        organizationListPage.clickCheckbox(organization2.getName());
        assertTrue(organizationListPage.isBulkDeleteVisible());
        organizationListPage.clickCheckbox(organization1.getName());
        organizationListPage.clickCheckbox(organization2.getName());
        assertFalse(organizationListPage.isBulkDeleteVisible());
        organizationListPage.clickCheckbox(organization1.getName());
        organizationListPage.clickCheckbox(organization2.getName());
        assertTrue(organizationListPage.isBulkDeleteVisible());

        organizationListPage.clickBulkDeleteButton();
        confirmDialog.waitOpened();
        assertEquals(confirmDialog.getTitle(), "Delete organizations");
        assertEquals(confirmDialog.getMessage(), String.format("Would you like to delete these %s organizations?", 2));
        assertEquals(confirmDialog.getConfirmButtonTitle(), "Delete");
        assertEquals(confirmDialog.getCancelButtonTitle(), "Close");
        confirmDialog.clickConfirm();

        confirmDialog.waitClosed();
        organizationListPage.waitForOrganizationsList();

        organizationListPage.waitForOrganizationIsRemoved(organization1.getName());
        organizationListPage.waitForOrganizationIsRemoved(organization2.getName());

        assertEquals(organizationListPage.getOrganizationListItemCount(), organizationsCount - 2);
        assertFalse(organizationListPage.getValues(NAME).contains(organization1.getName()));
        assertFalse(organizationListPage.getValues(NAME).contains(organization2.getName()));
        assertEquals(navigationBar.getMenuCounterValue(ORGANIZATIONS), String.valueOf(organizationsCount - 2));
    }
}
