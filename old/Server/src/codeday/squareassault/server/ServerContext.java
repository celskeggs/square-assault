package codeday.squareassault.server;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import codeday.squareassault.protobuf.Messages.ToClient;

public class ServerContext {

	private final ArrayList<ClientContext> clients = new ArrayList<>();

	public ClientContext newClient(LinkedBlockingQueue<ToClient> sendQueue) {
		return new ClientContext(this, sendQueue);
	}

	public synchronized void register(ClientContext context) {
		this.clients.add(context);
	}

	public synchronized void removeClient(ClientContext context) {
		this.clients.remove(context);
	}

	public synchronized void tick() {
		
	}

}
