import ballerina/constraint;

@constraint:String {minLength: 7}
public type PersonDetailsItemsString string;

@constraint:Float {maxValue: 445.4}
public type PersonFeeItemsNumber float;

@constraint:Int {maxValue: 67}
public type PersonLimitItemsInteger int;

@constraint:Array {maxLength: 5, minLength: 2}
public type Hobby HobbyItemsString[];

@constraint:String {maxLength: 23}
public type HobbyItemsString string;

public type Person record {
    Hobby hobby?;
    @constraint:Array {maxLength: 5}
    PersonDetailsItemsString[] Details?;
    int id;
    PersonFeeItemsNumber[] fee?;
    PersonLimitItemsInteger[] 'limit?;
};
