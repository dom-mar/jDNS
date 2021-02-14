import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.util.Arrays;

public class DnsResolver {

    public static void main(String[] args) throws Exception {

        // Domain that is being resolved
        String qname = "www.yahoo.com";
        QueryType queryType = QueryType.A;

        // Use Google's DNS resolver
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
        // Use 8.8.8.8 as address and 53 as port for UDP packet
        DatagramPacket udpPacket = new DatagramPacket(Arrays.copyOfRange(requestBuffer.getBuffer(),
                0, requestBuffer.getPosition()), requestBuffer
                .getPosition(), Inet4Address.getByName(server), port);

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

        // Print retrieved information
        System.out.println(packet.getHeader());
        responsePacket.getQuestions().forEach(System.out::println);
        responsePacket.getAnswers().forEach(System.out::println);
        responsePacket.getAuthorityRecords().forEach(System.out::println);
        responsePacket.getResourceRecords().forEach(System.out::println);


    }

}
