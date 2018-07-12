package editor;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.plaf.basic.BasicButtonUI;

import session.DeckHeader;
import session.Session;
import session.Tag;
import ui.GlassPane;
import ui.JandorButton;
import ui.pwidget.ColorUtil;
import ui.pwidget.JUtil;
import ui.pwidget.PPanel;
import ui.view.CollectionEditorView;
import util.ImageUtil;

public class TagRow extends PPanel {

	protected CollectionEditorView view;
	protected int tagId;
	protected boolean selected = false;
	protected TagButton tagButton;
	protected JandorButton removeButton;
	protected JandorButton renameButton;
	
	public TagRow(CollectionEditorView view, Tag tag) {
		this(view, tag.getId());
	}
	
	public TagRow(CollectionEditorView view, int tagId) {
		super();
		this.view = view;
		this.tagId = tagId;
		init();
	}
	
	public int getTagId() {
		return tagId;
	}
	
	public Tag getTag() {
		return Session.getInstance().getTag(tagId);
	}
	
	public TagButton getTagButton() {
		return tagButton;
	}
	
	private void init() {
		removeButton = JUtil.buildCloseButton();
		removeButton.hide();
		removeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(JUtil.showConfirmYesNoCancelDialog(JUtil.getFrame(view), "Delete Tag \"" + getTag().getName() + "\"", "Are you want to permanently delete this tag?\nIt will be removed from all decks.")) {
					remove();
				}
			}
			
		});
		removeButton.setToolTipText("Remove");
		
		renameButton = buildRenameButton();
		renameButton.hide();
		renameButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				rename();
			}
			
		});
		renameButton.setToolTipText("Rename");
		
		tagButton = new TagButton(view, tagId);
		
		addc(tagButton);
		c.gridx++;
		c.insets(0,5);
		addc(renameButton);
		c.gridx++;
		addc(removeButton);
	}
	
	private JandorButton buildRenameButton() {
		JandorButton button = new JandorButton();
		ImageIcon icon = new ImageIcon(ImageUtil.getEditIcon());
	
		button.setIcon(icon);
        button.setRolloverIcon(new ImageIcon(ImageUtil.getEditIconFull()));
        button.setPressedIcon(new ImageIcon(ImageUtil.getEditIconFullDown()));
        button.setRolloverEnabled(true);
        
        button.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
        button.setUI(new BasicButtonUI());
        button.setBorderPainted(false);
        button.setBackground(ColorUtil.DARK_GRAY_3);
	
        return button;
	}
	
	public void remove() {
		view.disableEvents();
		for(DeckHeader header : Session.getInstance().getDeckHeadersWithTagId(tagId)) {
			header.removeTagId(tagId);
			header.save();
		}
		getTag().delete();
		view.enableEvents();
		view.setCurrentTagId(Tag.ALL_ID);
		view.handleEvent();
	}
	
	public void rename() {
		Tag tag = getTag();
		String text = JUtil.showInputDialog(JUtil.getFrame(this), "Rename Tag \"" + tag.getName() + "\"", "", tag.getName()); 
		if(text == null) {
			return;
		}
		if(!text.equals(tag.getName())) {
			tag.setName(text);
			tag.save();
		}
	}
	
	private static GlassPane lastGlassPane = null;
	public GlassPane buildGlassPane() {
		final GlassPane gp = new GlassPane(this) {
			
			@Override
			public void mouseEntered(MouseEvent e) {
				removeButton.show();
				removeButton.setRolloverEnabled(false);
				renameButton.show();
				renameButton.setRolloverEnabled(false);
				super.mouseEntered(e);
				removeButton.setRolloverEnabled(true);
				renameButton.setRolloverEnabled(true);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				removeButton.hide();
				removeButton.setRolloverEnabled(false);	
				renameButton.hide();
				renameButton.setRolloverEnabled(false);
				super.mouseExited(e);
				removeButton.setRolloverEnabled(true);
				renameButton.setRolloverEnabled(true);
			}
				
		};
		return gp;
	}

}
