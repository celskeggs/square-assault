package codeday.squareassault.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import codeday.squareassault.protobuf.Messages;

public class Map {

	public final ArrayList<String> names = new ArrayList<>();
	public int[][] cells;
	public int spawnX = 100, spawnY = 100;

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

	public static final String[] commands = new String[] { "LOAD", "SAVE", "ADD", "REMOVE" };

	public void command(int i) {
		JFileChooser chooser;
		String icon;
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
		case 2: // ADD ICON
			icon = JOptionPane.showInputDialog("Enter Icon to Add");
			if (icon != null && !names.contains(icon)) {
				names.add(icon);
			}
			break;
		case 3: // REMOVE ICON
			icon = JOptionPane.showInputDialog("Enter Icon to Remove", names);
			if (icon != null && names.contains(icon)) {
				int ind = names.indexOf(icon);
				names.set(ind, names.remove(names.size() - 1));
				remap(names.size(), ind);
			}
			break;
		}
	}

	private void remap(int old, int updated) {
		for (int x=0; x<cells.length; x++) {
			for (int y=0; y<cells[x].length; y++) {
				if (cells[x][y] == old) {
					cells[x][y] = updated;
				}
			}
		}
	}

	private Messages.Map save() {
		Messages.Map.Builder builder = Messages.Map.newBuilder().setWidth(cells.length).setHeight(cells[0].length).addAllTilenames(names).setSpawnX(spawnX).setSpawnY(spawnY);
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
		spawnX = map.getSpawnX();
		spawnY = map.getSpawnY();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				cells[x][y] = map.getCells(x + y * width);
			}
		}
	}
}
