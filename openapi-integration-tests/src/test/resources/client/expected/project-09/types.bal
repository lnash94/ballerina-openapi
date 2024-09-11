// AUTO-GENERATED FILE. DO NOT MODIFY.
// This file is auto-generated by the Ballerina OpenAPI tool.

import ballerina/constraint;
import ballerina/http;

# Content filtering results for a single prompt in the request.
public type promptFilterResult record {
    # The index of the prompt in the set of prompt results.
    int prompt_index?;
    contentFilterResults content_filter_results?;
};

# The name and arguments of a function that should be called, as generated by the model.
public type chatCompletionRequestMessage_function_call record {
    # The name of the function to call.
    string name?;
    # The arguments to call the function with, as generated by the model in JSON format. Note that the model does not always generate valid JSON, and may hallucinate parameters not defined by your function schema. Validate the arguments in your code before calling your function.
    string arguments?;
};

# Content filtering results for zero or more prompts in the request. In a streaming request, results for different prompts may arrive at different times or in different orders.
public type promptFilterResults promptFilterResult[];

# The parameters the functions accepts, described as a JSON Schema object. See the [guide](/docs/guides/gpt/function-calling) for examples, and the [JSON Schema reference](https://json-schema.org/understanding-json-schema/) for documentation about the format.
public type chatCompletionFunctionParameters record {
};

public type chatCompletionRequestMessage record {
    # The role of the messages author. One of `system`, `user`, `assistant`, or `function`.
    "system"|"user"|"assistant"|"function" role;
    # The contents of the message. `content` is required for all messages except assistant messages with function calls.
    string? content;
    # The name of the author of this message. `name` is required if role is `function`, and it should be the name of the function whose response is in the `content`. May contain a-z, A-Z, 0-9, and underscores, with a maximum length of 64 characters.
    string name?;
    chatCompletionRequestMessage_function_call function_call?;
};

public type chatCompletionsResponseCommon_usage record {
    int prompt_tokens;
    int completion_tokens;
    int total_tokens;
};

public type createChatCompletionRequest record {
    *chatCompletionsRequestCommon;
    # A list of messages comprising the conversation so far. [Example Python code](https://github.com/openai/openai-cookbook/blob/main/examples/How_to_format_inputs_to_ChatGPT_models.ipynb).
    chatCompletionRequestMessage[] messages;
    # A list of functions the model may generate JSON inputs for.
    chatCompletionFunctions[] functions?;
    # Controls how the model responds to function calls. "none" means the model does not call a function, and responds to the end-user. "auto" means the model can pick between an end-user or calling a function.  Specifying a particular function via `{"name":\ "my_function"}` forces the model to call that function. "none" is the default when no functions are present. "auto" is the default if functions are present.
    "none"|"auto"|record {string name;} function_call?;
    # How many chat completion choices to generate for each input message.
    int? n = 1;
};

public type chatCompletionsRequestCommon record {
    # What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic.
    # We generally recommend altering this or `top_p` but not both.
    decimal? temperature = 1;
    # An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top 10% probability mass are considered.
    # We generally recommend altering this or `temperature` but not both.
    decimal? top_p = 1;
    # If set, partial message deltas will be sent, like in ChatGPT. Tokens will be sent as data-only server-sent events as they become available, with the stream terminated by a `data: [DONE]` message.
    boolean? 'stream = false;
    # Up to 4 sequences where the API will stop generating further tokens.
    string|string[]? stop = ();
    # The maximum number of tokens allowed for the generated answer. By default, the number of tokens the model can return will be (4096 - prompt tokens).
    int max_tokens = 4096;
    # Number between -2.0 and 2.0. Positive values penalize new tokens based on whether they appear in the text so far, increasing the model's likelihood to talk about new topics.
    @constraint:Number {minValue: -2, maxValue: 2}
    decimal presence_penalty = 0;
    # Number between -2.0 and 2.0. Positive values penalize new tokens based on their existing frequency in the text so far, decreasing the model's likelihood to repeat the same line verbatim.
    @constraint:Number {minValue: -2, maxValue: 2}
    decimal frequency_penalty = 0;
    # Modify the likelihood of specified tokens appearing in the completion. Accepts a json object that maps tokens (specified by their token ID in the tokenizer) to an associated bias value from -100 to 100. Mathematically, the bias is added to the logits generated by the model prior to sampling. The exact effect will vary per model, but values between -1 and 1 should decrease or increase likelihood of selection; values like -100 or 100 should result in a ban or exclusive selection of the relevant token.
    record {}? logit_bias?;
    # A unique identifier representing your end-user, which can help Azure OpenAI to monitor and detect abuse.
    string user?;
};

public type chatCompletionChoiceCommon record {
    int index?;
    string finish_reason?;
};

# Provides a set of configurations for controlling the behaviours when communicating with a remote HTTP endpoint.
@display {label: "Connection Config"}
public type ConnectionConfig record {|
    # Provides Auth configurations needed when communicating with a remote HTTP endpoint.
    http:BearerTokenConfig|ApiKeysConfig auth;
    # The HTTP version understood by the client
    http:HttpVersion httpVersion = http:HTTP_2_0;
    # Configurations related to HTTP/1.x protocol
    ClientHttp1Settings http1Settings?;
    # Configurations related to HTTP/2 protocol
    http:ClientHttp2Settings http2Settings?;
    # The maximum time to wait (in seconds) for a response before closing the connection
    decimal timeout = 60;
    # The choice of setting `forwarded`/`x-forwarded` header
    string forwarded = "disable";
    # Configurations associated with request pooling
    http:PoolConfiguration poolConfig?;
    # HTTP caching related configurations
    http:CacheConfig cache?;
    # Specifies the way of handling compression (`accept-encoding`) header
    http:Compression compression = http:COMPRESSION_AUTO;
    # Configurations associated with the behaviour of the Circuit Breaker
    http:CircuitBreakerConfig circuitBreaker?;
    # Configurations associated with retrying
    http:RetryConfig retryConfig?;
    # Configurations associated with inbound response size limits
    http:ResponseLimitConfigs responseLimits?;
    # SSL/TLS-related options
    http:ClientSecureSocket secureSocket?;
    # Proxy server related options
    http:ProxyConfig proxy?;
    # Enables the inbound payload validation functionality which provided by the constraint package. Enabled by default
    boolean validation = true;
|};

# Request for the chat completions using extensions
public type extensionsChatCompletionsRequest record {
    *chatCompletionsRequestCommon;
    # A list of messages comprising the conversation so far. [Example Python code](https://github.com/openai/openai-cookbook/blob/main/examples/How_to_format_inputs_to_ChatGPT_models.ipynb).
    message[] messages;
    # The data sources to be used for the Azure OpenAI on your data feature.
    dataSource[] dataSources?;
};

# Represents the Queries record for the operation: ExtensionsChatCompletions_Create
public type ExtensionsChatCompletionsCreateQueries record {
    string api\-version;
};

# Error information returned by the service.
public type errorBase record {
    # The error code.
    string code?;
    # The error message.
    string message?;
};

# The response of the extensions chat completions.
public type extensionsChatCompletionsResponse record {
    *chatCompletionsResponseCommon;
    # A list of chat completion choices.
    extensionsChatCompletionChoice[] choices?;
};

# The conversation context
public type message_context record {
    # Messages exchanged between model and extensions prior to final message from model
    message[]? messages?;
};

# Information about the content filtering category including the severity level (very_low, low, medium, high-scale that determines the intensity and risk level of harmful content) and if it has been filtered or not.
public type contentFilterResult record {
    # The severity level of the content filter result.
    "safe"|"low"|"medium"|"high" severity;
    # Whether the content filter result has been filtered or not.
    boolean filtered;
};

# Information about the content filtering category (hate, sexual, violence, self_harm), if it has been detected, as well as the severity level (very_low, low, medium, high-scale that determines the intensity and risk level of harmful content) and if it has been filtered or not.
public type contentFilterResults record {
    contentFilterResult sexual?;
    contentFilterResult violence?;
    contentFilterResult hate?;
    contentFilterResult self_harm?;
    errorBase 'error?;
};

public type createChatCompletionResponse record {
    *chatCompletionsResponseCommon;
    promptFilterResults prompt_filter_results?;
    record {*chatCompletionChoiceCommon; chatCompletionResponseMessage message?; contentFilterResults content_filter_results?;}[] choices;
};

# Proxy server configurations to be used with the HTTP client endpoint.
public type ProxyConfig record {|
    # Host name of the proxy server
    string host = "";
    # Proxy server port
    int port = 0;
    # Proxy server username
    string userName = "";
    # Proxy server password
    @display {label: "", kind: "password"}
    string password = "";
|};

# A chat message.
public type message record {
    # The index of the message in the conversation.
    int index?;
    # The role of the author of this message.
    "system"|"user"|"assistant"|"tool" role;
    # The recipient of the message in the format of <namespace>.<operation>. Present if and only if the recipient is tool.
    string recipient?;
    # The contents of the message
    string content;
    # Whether the message ends the turn.
    boolean end_turn?;
    message_context? context?;
};

public type chatCompletionResponseMessage record {
    # The role of the author of this message.
    "system"|"user"|"assistant"|"function" role;
    # The contents of the message.
    string content?;
    chatCompletionRequestMessage_function_call function_call?;
};

# Represents the Queries record for the operation: ChatCompletions_Create
public type ChatCompletionsCreateQueries record {
    string api\-version;
};

public type extensionsChatCompletionChoice record {
    *chatCompletionChoiceCommon;
    message message?;
};

public type chatCompletionsResponseCommon record {
    string id;
    string 'object;
    int created;
    string model;
    chatCompletionsResponseCommon_usage usage?;
};

# Provides settings related to HTTP/1.x protocol.
public type ClientHttp1Settings record {|
    # Specifies whether to reuse a connection for multiple requests
    http:KeepAlive keepAlive = http:KEEPALIVE_AUTO;
    # The chunking behaviour of the request
    http:Chunking chunking = http:CHUNKING_AUTO;
    # Proxy server related options
    ProxyConfig proxy?;
|};

public type chatCompletionFunctions record {
    # The name of the function to be called. Must be a-z, A-Z, 0-9, or contain underscores and dashes, with a maximum length of 64.
    string name;
    # The description of what the function does.
    string description?;
    chatCompletionFunctionParameters parameters?;
};

# The data source to be used for the Azure OpenAI on your data feature.
public type dataSource record {
    # The data source type.
    string 'type;
    # The parameters to be used for the data source in runtime.
    record {} parameters?;
};

# Provides API key configurations needed when communicating with a remote HTTP endpoint.
public type ApiKeysConfig record {|
    # The key used to access the OpenAI APIs
    @display {label: "", kind: "password"}
    string api\-key;
|};
