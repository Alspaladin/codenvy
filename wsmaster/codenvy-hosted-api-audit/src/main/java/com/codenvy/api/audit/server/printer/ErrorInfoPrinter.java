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
package com.codenvy.api.audit.server.printer;

import org.eclipse.che.api.core.ServerException;

import java.nio.file.Path;

/**
 * Prints error in format:
 * [ERROR] <error text>!
 *
 * @author Dmytro Nochevnov
 */
public class ErrorInfoPrinter extends Printer {

    private String error;

    public ErrorInfoPrinter(Path auditReport, String error) {
        super(auditReport);

        this.error = error;
    }

    @Override
    public void print() throws ServerException {
        printError(error);
    }

}
