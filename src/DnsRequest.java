import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class DnsRequest {
    public static void main(String[] args) throws Exception {
        InetAddress ipAddress = InetAddress.getByName("8.8.8.8");

        byte[] dnsFrame = createDnsRequest(args[0]);

        DatagramSocket socket = new DatagramSocket();
        DatagramPacket dnsReqPacket = new DatagramPacket(dnsFrame, dnsFrame.length, ipAddress, 53);
        socket.send(dnsReqPacket);

        byte[] buffer = new byte[1024];
        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(responsePacket);

        String Address = parseDnsResponse(responsePacket.getData());

        System.out.println("IP address: " + Address);

        socket.close();
    }
    private static String parseDnsResponse(byte[] data) {
        int pos = 12;
        pos += 10;
        return (data[pos] & 0xff) + "." + (data[pos + 1] & 0xff) + "." + (data[pos + 2] & 0xff) + "." + (data[pos + 3] & 0xff);
    }

    private static byte[] createDnsRequest(String domainName) throws IOException {

        Random random = new Random();
        short ID = (short)random.nextInt(32767);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        short requestFlags = Short.parseShort("0000000100000000", 2);
        ByteBuffer flagsByteBuffer = ByteBuffer.allocate(2).putShort(requestFlags);
        byte[] flagsByteArray = flagsByteBuffer.array();

        short QDCOUNT = 1;
        short ANCOUNT = 0;
        short NSCOUNT = 0;
        short ARCOUNT = 0;

        dataOutputStream.writeShort(ID);
        dataOutputStream.write(flagsByteArray);
        dataOutputStream.writeShort(QDCOUNT);
        dataOutputStream.writeShort(ANCOUNT);
        dataOutputStream.writeShort(NSCOUNT);
        dataOutputStream.writeShort(ARCOUNT);


        String[] domainParts = domainName.split("\\.");

        for (int i = 0; i < domainParts.length; i++) {
            byte[] domainBytes = domainParts[i].getBytes(StandardCharsets.UTF_8);
            dataOutputStream.writeByte(domainBytes.length);
            dataOutputStream.write(domainBytes);
        }

        dataOutputStream.writeByte(0);
        dataOutputStream.writeShort(1);
        dataOutputStream.writeShort(1);

        return byteArrayOutputStream.toByteArray();
    }
}
