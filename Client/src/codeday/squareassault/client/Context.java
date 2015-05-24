package codeday.squareassault.client;

import java.awt.image.BufferedImage;

import codeday.squareassault.protobuf.Messages;
import codeday.squareassault.protobuf.Messages.LineMessage;

public class Context {

	public String activeString = "Hello, World!";
	private final Network net;
	public BufferedImage[][] backgroundImages = new BufferedImage[10][10];

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
}
