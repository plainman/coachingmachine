package de.bjoernschneider.coachingmachine.model.test;

public class TextDB {
	
	private static class TextEntry {
		String text;
		int delay;
		private TextEntry(String text, int delay) {
			this.text=text;
			this.delay=delay;
		}
	}
	
	private static TextEntry[] texts = {
	/*000*/	new TextEntry("Nichts zu sprechen", 0),
			new TextEntry("Und wenn Du daran denkst, dann macht das", 0),
			new TextEntry("Denk nochmal an deine Situation. Und das macht dann", 0),
			new TextEntry("Eemotionalen Stress", 739),
			new TextEntry("Körperlichen Stress", 316),
	/*005*/ new TextEntry("Und dabei fühlst du", 0),
			new TextEntry("Fühl nochmal in dich hinein. Du fühlst", 0),
			new TextEntry("Angst", 458),
			new TextEntry("Wut", 350),
			new TextEntry("Trauer", 361),
	/*010*/	new TextEntry("Hilflosigkeit", 797),
			new TextEntry("Ekel", 367),
			new TextEntry("Scham", 456),
			new TextEntry("Schock", 356),
			new TextEntry("Überraschung", 632),
	/*015*/	new TextEntry("Schuld", 405),
			new TextEntry("Nicht-Fühlen", 894),
			new TextEntry("Verlust", 598),
			new TextEntry("Ausgeliefertsein", 680),
			new TextEntry("Schmerz", 625),
	/*020*/	new TextEntry("Erschöpfung", 477),
			new TextEntry("Krankheit", 366),
			new TextEntry("Müdigkeit", 532),
			new TextEntry("Überanstrengung", 847),
			new TextEntry("Verletzung", 544),
	/*025*/	new TextEntry("Hunger", 297),
			new TextEntry("Atemnot", 658),
			new TextEntry("Durst", 387),
			new TextEntry("Infektion", 515),
			new TextEntry("Temperatur", 701),
	/*030*/	new TextEntry("Und dieser Stress und dieses Gefühl liegt in der", 0),
			new TextEntry("Welcher Zeitpunkt passt zu dem Gefühl? Es ist die", 0),
			new TextEntry("Gegenwart", 578),
			new TextEntry("Vergangenheit", 466),
			new TextEntry("Zukunft", 540),
	/*035*/	new TextEntry("Der Stress liegt in der Vergangenheit. Genauer gesagt in der", 0),
			new TextEntry("Hauptsächlich liegt der Stress in der", 0),
			new TextEntry("Kindheit", 288),
			new TextEntry("Jugend", 462),
			new TextEntry("Erwachsenenleben", 540),
	/*040*/	new TextEntry("Dein Alter als der Stress auftratt, war", 0),
			new TextEntry("Du warst im Alter", 0),
			new TextEntry("Null bis vier Jahre", 908),
			new TextEntry("fünf bis neun Jahre", 1058),
			new TextEntry("vor der Geburt", 924),
	/*045*/	new TextEntry("älter oder gleich zehn Jahre", 1500),
			new TextEntry("zehn bis vierzehn Jahre", 1161),
			new TextEntry("fünfzehn bis neunzehn Jahre", 1662),
			new TextEntry("jünger als zehn Jahre", 1161),
			new TextEntry("älter oder gleich zwanzig Jahre", 1714),
	/*050*/	new TextEntry("älter oder gleich achtzehn Jahre", 1929),
			new TextEntry("älter oder gleich zwanzig Jahre", 2025),
			new TextEntry("älter oder gleich fünfundzwanzig Jahre", 2388),
			new TextEntry("älter oder gleich dreissig Jahre", 1813),
			new TextEntry("älter oder gleich fünfunddreissig Jahre", 2222),
	/*055*/	new TextEntry("älter oder gleich vierzig Jahre", 1785),	
			new TextEntry("Der Stress hat zu tun mit", 0),
			new TextEntry("Der Stress ist aufgetreten im Kontext von", 0),
			new TextEntry("Schule", 332),
			new TextEntry("Familie", 569),
	/*060*/	new TextEntry("Freizeit", 721),
			new TextEntry("Studium", 643),
			new TextEntry("Beziehung", 424),
			new TextEntry("Hobby", 340),
			new TextEntry("Beim Stress handelt es sich um", 0),
	/*065*/	new TextEntry("Bei den auftretenen Gefühlen handelt es sich um", 0),
			new TextEntry("die eigenen Gefühle", 567),
			new TextEntry("die Gefühle anderer", 1065),
			new TextEntry("ein höheres System", 1161),
			new TextEntry("Stell dir deine problematische Situation vor. Fühl in deinen Körper hinein und bleib die ganze Zeit bei dieser Vorstellung und diesem Gefühl.", 0),
	/*070*/ new TextEntry("Jetzt wissen wir genug", 1260),
			new TextEntry("Bitte denk weiter an deine problematische Situation und folge den Geräuschen", 0),
			new TextEntry("Die Situation ist erst mal okee so", 1815),
			new TextEntry("Die Situation ist noch nicht okee, wir machen weiter.", 0),
			new TextEntry("Da ist noch etwas dahinter", 655),
	/*075*/ new TextEntry("Da ist noch weiterer Stress in dieser Situation, wir machen weiter.", 0),
			new TextEntry("Da ist noch mehr Stress.", 722),
			new TextEntry("Damit sind wir erst mal fertig. Vielen Dank!", 0),
			new TextEntry("Bitte klicke auf Weiter, wenn du bereit sind", 0),
			new TextEntry("Dein Name ist Fritz", 1404),
	/*080*/	new TextEntry("Dein Name ist Friederike", 1649),
			new TextEntry("Dein Name ist Björn", 1309),
			new TextEntry("Dein Name ist Hannelore", 1532),
			new TextEntry("Die Pizza ist zu heiss zum Fliegen", 2218),
			new TextEntry("Die Pizza ist zu heiss zum Essen", 1788),
	/*085*/	new TextEntry("Sabine trinkt ein Glas Beton", 1950),
			new TextEntry("Sabine trinkt ein Glas Tee", 1858),
			new TextEntry("Dein Name ist Phillip", 1428),
	        new TextEntry("Dein Name ist Sophie", 1521),
			new TextEntry("Mit der Playstation kann man spielen ", 2241),
	/*090*/	new TextEntry("Mit der Playstation kann man essen ", 1885),
			new TextEntry("Bitte hör dem folgenden Satz ganz genau zu", 0),
			new TextEntry("Bitte mit konstantem Druck halten und nachgeben, wenn es nicht mehr zu halten ist", 0)

			
	};
	
	public static boolean isValidTextID(int id) {
		if (id>=0&&id<texts.length) return true;
		return false;
	}
	public static String getText(int id) {
		if (isValidTextID(id)) return texts[id].text;
		return "Falsche TextID";
	}
	public static int getDelay(int id) {
		if (isValidTextID(id)) return texts[id].delay;
		return 0;
	}
	

}
