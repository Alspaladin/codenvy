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
package com.codenvy.selenium.core.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.selenium.core.provider.TestDashboardUrlProvider;

import javax.inject.Named;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Anatolii Bazko
 */
@Singleton
public class OnpremTestDashboardUrlProvider implements TestDashboardUrlProvider {
    @Inject
    @Named("sys.protocol")
    private String protocol;
    @Inject
    @Named("sys.host")
    private String host;

    @Override
    public URL get() {
        try {
            return new URL(protocol, host, -1, "/dashboard/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
