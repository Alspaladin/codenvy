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
package com.codenvy.auth.sso.client;

import org.eclipse.che.commons.subject.Subject;

import java.security.Principal;

/** @author Sergii Kabashniuk */
public class SsoClientPrincipal implements Principal {
    private final Subject subject;
    private final String  clientUrl;
    private final String  token;

    public SsoClientPrincipal(String token,
                              String clientUrl,
                              Subject subject) {
        this.clientUrl = clientUrl;
        this.token = token;
        this.subject = subject;
    }


    public String getClientUrl() {
        return clientUrl;
    }

    public String getToken() {
        return token;
    }

    public Subject getUser() {
        return subject;
    }

    @Override
    public String getName() {
        return subject.getUserName();
    }
}
