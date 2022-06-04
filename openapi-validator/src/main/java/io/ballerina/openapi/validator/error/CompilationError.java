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
package io.ballerina.openapi.validator.error;

/**
 * This {@code DiagnosticMessages} enum class for containing the error message related to openapi validator plugin.
 *
 * @since 1.1.0
 */
public enum CompilationError {
    INVALID_CONTRACT_PATH(ErrorCode.OPENAPI_VALIDATOR_001, ErrorMessage.ERROR_001),
    INVALID_CONTRACT_FORMAT(ErrorCode.OPENAPI_VALIDATOR_002, ErrorMessage.ERROR_002),
    EMPTY_CONTRACT_PATH(ErrorCode.OPENAPI_VALIDATOR_003, ErrorMessage.ERROR_003), //DiagnosticSeverity.WARNING 
    NON_HTTP_SERVICE(ErrorCode.OPENAPI_VALIDATOR_004, ErrorMessage.ERROR_004), //DiagnosticSeverity.WARNING 
    TYPE_MISMATCH_FIELD(ErrorCode.OPENAPI_VALIDATOR_005, ErrorMessage.ERROR_005),
    TYPE_MISMATCH_PARAMETER(ErrorCode.OPENAPI_VALIDATOR_006, ErrorMessage.ERROR_006),
    UNDEFINED_BRECORD_FIELD(ErrorCode.OPENAPI_VALIDATOR_007, ErrorMessage.ERROR_007),
    MISSING_OAS_PROPERTY(ErrorCode.OPENAPI_VALIDATOR_008, ErrorMessage.ERROR_008),
    UNDEFINED_PARAMETER(ErrorCode.OPENAPI_VALIDATOR_009, ErrorMessage.ERROR_009),
    MISSING_PARAMETER(ErrorCode.OPENAPI_VALIDATOR_010, ErrorMessage.ERROR_010),
    UNEXPECTED_EXCEPTIONS(ErrorCode.OPENAPI_VALIDATOR_011, ErrorMessage.ERROR_011),
    PARSER_EXCEPTION(ErrorCode.OPENAPI_VALIDATOR_012, ErrorMessage.ERROR_012),
    BOTH_TAGS_AND_EXCLUDE_TAGS_ENABLES(ErrorCode.OPENAPI_VALIDATOR_013, ErrorMessage.ERROR_013),
    BOTH_OPERATIONS_AND_EXCLUDE_OPERATIONS_ENABLES(ErrorCode.OPENAPI_VALIDATOR_014, ErrorMessage.ERROR_014),
    MISSING_RESOURCE_FUNCTION(ErrorCode.OPENAPI_VALIDATOR_015, ErrorMessage.ERROR_015),
    UNDEFINED_RESOURCE_FUNCTIONS(ErrorCode.OPENAPI_VALIDATOR_016, ErrorMessage.ERROR_016),
    MISSING_RESOURCE_PATH(ErrorCode.OPENAPI_VALIDATOR_017, ErrorMessage.ERROR_017),
    UNDEFINED_RESOURCE_PATH(ErrorCode.OPENAPI_VALIDATOR_018, ErrorMessage.ERROR_018),
    TYPE_MISMATCH_HEADER_PARAMETER(ErrorCode.OPENAPI_VALIDATOR_019, ErrorMessage.ERROR_019),
    UNDEFINED_HEADER(ErrorCode.OPENAPI_VALIDATOR_020, ErrorMessage.ERROR_020),
    UNDEFINED_REQUEST_BODY(ErrorCode.OPENAPI_VALIDATOR_021, ErrorMessage.ERROR_021),
    TYPEMISMATCH_REQUEST_BODY_PAYLOAD(ErrorCode.OPENAPI_VALIDATOR_022, ErrorMessage.ERROR_022),
    UNDEFINED_RETURN_CODE(ErrorCode.OPENAPI_VALIDATOR_023, ErrorMessage.ERROR_023),
    UNDEFINED_RETURN_MEDIA_TYPE(ErrorCode.OPENAPI_VALIDATOR_024, ErrorMessage.ERROR_024),
    MISSING_HEADER(ErrorCode.OPENAPI_VALIDATOR_025, ErrorMessage.ERROR_025),
    MISSING_REQUEST_MEDIA_TYPE(ErrorCode.OPENAPI_VALIDATOR_026, ErrorMessage.ERROR_026),
    MISSING_REQUEST_BODY(ErrorCode.OPENAPI_VALIDATOR_027, ErrorMessage.ERROR_027),
    MISSING_STATUS_CODE(ErrorCode.OPENAPI_VALIDATOR_028, ErrorMessage.ERROR_028),
    MISSING_RESPONSE_MEDIA_TYPE(ErrorCode.OPENAPI_VALIDATOR_029, ErrorMessage.ERROR_029),
    FOUR_ANNOTATION_FIELDS(ErrorCode.OPENAPI_VALIDATOR_030, ErrorMessage.ERROR_030),
    UNDEFINED_REQUEST_MEDIA_TYPE(ErrorCode.OPENAPI_VALIDATOR_031, ErrorMessage.ERROR_031);

    private final String code;
    private final String description;

    CompilationError(ErrorCode code, ErrorMessage description) {
        this.code = code.name();
        this.description = description.getMessage();
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

}

