package util.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import accordion.PAccordion;
import accordion.PAccordionPanel;

import mail.JandorMessage;
import session.Contact;
import session.DraftHeader;
import session.Session;
import ui.pwidget.JUtil;
import ui.pwidget.JandorTabFrame;
import ui.view.DeckEditorView;
import ui.view.DraftEditorView;
import util.DraftUtil;
import util.IDUtil;

public class SessionEventManager {
	
	private static Map<String, List<SessionEventListener>> listenersByEventKey = new HashMap<String, List<SessionEventListener>>();
		
	private static boolean enableEvents = true;
	
	public static synchronized void enableEvents() {
		enableEvents = true;
	}
	
	public static synchronized void disableEvents() {
		enableEvents = false;
	}
	
	public static synchronized void addListener(Class sessionDataClass, String type, SessionEventListener listener) {
		addListener(sessionDataClass, IDUtil.NONE, type, listener);
	}
	
	public static synchronized void addListener(Class sessionDataClass, int sessionDataId, String type, SessionEventListener listener) {
		String key = SessionEvent.toKey(sessionDataClass, sessionDataId, type);
		if(!listenersByEventKey.containsKey(key)) {
			listenersByEventKey.put(key, new ArrayList<SessionEventListener>());
		}
		if(!listenersByEventKey.get(key).contains(listener)) {
			listenersByEventKey.get(key).add(listener);
		}
	}
	
	public static synchronized void removeListeners(Object owner) {
		for(String key : listenersByEventKey.keySet()) {
			Iterator<SessionEventListener> it = listenersByEventKey.get(key).iterator();
			while(it.hasNext()) {
				SessionEventListener listener = it.next();
				if(owner != null && owner.equals(listener.getOwner())) {
					listener.clearOwner();
					it.remove();
				}
			}
		}
	}
	
	public static synchronized void removeListener(SessionEventListener listener) {
		if(listener == null) {
			return;
		}
		for(String key : listenersByEventKey.keySet()) {
			Iterator<SessionEventListener> it = listenersByEventKey.get(key).iterator();
			while(it.hasNext()) {
				SessionEventListener l = it.next();
				if(l.equals(listener)) {
					l.clearOwner();
					it.remove();
				}
			}
		}
	}
	
	public static synchronized void fireEvent(Class sessionDataClass, int sessionDataId, String type) {
		fireEvent(new SessionEvent<Object>(sessionDataClass, sessionDataId, type));
	}
	
	public static synchronized void fireMessageEvent(Class sessionDataClass, int sessionDataId, String type, List<JandorMessage> messages) {
		fireEvent(new MessageEvent(sessionDataClass, sessionDataId, type, messages));
	}
	
	public static synchronized <T> void fireEvent(Class sessionDataClass, int sessionDataId, String type, T data) {
		fireEvent(new SessionEvent<T>(sessionDataClass, sessionDataId, type, data));
	}
	
	public static synchronized <T> void fireEvent(SessionEvent<T> event) {
		if(!enableEvents) {
			return;
		}
		
		List<String> usedKeys = new ArrayList<String>();
		
		// Event as given
		String key = event.getKey();
		usedKeys.add(key);
		handleEvent(key, event);
		
		// Any id
		key = event.getKey(false, true, false);
		if(!usedKeys.contains(key)) {
			handleEvent(key, event);
			usedKeys.add(key);
		}
		
		// Any type
		key = event.getKey(false, false, true);
		if(!usedKeys.contains(key)) {
			handleEvent(key, event);
			usedKeys.add(key);
		}
		
		// Any type any id
		key = event.getKey(false, true, true);
		if(!usedKeys.contains(key)) {
			handleEvent(key, event);
			usedKeys.add(key);
		}
		
		// Any class any id
		key = event.getKey(true, true, false);
		if(!usedKeys.contains(key)) {
			handleEvent(key, event);
			usedKeys.add(key);
		}
		
		// Any class any id any type
		key = event.getKey(true, true, true);
		if(!usedKeys.contains(key)) {
			handleEvent(key, event);
			usedKeys.add(key);
		}

	}
	
	private static synchronized <T> void handleEvent(String key, SessionEvent<T> event) {
		if(!listenersByEventKey.containsKey(key)) {
			return;
		}
		
		Iterator<SessionEventListener> it = listenersByEventKey.get(key).iterator();
		while(it.hasNext()) {
			SessionEventListener listener = it.next();
			listener.handleEvent(event);
		}
	}
	
}
