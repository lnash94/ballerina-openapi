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


import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.corenew.typegenerator.BallerinaTypesGenerator;
import io.ballerina.openapi.corenew.typegenerator.GeneratorUtils;
import io.ballerina.openapi.corenew.typegenerator.exception.BallerinaOpenApiException;
import io.ballerina.openapi.corenew.typegenerator.model.GeneratorMetaData;
import io.ballerina.openapi.corenew.typegenerator.GeneratorConstants;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.openapi.corenew.typegenerator.GeneratorUtils.getValidName;

/**
 * Abstract class for schema types.
 *
 * @since 1.3.0
 */
public abstract class TypeGenerator {

    Schema schema;
    String typeName;
    final LinkedHashSet<String> imports = new LinkedHashSet<>();
    final HashMap<String, TypeDefinitionNode> subTypesMap;
    final HashMap<String, NameReferenceNode> pregeneratedTypeMap;

    public TypeGenerator(Schema schema, String typeName, HashMap<String, TypeDefinitionNode> subTypesMap, HashMap<String, NameReferenceNode> pregeneratedTypeMap) {
        this.schema = schema;
        this.typeName = typeName;
        this.subTypesMap = subTypesMap;
        this.pregeneratedTypeMap = pregeneratedTypeMap;
    }

    public LinkedHashSet<String> getImports() {
        return imports;
    }

    /**
     * Create Type Definition Node for a given OpenAPI schema.
     *
     * @param typeName        IdentifierToken of the name of the type
     * @param typeAnnotations Annotations related to the type. Currently, only includes `Deprecated` annotation
     * @return {@link TypeDefinitionNode}
     * @throws BallerinaOpenApiException when unsupported schema type is found
     */
    public TypeDefinitionNode generateTypeDefinitionNode(IdentifierToken typeName, List<AnnotationNode> typeAnnotations)
            throws BallerinaOpenApiException {
        //Check the annotation for constraint support
        boolean nullable = GeneratorMetaData.getInstance().isNullable();
        for (AnnotationNode annotation : typeAnnotations) {
            String annotationRef = annotation.annotReference().toString();
            if (annotationRef.startsWith(GeneratorConstants.CONSTRAINT) && !nullable) {
                ImportDeclarationNode constraintImport = GeneratorUtils.getImportDeclarationNode(GeneratorConstants.BALLERINA, GeneratorConstants.CONSTRAINT);
                //Here we are unable to add ImportDeclarationNode since newly generated node has different hashcode.
                imports.add(constraintImport.toSourceCode());
            }
        }

//        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(schemaDoc));
//        MetadataNode metadataNode = createMetadataNode(documentationNode, createNodeList(typeAnnotations));
        return createTypeDefinitionNode(null, createToken(PUBLIC_KEYWORD), createToken(TYPE_KEYWORD),
                typeName, generateTypeDescriptorNode(), createToken(SEMICOLON_TOKEN));
    }

    /**
     * Create Type Descriptor Node for a given OpenAPI schema.
     *
     * @return {@link TypeDescriptorNode}
     * @throws BallerinaOpenApiException when unsupported schema type is found
     */
    public abstract TypeDescriptorNode generateTypeDescriptorNode() throws BallerinaOpenApiException;
}
