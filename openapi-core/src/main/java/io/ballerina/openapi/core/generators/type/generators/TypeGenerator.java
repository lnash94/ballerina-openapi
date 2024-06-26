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

package io.ballerina.openapi.core.generators.type.generators;

import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;

/**
 * Abstract class for schema types.
 *
 * @since 1.3.0
 */
public abstract class TypeGenerator {

    Schema schema;
    String typeName;
    final boolean ignoreNullableFlag;
    final LinkedHashSet<String> imports = new LinkedHashSet<>();
    final HashMap<String, TypeDefinitionNode> subTypesMap;
    final HashMap<String, NameReferenceNode> pregeneratedTypeMap;
    final List<Diagnostic> diagnostics = new ArrayList<>();

    public TypeGenerator(Schema schema, String typeName, boolean ignoreNullableFlag,
                         HashMap<String, TypeDefinitionNode> subTypesMap,
                         HashMap<String, NameReferenceNode> pregeneratedTypeMap) {
        this.schema = schema;
        this.typeName = typeName;
        this.ignoreNullableFlag = ignoreNullableFlag;
        this.subTypesMap = subTypesMap;
        this.pregeneratedTypeMap = pregeneratedTypeMap;
    }

    public LinkedHashSet<String> getImports() {
        return imports;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    /**
     * Create Type Definition Node for a given OpenAPI schema.
     *
     * @param typeName        IdentifierToken of the name of the type
     * @return {@link TypeDefinitionNode}
     * @throws OASTypeGenException when unsupported schema type is found
     */
    public TypeDefinitionNode generateTypeDefinitionNode(IdentifierToken typeName)
            throws OASTypeGenException {
        return createTypeDefinitionNode(null, createToken(PUBLIC_KEYWORD), createToken(TYPE_KEYWORD),
                typeName, generateTypeDescriptorNode(), createToken(SEMICOLON_TOKEN));
    }

    /**
     * Create Type Descriptor Node for a given OpenAPI schema.
     *
     * @return {@link TypeDescriptorNode}
     * @throws OASTypeGenException when unsupported schema type is found
     */
    public abstract TypeDescriptorNode generateTypeDescriptorNode() throws OASTypeGenException;
}
