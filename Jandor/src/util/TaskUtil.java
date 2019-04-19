package util;

import javax.swing.SwingWorker;

public class TaskUtil {

	private TaskUtil() {}

	public static interface Task {

		public void run();

	}

	public static void run(Task task) {
		if(task == null) {
			return;
		}
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				task.run();
				return null;
			}

		};
		worker.execute();
	}

}
