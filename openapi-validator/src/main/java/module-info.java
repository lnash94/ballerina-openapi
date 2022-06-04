/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

module io.ballerina.openapi.validator {
    requires io.ballerina.lang;
    requires io.ballerina.parser;
    requires io.ballerina.tools.api;
    requires io.swagger.v3.oas.models;
    requires swagger.parser.v3;
    requires swagger.parser.core;
    exports io.ballerina.openapi.validator;
    exports io.ballerina.openapi.validator.error;
    exports io.ballerina.openapi.validator.model;
}

