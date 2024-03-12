/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.corenew.typegenerator.generators;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RecordRestDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.corenew.typegenerator.GeneratorUtils;
import io.ballerina.openapi.corenew.typegenerator.TypeGeneratorUtils;
import io.ballerina.openapi.corenew.typegenerator.exception.BallerinaOpenApiException;
import io.ballerina.openapi.corenew.typegenerator.model.RecordMetadata;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ELLIPSIS_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.openapi.corenew.typegenerator.GeneratorUtils.getValidName;

/**
 * Generate TypeDefinitionNode and TypeDescriptorNode for object type schema.
 * -- ex:
 * Sample OpenAPI :
 * <pre>
 *      components:
 *          schemas:
 *              Pet:
 *                  required:
 *                      - id
 *                      - name
 *                  properties:
 *                      id:
 *                          type: integer
 *                          format: int64
 *                      name:
 *                          type: string
 *                      tag:
 *                          type: string
 *                      type:
 *                          type: string
 *  </pre>
 * Generated Ballerina type for the schema `Pet` :
 * <pre>
 * public type Pet record {
 *      int id;
 *      string name;
 *      string tag?;
 *      string 'type?;
 * };
 * </pre>
 *
 * @since 1.3.0
 */
public class RecordTypeGenerator extends TypeGenerator {

    public static final PrintStream OUT_STREAM = System.err;
    public RecordTypeGenerator(Schema schema, String typeName, HashMap<String, TypeDefinitionNode> subTypesMap, HashMap<String, NameReferenceNode> pregeneratedTypeMap) {
        super(schema, typeName, subTypesMap, pregeneratedTypeMap);
    }

    /**
     * Generate TypeDescriptorNode for object type schemas.
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws BallerinaOpenApiException {
        List<Node> recordFields = new LinkedList<>();
        // extract metadata with additional properties
        RecordMetadata metadataBuilder = getRecordMetadata();

        if (schema.getProperties() != null) {
            Map<String, Schema<?>> properties = schema.getProperties();
            List<String> required = schema.getRequired();
            recordFields.addAll(addRecordFields(required, properties.entrySet(), typeName));
            NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFields);
            return NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD),
                    metadataBuilder.isOpenRecord() ? createToken(OPEN_BRACE_TOKEN) : createToken(OPEN_BRACE_PIPE_TOKEN),
                    fieldNodes, metadataBuilder.getRestDescriptorNode(),
                    metadataBuilder.isOpenRecord() ? createToken(CLOSE_BRACE_TOKEN) :
                            createToken(CLOSE_BRACE_PIPE_TOKEN));
        } else {
            return NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD),
                    metadataBuilder.isOpenRecord() ? createToken(OPEN_BRACE_TOKEN) : createToken(OPEN_BRACE_PIPE_TOKEN),
                    createNodeList(recordFields), metadataBuilder.getRestDescriptorNode(),
                    metadataBuilder.isOpenRecord() ? createToken(CLOSE_BRACE_TOKEN) :
                            createToken(CLOSE_BRACE_PIPE_TOKEN));
        }
    }

    /**
     * This function is to extract the main details of the record need to be generated and return {@code
     * RecordMetadata} object with required details.
     *
     * @return {@code RecordMetadata}
     * @throws BallerinaOpenApiException throws when process has some failure.
     */
    public RecordMetadata getRecordMetadata() throws BallerinaOpenApiException {
        boolean isOpenRecord = true;
        RecordRestDescriptorNode recordRestDescNode = null;

        if (schema.getAdditionalProperties() != null) {
            Object additionalProperties = schema.getAdditionalProperties();
            if (additionalProperties instanceof Schema) {
                Schema<?> additionalPropSchema = (Schema<?>) additionalProperties;
                if (GeneratorUtils.hasConstraints(additionalPropSchema)) {
                    // use printStream to echo the error, because current openapi to ballerina implementation doesn't
                    // handle diagnostic message.
                    isOpenRecord = false;
                    OUT_STREAM.println("WARNING: constraints in the OpenAPI contract will be ignored for the " +
                            "additionalProperties field, as constraints are not supported on Ballerina rest record " +
                            "field.");
                }
                if (additionalPropSchema.get$ref() != null) {
                    isOpenRecord = false;
                    recordRestDescNode = getRestDescriptorNodeForReference(additionalPropSchema);
                } else if (GeneratorUtils.getOpenAPIType(additionalPropSchema) != null) {
                    isOpenRecord = false;
                    recordRestDescNode = getRecordRestDescriptorNode(additionalPropSchema);
                } else if (GeneratorUtils.isComposedSchema(additionalPropSchema)) {
                    OUT_STREAM.println("WARNING: generating Ballerina rest record field will be ignored for the " +
                            "OpenAPI contract additionalProperties type `ComposedSchema`, as it is not supported on " +
                            "Ballerina rest record field.");
                }
            } else if (additionalProperties.equals(false)) {
                isOpenRecord = false;
            }
        }

        return new RecordMetadata.Builder()
                        .withIsOpenRecord(isOpenRecord)
                        .withRestDescriptorNode(recordRestDescNode).build();
    }

    /**
     * Creates reference rest node when additional property has reference.
     */
    public RecordRestDescriptorNode getRestDescriptorNodeForReference(Schema<?> additionalPropSchema)
            throws BallerinaOpenApiException {
        ReferencedTypeGenerator referencedTypeGenerator = new ReferencedTypeGenerator(additionalPropSchema, null, subTypesMap, pregeneratedTypeMap);
        TypeDescriptorNode refNode = referencedTypeGenerator.generateTypeDescriptorNode();
        return NodeFactory.createRecordRestDescriptorNode(refNode, createToken(ELLIPSIS_TOKEN),
                createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generates {@code RecordRestDescriptorNode} for the additional properties in object schema.
     * <pre>
     *    type User record {
     *       string...;
     *     }
     * </pre>
     */
    public RecordRestDescriptorNode getRecordRestDescriptorNode(Schema<?> additionalPropSchema)
            throws BallerinaOpenApiException {

        RecordRestDescriptorNode recordRestDescNode = null;
        if (GeneratorUtils.isNumberSchema(additionalPropSchema) && additionalPropSchema.getFormat() != null) {
            // this is special for `NumberSchema` because it has format with its expected type.
            SimpleNameReferenceNode numberNode = createSimpleNameReferenceNode(
                    createIdentifierToken(GeneratorUtils.convertOpenAPITypeToBallerina(additionalPropSchema)));
            recordRestDescNode = NodeFactory.createRecordRestDescriptorNode(
                    TypeGeneratorUtils.getNullableType(additionalPropSchema, numberNode),
                    createToken(ELLIPSIS_TOKEN),
                    createToken(SEMICOLON_TOKEN));
        } else if (GeneratorUtils.isObjectSchema(additionalPropSchema) ||
                GeneratorUtils.isMapSchema(additionalPropSchema)) {
            RecordTypeGenerator record = new RecordTypeGenerator(additionalPropSchema, null, subTypesMap, pregeneratedTypeMap);
            TypeDescriptorNode recordNode = TypeGeneratorUtils.getNullableType(additionalPropSchema,
                    record.generateTypeDescriptorNode());
            recordRestDescNode = NodeFactory.createRecordRestDescriptorNode(recordNode, createToken(ELLIPSIS_TOKEN),
                    createToken(SEMICOLON_TOKEN));
        } else if (GeneratorUtils.isArraySchema(additionalPropSchema)) {
            ArrayTypeGenerator arrayTypeGenerator = new ArrayTypeGenerator(additionalPropSchema, null, null, subTypesMap, pregeneratedTypeMap);
            TypeDescriptorNode arrayNode = arrayTypeGenerator.generateTypeDescriptorNode();
            recordRestDescNode = NodeFactory.createRecordRestDescriptorNode(arrayNode, createToken(ELLIPSIS_TOKEN),
                    createToken(SEMICOLON_TOKEN));
        } else if (GeneratorUtils.isIntegerSchema(additionalPropSchema) ||
                GeneratorUtils.isStringSchema(additionalPropSchema) ||
                GeneratorUtils.isBooleanSchema(additionalPropSchema)) {
            PrimitiveTypeGenerator primitiveTypeGenerator = new PrimitiveTypeGenerator(additionalPropSchema, null, subTypesMap, pregeneratedTypeMap);
            TypeDescriptorNode primitiveNode = primitiveTypeGenerator.generateTypeDescriptorNode();
            recordRestDescNode = NodeFactory.createRecordRestDescriptorNode(primitiveNode, createToken(ELLIPSIS_TOKEN),
                    createToken(SEMICOLON_TOKEN));
        } else {
            OUT_STREAM.printf("WARNING: the Ballerina rest record field does not support with the data type `%s`",
                    GeneratorUtils.getOpenAPIType(additionalPropSchema));
        }
        return recordRestDescNode;
    }

    /**
     * This util for generating record field with given schema properties.
     */
    List<Node> addRecordFields(List<String> required, Set<Map.Entry<String, Schema<?>>> fields,
                                      String recordName) throws BallerinaOpenApiException {
        // TODO: Handle allOf , oneOf, anyOf
        List<Node> recordFieldList = new ArrayList<>();
        for (Map.Entry<String, Schema<?>> field : fields) {
            String fieldNameStr = GeneratorUtils.escapeIdentifier(field.getKey().trim());
            // API doc generations
            Schema<?> fieldSchema = field.getValue();
            List<Node> schemaDoc = TypeGeneratorUtils.getFieldApiDocs(fieldSchema);
            NodeList<Node> schemaDocNodes = createNodeList(schemaDoc);

            IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(fieldNameStr);
            TypeGenerator typeGenerator = TypeGeneratorUtils.getTypeGenerator(fieldSchema, fieldNameStr, recordName, subTypesMap, pregeneratedTypeMap);
            TypeDescriptorNode fieldTypeName = typeGenerator.generateTypeDescriptorNode();
            if (typeGenerator instanceof RecordTypeGenerator) {
                fieldTypeName = TypeGeneratorUtils.getNullableType(fieldSchema, fieldTypeName);
            }
            imports.addAll(typeGenerator.getImports());
            ImmutablePair<List<Node>, Set<String>> fieldListWithImports =
                    TypeGeneratorUtils.updateRecordFieldListWithImports(required, recordFieldList, field, fieldSchema,
                            schemaDocNodes, fieldName, fieldTypeName);
            recordFieldList = fieldListWithImports.getLeft();
            imports.addAll(fieldListWithImports.getRight());
        }
        return recordFieldList;
    }
}
