package codeday.squareassault.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

import codeday.squareassault.protobuf.Messages;
import codeday.squareassault.protobuf.Messages.Connect;
import codeday.squareassault.protobuf.Messages.ToClient;
import codeday.squareassault.protobuf.QueueReceiver;
import codeday.squareassault.protobuf.QueueSender;
import codeday.squareassault.protobuf.SharedConfig;

public class Network {
	private final Socket socket;
	private final LinkedBlockingQueue<Messages.ToServer> sendQueue = new LinkedBlockingQueue<>();
	private final LinkedBlockingQueue<Messages.ToClient> recvQueue = new LinkedBlockingQueue<>();
	private final Connect info;

	public Network(String server, String username) throws UnknownHostException, IOException {
		socket = new Socket(InetAddress.getByName(server), SharedConfig.PORT);
		InputStream input = socket.getInputStream();
		OutputStream output = socket.getOutputStream();
		System.out.println("Ready");
		Messages.Identify.newBuilder().setName(username).build().writeDelimitedTo(output);
		System.out.println("Sent");
		this.info = Messages.Connect.parseDelimitedFrom(input);
		System.out.println("Got");
		new Thread(new QueueReceiver<Messages.ToClient>(recvQueue, input, Messages.ToClient.newBuilder()), "Receiver").start();
		new Thread(new QueueSender<>(this.sendQueue, output), "Sender").start();
	}
	
	public void send(Messages.ToServer serv) throws InterruptedException {
		sendQueue.put(serv);
	}

	public void handleAll(Context context) throws InterruptedException {
		context.handleConnectInfo(info);
		while (true) {
			ToClient taken = recvQueue.take();
			if (taken == null) {
				break;
			}
			if (taken.hasLine()) {
				context.handleLineMessage(taken.getLine());
			} else {
				System.out.println("Bad input: " + taken);
			}
		}
	}
}
