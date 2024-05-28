/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnosticImp;
import io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.document.ClientDocCommentGenerator;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnStatementNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_DEFINITION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ISOLATED_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURN_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;

/**
 * This class iss contain the advance client generation when the client generation enable with status code bindings.
 * @since 2.1.0
 */
public class AdvanceMockClientGenerator extends BallerinaClientGeneratorWithStatusCodeBinding {
    public AdvanceMockClientGenerator(OASClientConfig oasClientConfig) {
        super(oasClientConfig);
    }

    @Override
    public FunctionDefinitionNode createInitFunction() {
        FunctionSignatureNode functionSignatureNode = super.getInitFunctionSignatureNode();
        NodeList<Token> qualifierList = createNodeList(createToken(PUBLIC_KEYWORD), createToken(ISOLATED_KEYWORD));
        IdentifierToken functionName = createIdentifierToken("init");
        List<StatementNode> assignmentNodes = new ArrayList<>();
        ReturnStatementNode returnStatementNode = createReturnStatementNode(createToken(
                RETURN_KEYWORD), null, createToken(SEMICOLON_TOKEN));
        assignmentNodes.add(returnStatementNode);
        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);
        FunctionBodyBlockNode functionBodyNode = createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementList, createToken(CLOSE_BRACE_TOKEN), null);
        return createFunctionDefinitionNode(FUNCTION_DEFINITION, super.getInitDocComment(), qualifierList,
                createToken(FUNCTION_KEYWORD), functionName, createEmptyNodeList(), functionSignatureNode,
                functionBodyNode);
    }

    @Override
    protected List<ModuleMemberDeclarationNode> getModuleMemberDeclarationNodes() throws BallerinaOpenApiException {
        List<ModuleMemberDeclarationNode> nodes = new ArrayList<>();
        nodes.add(getSetModuleFunction());
        nodes.add(getModuleInitFunction());
        nodes.add(getClientMethodImplType());
        nodes.add(getMethodImplAnnotation());
        nodes.add(getClientErrorType());
        nodes.addAll(super.getModuleMemberDeclarationNodes());
        return nodes;
    }

    @Override
    protected void addClientFunctionImpl(Map.Entry<String, Map<PathItem.HttpMethod, Operation>> operation,
                                         Map.Entry<PathItem.HttpMethod, Operation> operationEntry,
                                         List<FunctionDefinitionNode> clientFunctionNodes) {
        FunctionDefinitionNode clientExternFunction = clientFunctionNodes.get(clientFunctionNodes.size() - 1);
        Optional<FunctionDefinitionNode> implFunction = createImplFunction(operation.getKey(), operationEntry, openAPI,
                authConfigGeneratorImp, ballerinaUtilGenerator, clientExternFunction, oasClientConfig.isMock());
        if (implFunction.isPresent()) {
            clientFunctionNodes.add(implFunction.get());
        } else {
            diagnostics.add(new ClientDiagnosticImp(DiagnosticMessages.OAS_CLIENT_112,
                    operationEntry.getValue().getOperationId()));
            clientFunctionNodes.remove(clientFunctionNodes.size() - 1);
        }
    }

    /**
     * This method for generate the client syntax tree.
     *
     * @return return Syntax tree for the ballerina code.
     * @throws BallerinaOpenApiException When function fail in process.
     */
    @Override
    public SyntaxTree generateSyntaxTree() throws BallerinaOpenApiException, ClientException {

        // Create `ballerina/http` import declaration node
        List<ImportDeclarationNode> importForHttp = getImportDeclarationNodes();
        imports.addAll(importForHttp);

        // Add authentication related records
        authConfigGeneratorImp.addAuthRelatedRecords(openAPI);

        List<ModuleMemberDeclarationNode> nodes = getModuleMemberDeclarationNodes();
        NodeList<ImportDeclarationNode> importsList = createNodeList(imports);
        ModulePartNode modulePartNode =
                createModulePartNode(importsList, createNodeList(nodes), createToken(EOF_TOKEN));
        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        syntaxTree = syntaxTree.modifyWith(modulePartNode);
        //Add comments
        ClientDocCommentGenerator clientDocCommentGenerator = new ClientDocCommentGenerator(syntaxTree, openAPI,
                oasClientConfig.isResourceMode());
        return clientDocCommentGenerator.updateSyntaxTreeWithDocComments();
    }
}
