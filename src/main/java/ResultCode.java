/*
 * Enum for values of rescode field - not all are implemented.
 * For more https://tools.ietf.org/html/rfc5395#page-5 */
public enum ResultCode {
    NOERROR,   // 0
    FORMERR,   // 1
    SERVFAIL,  // 2
    NXDOMAIN,  // 3
    NOTIMP,    // 4
    REFUSED;   // 5

    static ResultCode fromNumber(byte number) {
        return switch (number) {
            case (byte) 1 -> SERVFAIL;
            case (byte) 2 -> FORMERR;
            case (byte) 3 -> NXDOMAIN;
            case (byte) 4 -> NOTIMP;
            case (byte) 5 -> REFUSED;
            default -> NOERROR;
        };
    }
}
