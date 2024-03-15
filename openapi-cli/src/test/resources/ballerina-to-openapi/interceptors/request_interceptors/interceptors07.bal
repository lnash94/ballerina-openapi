import ballerina/http;

type Course record {|
    string name;
    int duration;
    string lecturer;
|};

enum Path {
    FOO = "foo",
    BAR = "bar"
}

service class RequestInterceptor {
    *http:RequestInterceptor;

    resource function post foo/[Path path](http:RequestContext ctx) returns Course|http:NextService? {
        return checkpanic ctx.next();
    }
}

type Person record {|
    string name;
    int age;
    string address;
|};

service http:InterceptableService /payloadV on new http:Listener(9090) {

    public function createInterceptors() returns [RequestInterceptor] {
        return [];
    }

    resource function get hello() returns string {
        return "Hello, World!";
    }

    resource function post foo/bar() returns Person {
        Person person = {name: "John", age: 30, address: "Colombo"};
        return person;
    }

    resource function get foo/bar() returns string {
        return "Hello, World!";
    }

    resource function post foo/[string path]() returns string {
        return "Hello, World!";
    }
}
