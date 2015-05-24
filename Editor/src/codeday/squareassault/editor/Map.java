package codeday.squareassault.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import codeday.squareassault.protobuf.Messages;

public class Map {

	public final ArrayList<String> names = new ArrayList<>();
	public int[][] cells;

	{
		names.add("tiletest");
		names.add("tiletest2");
	}

	public Map(int w, int h) {
		cells = new int[w][h];
	}

	public void resize(int w, int h) {
		int[][] old = cells;
		cells = new int[w][h];
		for (int x = 0; x < w && x < old.length; x++) {
			for (int y = 0; y < h && y < old[x].length; y++) {
				cells[x][y] = old[x][y];
			}
		}
	}

	public static final String[] commands = new String[] { "LOAD", "SAVE" };

	public void command(int i) {
		JFileChooser chooser;
		switch (i) {
		case 0: // LOAD
			chooser = new JFileChooser(new File("."));
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				try {
					try (FileInputStream fin = new FileInputStream(chooser.getSelectedFile())) {
						loadFrom(Messages.Map.parseFrom(fin));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;
		case 1: // SAVE
			chooser = new JFileChooser(new File("."));
			if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				try {
					Messages.Map out = save();
					try (FileOutputStream fout = new FileOutputStream(chooser.getSelectedFile())) {
						out.writeTo(fout);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;
		}
	}

	private Messages.Map save() {
		Messages.Map.Builder builder = Messages.Map.newBuilder().setWidth(cells.length).setHeight(cells[0].length).addAllTilenames(names);
		for (int y = 0; y < cells[0].length; y++) {
			for (int x = 0; x < cells.length; x++) {
				builder.addCells(cells[x][y]);
			}
		}
		return builder.build();
	}

	private void loadFrom(Messages.Map map) {
		names.clear();
		names.addAll(map.getTilenamesList());
		int width = map.getWidth(), height = map.getHeight();
		cells = new int[width][height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				cells[x][y] = map.getCells(x + y * width);
			}
		}
	}
}
