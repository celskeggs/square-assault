package codeday.squareassault.server;

import java.util.concurrent.LinkedBlockingQueue;

import codeday.squareassault.protobuf.Messages.EchoMessage;
import codeday.squareassault.protobuf.Messages.LineMessage;
import codeday.squareassault.protobuf.Messages.ToClient;
import codeday.squareassault.protobuf.Messages.ToServer;

public class ClientContext {

	public final ServerContext server;
	private final LinkedBlockingQueue<ToClient> sendQueue;
	public final String name;

	public ClientContext(ServerContext serverContext, LinkedBlockingQueue<ToClient> sendQueue, String name) {
		this.server = serverContext;
		this.sendQueue = sendQueue;
		this.name = name;
		serverContext.register(this);
	}

	public void receiveMessage(ToServer taken) {
		if (taken.hasEcho()) {
			handleEchoMessage(taken.getEcho());
		} else {
			System.out.println("Bad message: " + taken); // TODO: logging
		}
	}

	private void handleEchoMessage(EchoMessage echo) {
		System.out.println("Echoing: " + echo.getText()); // TODO: logging
		sendQueue.add(ToClient.newBuilder().setLine(LineMessage.newBuilder().setText(echo.getText())).build());
	}
}
