/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.ballerinalang.ballerina.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.swagger.models.Path;
import io.swagger.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.ballerina.OpenApiConverterUtils.getServiceBasePath;

/**
 * OpenApiServiceMapper provides functionality for reading and writing OpenApi, either to and from ballerina service, or
 * to, as well as related functionality for performing conversions between openapi and ballerina.
 */
public class OpenApiServiceMapper {
    private static final Logger logger = LoggerFactory.getLogger(
            OpenApiServiceMapper.class);
    private String httpAlias;
    private String openApiAlias;
    private ObjectMapper objectMapper;
    private final SemanticModel semanticModel;


    /**
     * Initializes a service parser for OpenApi.
     *  @param httpAlias    The alias for ballerina/http module.
     * @param openApiAlias The alias for ballerina.openapi module.
     * @param semanticModel semanticModel used for the resolve the reference in the record.
     */
    public OpenApiServiceMapper(String httpAlias, String openApiAlias, SemanticModel semanticModel) {
        // Default object mapper is JSON mapper available in openApi utils.
        this.httpAlias = httpAlias;
        this.openApiAlias = openApiAlias;
        this.semanticModel = semanticModel;
        this.objectMapper = Json.mapper();
    }

    /**
     * Retrieves the String definition of a OpenApi object.
     *
     * @param openapi OpenApi definition
     * @return String representation of current service object.
     */
    public String generateOpenApiString(OpenAPI openapi) {
        try {
            return objectMapper.writeValueAsString(openapi);
        } catch (JsonProcessingException e) {
            logger.error("Error while generating openApi string from definition" + e);
            return "Error";
        }
    }

    /**
     * This method will convert ballerina @Service to OpenApi @OpenApi object.
     *
     * @param service ballerina @Service object to be map to openapi definition
     * @return OpenApi object which represent current service.
     */
    public OpenAPI convertServiceToOpenApi(ServiceDeclarationNode service) {
        OpenAPI openapi = new OpenAPI();
        String currentServiceName = getServiceBasePath(service);
        return convertServiceToOpenApi(service, openapi, currentServiceName);
    }

    /**
     * This method will convert ballerina @Service to openApi @OpenApi object.
     *
     * @param service ballerina @Service object to be map to openApi definition
     * @param openapi OpenApi model to populate
     * @param basePath for string base path
     * @return OpenApi object which represent current service.
     */
    public OpenAPI convertServiceToOpenApi(ServiceDeclarationNode service, OpenAPI openapi, String basePath) {
        // Setting default values.
//        Info info = new Info().version("1.0.0").title(basePath.replace("/", "_"));
        Info info = new Info();
        info.setVersion("1.0.0");
        info.setTitle(basePath.replace("/", "_"));
        openapi.setInfo(info);
        Server server = new Server();
        server.setUrl(basePath.trim());
        openapi.addServersItem(server);

        NodeList<Node> functions = service.members();
        List<FunctionDefinitionNode> resource = new ArrayList<>();
        for (Node function: functions) {
            SyntaxKind kind = function.kind();
            if (kind.equals(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION)) {
                resource.add((FunctionDefinitionNode) function);
            }
        }
        OpenApiResourceMapper resourceMapper = new OpenApiResourceMapper(openapi, semanticModel);
        Map<String, Path> stringPathMap = resourceMapper.convertResourceToPath(resource);
        if (!stringPathMap.isEmpty()) {
            Paths paths = new Paths();
            for (Map.Entry<String, Path> path : stringPathMap.entrySet()) {

            }
            openapi.setPaths(paths);
        }
        return openapi;
    }

}
