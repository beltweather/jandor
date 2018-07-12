package editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import session.Session;
import session.Tag;
import ui.GlassPane;
import ui.JandorButton;
import ui.pwidget.G;
import ui.pwidget.JUtil;
import ui.pwidget.PPanel;

public abstract class TagLabel extends PPanel {

	protected int tagId;
	protected JandorButton removeButton;
	
	public TagLabel(int tagId) {
		super();
		this.tagId = tagId;
		init();
	}
	
	private void init() {
		removeButton = JUtil.buildCloseButton();
		removeButton.hide();
		removeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				handleRemove(tagId);
			}
			
		});
		
		c.anchor = G.CENTER;
		addc(new JLabel("#" + getTag().getName()));
		c.gridx++;
		c.insets(0,2);
		addc(removeButton);
	}
	
	public Tag getTag() {
		return Session.getInstance().getTag(tagId);
	}
	
	public abstract void handleRemove(int tagId);
	
	public GlassPane buildGlassPane() {
		GlassPane gp = new GlassPane(this) {
			
			@Override
			public void mouseEntered(MouseEvent e) {
				removeButton.show();
				super.mouseEntered(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				removeButton.hide();
				super.mouseExited(e);
			}
				
		};
		return gp;
	}
	
}
