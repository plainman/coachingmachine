package de.bjoernschneider.coachingmachine.model.test;

import java.util.ArrayList;

public class StressDB {
	
	public class StressEntry {
		private int level;
		private int textID;
		private int prio;
		private StressEntry parentStressEntry;
		public StressEntry(int level, int stringID, int prio, StressEntry parentStressEntry) {
			this.level=level;
			this.textID=stringID;
			this.prio=prio;
			this.parentStressEntry=parentStressEntry;
		}
		public int getLevel() { return level; }
		public int getTextID() { return textID; }
		public String getText() { return TextDB.getText(textID); }
		public int getPrio() { return prio; }
		public StressEntry getParentStressEntry() { return parentStressEntry; }
	}
	
	private ArrayList<StressEntry> stressList;
	private ArrayList<Integer> startIndex;
	private ArrayList<Integer> endIndex;
	private int levels;
	private ArrayList<Integer> introText; 
	private ArrayList<Integer> repeatText; 
	
	public StressDB() {
		stressList = new ArrayList<StressDB.StressEntry>();
		startIndex = new ArrayList<Integer>();
		endIndex = new ArrayList<Integer>();
		introText = new ArrayList<Integer>();
		repeatText = new ArrayList<Integer>();
		
		levels=0; startIndex.add(stressList.size()); //001-004
		introText.add(1);
		repeatText.add(2);
		StressEntry emo = new StressEntry(levels, 3, 1, null); stressList.add(emo);
		StressEntry body = new StressEntry(levels, 4, 1, null); stressList.add(body);
		endIndex.add(stressList.size());
		
		levels++; startIndex.add(stressList.size()); //005-029
		introText.add(5);
		repeatText.add(6);
		stressList.add(new StressEntry(levels, 7, 1, emo));
		stressList.add(new StressEntry(levels, 8, 1, emo));
		stressList.add(new StressEntry(levels, 9, 1, emo));
		stressList.add(new StressEntry(levels, 10, 2, emo));
		stressList.add(new StressEntry(levels, 11, 2, emo));
		stressList.add(new StressEntry(levels, 12, 2, emo));
		stressList.add(new StressEntry(levels, 13, 2, emo));
		stressList.add(new StressEntry(levels, 14, 3, emo));
		stressList.add(new StressEntry(levels, 15, 3, emo));
		stressList.add(new StressEntry(levels, 16, 3, emo));
		stressList.add(new StressEntry(levels, 17, 4, emo));
		stressList.add(new StressEntry(levels, 18, 5, emo));
		stressList.add(new StressEntry(levels, 19, 1, body));
		stressList.add(new StressEntry(levels, 20, 1, body));
		stressList.add(new StressEntry(levels, 21, 1, body));
		stressList.add(new StressEntry(levels, 22, 2, body));
		stressList.add(new StressEntry(levels, 23, 2, body));
		stressList.add(new StressEntry(levels, 24, 3, body));
		stressList.add(new StressEntry(levels, 25, 3, body));
		stressList.add(new StressEntry(levels, 26, 3, body));
		stressList.add(new StressEntry(levels, 27, 4, body));
		stressList.add(new StressEntry(levels, 28, 5, body));
		stressList.add(new StressEntry(levels, 29, 5, body));	
		endIndex.add(stressList.size());

		levels++; startIndex.add(stressList.size()); //030-034
		introText.add(30);
		repeatText.add(31);
		stressList.add(new StressEntry(levels, 32, 1, null));
		StressEntry past=new StressEntry(levels, 33, 1, null); stressList.add(past);
		stressList.add(new StressEntry(levels, 34, 1, null));
		endIndex.add(stressList.size());
		
		levels++; startIndex.add(stressList.size()); //035-039
		introText.add(35);
		repeatText.add(36);
		StressEntry se1=new StressEntry(levels, 37, 1, past); stressList.add(se1);
		StressEntry se2=new StressEntry(levels, 38, 2, past); stressList.add(se2);
		StressEntry se3=new StressEntry(levels, 39, 3, past); stressList.add(se3);
		endIndex.add(stressList.size());

		levels++; startIndex.add(stressList.size()); //040-055
		introText.add(40);
		repeatText.add(41);
		stressList.add(new StressEntry(levels, 42, 1, se1));
		stressList.add(new StressEntry(levels, 43, 2, se1));
		stressList.add(new StressEntry(levels, 44, 3, se1));
		stressList.add(new StressEntry(levels, 45, 4, se1));
		stressList.add(new StressEntry(levels, 46, 1, se2));
		stressList.add(new StressEntry(levels, 47, 2, se2));
		stressList.add(new StressEntry(levels, 48, 3, se2));
		stressList.add(new StressEntry(levels, 49, 4, se2));
		stressList.add(new StressEntry(levels, 50, 1, se3));
		stressList.add(new StressEntry(levels, 51, 2, se3));
		stressList.add(new StressEntry(levels, 52, 3, se3));
		stressList.add(new StressEntry(levels, 53, 4, se3));
		stressList.add(new StressEntry(levels, 54, 5, se3));
		stressList.add(new StressEntry(levels, 55, 6, se3));
		endIndex.add(stressList.size());

		levels++; startIndex.add(stressList.size()); //056-063
		introText.add(56);
		repeatText.add(57);
		stressList.add(new StressEntry(levels, 58, 1, null));
		stressList.add(new StressEntry(levels, 59, 1, null));
		stressList.add(new StressEntry(levels, 60, 1, null));
		stressList.add(new StressEntry(levels, 61, 2, null));
		stressList.add(new StressEntry(levels, 62, 2, null));
		stressList.add(new StressEntry(levels, 63, 2, null));
		endIndex.add(stressList.size());

		levels++; startIndex.add(stressList.size()); //064-068
		introText.add(64);
		repeatText.add(65);
		stressList.add(new StressEntry(levels, 66, 1, null));
		stressList.add(new StressEntry(levels, 67, 1, null));
		stressList.add(new StressEntry(levels, 68, 2, null));
		endIndex.add(stressList.size());
		
		levels++; // now amount of levels = 0..levels-1
	}
	
	public StressEntry getStressEntry(int index) {
		return stressList.get(index);
	}
	
	public void reSortStressLevel(int level) {
		int start=startIndex.get(level);
		int end=endIndex.get(level);
		int size=end-start;
		// swap some entries
		if (size>2) {
			for (int i=0; i<size*(int)(Math.random()*5+1); i++) {
				int first = (int)(Math.random()*(end-start)+start);
				int second = (int)(Math.random()*(end-start)+start);
				StressEntry swap = stressList.get(first);
				stressList.set(first, stressList.get(second));
				stressList.set(second, swap);
			}
		}
	}
	public int getLevelCount() {
		return levels;
	}
	public int getPrioCount(int level) {
		int highest=0;
		for (int i=startIndex.get(level); i<endIndex.get(level); i++) {
			if (stressList.get(i).getPrio()>highest) highest=stressList.get(i).getPrio();
		}
		return highest;
	}
	
	public int getStressStart(int level) {
		return startIndex.get(level);
	}
	public int getStressEnd(int level) {
		return endIndex.get(level);
	}
	public int getIntroTextID(int level) {
		return introText.get(level);
	}
	public int getRepeatTextID(int level) {
		return repeatText.get(level);
	}
	
}
