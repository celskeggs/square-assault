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

	public static final int NETWORK_PROTOCOL_VERSION = 0;

	private final Socket socket;
	private final LinkedBlockingQueue<Messages.ToServer> sendQueue = new LinkedBlockingQueue<>();
	private final LinkedBlockingQueue<Messages.ToClient> recvQueue = new LinkedBlockingQueue<>();
	private final Connect info;
	private final int protocol;

	private static final Messages.ToClient sentinel = Messages.ToClient.newBuilder().build();

	public Network(String server, String username) throws UnknownHostException, IOException {
		socket = new Socket(InetAddress.getByName(server), SharedConfig.PORT);
		socket.setTcpNoDelay(true);
		InputStream input = socket.getInputStream();
		OutputStream output = socket.getOutputStream();
		System.out.println("Ready");
		Messages.Identify.newBuilder().setName(username).setProtocol(NETWORK_PROTOCOL_VERSION).build().writeDelimitedTo(output);
		System.out.println("Sent");
		this.info = Messages.Connect.parseDelimitedFrom(input);
		if (info == null) {
			throw new IOException("No connection message!");
		}
		protocol = Math.min(info.getProtocol(), NETWORK_PROTOCOL_VERSION);
		System.out.println("Got");
		new Thread(new QueueReceiver<Messages.ToClient>(recvQueue, input, Messages.ToClient.newBuilder(), sentinel), "Receiver").start();
		new Thread(new QueueSender<>(this.sendQueue, output), "Sender").start();
	}

	public void send(Messages.ToServer serv) throws InterruptedException {
		sendQueue.put(serv);
	}

	public void handleAll(Context context) throws InterruptedException {
		context.handleConnectInfo(info);
		while (true) {
			ToClient taken = recvQueue.take();
			if (taken == sentinel) {
				break;
			}
			switch (taken.getMessageCase()) {
			case CHAT:
				context.handleChat(taken.getChat());
				break;
			case COUNT:
				context.handleTurretCount(taken.getCount());
				break;
			case DISCONNECT:
				context.handleDisconnect(taken.getDisconnect());
				break;
			case POSITION:
				context.handleSetPosition(taken.getPosition());
				break;
			default:
				System.out.println("Bad input: " + taken);
				break;
			}
		}
	}
}
