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
package io.ballerina.openapi.service.mapper;

import io.ballerina.compiler.api.symbols.Documentable;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.OperationInventory;
import io.ballerina.openapi.service.mapper.parameter.ParameterMapper;
import io.ballerina.openapi.service.mapper.parameter.ParameterMapperException;
import io.ballerina.openapi.service.mapper.response.ResponseMapper;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.Constants.DEFAULT;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getOperationId;

/**
 * This {@link ResourceMapperImpl} class is the implementation of the {@link ResourceMapper} interface.
 * This class provides the functionality for mapping the Ballerina resources to OpenAPI operations.
 *
 * @since 1.0.0
 */
public class ResourceMapperImpl implements ResourceMapper {
    private final Paths pathObject = new Paths();
    private final AdditionalData additionalData;
    private final OpenAPI openAPI;
    private final List<FunctionDefinitionNode> resources;
    private final ServiceMapperFactory serviceMapperFactory;

    /**
     * Initializes a resource parser for openApi.
     */
    ResourceMapperImpl(OpenAPI openAPI, List<FunctionDefinitionNode> resources, AdditionalData additionalData,
                       ServiceMapperFactory serviceMapperFactory) {
        this.openAPI = openAPI;
        this.resources = resources;
        this.additionalData = additionalData;
        this.serviceMapperFactory = serviceMapperFactory;
    }

    public void setOperation() {
        for (FunctionDefinitionNode resource : resources) {
            addResourceMapping(resource);
        }
        openAPI.setPaths(pathObject);
    }

    private void addResourceMapping(FunctionDefinitionNode resource) {
        String path = MapperCommonUtils.unescapeIdentifier(generateRelativePath(resource));
        String httpMethod = resource.functionName().toString().trim();
        if (httpMethod.equals(String.format("'%s", DEFAULT)) || httpMethod.equals(DEFAULT)) {
            ExceptionDiagnostic error = new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_100,
                    resource.location());
            additionalData.diagnostics().add(error);
        } else {
            convertResourceToOperation(resource, httpMethod, path).ifPresent(
                    operation -> addPathItem(httpMethod, pathObject, operation.getOperation(), path));
        }
    }

    private void addPathItem(String httpMethod, Paths path, Operation operation, String pathName) {
        PathItem pathItem = new PathItem();
        switch (httpMethod.trim().toUpperCase(Locale.ENGLISH)) {
            case Constants.GET -> {
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setGet(operation);
                } else {
                    pathItem.setGet(operation);
                    path.addPathItem(pathName, pathItem);
                }
            }
            case Constants.PUT -> {
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setPut(operation);
                } else {
                    pathItem.setPut(operation);
                    path.addPathItem(pathName, pathItem);
                }
            }
            case Constants.POST -> {
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setPost(operation);
                } else {
                    pathItem.setPost(operation);
                    path.addPathItem(pathName, pathItem);
                }
            }
            case Constants.DELETE -> {
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setDelete(operation);
                } else {
                    pathItem.setDelete(operation);
                    path.addPathItem(pathName, pathItem);
                }
            }
            case Constants.OPTIONS -> {
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setOptions(operation);
                } else {
                    pathItem.setOptions(operation);
                    path.addPathItem(pathName, pathItem);
                }
            }
            case Constants.PATCH -> {
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setPatch(operation);
                } else {
                    pathItem.setPatch(operation);
                    path.addPathItem(pathName, pathItem);
                }
            }
            case Constants.HEAD -> {
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setHead(operation);
                } else {
                    pathItem.setHead(operation);
                    path.addPathItem(pathName, pathItem);
                }
            }
            default -> { }
        }
    }

    /**
     * This method will convert ballerina @Resource to ballerina @OperationAdaptor.
     *
     * @return Operation Adaptor object of given resource
     */
    private Optional<OperationInventory> convertResourceToOperation(FunctionDefinitionNode resourceFunction,
                                                                    String httpMethod, String generateRelativePath) {
        OperationInventory operationInventory = new OperationInventory();
        operationInventory.setHttpOperation(httpMethod);
        operationInventory.setPath(generateRelativePath);
        operationInventory.setOperationId(getOperationId(resourceFunction));
        // Set operation summary
        // Map API documentation
        Map<String, String> apiDocs = listAPIDocumentations(resourceFunction, operationInventory);
        //Add path parameters if in path and query parameters
        ParameterMapper parameterMapper = serviceMapperFactory.getParameterMapper(resourceFunction, apiDocs,
                operationInventory);
        try {
            parameterMapper.setParameters();
        } catch (ParameterMapperException exception) {
            additionalData.diagnostics().add(exception.getDiagnostic());
            return Optional.empty();
        }

        ResponseMapper responseMapper = serviceMapperFactory.getResponseMapper(resourceFunction, operationInventory);
        responseMapper.setApiResponses();
        return Optional.of(operationInventory);
    }

    /**
     * Filter the API documentations from resource function node.
     */
    private Map<String, String> listAPIDocumentations(FunctionDefinitionNode resource,
                                                      OperationInventory operationInventory) {

        Map<String, String> apiDocs = new HashMap<>();
        if (resource.metadata().isPresent()) {
            Optional<Symbol> resourceSymbol = additionalData.semanticModel().symbol(resource);
            if (resourceSymbol.isPresent()) {
                Symbol symbol = resourceSymbol.get();
                Optional<Documentation> documentation = ((Documentable) symbol).documentation();
                if (documentation.isPresent()) {
                    Documentation documentation1 = documentation.get();
                    Optional<String> description = documentation1.description();
                    if (description.isPresent()) {
                        String resourceFunctionAPI = description.get().trim();
                        apiDocs = documentation1.parameterMap();
                        operationInventory.setSummary(resourceFunctionAPI);
                    }
                }
            }
        }
        return apiDocs;
    }

    private String generateRelativePath(FunctionDefinitionNode resource) {

        StringBuilder relativePath = new StringBuilder();
        relativePath.append("/");
        if (!resource.relativeResourcePath().isEmpty()) {
            for (Node node: resource.relativeResourcePath()) {
                if (node instanceof ResourcePathParameterNode pathNode) {
                    relativePath.append("{");
                    Optional<Token> pathParamToken = pathNode.paramName();
                    if (pathParamToken.isPresent()) {
                        relativePath.append(pathParamToken.get());
                    } else {
                        relativePath.append("unsupported");
                    }
                    relativePath.append("}");
                } else if ((resource.relativeResourcePath().size() == 1) && (node.toString().trim().equals("."))) {
                    return relativePath.toString();
                } else {
                    relativePath.append(node.toString().trim());
                }
            }
        }
        return relativePath.toString();
    }
}
