package codeday.squareassault.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

import codeday.squareassault.protobuf.Messages;
import codeday.squareassault.protobuf.Messages.ToClient;
import codeday.squareassault.protobuf.QueueReceiver;
import codeday.squareassault.protobuf.QueueSender;
import codeday.squareassault.protobuf.SharedConfig;

public class Network {
	private final Socket socket;
	private final LinkedBlockingQueue<Messages.ToServer> sendQueue = new LinkedBlockingQueue<>();
	private final LinkedBlockingQueue<Messages.ToClient> recvQueue = new LinkedBlockingQueue<>();

	public Network(String server) throws UnknownHostException, IOException {
		socket = new Socket(InetAddress.getByName(server), SharedConfig.PORT);
		new Thread(new QueueReceiver<Messages.ToClient>(recvQueue, socket.getInputStream(), Messages.ToClient.newBuilder()), "Receiver").start();
		new Thread(new QueueSender<>(this.sendQueue, socket.getOutputStream()), "Sender").start();
	}
	
	public void send(Messages.ToServer serv) throws InterruptedException {
		sendQueue.put(serv);
	}

	public void handleAll(Context context) throws InterruptedException {
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
