package openhack.team12;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class MineStat {
    private static int dataSize = 512; // this will hopefully suffice since the MotD should be <=59 characters
    private static int numFields = 6;  // number of values expected from server

    private int m_currentPlayers = 0;
    private int m_maxPlayers = 0;
    private boolean m_isServerUp = false;

    public MineStat(String address, int port) throws Exception {
        byte[] rawServerData = new byte[dataSize];
        try {
            Socket tcpclient = new Socket(address, port);
            DataOutputStream dos = new DataOutputStream(tcpclient.getOutputStream());
            DataInputStream dis = new DataInputStream(tcpclient.getInputStream());

            byte[] payload = new byte[] { (byte)0xFE, 0x01 };
            dos.write(payload, 0, payload.length);
            dis.read(rawServerData, 0, dataSize);
            tcpclient.close();
        } catch (Exception e) {
            e.printStackTrace();
            m_isServerUp = false;
            return;
        }

        if (rawServerData.length == 0) {
            m_isServerUp = false;
        } else {
            String serverData = new String(rawServerData, "UTF-8");
            String[] values = serverData.split("\u0000\u0000\u0000");

            if (serverData.length() >= numFields) {
                m_isServerUp = true;
                m_currentPlayers = Integer.parseInt(values[4]);
                String maxPlayerS = values[5].replace(String.valueOf(values[5].charAt(1)), "");
                m_maxPlayers = Integer.parseInt(maxPlayerS);
            } else {
                m_isServerUp = false;
            }
        }
    }

    public int getCurrentPlayers() {
        return m_currentPlayers;
    }

    public int getMaximumPlayers() {
        return m_maxPlayers;
    }

    public boolean isServerUp() {
        return m_isServerUp;
    }
}
