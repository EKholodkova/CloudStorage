package BClient;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BClientPacker {
    private final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private final DataOutputStream dos = new DataOutputStream(bos);
    private final BClient client = new BClient();

    protected void sendMsg(String[] arguments) throws IOException {
        DataOutputStream out = client.getOut();
        String userName = null;
        String password = null;
        String command = null;
        List<String> params = new ArrayList<>();

        for(String s : arguments) {
            if(s.startsWith("-u")) {
                userName = s.substring(2);
            } else if(s.startsWith("-p")) {
                password = s.substring(2);
            } else {
                if(command == null) {
                    command = s;
                } else {
                    params.add(s);
                }
            }
        }
        dos.writeUTF(command);
        dos.writeUTF(userName);
        dos.writeUTF(password);
        if(params.size() > 0) {
            for(String s : params) {
                dos.writeUTF(s);
            }
        }
        dos.flush();
        int msgSize = bos.size();
        out.writeInt(msgSize);
        bos.writeTo(out);
    }

    protected void getAnswer() throws IOException {
        DataInputStream in = client.getIn();
        int answerSize = in.readInt();
        String status = in.readUTF();
        System.out.println(status);
    }

}
