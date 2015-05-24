package codeday.squareassault.protobuf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;

import com.google.protobuf.MessageLite;

public class QueueSender<T extends MessageLite> implements Runnable {
	private final OutputStream output;
	private final BlockingQueue<T> sendQueue;

	public QueueSender(BlockingQueue<T> sendQueue, OutputStream output) {
		this.sendQueue = sendQueue;
		this.output = output;
	}

	@Override
	public void run() {
		try {
			while (true) {
				sendQueue.take().writeDelimitedTo(output);
			}
		} catch (InterruptedException | IOException e) {
			e.printStackTrace(); // TODO: logging
		}
	}
}
