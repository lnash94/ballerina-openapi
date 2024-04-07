public type Pet02_body string|int|decimal;

public type Pet_body_1 Dog|Bird;

public type Bird record {
    string name?;
    boolean isFly?;
};

public type Cat record {
    string name?;
    string kind?;
};

public type Pet_body Dog|Cat;

public type Dog record {
    string name?;
    string age?;
};
