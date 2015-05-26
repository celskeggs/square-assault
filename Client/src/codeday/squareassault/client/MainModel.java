package codeday.squareassault.client;

import codeday.squareassault.protobuf.NewMessages.Model.Builder;

public class MainModel {

	public final Builder model;
	public int dx, dy;
	public int shiftX, shiftY;
	public final StringBuffer newMessage = new StringBuffer();

	public MainModel(Builder newBuilder) {
		this.model = newBuilder;
	}
}
