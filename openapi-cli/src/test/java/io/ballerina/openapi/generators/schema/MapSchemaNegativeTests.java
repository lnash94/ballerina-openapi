/*
 * Copyright (c) 2022, WSO2 LLC. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.openapi.generators.schema;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.corenew.typegenerator.BallerinaTypesGenerator;
import io.ballerina.openapi.generators.common.TestUtils;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.junit.After;
import org.junit.Before;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is for containing the negative tests related to the {@code MapSchema}.
 */
public class MapSchemaNegativeTests {

    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();
    private ByteArrayOutputStream output = new ByteArrayOutputStream();


    @Before
    public void setUp() {
        System.setErr(new PrintStream(output));
    }

    @Test(expectedExceptions = BallerinaOpenApiException.class,
    expectedExceptionsMessageRegExp = "OpenAPI definition has errors: \n" +
            "attribute components.schemas.User02.additionalProperties.*")
    public void testForAdditionalPropertiesWithParserIssue() throws IOException, BallerinaOpenApiException, io.ballerina.openapi.corenew.typegenerator.exception.BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/additional_properties_true_negative.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI, true);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
    }

    @Test
    public void testForAdditionalPropertiesWithoutParserIssue()
            throws IOException, BallerinaOpenApiException, io.ballerina.openapi.corenew.typegenerator.exception.BallerinaOpenApiException, FormatterException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve("swagger" +
                "/additional_properties_true_negative_without_parser_issue.yaml"), true);
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI, true);
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateTypeSyntaxTree();
        // Check the generated content, till the warning test enable.
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree(
                "schema/ballerina/additional_properties_negative.bal", syntaxTree);

        String expectedOut = "WARNING: constraints in the OpenAPI contract will be ignored for the " +
                "additionalProperties field, as constraints are not supported on Ballerina rest record field.\n" +
                "WARNING: generating Ballerina rest record field will be ignored for the OpenAPI contract " +
                "additionalProperties type `ComposedSchema`, as it is not supported on Ballerina rest record field.";
//        Assert.assertTrue(output.toString().contains(expectedOut));
    }

    @After
    public void tearDown() {
        output = new ByteArrayOutputStream();
        System.setErr(new PrintStream(output));
    }
}
