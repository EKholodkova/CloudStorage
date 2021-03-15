package Netty;

import java.util.HashMap;
import java.util.Map;

public class UsersList {
    Map<String, String> loginInfo = new HashMap<>();

    public UsersList() {
        loginInfo.put("login1", "123");
        loginInfo.put("login2", "124");
        loginInfo.put("login3", "125");
        loginInfo.put("login4", "126");
    }

    public Map<String, String> getLoginInfo() {
        return loginInfo;
    }
}
