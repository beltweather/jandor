package ui;

import javax.swing.JDialog;

import ui.ProgressBar.ProgressTask;
import ui.pwidget.CloseListener;
import ui.pwidget.JUtil;
import ui.pwidget.PButton;
import ui.pwidget.PPanel;

public abstract class ProgressDialog extends PPanel implements CloseListener {
	
	private String title;
	private String buttonText;
	private ProgressBar progressBar;
	private JDialog innerDialog = null;
	
	public ProgressDialog(String title) {
		this(title, null);
	}
		
	public ProgressDialog(String title, String buttonText) {
		this.title = title;
		this.buttonText = buttonText == null ? "" : buttonText;
		init();
	}
	
	public ProgressBar getProgressBar() {
		return progressBar;
	}
	
	private void init() {
		PButton startButton = new PButton(buttonText);
		progressBar = new ProgressBar(startButton) {
	
			@Override
			public void run(ProgressTask task) {
				ProgressDialog.this.run(task);
			}
	
			@Override
			public void finished(ProgressTask task) {
				ProgressDialog.this.finished(task);
			}
			
		};
		
		PPanel p = new PPanel();
		c.insets(0,0,10,0);
		addc(progressBar);
		c.gridy++;
		addc(startButton);
		
		if(buttonText.isEmpty()) {
			startButton.setVisible(false);
		}
	}
	
	public abstract void run(ProgressTask task);
	
	public abstract void finished(ProgressTask task);
	
	public JDialog showDialog() {
		innerDialog = JUtil.buildBlankDialog(null, title, this);
		if(buttonText.isEmpty()) {
			progressBar.start();
		}
		innerDialog.setVisible(true);
		return innerDialog;
	}
	
	public void closeDialog() {
		if(innerDialog != null) {
			innerDialog.setVisible(false);
		}
	}

	@Override
	public void handleClosed() {
		
	}
}
