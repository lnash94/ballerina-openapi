/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.converter.service;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.converter.Constants;
import io.ballerina.openapi.converter.utils.ConverterCommonUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.SyntaxKind.BOOLEAN_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.LIST_CONSTRUCTOR;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.MAPPING_CONSTRUCTOR;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.NIL_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.NUMERIC_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPTIONAL_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SIMPLE_NAME_REFERENCE;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;
import static io.ballerina.openapi.converter.utils.ConverterCommonUtils.getAnnotationNodesFromServiceNode;
import static io.ballerina.openapi.converter.utils.ConverterCommonUtils.handleReference;
import static io.ballerina.openapi.converter.utils.ConverterCommonUtils.unescapeIdentifier;


/**
 * This class processes mapping query parameters in between Ballerina and OAS.
 *
 * @since 2.0.0
 */
public class OpenAPIQueryParameterMapper {
    private final Components components;
    private final SemanticModel semanticModel;
    private final Map<String, String> apidocs;
    private final SyntaxKind[] validExpressionKind = {STRING_LITERAL, NUMERIC_LITERAL, BOOLEAN_LITERAL,
            LIST_CONSTRUCTOR, NIL_LITERAL, MAPPING_CONSTRUCTOR};

    public OpenAPIQueryParameterMapper(Map<String, String> apidocs, Components components,
                                       SemanticModel semanticModel) {
        this.apidocs = apidocs;
        this.components = components;
        this.semanticModel = semanticModel;
    }

    /**
     * Handle function query parameters for required parameters.
     */
    public Parameter createQueryParameter(RequiredParameterNode queryParam) {
        String queryParamName = unescapeIdentifier(queryParam.paramName().get().text());
        boolean isQuery = !queryParam.paramName().get().text().equals(Constants.PATH)
                && queryParam.annotations().isEmpty();
        if (queryParam.typeName() instanceof BuiltinSimpleNameReferenceNode && isQuery) {
            QueryParameter queryParameter = new QueryParameter();
            queryParameter.setName(unescapeIdentifier(queryParamName));
            Schema openApiSchema = ConverterCommonUtils.getOpenApiSchema(queryParam.typeName().toString().trim());
            queryParameter.setSchema(openApiSchema);
            queryParameter.setRequired(true);
            if (!apidocs.isEmpty() && queryParam.paramName().isPresent() && apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName.trim()));
            }
            return queryParameter;
        } else if (queryParam.typeName().kind() == OPTIONAL_TYPE_DESC && isQuery) {
            // Handle optional query parameter
            NodeList<AnnotationNode> annotations = getAnnotationNodesFromServiceNode(queryParam);
            String isOptional = Constants.TRUE;
            if (!annotations.isEmpty()) {
                Optional<String> values = ConverterCommonUtils.extractServiceAnnotationDetails(annotations,
                        "http:ServiceConfig", "treatNilableAsOptional");
                if (values.isPresent()) {
                    isOptional = values.get();
                }
            }
            return setOptionalQueryParameter(queryParamName, ((OptionalTypeDescriptorNode) queryParam.typeName()),
                    isOptional);
        } else if (queryParam.typeName().kind() == SyntaxKind.ARRAY_TYPE_DESC && isQuery) {
            // Handle required array type query parameter
            ArrayTypeDescriptorNode arrayNode = (ArrayTypeDescriptorNode) queryParam.typeName();
            return handleArrayTypeQueryParameter(queryParamName, arrayNode);
        } else if (queryParam.typeName() instanceof SimpleNameReferenceNode && isQuery) {
            QueryParameter queryParameter = new QueryParameter();
            queryParameter.setName(unescapeIdentifier(queryParamName));
            SimpleNameReferenceNode queryNode = (SimpleNameReferenceNode) queryParam.typeName();
            OpenAPIComponentMapper componentMapper = new OpenAPIComponentMapper(components);
            TypeSymbol typeSymbol = (TypeSymbol) semanticModel.symbol(queryNode).orElseThrow();
            componentMapper.createComponentSchema(components.getSchemas(), typeSymbol);
            Schema<?> schema = new Schema<>();
            schema.set$ref(unescapeIdentifier(queryNode.name().text().trim()));
            queryParameter.setSchema(schema);
            queryParameter.setRequired(true);
            if (!apidocs.isEmpty() && queryParam.paramName().isPresent() && apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName.trim()));
            }
            return queryParameter;
        } else if (queryParam.typeName().kind() == SIMPLE_NAME_REFERENCE) {
            QueryParameter queryParameter = new QueryParameter();
            Schema<?> refSchema = handleReference(semanticModel, components, (SimpleNameReferenceNode)
                    queryParam.typeName());
            queryParameter.setSchema(refSchema);
            queryParameter.setRequired(true);
            if (!apidocs.isEmpty() && apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName));
            }
            return queryParameter;
        } else {
            QueryParameter queryParameter = createContentTypeForMapJson(queryParamName, false);
            if (!apidocs.isEmpty() && queryParam.paramName().isPresent() && apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName.trim()));
            }
            return queryParameter;
        }
    }

    /**
     * Create OAS query parameter for default query parameters.
     */
    public Parameter createQueryParameter(DefaultableParameterNode defaultableQueryParam) {

        String queryParamName = defaultableQueryParam.paramName().get().text();
        boolean isQuery = !defaultableQueryParam.paramName().get().text().equals(Constants.PATH) &&
                defaultableQueryParam.annotations().isEmpty();

        QueryParameter queryParameter = new QueryParameter();
        if (defaultableQueryParam.typeName() instanceof BuiltinSimpleNameReferenceNode && isQuery) {
            queryParameter.setName(unescapeIdentifier(queryParamName));
            Schema openApiSchema = ConverterCommonUtils.getOpenApiSchema(
                    defaultableQueryParam.typeName().toString().trim());
            queryParameter.setSchema(openApiSchema);
            if (!apidocs.isEmpty() && defaultableQueryParam.paramName().isPresent() &&
                    apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName.trim()));
            }
        } else if (defaultableQueryParam.typeName().kind() == OPTIONAL_TYPE_DESC && isQuery) {
            // Handle optional query parameter
            queryParameter = setOptionalQueryParameter(queryParamName,
                    ((OptionalTypeDescriptorNode) defaultableQueryParam.typeName()),
                    Constants.TRUE);
        } else if (defaultableQueryParam.typeName() instanceof ArrayTypeDescriptorNode && isQuery) {
            // Handle required array type query parameter
            ArrayTypeDescriptorNode arrayNode = (ArrayTypeDescriptorNode) defaultableQueryParam.typeName();
            queryParameter = handleArrayTypeQueryParameter(queryParamName, arrayNode);
        } else if (defaultableQueryParam.typeName().kind() == SIMPLE_NAME_REFERENCE) {
            queryParameter.setName(unescapeIdentifier(queryParamName));
            Schema<?> refSchema = handleReference(semanticModel, components,
                    (SimpleNameReferenceNode) defaultableQueryParam.typeName());
            queryParameter.setSchema(refSchema);
            queryParameter.setRequired(true);
            if (!apidocs.isEmpty() && apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName));
            }
        } else {
            queryParameter = createContentTypeForMapJson(queryParamName, false);
            if (!apidocs.isEmpty() && defaultableQueryParam.paramName().isPresent() &&
                    apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName.trim()));
            }
        }

        if (Arrays.stream(validExpressionKind).anyMatch(syntaxKind -> syntaxKind ==
                defaultableQueryParam.expression().kind())) {
            SyntaxKind kind = defaultableQueryParam.expression().kind();
            Object defaultValue;
            if (kind == LIST_CONSTRUCTOR) {
                SeparatedNodeList<Node> expressions = ((ListConstructorExpressionNode) defaultableQueryParam.expression()).expressions();
                Object[] values = new Object[expressions.size()];
                int i = 0;
                for(Node node: expressions) {
                    SyntaxKind NodeKind = node.kind();
                    if (NodeKind == STRING_LITERAL) {
                        values[i] = node.toString().replaceAll("\"", "");
                    } else if (NodeKind == NUMERIC_LITERAL) {
                        values[i] = Integer.parseInt(node.toString());
                    } else if (NodeKind == BOOLEAN_LITERAL) {
                        values[i] = Boolean.parseBoolean(node.toString());
                    }
//                    values[i] = Integer.parseInt(node.toString());
                    i++;
                }
                defaultValue = values;
            } else {
                defaultValue = defaultableQueryParam.expression().toString().replaceAll("\"", "");
            }

            if (kind == NIL_LITERAL) {
                defaultValue = null;
            }
            if (queryParameter.getContent() != null) {
                Content content = queryParameter.getContent();
                for (Map.Entry<String, MediaType> stringMediaTypeEntry : content.entrySet()) {
                    Schema schema = stringMediaTypeEntry.getValue().getSchema();
                    schema.setDefault(defaultValue);
                    io.swagger.v3.oas.models.media.MediaType media = new io.swagger.v3.oas.models.media.MediaType();
                    media.setSchema(schema);
                    content.addMediaType(stringMediaTypeEntry.getKey(), media);
                }
            } else {
                Schema schema = queryParameter.getSchema();
                schema.setDefault(defaultValue);
                queryParameter.setSchema(schema);
            }
        }
        return queryParameter;
    }

    /**
     * Handle array type query parameter.
     */
    private QueryParameter handleArrayTypeQueryParameter(String queryParamName, ArrayTypeDescriptorNode arrayNode) {
        QueryParameter queryParameter = new QueryParameter();
        ArraySchema arraySchema = new ArraySchema();
        queryParameter.setName(unescapeIdentifier(queryParamName));
        TypeDescriptorNode itemTypeNode = arrayNode.memberTypeDesc();
        Schema<?> itemSchema;
        if (arrayNode.memberTypeDesc().kind() == OPTIONAL_TYPE_DESC) {
            itemSchema = ConverterCommonUtils.getOpenApiSchema(
                    ((OptionalTypeDescriptorNode) itemTypeNode).typeDescriptor().toString().trim());
            itemSchema.setNullable(true);
        } else if (arrayNode.memberTypeDesc().kind() == SIMPLE_NAME_REFERENCE) {
            itemSchema = getItemSchemaForReference(arrayNode);
        } else {
            itemSchema = ConverterCommonUtils.getOpenApiSchema(itemTypeNode.toString().trim());
        }
        arraySchema.setItems(itemSchema);
        queryParameter.schema(arraySchema);
        queryParameter.setRequired(true);
        if (!apidocs.isEmpty() && apidocs.containsKey(queryParamName)) {
            queryParameter.setDescription(apidocs.get(queryParamName));
        }
        return queryParameter;
    }

    private Schema<?> getItemSchemaForReference(ArrayTypeDescriptorNode arrayNode) {
        SimpleNameReferenceNode record = (SimpleNameReferenceNode) arrayNode.memberTypeDesc();
        return handleReference(semanticModel, components, record);
    }

    /**
     * Handle optional query parameter.
     */
    private QueryParameter setOptionalQueryParameter(String queryParamName, OptionalTypeDescriptorNode typeNode,
                                                String isOptional) {
        QueryParameter queryParameter = new QueryParameter();
        if (isOptional.equals(Constants.FALSE)) {
            queryParameter.setRequired(true);
        }
        queryParameter.setName(unescapeIdentifier(queryParamName));
        Node node = typeNode.typeDescriptor();
        if (node.kind() == SyntaxKind.ARRAY_TYPE_DESC) {
            ArraySchema arraySchema = new ArraySchema();
            arraySchema.setNullable(true);
            ArrayTypeDescriptorNode arrayNode = (ArrayTypeDescriptorNode) node;
            TypeDescriptorNode itemTypeNode = arrayNode.memberTypeDesc();
            Schema itemSchema;
            // handle optional array with references
            if (arrayNode.memberTypeDesc().kind() == SIMPLE_NAME_REFERENCE) {
                itemSchema = getItemSchemaForReference(arrayNode);
            } else {
                itemSchema = ConverterCommonUtils.getOpenApiSchema(itemTypeNode.toString().trim());
            }
            arraySchema.setItems(itemSchema);
            queryParameter.schema(arraySchema);
            queryParameter.setName(unescapeIdentifier(queryParamName));
            if (!apidocs.isEmpty() && apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName));
            }
            return queryParameter;
        } else if (node.kind() == SyntaxKind.MAP_TYPE_DESC) {
            queryParameter = createContentTypeForMapJson(queryParamName, true);
            if (isOptional.equals(Constants.FALSE)) {
                queryParameter.setRequired(true);
            }
            if (!apidocs.isEmpty() && apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName));
            }
            return queryParameter;
        } else if (node.kind() == SIMPLE_NAME_REFERENCE) {
            Schema<?> refSchema = handleReference(semanticModel, components, (SimpleNameReferenceNode) node);
            queryParameter.setSchema(refSchema);
            if (isOptional.equals(Constants.FALSE)) {
                queryParameter.setRequired(true);
            }
            if (!apidocs.isEmpty() && apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName));
            }
            return queryParameter;
        } else {
            Schema<?> openApiSchema = ConverterCommonUtils.getOpenApiSchema(node.toString().trim());
            openApiSchema.setNullable(true);
            queryParameter.setSchema(openApiSchema);
            if (!apidocs.isEmpty() && apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName));
            }
            return queryParameter;
        }
    }

    private QueryParameter createContentTypeForMapJson(String queryParamName, boolean nullable) {
        QueryParameter queryParameter = new QueryParameter();
        ObjectSchema objectSchema = new ObjectSchema();
        if (nullable) {
            objectSchema.setNullable(true);
        }
        objectSchema.setAdditionalProperties(true);
        MediaType media = new MediaType();
        media.setSchema(objectSchema);
        queryParameter.setContent(new Content().addMediaType("application/json", media));
        queryParameter.setName(unescapeIdentifier(queryParamName));
        return queryParameter;
    }
}
