package codeday.squareassault.server;

import codeday.squareassault.protobuf.Messages.ObjectType;

public class TurretContext extends ObjectContext {

	private int playerID;
	
	public TurretContext(ServerContext server, int x, int y, int playerID) {
		super(server, x, y);
		this.playerID = playerID;
	}

	@Override
	protected ObjectType getType() {
		return ObjectType.TURRET;
	}

	@Override
	public void tick() {
		if (server.getObject(playerID) == null) {
			server.delete(this);
		}
	}

	@Override
	protected String getIcon() {
		return "turret";
	}
}
