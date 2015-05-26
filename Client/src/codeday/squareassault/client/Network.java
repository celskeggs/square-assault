package codeday.squareassault.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import codeday.squareassault.protobuf.DiffEngine;
import codeday.squareassault.protobuf.NewMessages;
import codeday.squareassault.protobuf.QueueReceiver;
import codeday.squareassault.protobuf.QueueSender;
import codeday.squareassault.protobuf.SharedConfig;

public class Network {

	public static final int NETWORK_PROTOCOL_VERSION = 1;

	private final Socket socket;
	private final LinkedBlockingQueue<NewMessages.Model> sendQueue = new LinkedBlockingQueue<>();
	private final LinkedBlockingQueue<NewMessages.Model> recvQueue = new LinkedBlockingQueue<>();
	private final MainModel status;
	private NewMessages.Model lastReceived;
	private final int protocol;

	private static final NewMessages.Model sentinel = NewMessages.Model.newBuilder().build();

	public Network(String server, String username) throws UnknownHostException, IOException {
		socket = new Socket(InetAddress.getByName(server), SharedConfig.PORT);
		socket.setTcpNoDelay(true);
		InputStream input = socket.getInputStream();
		OutputStream output = socket.getOutputStream();
		System.out.println("Ready");
		NewMessages.Identify.newBuilder().setName(username).setProtocol(NETWORK_PROTOCOL_VERSION).build().writeDelimitedTo(output);
		System.out.println("Sent");
		this.status = new MainModel(NewMessages.Model.newBuilder());
		status.model.mergeDelimitedFrom(input);
		if (status == null) {
			throw new IOException("No connection message!");
		}
		protocol = Math.min(status.model.getProtocol(), NETWORK_PROTOCOL_VERSION);
		if (protocol != 1) {
			throw new IOException("Server does not support same protocol version!");
		}
		if (!status.model.hasMap()) {
			throw new IOException("Server did not send map!");
		}
		System.out.println("Got");
		new Thread(new QueueReceiver<>(recvQueue, input, NewMessages.Model.newBuilder(), sentinel), "Receiver").start();
		new Thread(new QueueSender<>(this.sendQueue, output), "Sender").start();
	}

	public MainModel getNetworkedModel() {
		return status;
	}

	public void handleAll() throws InterruptedException {
		HashMap<Integer, NewMessages.Entity.Builder> found = new HashMap<>();
		ArrayList<NewMessages.Entity.Builder> toRemove = new ArrayList<>();
		while (true) {
			NewMessages.Model taken = recvQueue.take();
			if (taken == sentinel) {
				break;
			}
			synchronized (status) {
				if (status.model.hasMap() && taken.hasMap()) {
					status.model.clearMap();
				}
				status.model.mergeFrom(taken);
				found.clear();
				for (NewMessages.Entity.Builder ent : status.model.getEntityBuilderList()) {
					if (found.containsKey(ent.getId())) {
						if (ent.getType() == NewMessages.EntityType.NONEXISTENT) {
							toRemove.add(found.get(ent.getId()));
						} else {
							found.get(ent.getId()).mergeFrom(ent.build());
						}
						toRemove.add(ent);
					} else {
						found.put(ent.getId(), ent);
					}
				}
				for (NewMessages.Entity.Builder ent : toRemove) {
					status.model.removeEntity(status.model.getEntityBuilderList().indexOf(ent));
				}
				lastReceived = status.model.build();
			}
		}
	}

	public void synch() {
		synchronized (status) {
			NewMessages.Model model = DiffEngine.diff(lastReceived, status.model);
			if (model != null) {
				sendQueue.add(model);
			}
		}
	}
}
