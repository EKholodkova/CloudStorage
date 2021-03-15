package client;

import Netty.UsersList;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Client {
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;

	private JFrame frame;
	private JLabel statusLabel;

	public Client() throws IOException {
		socket = new Socket("localhost", 3000);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		runClient();
	}

	private void runClient() {
		new LoginWindow(null, new UsersList().getLoginInfo(), result -> {
			if (result)
				makeAppWindow();
		});
		//makeAppWindow();
		//new LoginWindow(frame, new UsersList().getLoginInfo());
	}

	private void makeAppWindow() {
		frame = new JFrame("Cloud Storage");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);

		JTextArea ta = new JTextArea();

		JList<String> list = new JList<>();
		DefaultListModel<String> myModel = new DefaultListModel<>();
		list.setModel(myModel);

		JPanel listJp = new JPanel();
		listJp.add(BorderLayout.CENTER, new JScrollPane(list));

		fillist(myModel);

		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		frame.add(statusPanel, BorderLayout.NORTH);
		statusPanel.setPreferredSize(new Dimension(frame.getWidth(), 32));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		statusLabel = new JLabel();
		statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(statusLabel);

		frame.getContentPane().add(BorderLayout.WEST,listJp);

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



	private void fillist(DefaultListModel<String> model) {
		java.util.List<String> list = downloadFileList();
		model.clear();
		for(String fileName : list) {
			model.addElement(fileName);
		}
	}

	private List<String> downloadFileList() {
		List<String> list = new ArrayList<>();
		try {
			StringBuilder sb = new StringBuilder();
			out.write("list-files".getBytes(StandardCharsets.UTF_8));
			while (true) {
				byte[] buffer = new byte[512];
				int size = in.read(buffer);
				sb.append(new String(buffer, 0, size));
				if(sb.toString().endsWith("end")) {
					break;
				}
			}
			String fileString = sb.substring(0, sb.toString().length() - 4);
			list = Arrays.asList(fileString.split("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
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
