package editor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import session.Session;
import session.Tag;
import ui.pwidget.ColorUtil;
import ui.pwidget.PButton;
import ui.view.CollectionEditorView;
import util.MailUtil;

public class TagButton extends PButton {

	protected CollectionEditorView view;
	protected int tagId;
	protected boolean selected = false;
	
	public TagButton(CollectionEditorView view, Tag tag) {
		this(view, tag.getId());
	}
	
	public TagButton(CollectionEditorView view, int tagId) {
		super();
		this.view = view;
		this.tagId = tagId;
		init();
	}
	
	private void init() {
		Tag tag = getTag();
		if(tagId == Tag.INBOX_ID) {
			setText(tag.getName() + " (" + Session.getInstance().getDeckHeadersInInbox().size() + ")");
		} else if(tagId == Tag.ALL_ID) {
			setText(tag.getName() + " (" + Session.getInstance().getDeckHeaders().size() + ")");
		} else {
			setText("#" + tag.getName() + " (" + Session.getInstance().getDeckHeaderIdsWithTag(tag).size() + ")");
		}
		setPreferredSize(new Dimension(120, 30));
		setCornerRadius(0);
		setGradientType(ColorUtil.GRAD_NONE);
		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				view.setCurrentTagId(tagId);
			}
			
		});
		
		setBackground(ColorUtil.TRANSPARENT);
		setHoverBackground(ColorUtil.DARK_GRAY_2);
		setBorder(null);
		setBorderPainted(false);
		setBorderColor(ColorUtil.TRANSPARENT);
		setFont(getFont().deriveFont(Font.PLAIN));
		setSelected(false);
	}
	
	public int getTagId() {
		return tagId;
	}
	
	public Tag getTag() {
		return Session.getInstance().getTag(tagId);
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
		if(selected) {
			setBackground(ColorUtil.DARK_GRAY_1);
			setHoverBackground(ColorUtil.DARK_GRAY_1);
			setFont(getFont().deriveFont(Font.BOLD));
			/*if(tagId == Tag.INBOX_ID) {
				MailUtil.receiveDecks();
			}*/
		} else {
			setBackground(ColorUtil.TRANSPARENT);
			setHoverBackground(ColorUtil.DARK_GRAY_2);
			setFont(getFont().deriveFont(Font.PLAIN));
		}
	}
	
}
