package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mail.JandorMessage;
import session.BoosterHeader;
import session.Contact;
import session.DraftHeader;
import session.Session;
import session.User;
import ui.pwidget.JUtil;
import ui.pwidget.JandorTabFrame;
import ui.view.DraftEditorView;
import util.event.MessageEvent;
import util.event.SessionEvent;
import util.event.SessionEventListener;
import util.event.SessionEventManager;
import accordion.PAccordion;
import accordion.PAccordionData;

public class DraftUtil {

	private static final List<Integer> boosterIds = Collections.synchronizedList(new ArrayList<Integer>());
	
	private static DraftUtil instance;
	
	public static void init() {
		instance = new DraftUtil();
	}
	
	private DraftUtil() {
		addListeners();
	}
	
	public static int handleDraftInvite(JandorMessage message) {
		if(message == null || !message.isDraft()) {
			cleanupOldDrafts();
			return IDUtil.NONE;
		}
		
		int draftId = message.getDataId();
		DraftHeader draft = Session.getInstance().getDraftHeader(draftId);
		if(draft == null) {
			cleanupOldDrafts();
			return IDUtil.NONE;
		}
		User user = UserUtil.getUserByGUID(draft.getAuthorGUID());
		if(user == null) {
			cleanupOldDrafts();
			return IDUtil.NONE;
		}
		String authorName = draft.getAuthorFormatted();
		if(!JUtil.showConfirmYesNoDialog(null, authorName + " Wants to Draft", "Will you accept this invitation from " + authorName + "?")) {
			cleanupOldDrafts();
			return IDUtil.NONE;
		}
		
		if(JandorTabFrame.getAllFrames().size() == 0) {
			cleanupOldDrafts();
			return IDUtil.NONE;
		}

		JandorTabFrame frame = JandorTabFrame.getAllFrames().get(0);
		PAccordion accordion = new PAccordion();
		DraftEditorView deckEditorView = DraftEditorView.addDraftEditorView(accordion, draft.getId(), IDUtil.NONE, (PAccordionData) null);
		accordion.build();
		frame.getTabPane().addTab("Draft", accordion);
		frame.getTabPane().setSelectedIndex(frame.getTabPane().getTabCount() - 1);
		deckEditorView.flagModified();
		
		cleanupOldDrafts(draftId);
		
		frame.revalidate();
		frame.repaint();
		
		return draftId;
	}
	
	public static int handleBooster(JandorMessage message) {
		synchronized(boosterIds) {
		
			if(message == null || !message.isBooster()) {
				cleanupOldDrafts();
				return IDUtil.NONE;
			}
			
			int boosterId = message.getDataId();
			BoosterHeader booster = Session.getInstance().getBoosterHeader(boosterId);
			if(booster == null) {
				return IDUtil.NONE;
			}
			
			User user = UserUtil.getUserByGUID(booster.getAuthorGUID());
			if(user == null) {
				return IDUtil.NONE;
			}
			
			// Add a new booster to the queue, sorting as we go
			boosterIds.add(boosterId);
			Collections.sort(boosterIds, new Comparator<Integer>() {
	
				@Override
				public int compare(Integer idA, Integer idB) {
					BoosterHeader headerA = Session.getInstance().getBoosterHeader(idA);
					BoosterHeader headerB = Session.getInstance().getBoosterHeader(idB);
					int comp = headerA.getRound() - headerB.getRound();
					if(comp == 0) {
						comp = headerA.getTurn() - headerB.getTurn();
						if(comp == 0) {
							return (int) (headerA.getTimeFirstCreated() - headerB.getTimeFirstCreated());
						}
						return comp;
					}
					return comp;
				}
				
			});
			
			return boosterId;
			
		}
	}

	public static void cleanupOldDrafts() {
		cleanupOldDrafts(-1);
	}
	
	public static void cleanupOldDrafts(int idToIgnore) {
		List<DraftHeader> oldHeaders = Session.getInstance().getDraftHeaders();
		//List<BoosterHeader> oldBoosterHeaders = Session.getInstance().getBoosterHeaders();
		for(DraftHeader h : oldHeaders) {
			if(idToIgnore > 0 && h.getId() == idToIgnore) {
				continue;
			}
			h.delete();
			/*for(BoosterHeader bh : oldBoosterHeaders) {
				if(bh.getDraftId() == h.getId()) {
					bh.delete();
				}
			}*/
		}
	}
	
	private void addListeners() {
		SessionEventManager.addListener(DraftHeader.class, SessionEvent.TYPE_CREATED, new SessionEventListener(this) {
	
			@Override
			public void handleEvent(SessionEvent event) {
				if(!(event instanceof MessageEvent)) {
					return;
				}
				MessageEvent e = (MessageEvent) event;
				for(JandorMessage message : e.getData()) {
					if(handleDraftInvite(message) != IDUtil.NONE) {
						break;
					}
				}
			}
			
		});
		
		SessionEventManager.addListener(BoosterHeader.class, SessionEvent.TYPE_CREATED, new SessionEventListener(this) {
			
			@Override
			public void handleEvent(SessionEvent event) {
				if(!(event instanceof MessageEvent)) {
					return;
				}
				MessageEvent e = (MessageEvent) event;
				boolean newBoosters = false;
				for(JandorMessage message : e.getData()) {
					if(handleBooster(message) != IDUtil.NONE) {
						newBoosters = true;
					}
				}
				
				if(newBoosters) {
					SessionEventManager.fireEvent(BoosterHeader.class, IDUtil.NONE, SessionEvent.TYPE_ALERT);
				}
			}
			
		});
	}
	
	public static boolean hasBoosters() {
		return !boosterIds.isEmpty();
	}
	
	public static int getCurrentBooster(int draftId) {
		return getCurrentBooster(Session.getInstance().getDraftHeader(draftId));
	}
		
	public static int getCurrentBooster(DraftHeader draftHeader) {
		synchronized(boosterIds) {
			
			if(boosterIds.isEmpty()) {
				return IDUtil.NONE;
			}
			int currentBoosterId = IDUtil.NONE;
			int idx = -1;
			for(int i = 0; i < boosterIds.size(); i++) {
				int boosterId = boosterIds.get(i);
				if(draftHeader.isValidCurrentBooster(boosterId)) {
					currentBoosterId = boosterId;
					idx = i;
					break;
				}
			}
			
			if(currentBoosterId != IDUtil.NONE) {
				boosterIds.remove(idx);
			}
			
			return currentBoosterId;
		
		}
	}
	
}
