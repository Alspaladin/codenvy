/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.gitlab.ide.authenticator;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

/**
 * @author Michail Kuznyetsov
 */
@ImplementedBy(GitLabAuthenticatorViewImpl.class)
public interface GitLabAuthenticatorView extends View<GitLabAuthenticatorView.ActionDelegate> {

    interface ActionDelegate {
        /** Defines what's done when the user clicks cancel. */
        void onCancelled();

        /** Defines what's done when the user clicks OK. */
        void onAccepted();
    }

    /** Show dialog. */
    void showDialog();

    /** Performs when user select generate keys. */
    boolean isGenerateKeysSelected();
}
