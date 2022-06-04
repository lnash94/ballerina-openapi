/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.validator;

import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * Abstract class for store common attribute.
 *
 * @since 1.1.0
 */
public abstract class AbstractMetaData {
    protected final SyntaxNodeAnalysisContext context;
    protected final OpenAPI openAPI;
    protected final String path;
    protected final String method;
    protected final DiagnosticSeverity severity;
    // This default location is map to relevant resource function
    protected Location location;

    public AbstractMetaData(SyntaxNodeAnalysisContext context, OpenAPI openAPI, String path, String method,
                            DiagnosticSeverity severity, Location location) {
        this.context = context;
        this.openAPI = openAPI;
        this.path = path;
        this.method = method;
        this.severity = severity;
        this.location = location;
    }
}
