package com.example.newplayer;

import java.util.ArrayList;
import java.util.Random;

public class Randomizer {

	public class Entry implements Comparable<Entry>{
		long value;
		long id;
		
		@Override
		public int compareTo(Entry another) {
			return (int) (value - another.value);
		}
	}
	
	private ArrayList<Entry> entries;
	private Random random = new Random();
	
	public Randomizer(ArrayList<Entry> e) {
		entries = e;
	}
	
	private void prepareSequence() {
		
	}
}
