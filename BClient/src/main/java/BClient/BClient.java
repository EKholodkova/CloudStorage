package BClient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BClient {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public BClient() {
        try {
            socket = new Socket("localhost", 3000);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DataInputStream getIn() {
        return in;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void close() throws IOException {
        in.close();
        out.close();
    }
}
