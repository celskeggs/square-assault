package codeday.squareassault.client;

import codeday.squareassault.protobuf.Messages;
import codeday.squareassault.protobuf.Messages.Connect;
import codeday.squareassault.protobuf.Messages.LineMessage;
import codeday.squareassault.protobuf.Messages.Map;

public class Context {

	public String activeString = "Hello, World!";
	private final Network net;

	public Context(Network net) {
		this.net = net;
	}

	public void handleLineMessage(LineMessage line) {
		activeString = line.getText();
	}

	public void handleKey(int keyCode, char keyChar) throws InterruptedException {
		if (keyChar == ' ') {
			System.out.println("Sending...");
			net.send(Messages.ToServer.newBuilder().setEcho(Messages.EchoMessage.newBuilder().setText("Ping!").build()).build());
		}
	}

	public void handleMap(Map map) {
		int width = map.getWidth();
		int height = map.getHeight();
		
	}

	public void handleConnectInfo(Connect info) {
		if (info.hasMap()) {
			handleMap(info.getMap());
		}
	}
}
