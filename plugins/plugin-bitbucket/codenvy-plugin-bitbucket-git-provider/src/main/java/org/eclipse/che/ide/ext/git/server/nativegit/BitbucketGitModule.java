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
package org.eclipse.che.ide.ext.git.server.nativegit;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.git.CredentialsProvider;
import org.eclipse.che.inject.DynaModule;

/**
 * The module that contains configuration of the server side part of the Git extension.
 *
 * @author Kevin Pollet
 */
@DynaModule
public class BitbucketGitModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), CredentialsProvider.class).addBinding().to(BitbucketOAuthCredentialProvider.class);
    }
}

