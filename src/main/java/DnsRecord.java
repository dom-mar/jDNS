import java.net.Inet4Address;

public class DnsRecord {
    /*
     * DNS Resource Records are defined as:
     *
     *     0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5
     *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     *   |                                               |
     *   /                                               /
     *   /                      NAME                     /
     *   /                                               /
     *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     *   |                      TYPE                     |
     *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     *   |                     CLASS                     |
     *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     *   |                      TTL                      |
     *   |                                               |
     *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     *   |                   RDLENGTH                    |
     *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--|
     *   /                     RDATA                     /
     *   /                                               /
     *   +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
     *
     *    * */

    // Not all fields are used for all records
    private String domain;
    private short queryTypeNumber;
    private short dataLength;
    private int ttl;
    private Inet4Address addr;
    private DnsRecordType dnsRecordType;

    enum DnsRecordType {
        A,
        UNKNOWN,
    }

    /*
     * Reads DNS record from given buffer */
    public DnsRecord read(BytePacketBuffer buffer) throws Exception {
        this.domain = buffer.readQName();
        this.queryTypeNumber = buffer.readTwo();
        QueryType qtype = QueryType.fromNumber(this.queryTypeNumber);
        // Ignoring class for now
        buffer.readTwo();
        this.ttl = buffer.readFour();
        this.dataLength = buffer.readTwo();
        // Switch based on type of record
        switch (qtype) {
            // A record contains IP address
            case A -> {
                this.dnsRecordType = DnsRecordType.A;
                byte[] rawAddr = new byte[4];
                rawAddr[0] = buffer.readOne();
                rawAddr[1] = buffer.readOne();
                rawAddr[2] = buffer.readOne();
                rawAddr[3] = buffer.readOne();
                this.addr = (Inet4Address) Inet4Address.getByAddress(rawAddr);
            }
            case UNKNOWN -> {
                this.dnsRecordType = DnsRecordType.UNKNOWN;
                // Skip payload for now
                buffer.forward((int) this.dataLength);
            }
        }

        return this;

    }

    /*
     * Writes current record to buffer and returns length of written data */
    public int write(BytePacketBuffer buffer) {
        int startPosition = buffer.getPosition();
        switch (this.dnsRecordType) {
            // For A record: name - domain, qtype - A, class - IN (1)
            // ttl - ttl, rdlength - 4 octets for IP addr
            // rdata - IP addr
            case A -> {
                buffer.writeQName(this.domain);
                buffer.writeTwo(this.queryTypeNumber);
                buffer.writeTwo((short) 1);
                buffer.writeFour(this.ttl);
                // 4 octets for IP address
                buffer.writeTwo((short) 4);

                // Write IP octets to buffers
                for (byte ipOctet : this.addr.getAddress()) {
                    buffer.writeOne(ipOctet);
                }
            }
            // Ignore for now
            case UNKNOWN -> {
                System.out.println("Ignoring for now : ");
                System.out.println(this);
            }
        }
        return (buffer.getPosition() - startPosition);
    }

    @Override
    public String toString() {
        //Custom string format depending on type of record
        return switch (this.dnsRecordType) {
            case A -> "A {" + "\n" +
                    "\t" + "domain = '" + domain + '\'' + "\n" +
                    "\t" + "ttl = " + DnsHeader.convertSingedIntToUnsigned(ttl) + "\n" +
                    "\t" + "addr = " + addr.getHostAddress() + "\n" +
                    '}';
            case UNKNOWN -> "UNKNOWN {" + "\n" +
                    "\t" + "domain = '" + domain + '\'' + "\n" +
                    "\t" + "queryTypeNumber = " + queryTypeNumber + "\n" +
                    "\t" + "dataLength = " + dataLength + "\n" +
                    "\t" + "ttl = " + DnsHeader.convertSingedIntToUnsigned(ttl) + "\n" +
                    '}';
        };

    }
}
