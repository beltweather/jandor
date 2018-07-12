package util.event;

import java.util.List;

import mail.JandorMessage;

public class MessageEvent extends SessionEvent<List<JandorMessage>> {
	
	public MessageEvent(Class sessionDataClass, String type, List<JandorMessage> messages) {
		super(sessionDataClass, type, messages);
	}
	
	public MessageEvent(Class sessionDataClass, int sessionDataId, String type, List<JandorMessage> messages) {
		super(sessionDataClass, sessionDataId, type, messages);
	}
	
}
