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
package com.codenvy.auth.sso.client.token;

import javax.servlet.http.HttpServletRequest;

/**
 * Try to extract token from request in 3 steps.
 * 1. From query parameter.
 * 2. From header.
 * 3. From cookie.
 *
 * @author Sergii Kabashniuk
 */
public class ChainedTokenExtractor implements RequestTokenExtractor {
    private final CookieRequestTokenExtractor cookieRequestTokenExtractor;

    private final HeaderRequestTokenExtractor headerRequestTokenExtractor;

    private final QueryRequestTokenExtractor queryRequestTokenExtractor;

    public ChainedTokenExtractor() {
        cookieRequestTokenExtractor = new CookieRequestTokenExtractor();
        headerRequestTokenExtractor = new HeaderRequestTokenExtractor();
        queryRequestTokenExtractor = new QueryRequestTokenExtractor();
    }

    @Override
    public String getToken(HttpServletRequest req) {
        String token;
        if ((token = queryRequestTokenExtractor.getToken(req)) == null) {
            if ((token = headerRequestTokenExtractor.getToken(req)) == null) {
                token = cookieRequestTokenExtractor.getToken(req);
            }
        }
        return token;
    }
}
