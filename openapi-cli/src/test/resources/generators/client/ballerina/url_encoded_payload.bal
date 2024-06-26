import ballerina/http;

# The Stripe REST API. Please see https://stripe.com/docs/api for more details.
public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(string serviceUrl, ConnectionConfig config =  {}) returns error? {
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

    # <p>Retrieves a PaymentMethod object.</p>
    #
    # + payment_method - Payment Method
    # + headers - Headers to be sent with the request
    # + return - Successful response.
    remote isolated function getPaymentMethodsPaymentMethod(string payment_method, map<string|string[]> headers = {}) returns json|error {
        string resourcePath = string `/v1/payment_methods/${getEncodedUri(payment_method)}`;
        return self.clientEp->get(resourcePath, headers);
    }

    # <p>Creates a new customer object.</p>
    #
    # + customer - Customer ID
    # + headers - Headers to be sent with the request
    # + payload - Customer Details
    # + return - Successful response.
    remote isolated function postCustomers(string customer, customer_customer_body payload, map<string|string[]> headers = {}) returns Customer|error {
        string resourcePath = string `/v1/customer/${getEncodedUri(customer)}`;
        http:Request request = new;
        string encodedRequestBody = createFormURLEncodedRequestBody(payload);
        request.setPayload(encodedRequestBody, "application/x-www-form-urlencoded");
        return self.clientEp->post(resourcePath, request, headers);
    }
}
