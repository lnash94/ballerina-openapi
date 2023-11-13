import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {

    resource function get ping05(float[] offset = [2.4, 1.2, 3.3, 4.3]) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
    resource function get ping06(int? offset = ()) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
    resource function get ping07(map<json>? offset = {"x": {"id": "sss"}}) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }

    # Mock resource function
    #
    # + offset - Mock query parameter
    # + return - Return Value Description
    resource function get ping08(map<json> offset = {"x": {"id": "sss"}}) returns http:Ok {
         http:Ok ok = {body: ()};
         return ok;
    }

    resource function get ping09(int[] offset = []) returns http:Ok {
         http:Ok ok = {body: ()};
         return ok;
    }
}
