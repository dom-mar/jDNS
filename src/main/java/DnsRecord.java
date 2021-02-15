import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

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
    private String host;
    private short queryTypeNumber;
    private short dataLength;
    private short priority;
    private int ttl;
    private InetAddress addr;
    private DnsRecordType dnsRecordType;

    enum DnsRecordType {
        UNKNOWN,
        A,
        NS,
        CNAME,
        MX,
        AAAA
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
                this.addr = Inet4Address.getByAddress(rawAddr);
            }
            // AAAA record contains IPv6 address
            case AAAA -> {
                this.dnsRecordType = DnsRecordType.AAAA;
                byte[] rawAddr = new byte[16];
                for (int i = 0; i < 16; i++) {
                    rawAddr[i] = buffer.readOne();
                }
                this.addr = Inet6Address.getByAddress(rawAddr);
            }
            // NS record contains host
            case NS -> {
                this.dnsRecordType = DnsRecordType.NS;
                this.host = buffer.readQName();
            }
            // CNAME record contains host
            case CNAME -> {
                this.dnsRecordType = DnsRecordType.CNAME;
                this.host = buffer.readQName();
            }
            // MX record contains priority and host
            case MX -> {
                this.dnsRecordType = DnsRecordType.MX;
                this.priority = buffer.readTwo();
                this.host = buffer.readQName();
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
            // ttl - ttl, rdlength - 4 octets for IP addr,
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
            // For NS and CNAME record: name - domain, qtype - NS / CNAME, class - IN (1)
            // ttl - ttl, rdlength - to be determined (len need for writing host name),
            // rdata - host
            case NS, CNAME -> {
                buffer.writeQName(this.domain);
                buffer.writeTwo(this.queryTypeNumber);
                buffer.writeTwo((short) 1);
                buffer.writeFour(this.ttl);

                // We don't know yet what is final length of the data
                // so we need to remember current position
                int lenPos = buffer.getPosition();
                buffer.writeTwo((short) 0);

                buffer.writeQName(this.host);

                // Calculating length of host
                short size = (short) (buffer.getPosition() - (lenPos + 2));

                // Setting length of the data
                buffer.setTwo(lenPos, size);
            }
            // For MX record: name - domain, qtype - MX, class - IN (1)
            // ttl - ttl, rdlength - to be determined (len need for writing
            // priority + host name), rdata - host
            case MX -> {
                buffer.writeQName(this.domain);
                buffer.writeTwo(this.queryTypeNumber);
                buffer.writeTwo((short) 1);
                buffer.writeFour(this.ttl);

                int lenPos = buffer.getPosition();
                buffer.writeTwo((short) 0);

                buffer.writeTwo(this.priority);
                buffer.writeQName(this.host);

                short size = (short) (buffer.getPosition() - (lenPos + 2));
                buffer.setTwo(lenPos, size);
            }
            // For AAAA record: name - domain, qtype - AAAA, class - IN (1)
            // ttl - ttl, rdlength - 16 octets for IP addr,
            // rdata - IP addr
            case AAAA -> {
                buffer.writeQName(this.domain);
                buffer.writeTwo(this.queryTypeNumber);
                buffer.writeTwo((short) 1);
                buffer.writeFour(this.ttl);

                // 16 octets for IPv6 address
                buffer.writeTwo((short) 16);

                // Write IPv6 octets to buffers
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
            case UNKNOWN -> "UNKNOWN {" + "\n" +
                    "\t" + "domain = '" + domain + '\'' + "\n" +
                    "\t" + "queryTypeNumber = " + queryTypeNumber + "\n" +
                    "\t" + "dataLength = " + dataLength + "\n" +
                    "\t" + "ttl = " + DnsHeader.convertSingedIntToUnsigned(ttl) + "\n" +
                    '}';
            case A -> "A {" + "\n" +
                    "\t" + "domain = '" + domain + '\'' + "\n" +
                    "\t" + "addr = " + addr.getHostAddress() + "\n" +
                    "\t" + "ttl = " + DnsHeader.convertSingedIntToUnsigned(ttl) + "\n" +
                    '}';
            case NS -> "NS {" + "\n" +
                    "\t" + "domain = '" + domain + '\'' + "\n" +
                    "\t" + "host = " + host + "\n" +
                    "\t" + "ttl = " + DnsHeader.convertSingedIntToUnsigned(ttl) + "\n" +
                    '}';
            case CNAME -> "CNAME {" + "\n" +
                    "\t" + "domain = '" + domain + '\'' + "\n" +
                    "\t" + "host = " + host + "\n" +
                    "\t" + "ttl = " + DnsHeader.convertSingedIntToUnsigned(ttl) + "\n" +
                    '}';
            case MX -> "MX {" + "\n" +
                    "\t" + "domain = '" + domain + '\'' + "\n" +
                    "\t" + "priority = " + DnsHeader.convertSingedShortToUnsigned(priority) + "\n" +
                    "\t" + "host = " + host + "\n" +
                    "\t" + "ttl = " + DnsHeader.convertSingedIntToUnsigned(ttl) + "\n" +
                    '}';
            case AAAA -> "AAAA {" + "\n" +
                    "\t" + "domain = '" + domain + '\'' + "\n" +
                    "\t" + "addr = " + addr.getHostAddress() + "\n" +
                    "\t" + "ttl = " + DnsHeader.convertSingedIntToUnsigned(ttl) + "\n" +
                    '}';
        };

    }
}
