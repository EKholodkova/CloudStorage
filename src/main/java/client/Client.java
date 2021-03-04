package client;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Client {
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;

	private JLabel statusLabel;

	public Client() throws IOException {
		socket = new Socket("localhost", 1235);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		runClient();
	}

	private void runClient() {
		JFrame frame = new JFrame("Cloud Storage");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 300);

		JTextArea ta = new JTextArea();

		String[] arr = {"1.txt", "2.txt"};  //здесь никакого функционала. Просто посмотрела, что за JList такой
		JList jl = new JList(arr);			//не совсем понимаю, что в нем должно отображаться. Файлы на сервере или со стороны клиента?
		JPanel listJp = new JPanel();
		listJp.add(BorderLayout.CENTER, jl);

		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		frame.add(statusPanel, BorderLayout.NORTH);
		statusPanel.setPreferredSize(new Dimension(frame.getWidth(), 32));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		statusLabel = new JLabel();
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(statusLabel);

		frame.getContentPane().add(BorderLayout.WEST,listJp);
		// TODO: 02.03.2021
		// list file - JList
		JButton uploadButton = new JButton("Upload");
		JButton downloadButton = new JButton("Download");
		JButton deleteButton = new JButton("Delete");

		JPanel jp = new JPanel();
		jp.setLayout(new FlowLayout());
		frame.getContentPane().add(BorderLayout.SOUTH, jp);
		jp.add(deleteButton);
		jp.add(uploadButton);
		jp.add(downloadButton);

		frame.getContentPane().add(BorderLayout.CENTER, ta);


		frame.setVisible(true);

		uploadButton.addActionListener(a -> {
			System.out.println(sendFile(ta.getText()));
		});
		downloadButton.addActionListener(a -> {
			getFile(ta.getText());
		});
		deleteButton.addActionListener(a -> {
			removeFile(ta.getText());
		});
	}

	private String sendFile(String filename) {
		try {
			File file = new File("client" + File.separator + filename);
			if (file.exists()) {
				out.writeUTF("upload");
				out.writeUTF(filename);
				long length = file.length();
				out.writeLong(length);
				FileInputStream fis = new FileInputStream(file);
				int read = 0;
				byte[] buffer = new byte[256];
				while ((read = fis.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}
				out.flush();
				String status = in.readUTF();
				statusLabel.setText(status);
				return status;
			} else {
				return "File is not exists";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something error";
	}

	private void getFile(String fileName) {
		try {
			out.writeUTF("download");
			out.writeUTF(fileName);
			File file = new File("client" + File.separator + fileName);
			if (!file.exists()) {
				file.createNewFile();
				System.out.println("file created");
			}
			long size = in.readLong();
			System.out.println("size:" + size);
			FileOutputStream fos = new FileOutputStream(file);
			byte[] buffer = new byte[256];
			for (int i = 0; i < (size + 255) / 256; i++) {
				int read = in.read(buffer);
				fos.write(buffer, 0, read);
			}
			fos.close();
			String status = in.readUTF();
			statusLabel.setText(status);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void removeFile(String fileName) {
		try {
			out.writeUTF("remove");
			out.writeUTF(fileName);
			String status = in.readUTF();
			statusLabel.setText(status);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		new Client();
	}
}
