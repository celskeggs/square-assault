package codeday.squareassault.server;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import codeday.squareassault.protobuf.Messages;
import codeday.squareassault.protobuf.Messages.Map;
import codeday.squareassault.protobuf.Messages.ToClient;

public class ServerContext {

	private final ArrayList<ClientContext> clients = new ArrayList<>();

	public ClientContext newClient(LinkedBlockingQueue<ToClient> sendQueue, String name) {
		return new ClientContext(this, sendQueue, name);
	}

	public synchronized void register(ClientContext context) {
		this.clients.add(context);
	}

	public synchronized void removeClient(ClientContext context) {
		this.clients.remove(context);
	}

	public synchronized void tick() {
		
	}

	public Map getMap() {
		Map.Builder builder = Messages.Map.newBuilder().setWidth(5).setHeight(5).addTilenames("tiletest").addTilenames("tiletest2");
		for (int x=0; x<5; x++) {
			for (int y=0; y<5; y++) {
				builder.addCells((x + y) % 2);
			}
		}
		return builder.build();
	}

}
