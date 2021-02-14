import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class DnsResolver {

    public static void main(String[] args) throws Exception {
//        Path path = Paths.get("C:\\Users\\Dominik\\Documents\\google_dns_response.txt");
//        BytePacketBuffer buffer = new BytePacketBuffer();
//        buffer.setBuffer(Files.readAllBytes(path));
//
//
//        DnsPacket packet = new DnsPacket();
//        packet.fromBuffer(buffer);
//
//
//        System.out.println(packet.getHeader());
//        packet.getQuestions().forEach(System.out::println);
//        packet.getAnswers().forEach(System.out::println);
//        packet.getAuthorityRecords().forEach(System.out::println);
//        packet.getResourceRecords().forEach(System.out::println);

        String qname = "www.yahoo.com";
        QueryType queryType = QueryType.A;

        // Use Google's DNS resolver
        String server = "8.8.8.8";
        int port = 53;

        DatagramSocket socket = new DatagramSocket();

        DnsPacket packet = new DnsPacket();
        packet.getHeader().setId((short) 7654);
        packet.getHeader().setQuestionsCnt((short) 1);
        packet.getHeader().setRecursionDesired(true);

        packet.getQuestions().add(new DnsQuestion(qname,queryType));
        BytePacketBuffer requestBuffer = new BytePacketBuffer();

        packet.write(requestBuffer);

        DatagramPacket udpPacket = new DatagramPacket(Arrays.copyOfRange(requestBuffer.getBuffer(), 0, requestBuffer.getPosition()), requestBuffer.getPosition(), Inet4Address.getByName(server),port);
        socket.send(udpPacket);

        BytePacketBuffer responseBuffer = new BytePacketBuffer();
        udpPacket = new DatagramPacket(responseBuffer.getBuffer(),responseBuffer.getBuffer().length);

        socket.receive(udpPacket);

        DnsPacket responsePacket = new DnsPacket();
        responsePacket.fromBuffer(responseBuffer);

        System.out.println(packet.getHeader());
        responsePacket.getQuestions().forEach(System.out::println);
        responsePacket.getAnswers().forEach(System.out::println);
        responsePacket.getAuthorityRecords().forEach(System.out::println);
        responsePacket.getResourceRecords().forEach(System.out::println);




    }

}
