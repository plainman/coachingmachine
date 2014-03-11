package de.bjoernschneider.coachingmachine.model.test;

public abstract class Tester {
	
	public enum Kind {
		MANUAL, MUSCLE, MINDWAVE, OPENEEG
	}
	
	@Override
	public String toString() {
		return testerKindString()+" Tester";
	}

	public abstract String testerKindString();
	public abstract Kind testerKind();

	public enum Result {
		STRONG, WEAK, ERROR
	}
	public class ResultValue {
		private Result r;
		private long rawData;
		public ResultValue() { r=Result.ERROR; }
		public ResultValue(Result result) { r=result; }
		public void setValue(Result result) { r=result; }
		public Result getValue()   { return r; }
		public boolean isWeak()   { return r==Result.WEAK?true:false; }
		public boolean isStrong() { return r==Result.STRONG?true:false; }
		public boolean isError()  { return r==Result.ERROR?true:false; }
		public boolean isOk()     { return !isError(); }
		public long getRawData() { return rawData; }
		public void setRawData(long rawData) { this.rawData = rawData; }
	}
	protected ResultValue testResult;
	protected TestCompletionListener testCompletionListener;
	
	protected Tester() {}
	
	public Tester(TestCompletionListener tcl) {
		testCompletionListener=tcl;
		testResult=new ResultValue(Result.ERROR);
	}

	public abstract boolean init();

	public abstract boolean start();
	
	public abstract void test(boolean syncTest) throws InterruptedException;
	
	public abstract void stop();
		
}
