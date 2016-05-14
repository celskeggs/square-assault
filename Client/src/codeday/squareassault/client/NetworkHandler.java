package codeday.squareassault.client;

import codeday.squareassault.protobuf.Messages.ChatMessage;
import codeday.squareassault.protobuf.Messages.Disconnect;
import codeday.squareassault.protobuf.Messages.SetPosition;
import codeday.squareassault.protobuf.Messages.TurretCount;

public class NetworkHandler {

	private final Context context;

	public NetworkHandler(Context context) {
		this.context = context;
	}

	public void handleDisconnect(Disconnect disconnect) {
		context.handleDisconnect(disconnect);
	}

	public void handleSetPosition(SetPosition position) {
		context.handleSetPosition(position);
	}

	public void handleTurretCount(TurretCount count) {
		context.handleTurretCount(count);
	}

	public void handleChat(ChatMessage chat) {
		context.handleChat(chat);
	}

}
