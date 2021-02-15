/*
 * Enum for types of queries */
public enum QueryType {
    UNKNOWN,
    A,      // 1
    NS,     // 2
    CNAME,  // 5
    MX,     // 15
    AAAA;   // 28


    public short toNumber() {
        return switch (this) {
            case A -> (short) 1;
            case NS -> (short) 2;
            case CNAME -> (short) 5;
            case MX -> (short) 15;
            case AAAA -> (short) 28;
            default -> (short) 0;
        };
    }

    static QueryType fromNumber(short number) {
        return switch (number) {
            case (short) 1 -> A;
            case (short) 2 -> NS;
            case (short) 5 -> CNAME;
            case (short) 15 -> MX;
            case (short) 28 -> AAAA;
            default -> UNKNOWN;
        };
    }

}
