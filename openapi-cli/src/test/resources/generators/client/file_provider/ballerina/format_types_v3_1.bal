import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "https://app.launchdarkly.com/api/v2") returns error? {
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
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
        return;
    }
    # + headers - Headers to be sent with the request
    # + return - Feature flag approval request response
    remote isolated function op1(map<string|string[]> headers = {}) returns StringObject|error {
        string resourcePath = string `/projects`;
        return self.clientEp->get(resourcePath, headers);
    }
    # + headers - Headers to be sent with the request
    # + return - Feature flag approval request response
    remote isolated function op2(map<string|string[]> headers = {}) returns IntegerObject|error {
        string resourcePath = string `/projects`;
        http:Request request = new;
        return self.clientEp->post(resourcePath, request, headers);
    }
    # + headers - Headers to be sent with the request
    # + return - Feature flag approval request response
    remote isolated function op3(map<string|string[]> headers = {}) returns NumberObject|error {
        string resourcePath = string `/projects`;
        return self.clientEp->delete(resourcePath, headers = headers);
    }
}
