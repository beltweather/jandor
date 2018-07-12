package ui.view;

import session.DeckHeader;
import session.Session;
import session.Tag;
import util.IDUtil;
import accordion.PAccordion;
import accordion.PAccordionData;


public class DraftEditorView extends DeckEditorView {
	
	public static DraftEditorView addDraftEditorView(PAccordion accordion, int draftId, int deckId, CollectionEditorView parent) {
		DraftEditorView draftEditorView = new DraftEditorView(draftId, deckId);
		return addDraftEditorView(accordion, draftEditorView, parent);
	}
	
	public static DraftEditorView addDraftEditorView(PAccordion accordion, int draftId, int deckId, PAccordionData parentAccordionData) {
		DraftEditorView draftEditorView = new DraftEditorView(draftId, deckId);
		return addDraftEditorView(accordion, draftEditorView, parentAccordionData);
	}
	
	public static DraftEditorView addDraftEditorView(PAccordion accordion, DraftEditorView draftEditorView, CollectionEditorView parent) {
		PAccordionData deckEditorData = new PAccordionData(draftEditorView.getName(), draftEditorView);
		deckEditorData.setHeaderComponent(draftEditorView.getPageHeader());
		deckEditorData.setFooterComponent(draftEditorView.getPageFooter());
		deckEditorData.setRemoveable(true);
		
		accordion.add(deckEditorData, parent == null ? null : parent.getAccordionData());
		if(draftEditorView.getDraftId() != IDUtil.NONE) {
			DraftSearchView.addDraftSearchView(draftEditorView.getDraftId(), accordion, draftEditorView);
		}
		
		return draftEditorView;
	}
	
	public static DraftEditorView addDraftEditorView(PAccordion accordion, DraftEditorView draftEditorView, PAccordionData parentAccordionData) {
		PAccordionData deckEditorData = new PAccordionData(draftEditorView.getName(), draftEditorView);
		deckEditorData.setHeaderComponent(draftEditorView.getPageHeader());
		deckEditorData.setFooterComponent(draftEditorView.getPageFooter());
		deckEditorData.setRemoveable(true);
		
		accordion.add(deckEditorData, parentAccordionData);
		if(draftEditorView.getDraftId() != IDUtil.NONE) {
			DraftSearchView.addDraftSearchView(draftEditorView.getDraftId(), accordion, draftEditorView);
		}
		
		return draftEditorView;
	}
	
	protected int draftId;
	
	public DraftEditorView(int draftId, int deckId) {
		super(deckId);
		this.draftId = draftId;
		searchButton.setVisible(false);
		clearButton.setVisible(false);
		draftButton.setVisible(false);
		editDraftButton.setVisible(false);
		editDeckButton.setVisible(true);
	}
	
	@Override
	public void initDeckHeader() {
		boolean newDraft = deckId == IDUtil.NONE;
		super.initDeckHeader();
		if(newDraft) {
			Tag tag = Session.getInstance().getTag("draft");
			if(tag == null) {
				tag = Tag.createDefaultTag("draft");
				tag.save();
			}
			deckHeader.addTag(tag);
			deckHeader.setName(getUniqueName("Draft", true));
			deckHeader.save();
			setName(deckHeader.getName());
		}
	}

	public int getDraftId() {
		return draftId;
	}
	
	@Override
	protected String getDeckText() {
		return "Draft Deck";
	}
	
	@Override
	protected String getSideboardText() {
		return "Draft Sideboard";
	}
	
	
}
