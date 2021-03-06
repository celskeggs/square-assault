package codeday.squareassault.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.JFileChooser;

import codeday.squareassault.protobuf.Messages;
import codeday.squareassault.protobuf.QueueReceiver;
import codeday.squareassault.protobuf.QueueSender;
import codeday.squareassault.protobuf.SharedConfig;

public class Main implements Runnable {

	private static final long TICK_DELAY = 25;
	private final Socket conn;
	private final InputStream input;
	private final OutputStream output;
	private final int tid;
	private final ServerContext server;

	public Main(ServerContext server, Socket conn, int tid) throws IOException {
		this.server = server;
		this.conn = conn;
		this.tid = tid;
		this.input = conn.getInputStream();
		this.output = conn.getOutputStream();
	}

	public static void main(String[] args) throws IOException {
		File target;
		if (args.length >= 1) {
			target = new File(args[0]);
		} else {
			JFileChooser jfc = new JFileChooser(new File("../Editor"));
			if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				target = jfc.getSelectedFile();
			} else {
				return;
			}
		}
		Messages.Map map;
		try (FileInputStream fin = new FileInputStream(target)) {
			map = Messages.Map.parseFrom(fin);
		}
		final ServerContext server = new ServerContext(map);
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			long count = 0;
			@Override
			public void run() {
				long start = System.nanoTime();
				server.tick();
				long len = System.nanoTime() - start;
				count++;
				if (count % 500 == 0) {
					System.out.println("Completed " + count + " in " + len / 1000000.0 + " ms: " + (len / 1000000.0 / count) + " ms.");
				}
			}
		}, TICK_DELAY, TICK_DELAY);
		ServerSocket sock = new ServerSocket(SharedConfig.PORT);
		//sock.setPerformancePreferences(1, 2, 0);
		try {
			int n = 0;
			while (true) {
				Socket conn = sock.accept();
				conn.setTcpNoDelay(true);
				try {
					new Thread(new Main(server, conn, n), "ClientHandler-" + (n)).start();
				} catch (IOException ex) {
					Logger.warning("Failed to start thread", ex);
				}
				n++;
			}
		} finally {
			sock.close();
		}
	}

	@Override
	public void run() {
		try {
			Messages.Identify ident = Messages.Identify.parseDelimitedFrom(input);
			if (!ident.hasName()) {
				throw new RuntimeException("Failed: client did not provide name.");
			}
			ClientContext context = server.newClient(ident.getName(), Math.min(ident.getProtocol(), ClientContext.NETWORK_PROTOCOL_VERSION));
			Messages.Connect.newBuilder().setProtocol(ClientContext.NETWORK_PROTOCOL_VERSION).setMap(server.getMap()).setObjectID(context.objectID).build().writeDelimitedTo(output);
			new Thread(new QueueSender<>(context.sendQueue, output), "Sender-" + tid).start();
			ArrayBlockingQueue<Messages.ToServer> recvQueue = new ArrayBlockingQueue<>(128);
			Messages.ToServer sentinel = Messages.ToServer.newBuilder().build();
			new Thread(new QueueReceiver<Messages.ToServer>(recvQueue, input, Messages.ToServer.newBuilder(), sentinel), "Receiver-" + tid).start();
			try {
				while (true) {
					Messages.ToServer taken;
					try {
						taken = recvQueue.take();
					} catch (InterruptedException e) {
						Logger.warning("Queue read interrupted", e);
						continue;
					}
					if (taken == sentinel) {
						break;
					}
					context.receiveMessage(taken);
				}
			} finally {
				server.delete(context);
			}
		} catch (IOException e) {
			Logger.warning("Run error'd", e);
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				Logger.warning("Close error'd", e);
			}
			try {
				output.close();
			} catch (IOException e) {
				Logger.warning("Close error'd", e);
			}
			try {
				conn.close();
			} catch (IOException e) {
				Logger.warning("Close error'd", e);
			}
		}
	}
}
