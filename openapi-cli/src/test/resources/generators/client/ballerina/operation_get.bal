import  ballerina/http;


public isolated client class Client {
    public final http:Client clientEp;

    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(http:ClientConfiguration  clientConfig =  {}, string serviceUrl = "http://localhost:9090/petstore/v1") returns error? {
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
    remote isolated function  pet() returns http:Response | error {
        string resourcePath = string `/pet`;
        return self.clientEp->get(resourcePath);
    }
    remote isolated function getPetId(string petId) returns http:Response | error {
        string resourcePath = string `/pets/${getEncodedUri(petId)}`;
        return self.clientEp->get(resourcePath);
    }
    remote isolated function  ImageByimageId(int petId, string imageId) returns http:Response | error {
        string resourcePath = string `/pets/${getEncodedUri(petId)}/Image/${getEncodedUri(imageId)}`;
        return self.clientEp->get(resourcePath);
    }
    remote isolated function  pets(int offset) returns http:Response | error {
        string resourcePath = string `/pets`;
        map<anydata> queryParam = {offset: offset};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp->get(resourcePath);
    }
    remote isolated function  users(string[]? offset) returns http:Response | error {
        string resourcePath = string `/users`;
        map<anydata> queryParam = {offset: offset};
        resourcePath = resourcePath + getPathForQueryParam(queryParam);
        return self.clientEp->get(resourcePath);
    }
    remote isolated function getImage(string? tag, int? 'limit) returns http:Response | error {
        string resourcePath = string `/image`;
        map<anydata> queryParam = {tag: tag, 'limit: 'limit};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp->get(resourcePath);
    }
    remote isolated function  header(string XClient) returns http:Response | error {
        string resourcePath = string `/header`;
        map<string|string[]> httpHeaders = {XClient: XClient};
        return self.clientEp->get(resourcePath, httpHeaders);
    }
}

isolated function  getPathForQueryParam(map<anydata>   queryParam)  returns  string|error {
    string[] param = [];
    param[param.length()] = "?";
    foreach  var [key, value] in  queryParam.entries() {
        if  value  is  () {
            _ = queryParam.remove(key);
        } else {
            if  string:startsWith( key, "'") {
                 param[param.length()] = string:substring(key, 1, key.length());
            } else {
                param[param.length()] = key;
            }
            param[param.length()] = "=";
            if  value  is  string {
                string updateV =  check url:encode(value, "UTF-8");
                param[param.length()] = updateV;
            } else {
                param[param.length()] = value.toString();
            }
            param[param.length()] = "&";
        }
    }
    _ = param.remove(param.length()-1);
    if  param.length() ==  1 {
        _ = param.remove(0);
    }
    string restOfPath = string:'join("", ...param);
    return restOfPath;
}
