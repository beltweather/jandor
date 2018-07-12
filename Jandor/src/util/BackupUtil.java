package util;

import java.util.List;

import javax.swing.JDialog;

import session.Session;
import session.User;
import ui.ProgressBar;
import ui.pwidget.JUtil;
import ui.pwidget.PButton;
import ui.pwidget.PPanel;

public class BackupUtil {

	private BackupUtil() {}
	
	private static JDialog backupDialog = null;
	
	public static void backup() {
		User user = LoginUtil.getUser();
		final String backupFolderName = DriveUtil.toBackupFolderName(user);
		if(DriveUtil.exists(backupFolderName)) {
			if(!JUtil.showConfirmDialog(null, "Overwrite Previous Backup", "A backup already exists, do you want to overwrite it?")) {
				return;
			}
			DriveUtil.deleteFile(DriveUtil.toBackupFolderId(user));
		}
		
		PButton startButton = new PButton("Backup");
		ProgressBar progressBar = new ProgressBar(startButton) {

			private int deckCount = 0;
			private int success = 0;
			
			@Override
			public void run(ProgressTask task) {
				String backupFolderId = DriveUtil.createFolder(backupFolderName, DriveUtil.FOLDER_ID_BACKUPS);
				List<Integer> deckIds = Session.getInstance().getDeckIds();
				for(int deckId : deckIds) {
					if(MailUtil.sendToDriveBackup(backupFolderId, IDUtil.PREFIX_DECK, MailUtil.toDeckXML(deckId))) {
						success++;
					}
					deckCount++;
					task.setWorkerProgress(deckCount, deckIds.size());
				}
			}

			@Override
			public void finished(ProgressTask task) {
				if(backupDialog != null) {
					backupDialog.setVisible(false);
					backupDialog.dispose();
					backupDialog = null;
				}
				JUtil.showMessageDialog(null, "Backup Decks to Cloud", "Successfully backed up " + success + " / " + deckCount + " decks.");
			}
			
		};
		
		PPanel p = new PPanel();
		p.c.insets(0,0,10,0);
		p.addc(progressBar);
		p.c.gridy++;
		p.addc(startButton);
		
		backupDialog = JUtil.buildBlankDialog(null, "Backup Decks to Cloud", p);
		backupDialog.setVisible(true);
	}
	
	public static void restore() {
		User user = LoginUtil.getUser();
		String backupFolderName = DriveUtil.toBackupFolderName(user);
		if(!DriveUtil.exists(backupFolderName)) {
			JUtil.showMessageDialog(null, "Restore", "There is no backup to restore from.");
			return;
		}
		
		PButton startButton = new PButton("Restore");
		ProgressBar progressBar = new ProgressBar(startButton) {

			private int deckCount = 0;
			
			@Override
			public void run(ProgressTask task) {
				deckCount = MailUtil.receiveFilesFromBackupFolder(task);
			}

			@Override
			public void finished(ProgressTask task) {
				if(backupDialog != null) {
					backupDialog.setVisible(false);
					backupDialog.dispose();
					backupDialog = null;
				}
				JUtil.showMessageDialog(null, "Restore Decks from Cloud", "Successfully restored " + deckCount + " decks.");
			}
			
		};
		
		PPanel p = new PPanel();
		p.c.insets(0,0,10,0);
		p.addc(progressBar);
		p.c.gridy++;
		p.addc(startButton);
		
		backupDialog = JUtil.buildBlankDialog(null, "Restore Decks from Cloud", p);
		backupDialog.setVisible(true);
	}
	
}
