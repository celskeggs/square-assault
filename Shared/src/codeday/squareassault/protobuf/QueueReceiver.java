package codeday.squareassault.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLite.Builder;

public class QueueReceiver<T extends MessageLite> implements Runnable {
	private final InputStream input;
	private final BlockingQueue<T> recvQueue;
	private final Builder builder;

	public QueueReceiver(BlockingQueue<T> recvQueue, InputStream output, MessageLite.Builder builder) {
		this.recvQueue = recvQueue;
		this.input = output;
		this.builder = builder;
	}

	@Override
	public void run() {
		try {
			while (true) {
				builder.clear();
				if (!builder.mergeDelimitedFrom(input)) {
					break;
				}
				recvQueue.put((T) builder.build()); // TODO: find a better solution than just casting
			}
		} catch (InterruptedException | IOException e) {
			e.printStackTrace(); // TODO: logging
		}
		try {
			recvQueue.put(null); // TODO: this doesn't work
		} catch (InterruptedException e) {
			e.printStackTrace(); // TODO: logging
		}
		System.out.println("receiver ended");
	}
}
