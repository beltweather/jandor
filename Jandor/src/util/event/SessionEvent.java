package util.event;

import session.SessionData;
import util.IDUtil;

public class SessionEvent<T> {

	public static String toKey(Class sessionDataClass, int sessionDataId, String type) {
		return sessionDataClass.getSimpleName() + ":" + sessionDataId + ":" + type;
	}

	public static final String TYPE_MODIFIED = "modified";
	public static final String TYPE_DELETED = "deleted";
	public static final String TYPE_CREATED = "created";
	public static final String TYPE_ALERT = "alert";
	public static final String TYPE_ANY = "any";
	public static final String TYPE_NONE = "";

	private Class sessionDataClass;
	private int sessionDataId;
	private String type;
	private T data = null;

	public SessionEvent(Class sessionDataClass, String type) {
		this(sessionDataClass, type, null);
	}

	public SessionEvent(Class sessionDataClass, String type, T data) {
		this(sessionDataClass, IDUtil.NONE, type, data);
	}

	public SessionEvent(Class sessionDataClass, int sessionDataId, String type) {
		this(sessionDataClass, sessionDataId, type, null);
	}

	public SessionEvent(Class sessionDataClass, int sessionDataId, String type, T data) {
		this.sessionDataClass = sessionDataClass == null ? SessionData.class : sessionDataClass;
		this.sessionDataId = sessionDataId;
		this.type = type == null ? TYPE_NONE : type;
		this.data = data;
	}

	public boolean hasSessionDataId() {
		return sessionDataId != IDUtil.NONE;
	}

	public int getSessionDataId() {
		return sessionDataId;
	}

	public Class getSessionDataClass() {
		return sessionDataClass;
	}

	public String getType() {
		return type;
	}

	public boolean isType(String type) {
		return this.type != null && this.type.equals(type);
	}

	public String getKey() {
		return toKey(sessionDataClass, sessionDataId, type);
	}

	public String getKey(boolean anyClass, boolean anyId, boolean anyType) {
		return toKey(anyClass ? SessionData.class : sessionDataClass,
				     anyId ? IDUtil.NONE : sessionDataId,
				     anyType ? SessionEvent.TYPE_ANY : type);
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	public boolean hasData() {
		return data != null;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null || !(obj instanceof SessionEvent)) {
			return false;
		}

		SessionEvent e = (SessionEvent) obj;
		if(!type.equals(e.getType())) {
			return false;
		}

		if(sessionDataId != e.getSessionDataId()) {
			return false;
		}

		if(!sessionDataClass.equals(e.getSessionDataClass())) {
			return false;
		}

		return true;
	}

}
