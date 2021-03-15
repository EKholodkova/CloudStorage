package client;

import Netty.UsersList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

interface LoginWindowListener {
    void onLoginResult(boolean result);
}

public class LoginWindow implements ActionListener {
    private Map<String, String> list = new HashMap<>();
    private UsersList us = new UsersList();

    private JDialog enterFrame;
    private JPanel enterPanel = new JPanel();
    private JLabel userLabel = new JLabel("User");
    private JTextField userText = new JTextField();
    private JLabel passwordLabel = new JLabel("Password");
    private JPasswordField passwordText = new JPasswordField();
    private JButton loginButton = new JButton("Login");
    private JButton registerButton = new JButton("Register");
    private JLabel messageLabel = new JLabel();
    private LoginWindowListener listener;

    public LoginWindow(JFrame parent, Map<String, String> list, LoginWindowListener listener) {
        this.list = list;
        this.listener = listener;

        enterFrame = new JDialog(parent, true);
        enterFrame.setLocation(150, 100);
        enterFrame.setSize(350, 250);
        enterFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        enterFrame.add(enterPanel);

        enterPanel.setLayout(null);

        userLabel.setBounds(10, 20, 80, 25);
        enterPanel.add(userLabel);

        userText.setBounds(100, 20, 165, 25);
        enterPanel.add(userText);

        passwordLabel.setBounds(10, 50, 80, 25);
        enterPanel.add(passwordLabel);

        passwordText.setBounds(100, 50, 165, 25);
        enterPanel.add(passwordText);

        loginButton.setBounds(10, 80, 80, 25);
        loginButton.addActionListener(this);
        enterPanel.add(loginButton);

        registerButton.setBounds(10, 110, 80, 25);
        registerButton.addActionListener(this);
        enterPanel.add(registerButton);

        messageLabel.setBounds(10, 140, 250,65);
        enterPanel.add(messageLabel);

        enterFrame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == loginButton) {
            String user = userText.getText();
            String password = passwordText.getText();
            System.out.println(list.get(user));
            System.out.println(password);

            if(list.containsKey(user)) {
                System.out.println(list.get(user));
                if(list.get(user).equals(password)) {
                    messageLabel.setText("Login successful");
                    enterFrame.dispose();
                    if (listener != null)
                        listener.onLoginResult(true);
                } else {
                    messageLabel.setText("Wrong password");
                    if (listener != null)
                        listener.onLoginResult(false);
                }
            } else {
                messageLabel.setText("Username not found");
                if (listener != null)
                    listener.onLoginResult(false);
            }
        }
        if(e.getSource() == registerButton) {
            String user = userText.getText();
            String password = passwordText.getText();

            us.getLoginInfo().put(user, password);
            list = us.getLoginInfo();
            System.out.println(us.getLoginInfo());
            messageLabel.setText("You've been registered! Please login");
            userText.setText("");
            passwordText.setText("");

        }
    }

    public static void main(String[] args) {
//        new LoginWindow(new UsersList().getLoginInfo());
    }
}
