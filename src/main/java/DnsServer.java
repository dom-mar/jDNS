import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class DnsServer {

    /*
     * Preforms lookup by forwarding queries to another DNS server */
    public static DnsPacket lookup(String qname, QueryType queryType) throws Exception {

        // Use Google's DNS resolver for forwarding queries
        String server = "8.8.8.8";
        int port = 53;

        // Create new UDP socket
        DatagramSocket socket = new DatagramSocket();
        DnsPacket packet = new DnsPacket();

        // Set random ID - it doesn't matter
        packet.getHeader().setId((short) 7654);
        packet.getHeader().setQuestionsCnt((short) 1);

        // Set to true so Google's DNS can resolve it
        packet.getHeader().setRecursionDesired(true);

        // Create new question for A record of 'www.yahoo.com'
        packet.getQuestions().add(new DnsQuestion(qname, queryType));
        BytePacketBuffer requestBuffer = new BytePacketBuffer();

        // Write newly created DNS packet to buffer
        packet.write(requestBuffer);

        // Create UDP packet by copying part of the request buffer
        // up till the current position inside that buffer.
        // Size of copied buffer is equal to current position
        DatagramPacket udpPacket = new DatagramPacket(
                Arrays.copyOfRange(requestBuffer.getBuffer(), 0, requestBuffer.getPosition()),
                requestBuffer.getPosition(),
                InetAddress.getByName(server),
                port);

        // Send UDP packet
        socket.send(udpPacket);

        // Create new buffer for response
        BytePacketBuffer responseBuffer = new BytePacketBuffer();

        // Set UDP packet's buffer to newly created one
        udpPacket = new DatagramPacket(responseBuffer.getBuffer(), responseBuffer.getBuffer().length);

        // Receive data from Google's DNS resolver
        socket.receive(udpPacket);

        // Create new DNS packet and fill it with data from
        // response buffer
        DnsPacket responsePacket = new DnsPacket();
        responsePacket.fromBuffer(responseBuffer);
        return responsePacket;
    }

    /* Handles incoming query */
    public static void handleQuery(DatagramSocket socket) throws Exception {

        BytePacketBuffer requestBuffer = new BytePacketBuffer();

        DatagramPacket udpPacket = new DatagramPacket(requestBuffer.getBuffer(), requestBuffer.getBuffer().length);

        // Receive single query - blocking method
        socket.receive(udpPacket);

        // Remember address and port for responding
        InetAddress address = udpPacket.getAddress();
        int port = udpPacket.getPort();

        // Read data and covert it to DNS packet
        DnsPacket requestDnsPacket = new DnsPacket();
        requestDnsPacket.fromBuffer(requestBuffer);

        // Prepare response packet by copying ID and setting flags
        DnsPacket responseDnsPacket = new DnsPacket();
        responseDnsPacket.getHeader().setId(requestDnsPacket.getHeader().getId());
        responseDnsPacket.getHeader().setRecursionDesired(true);
        responseDnsPacket.getHeader().setRecursionAvailable(true);
        responseDnsPacket.getHeader().setResponse(true);

        // Check if request contains question
        if (!requestDnsPacket.getQuestions().isEmpty()) {
            DnsQuestion question = requestDnsPacket.getQuestions().get(0);
            System.out.println(">>> RECEIVED QUERY <<<");
            System.out.println(question);

            // Try to forward query to another DNS server. Query might
            // fail so we need to catch exception and set proper result
            // code.
            try {
                DnsPacket result = DnsServer.lookup(question.getName(), question.getQueryType());
                responseDnsPacket.getQuestions().add(question);

                // Copy response code from returned result
                responseDnsPacket.getHeader().setRescode(result.getHeader().getRescode());

                // Copy all records
                result.getAnswers().forEach(x -> {
                    System.out.println(">>> ANSWER <<<");
                    System.out.println(x);
                    responseDnsPacket.getAnswers().add(x);
                });

                result.getAuthorityRecords().forEach(x -> {
                    System.out.println(">>> AUTHORITY <<<");
                    System.out.println(x);
                    responseDnsPacket.getAuthorityRecords().add(x);
                });

                result.getResourceRecords().forEach(x -> {
                    System.out.println(">>> RESOURCE <<<");
                    System.out.println(x);
                    responseDnsPacket.getResourceRecords().add(x);
                });

            } catch (Exception ex) {
                responseDnsPacket.getHeader().setRescode(ResultCode.SERVFAIL);
            }

            // If query does not contain question, set response code to
            // indicate format error made by sender.
        } else {
            responseDnsPacket.getHeader().setRescode(ResultCode.FORMERR);
        }

        // Write response packet to new buffer, make a UDP packet
        // from new buffer and send it off
        BytePacketBuffer responseBuffer = new BytePacketBuffer();
        responseDnsPacket.write(responseBuffer);

        udpPacket = new DatagramPacket(Arrays.copyOfRange(responseBuffer.getBuffer(),
                0, responseBuffer.getPosition()), responseBuffer
                .getPosition(), address, port);

        socket.send(udpPacket);

    }
}
