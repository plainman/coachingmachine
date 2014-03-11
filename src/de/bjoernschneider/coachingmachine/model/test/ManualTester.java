package de.bjoernschneider.coachingmachine.model.test;

public class ManualTester extends Tester {

	private TestExecuterListener tel;
	
	@SuppressWarnings("unused")
	private ManualTester() {}
	
	public ManualTester(TestCompletionListener tcl, TestExecuterListener tel) {
		super(tcl);
		this.tel=tel;
	}
	
	@Override
	public boolean init() {
		// nothing to do here
		return true;
	}

	@Override
	public boolean start() {
		// nothing to do here
		return true;
	}

	@Override
	public void stop() {
		// nothing to do here
	}

	@Override
	public void test(boolean syncTest) throws InterruptedException {
		if (tel!=null) testResult.setValue(tel.queryTestResult());
		else testResult.setValue(Result.ERROR);
		testCompletionListener.testerComplete(testResult);
	}

	@Override
	public Tester.Kind testerKind() {
		return Kind.MANUAL;
	}

	@Override
	public String testerKindString() {
		return "MANUAL";
	}

}
