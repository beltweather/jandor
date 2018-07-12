package editor;

import java.awt.Color;
import java.awt.Component;
import java.util.Collection;

import javax.swing.BorderFactory;

import session.Session;
import session.Tag;
import ui.AutoComboBox;
import ui.pwidget.JUtil;
import ui.pwidget.PPanel;
import util.IDUtil;

public abstract class TagDialog extends PPanel {

	protected int deckId;
	protected AutoComboBox<Tag> tagCombo;
	protected Component parent;
	
	public TagDialog(Component parent) {
		this(parent, IDUtil.NONE);
	}
	
	public TagDialog(Component parent, int deckId) {
		this.parent = parent;
		this.deckId = deckId;
		init();
	}
	
	private void init() {
		
		tagCombo = new AutoComboBox<Tag>() {

			@Override
			public String buildTooltip(Tag selectedItem) {
				return null;
			}

			@Override
			public Collection<Tag> getSearchCollection(String searchString) {
				return Session.getInstance().getTags(true);
			}

			@Override
			public String toString(Tag searchedObject) {
				return searchedObject.getName();
			}

			@Override
			public void handleFound(Tag found) {
				
			}
		
		};
		tagCombo.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		
		addc(tagCombo);
		
	}
	
	public void showDialog() {
		if(JUtil.showConfirmDialog(parent, "Add Tags", this)) {
			String tagName = tagCombo.getTextField().getText();
			if(tagName.isEmpty()) {
				return;
			}
			Tag tag = Session.getInstance().getTag(tagName);
			if(tag == null) {
				tag = Tag.createDefaultTag(tagName);
				tag.save();
			}
			handleTagAdded(tag);
		}
	}
	
	public abstract void handleTagAdded(Tag tag);
	
}
