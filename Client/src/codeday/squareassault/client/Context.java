package codeday.squareassault.client;

import java.awt.image.BufferedImage;

import codeday.squareassault.protobuf.Messages;
import codeday.squareassault.protobuf.Messages.Connect;
import codeday.squareassault.protobuf.Messages.LineMessage;
import codeday.squareassault.protobuf.Messages.Map;

public class Context {

	public String activeString = "Hello, World!";
	private final Network net;
	public String[] cells;
	public int[][] backgroundImages;

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
		backgroundImages = new int[width][height];
		cells = map.getTilenamesList().toArray(new String[map.getTilenamesCount()]);
		for (int x=0; x<width; x++){
			for (int y=0; y<height; y++){
				backgroundImages[x][y] = map.getCells(x + y * width);
			}
		}
	}

	public void handleConnectInfo(Connect info) {
		if (info.hasMap()) {
			handleMap(info.getMap());
		}
	}
}
