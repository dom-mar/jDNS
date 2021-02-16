public class DnsHeader {

    /*
     * Header of DNS record is defined as:
     *
     *      0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
     *    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     *    |                      ID                       |
     *    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     *    |QR|   Opcode  |AA|TC|RD|RA| Z AD CD|   RCODE   |
     *    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     *    |                    QDCOUNT                    |
     *    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     *    |                    ANCOUNT                    |
     *    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     *    |                    NSCOUNT                    |
     *    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     *    |                    ARCOUNT                    |
     *    +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     *
     * */

    private short id;
    private boolean response; // QR field - query [0] or response [1]
    private byte opcode;      // We need only 4 bits
    private boolean authoritativeAnswer;
    private boolean truncatedMessage;
    private boolean recursionDesired;
    private boolean recursionAvailable;
    private boolean z;  // DNSSEC specific - ignored
    private boolean authenticData; // DNSSEC specific - ignored
    private boolean checkingDisabled; // DNSSEC specific - ignored
    private ResultCode rescode;

    private short questionsCnt;         // Number of questions
    private short answersCnt;           // Number of answers
    private short authorityRecordsCnt;  // Number of name server resource records
    private short resourceRecordsCnt;   // Number of additional resource records

    public DnsHeader() {
        this.id = 0;
        this.response = false;
        this.opcode = 0;
        this.authoritativeAnswer = false;
        this.truncatedMessage = false;
        this.recursionDesired = false;
        this.recursionAvailable = false;
        this.z = false;
        this.authenticData = false;
        this.checkingDisabled = false;
        this.rescode = ResultCode.NOERROR;
        this.questionsCnt = 0;
        this.answersCnt = 0;
        this.authorityRecordsCnt = 0;
        this.resourceRecordsCnt = 0;
    }

    /*
     * Reads header from given buffer */
    public void read(BytePacketBuffer buffer) {
        // Set header id
        this.id = buffer.readTwo();

        // Read first byte including flags QR, Opcode, AA, TC, RD
        byte upperBits = buffer.readOne();

        // Extract flags from first byte starting from back
        this.recursionDesired = (upperBits & 0b1) > 0;
        this.truncatedMessage = (upperBits & 0b10) > 0;
        this.authoritativeAnswer = (upperBits & 0b100) > 0;
        // Remove last three bits and apply 0b0000 1111 mask to get last 4 bits
        this.opcode = (byte) ((upperBits >>> 3) & 0x0F);
        // Extract 1st bit by applying mask 0b1000 0000
        this.response = (upperBits & 0x80) > 0;

        // Read second byte including flags RA, Z, AD, CD, RCODE
        byte lowerBits = buffer.readOne();

        //Extract flags from second byte starting from the back
        this.rescode = ResultCode.fromNumber((byte) (lowerBits & 0x0F));
        this.checkingDisabled = (lowerBits & 0x10) > 0;
        this.authenticData = (lowerBits & 0x20) > 0;
        this.z = (lowerBits & 0x40) > 0;
        this.recursionAvailable = (lowerBits & 0x80) > 0;

        this.questionsCnt = buffer.readTwo();
        this.answersCnt = buffer.readTwo();
        this.authorityRecordsCnt = buffer.readTwo();
        this.resourceRecordsCnt = buffer.readTwo();

    }

    /*
     * Writes current DNS header to given buffer */
    public void write(BytePacketBuffer buffer) {
        buffer.writeTwo(this.id);

        // First octet of flags
        buffer.writeOne((byte)
                ((byte) (this.recursionDesired ? 1 : 0)
                        | (byte) ((this.truncatedMessage ? 1 : 0) << 1)
                        | (byte) ((this.authoritativeAnswer ? 1 : 0) << 2)
                        | (this.opcode << 3)
                        | (byte) ((this.response ? 1 : 0) << 7)));

        // Second octet of flags
        buffer.writeOne((byte)
                ((byte) (this.rescode.ordinal())
                        | (byte) ((this.checkingDisabled ? 1 : 0) << 4)
                        | (byte) ((this.authenticData ? 1 : 0) << 5)
                        | (byte) ((this.z ? 1 : 0) << 6)
                        | (byte) ((this.recursionAvailable ? 1 : 0) << 7)));

        // Writing counters to buffer
        buffer.writeTwo(this.questionsCnt);
        buffer.writeTwo(this.answersCnt);
        buffer.writeTwo(this.authorityRecordsCnt);
        buffer.writeTwo(this.resourceRecordsCnt);
    }

    /*
     * Getters and setters for counters */
    public short getQuestionsCnt() {
        return questionsCnt;
    }

    public short getAnswersCnt() {
        return answersCnt;
    }

    public short getAuthorityRecordsCnt() {
        return authorityRecordsCnt;
    }

    public short getResourceRecordsCnt() {
        return resourceRecordsCnt;
    }

    public void setQuestionsCnt(short questionsCnt) {
        this.questionsCnt = questionsCnt;
    }

    public void setAnswersCnt(short answersCnt) {
        this.answersCnt = answersCnt;
    }

    public void setAuthorityRecordsCnt(short authorityRecordsCnt) {
        this.authorityRecordsCnt = authorityRecordsCnt;
    }

    public void setResourceRecordsCnt(short resourceRecordsCnt) {
        this.resourceRecordsCnt = resourceRecordsCnt;
    }

    /*
     * Getters and setters */
    public short getId() {
        return this.id;
    }

    public void setId(short id) {
        this.id = id;
    }

    public void setRecursionDesired(boolean recursionDesired) {
        this.recursionDesired = recursionDesired;
    }

    public void setResponse(boolean response) {
        this.response = response;
    }

    public void setRecursionAvailable(boolean recursionAvailable) {
        this.recursionAvailable = recursionAvailable;
    }

    public ResultCode getRescode() {
        return rescode;
    }

    public void setRescode(ResultCode rescode) {
        this.rescode = rescode;
    }

    @Override
    public String toString() {
        return "DnsHeader {" + "\n" +
                "\t" + "id = " + convertSingedShortToUnsigned(id) + "\n" +
                "\t" + "response = " + response + "\n" +
                "\t" + "opcode = " + opcode + "\n" +
                "\t" + "authoritativeAnswer = " + authoritativeAnswer + "\n" +
                "\t" + "truncatedMessage = " + truncatedMessage + "\n" +
                "\t" + "recursionDesired = " + recursionDesired + "\n" +
                "\t" + "recursionAvailable = " + recursionAvailable + "\n" +
                "\t" + "z = " + z + "\n" +
                "\t" + "authenticData = " + authenticData + "\n" +
                "\t" + "checkingDisabled = " + checkingDisabled + "\n" +
                "\t" + "rescode = " + rescode + "\n" +
                "\t" + "questionsCnt = " + questionsCnt + "\n" +
                "\t" + "answersCnt = " + answersCnt + "\n" +
                "\t" + "authorityRecordsCnt = " + authorityRecordsCnt + "\n" +
                "\t" + "resourceRecordsCnt = " + resourceRecordsCnt + "\n" +
                '}';
    }

    /*
     * Helper method for properly displaying short values */
    public static long convertSingedShortToUnsigned(short number) {
        return number & 0x0000FFFF;
    }

    /*
     * Helper method for properly displaying int values*/
    public static long convertSingedIntToUnsigned(int number) {
        return number & 0x00000000FFFFFFFFL;
    }

}
