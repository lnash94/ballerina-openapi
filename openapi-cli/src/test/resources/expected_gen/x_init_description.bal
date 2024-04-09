import ballerina/http;

# With the Movie Reviews API, you can search New York Times movie reviews by keyword and get lists of NYT Critics' Picks. This is a feature given by new york times.
# Please visit [NYTimes](https://developer.nytimes.com/accounts/login) for more details
public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    # Client initialization required API credentials and service URL.
    # The service URL may set to the default value. You can override if required.
    # Create [NYTimes](https://developer.nytimes.com/accounts/login) Developer Account.
    # Log into NYTimes Developer Portal by visiting https://developer.nytimes.com/accounts/login.
    # Register an app and obtain the API Key following the process summarized [here](https://developer.nytimes.com/get-started).
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config =  {}, string serviceUrl = "http://api.nytimes.com/svc/movies/v2") returns error? {
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
    # Get movie reviews that are critics' picks. You can either specify the reviewer name or use "all", "full-time", or "part-time".
    #
    # + return - An array of Movie Critics
    remote isolated function criticsPicks() returns Inline_response_200|error {
        string resourcePath = string `/`;
        return self.clientEp->get(resourcePath);
    }
}
