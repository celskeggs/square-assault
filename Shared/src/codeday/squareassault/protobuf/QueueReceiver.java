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
	private final T sentinel;

	public QueueReceiver(BlockingQueue<T> recvQueue, InputStream output, MessageLite.Builder builder, T sentinel) {
		this.recvQueue = recvQueue;
		this.input = output;
		this.builder = builder;
		this.sentinel = sentinel;
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
			recvQueue.put(sentinel);
		} catch (InterruptedException e) {
			e.printStackTrace(); // TODO: logging
		}
		System.out.println("receiver ended");
	}
}
