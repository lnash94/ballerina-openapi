// AUTO-GENERATED FILE. DO NOT MODIFY.
// This file is auto-generated by the Ballerina OpenAPI tool.

import ballerina/http;

type OASServiceType service object {
    *http:Service;
    resource function get pets(int? 'limit) returns Pets|http:Response;
    resource function post pets() returns http:Created|http:Response;
    resource function get pets/[string petId]() returns Dog|http:Response;
};
