// AUTO-GENERATED FILE. DO NOT MODIFY.
// This file is auto-generated by the Ballerina OpenAPI tool.

import ballerina/http;
import ballerina/jballerina.java;

function setModule() = @java:Method {'class: "io.ballerina.openapi.client.ModuleUtils"} external;

function init() {
    setModule();
}

type ClientMethodImpl record {|
    string name;
|};

annotation ClientMethodImpl MethodImpl on function;

type ClientMethodInvocationError http:ClientError;

public isolated client class Client {
    final http:StatusCodeClient clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "http://localhost:9090/api") returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, timeout: config.timeout, forwarded: config.forwarded, poolConfig: config.poolConfig, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, validation: config.validation};
        do {
            if config.http1Settings is ClientHttp1Settings {
                ClientHttp1Settings settings = check config.http1Settings.ensureType(ClientHttp1Settings);
                httpClientConfig.http1Settings = {...settings};
            }
            if config.http2Settings is http:ClientHttp2Settings {
                httpClientConfig.http2Settings = check config.http2Settings.ensureType(http:ClientHttp2Settings);
            }
            if config.cache is http:CacheConfig {
                httpClientConfig.cache = check config.cache.ensureType(http:CacheConfig);
            }
            if config.responseLimits is http:ResponseLimitConfigs {
                httpClientConfig.responseLimits = check config.responseLimits.ensureType(http:ResponseLimitConfigs);
            }
            if config.secureSocket is http:ClientSecureSocket {
                httpClientConfig.secureSocket = check config.secureSocket.ensureType(http:ClientSecureSocket);
            }
            if config.proxy is http:ProxyConfig {
                httpClientConfig.proxy = check config.proxy.ensureType(http:ProxyConfig);
            }
        }
        http:StatusCodeClient httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
        return;
    }

    # + headers - Headers to be sent with the request
    # + return - Ok
    @MethodImpl {name: "getAlbumsIdImpl"}
    resource isolated function get albums/[string id](map<string|string[]> headers = {}, typedesc<AlbumOk|ErrorMessageDefault> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResource"} external;

    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - Ok
    @MethodImpl {name: "getAlbumsImpl"}
    resource isolated function get albums(map<string|string[]> headers = {}, typedesc<AlbumArrayOk|ErrorMessageDefault> targetType = <>, *GetAlbumsQueries queries) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    # + headers - Headers to be sent with the request
    # + return - Created
    @MethodImpl {name: "postAlbumsImpl"}
    resource isolated function post albums(Album payload, map<string|string[]> headers = {}, typedesc<AlbumCreated|ErrorMessageDefault> targetType = <>) returns targetType|error = @java:Method {'class: "io.ballerina.openapi.client.GeneratedClient", name: "invokeResourceWithoutPath"} external;

    private isolated function getAlbumsIdImpl(string id, map<string|string[]> headers, typedesc<AlbumOk|ErrorMessageDefault> targetType) returns http:StatusCodeResponse|error {
        string resourcePath = string `/albums/${getEncodedUri(id)}`;
        var response = self.clientEp->get(resourcePath, headers, targetType = targetType);
        int[] nonDefaultStatusCodes = [200];
        return getValidatedResponseForDefaultMapping(response, nonDefaultStatusCodes);
    }

    private isolated function getAlbumsImpl(map<string|string[]> headers, typedesc<AlbumArrayOk|ErrorMessageDefault> targetType, *GetAlbumsQueries queries) returns http:StatusCodeResponse|error {
        string resourcePath = string `/albums`;
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        var response = self.clientEp->get(resourcePath, headers, targetType = targetType);
        int[] nonDefaultStatusCodes = [200];
        return getValidatedResponseForDefaultMapping(response, nonDefaultStatusCodes);
    }

    private isolated function postAlbumsImpl(Album payload, map<string|string[]> headers, typedesc<AlbumCreated|ErrorMessageDefault> targetType) returns http:StatusCodeResponse|error {
        string resourcePath = string `/albums`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        var response = self.clientEp->post(resourcePath, request, headers, targetType = targetType);
        int[] nonDefaultStatusCodes = [201];
        return getValidatedResponseForDefaultMapping(response, nonDefaultStatusCodes);
    }
}
