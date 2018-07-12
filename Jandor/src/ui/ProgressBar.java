package ui;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

public abstract class ProgressBar extends JProgressBar implements ActionListener, PropertyChangeListener {

	private JButton startButton;
	private ProgressTask task;
	
	public ProgressBar(JButton startButton) {
		super(0, 100);
		this.startButton = startButton;
		init();
	}
	
	private void init() {
		startButton.addActionListener(this);
		setValue(0);
		setStringPainted(true);
	}
	
	public abstract void run(ProgressTask task);
	
	public abstract void finished(ProgressTask task);

	public class ProgressTask extends SwingWorker<Void, Void> {
		
		private ProgressBar progressPanel;
		
		public ProgressTask(ProgressBar progressPanel) {
			this.progressPanel = progressPanel;
		}
		
		@Override
		public Void doInBackground() {
			setValue(0);
			progressPanel.run(this);
			setProgress(100);
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					progressPanel.finished(ProgressTask.this);
				}
			});
			return null;
		}
		
		private int toProgress(int numerator, int denominator) {
			return (int) Math.floor((numerator / (double) denominator) * 100);
		}
		
		public void setWorkerProgress(int numerator, int denominator) {
			setWorkerProgress(toProgress(numerator, denominator));
		}
		
		public void setWorkerProgress(int progress) {
			if(progress == getProgress()) {
				return;
			}
			
			if(progress < 0) {
				setProgress(0);
			} else if(progress > 100) {
				setProgress(100);
			} else {
				setProgress(progress);
			}
		}

		@Override
		public void done() {
			try {
				get();
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				Toolkit.getDefaultToolkit().beep();
				startButton.setEnabled(true);
				setCursor(null);
				startButton.setCursor(null);
			}
		}
	}
	
	public void trigger() {
		actionPerformed(null);
	}

	public void actionPerformed(ActionEvent evt) {
		start();
	}
	
	public void start() {
		startButton.setEnabled(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		startButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		task = new ProgressTask(this);
		task.addPropertyChangeListener(this);
		task.execute();
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			setValue(progress);
		} 
	}
	
}
