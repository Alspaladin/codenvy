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
package com.codenvy.plugin.webhooks.bitbucketserver.inject;

import com.codenvy.plugin.webhooks.bitbucketserver.BitbucketServerWebhookService;
import com.google.inject.AbstractModule;

/**
 * Guice binding for Bitbucket Server webhook service.
 *
 * @author Igor Vinokur
 */
public class BitbucketServerWebhookModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(BitbucketServerWebhookService.class);
    }
}
