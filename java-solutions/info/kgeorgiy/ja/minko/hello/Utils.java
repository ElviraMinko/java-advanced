package info.kgeorgiy.ja.minko.hello;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Utils {

    public static final Charset CHARSET = StandardCharsets.UTF_8;


    public static DatagramPacket createPacket(DatagramSocket socket, InetSocketAddress inetSocketAddress) throws SocketException {
        return new DatagramPacket(new byte[socket.getReceiveBufferSize()], socket.getReceiveBufferSize(), inetSocketAddress);
    }

    public static DatagramPacket createPacket(DatagramSocket socket) throws SocketException {
        return new DatagramPacket(new byte[socket.getReceiveBufferSize()], socket.getReceiveBufferSize());
    }

    public static String getString(DatagramPacket packet) {
        return getString(packet.getData(), packet.getOffset(), packet.getLength());
    }

    public static String getString(byte[] data, int offset, int length) {
        return new String(data, offset, length, CHARSET);
    }

    public static void setString(DatagramPacket packet, String string) {
        byte[] bytes = string.getBytes(CHARSET);
        packet.setData(bytes);
    }
}
