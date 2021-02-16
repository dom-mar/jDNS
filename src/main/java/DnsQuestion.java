public class DnsQuestion {

    /*
     * Question section format is defined as:
     *
     *     0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
     *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     *   |                                               |
     *   /                     QNAME                     /
     *   /                                               /
     *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     *   |                     QTYPE                     |
     *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     *   |                     QCLASS                    |
     *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     * */

    private String name;
    private QueryType queryType;

    public DnsQuestion(String name, QueryType queryType) {
        this.name = name;
        this.queryType = queryType;
    }

    /*
     * Reads name and query type from given buffer */
    public void read(BytePacketBuffer buffer) throws Exception {
        this.name = buffer.readQName();
        this.queryType = QueryType.fromNumber(buffer.readTwo());
        // Ignore class for now
        buffer.readTwo();
    }

    /*
     * Writes name, type and hardcoded class IN (1) that represents Internet */
    public void write(BytePacketBuffer buffer) {
        buffer.writeQName(this.name);
        buffer.writeTwo(this.queryType.toNumber());
        buffer.writeTwo((short) 1);

    }

    /*
     * Getters */
    public String getName() {
        return name;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    @Override
    public String toString() {
        return "DnsQuestion {" + "\n" +
                "\t" + "name = '" + name + '\'' + "\n" +
                "\t" + "queryType = " + queryType + "\n" +
                '}';
    }
}
