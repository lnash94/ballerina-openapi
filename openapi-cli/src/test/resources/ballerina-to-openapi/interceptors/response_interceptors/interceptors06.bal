import ballerina/http;
import ballerina/http.httpscerr;
import ballerina/time;

type ErrorPayloadNew record {|
    time:Civil timestamp;
    string message = "something went wrong";
    int code?;
|};

type InternalServerError record {|
    *http:InternalServerError;
    ErrorPayloadNew body;
|};

service class ResponseErrorInterceptor {
    *http:ResponseErrorInterceptor;

    remote function interceptResponseError(error err, http:RequestContext ctx) returns InternalServerError {
        return {
            body: {
                timestamp: {
                    year: 2021,
                    month: 1,
                    day: 1,
                    hour: 10,
                    minute: 10
                }
            }
        };
    }
}

type Person record {|
    string name;
    int age;
    string address;
|};

service http:InterceptableService /payloadV on new http:Listener(9090) {

    public function createInterceptors() returns ResponseErrorInterceptor {
        return new;
    }

    resource function get persons/[int id]() returns Person {
        return {name: "John", age: 30, address: "Colombo"};
    }

    resource function get persons() returns Person[] {
        return [{name: "John", age: 30, address: "Colombo"}, {name: "Doe", age: 40, address: "Kandy"}];
    }

    resource function post person(Person p) returns Person|error {
        return p;
    }

    resource function post persons(Person[] p) returns Person[]|http:BadRequest|httpscerr:DefaultStatusCodeError {
        return p;
    }
}
