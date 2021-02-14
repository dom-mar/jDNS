/*
* Enum for types of queries */
public enum QueryType {
    UNKNOWN,
    A;  // 1


    public short toNumber() {
        return switch (this) {
            case A -> (short) 1;
            default -> (short) 0;
        };
    }

    static QueryType fromNumber(short number) {
        return switch (number) {
            case (short) 1 -> A;
            default -> UNKNOWN;
        };
    }

}
