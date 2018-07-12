package drive;

import sheets.domain.UserSheet;

public class SheetTest {

	private SheetTest() {}
	
	public static void testReadWriteSpeed() {
		UserSheet sheet = new UserSheet(false);
		long timeStart = System.currentTimeMillis();
		sheet.read();
		long timeRead = System.currentTimeMillis();
		System.out.println("Time read: " + (timeRead - timeStart));
		sheet.write();
		long timeWrite = System.currentTimeMillis();
		System.out.println("Time write: " + (timeWrite - timeRead));
		sheet.read();
		long timeReadAgain = System.currentTimeMillis();
		System.out.println("Time read: " + (timeReadAgain - timeWrite));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		for(int i = 0; i < 60; i++) {
			testReadWriteSpeed();
		}
		System.out.println("Time total: " + (System.currentTimeMillis() - time));
	}
	
}
