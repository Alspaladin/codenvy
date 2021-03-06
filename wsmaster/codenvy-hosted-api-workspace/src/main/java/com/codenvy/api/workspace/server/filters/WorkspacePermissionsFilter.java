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
package com.codenvy.api.workspace.server.filters;

import com.codenvy.api.permission.server.SuperPrivilegesChecker;
import com.codenvy.api.permission.server.account.AccountOperation;
import com.codenvy.api.permission.server.account.AccountPermissionsChecker;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.inject.Inject;
import javax.ws.rs.Path;
import java.util.Map;
import java.util.Set;

import static com.codenvy.api.workspace.server.WorkspaceDomain.CONFIGURE;
import static com.codenvy.api.workspace.server.WorkspaceDomain.DELETE;
import static com.codenvy.api.workspace.server.WorkspaceDomain.DOMAIN_ID;
import static com.codenvy.api.workspace.server.WorkspaceDomain.READ;
import static com.codenvy.api.workspace.server.WorkspaceDomain.RUN;
import static com.codenvy.api.workspace.server.WorkspaceDomain.USE;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Restricts access to methods of {@link WorkspaceService} by users' permissions.
 *
 * <p>Filter contains rules for protecting of all methods of {@link WorkspaceService}.<br>
 * In case when requested method is unknown filter throws {@link ForbiddenException}
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/workspace{path:(/.*)?}")
public class WorkspacePermissionsFilter extends CheMethodInvokerFilter {
    private final WorkspaceManager                       workspaceManager;
    private final AccountManager                         accountManager;
    private final Map<String, AccountPermissionsChecker> accountTypeToPermissionsChecker;
    private final SuperPrivilegesChecker                 superPrivilegesChecker;

    @Inject
    public WorkspacePermissionsFilter(WorkspaceManager workspaceManager,
                                      AccountManager accountManager,
                                      Set<AccountPermissionsChecker> accountPermissionsCheckers,
                                      SuperPrivilegesChecker superPrivilegesChecker) {
        this.workspaceManager = workspaceManager;
        this.accountManager = accountManager;
        this.accountTypeToPermissionsChecker = accountPermissionsCheckers.stream()
                                                                         .collect(toMap(AccountPermissionsChecker::getAccountType,
                                                                                        identity()));
        this.superPrivilegesChecker = superPrivilegesChecker;
    }

    @Override
    public void filter(GenericResourceMethod genericResourceMethod, Object[] arguments) throws ForbiddenException,
                                                                                               ServerException,
                                                                                               NotFoundException {
        final String methodName = genericResourceMethod.getMethod().getName();

        final Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
        String action;
        String key;

        switch (methodName) {
            case "getSettings":
            case "getWorkspaces":
                //methods accessible to every user
                return;

            case "getByNamespace": {
                if (superPrivilegesChecker.hasSuperPrivileges()) {
                    return;
                }
                checkAccountPermissions((String)arguments[1], AccountOperation.MANAGE_WORKSPACES);
                return;
            }

            case "create": {
                checkAccountPermissions((String)arguments[3], AccountOperation.CREATE_WORKSPACE);
                return;
            }

            case "startFromConfig": {
                checkAccountPermissions((String)arguments[2], AccountOperation.CREATE_WORKSPACE);
                return;
            }

            case "delete":
                key = ((String)arguments[0]);
                action = DELETE;
                break;

            case "stop":
                if (superPrivilegesChecker.hasSuperPrivileges()) {
                    return;
                }
            case "startById":
            case "createSnapshot":
                key = ((String)arguments[0]);
                action = RUN;
                break;

            case "getSnapshot":
                key = ((String)arguments[0]);
                action = READ;
                break;

            case "getByKey":
                if (superPrivilegesChecker.hasSuperPrivileges()) {
                    return;
                }
            case "checkAgentHealth":
                key = ((String)arguments[0]);
                action = READ;
                break;

            case "update":
            case "addProject":
            case "deleteProject":
            case "updateProject":
            case "addEnvironment":
            case "deleteEnvironment":
            case "updateEnvironment":
            case "addCommand":
            case "deleteCommand":
            case "updateCommand":
                key = ((String)arguments[0]);
                action = CONFIGURE;
                break;

            // MachineService methods
            case "startMachine":
            case "stopMachine":
                key = ((String)arguments[0]);
                action = RUN;
                break;

            case "getMachineById":
            case "getMachines":
            case "executeCommandInMachine":
            case "getProcesses":
            case "stopProcess":
            case "getProcessLogs":
                key = ((String)arguments[0]);
                action = USE;
                break;

            default:
                throw new ForbiddenException("The user does not have permission to perform this operation");
        }

        final WorkspaceImpl workspace = workspaceManager.getWorkspace(key);
        try {
            checkAccountPermissions(workspace.getNamespace(), AccountOperation.MANAGE_WORKSPACES);
            // user is authorized to perform any operation if workspace belongs to account where he has the corresponding permissions
        } catch (ForbiddenException e) {
            //check permissions on workspace level
            if (!currentSubject.hasPermission(DOMAIN_ID, workspace.getId(), action)) {
                throw new ForbiddenException(
                        "The user does not have permission to " + action + " workspace with id '" + workspace.getId() + "'");
            }
        }
    }

    void checkAccountPermissions(String accountName, AccountOperation operation) throws ForbiddenException,
                                                                                        NotFoundException,
                                                                                        ServerException {
        if (accountName == null) {
            // default namespace will be used
            return;
        }

        final Account account = accountManager.getByName(accountName);

        AccountPermissionsChecker accountPermissionsChecker = accountTypeToPermissionsChecker.get(account.getType());

        if (accountPermissionsChecker == null) {
            throw new ForbiddenException("User is not authorized to use specified namespace");
        }

        accountPermissionsChecker.checkPermissions(account.getId(), operation);
    }
}
