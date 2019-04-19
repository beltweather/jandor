package util;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class TaskUtil {

	private TaskUtil() {}

	// Usage: run(() -> { ... do something ... })
	public static void run(Runnable runnable) {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				runnable.run();
				return null;
			}

		};
		worker.execute();
	}

	public static void runSwing(Runnable runnable) {
		SwingUtilities.invokeLater(runnable);
	}

}
