import java.net.DatagramSocket;
import java.net.Inet4Address;

public class DnsResolver {

    public static void main(String[] args) throws Exception {

        DatagramSocket socket = new DatagramSocket(5053, Inet4Address.getLoopbackAddress());
        System.out.println(">>> LISTENING <<<");

        // Single threaded sequential handling of requests
        while (true) {
            try {
                DnsServer.handleQuery(socket);
            } catch (Exception ex) {
                System.out.println(">>> ERROR <<<" + "\n" + ex.getMessage());
            }
        }

    }

}
