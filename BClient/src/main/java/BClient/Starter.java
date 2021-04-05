package BClient;

import java.io.IOException;

public class Starter {
    public static void main(String[] args) throws IOException {
        BClient client = new BClient();
        BClientPacker packer = new BClientPacker();
        packer.sendMsg(args);
        packer.getAnswer();
        client.close();
    }
}
