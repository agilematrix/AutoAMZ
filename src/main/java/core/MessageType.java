package core;

public enum MessageType {
	PASS, FAIL, MESSAGE;

	public static String getColor(MessageType messagetype) {
		switch (messagetype) {
		case PASS:
			return "32";
		case FAIL:
			return "31";
		case MESSAGE:
			return "34";
		}
		throw new RuntimeException("Unknown Message Type: "+messagetype);
	}
	
	public static MessageType getMessageType(String message) {
		if (message.startsWith("PASS")) {
			return MessageType.PASS;
		} else if (message.startsWith("FAIL")) {
			return MessageType.FAIL;
		} else {
			return MessageType.MESSAGE;
		}
	}
	
	public static MessageType getMessageTypeFromStatus(boolean status) {
		if (status) {
			return MessageType.PASS;
		}
		if (!status) {
			return MessageType.FAIL;
		}
		return null;
	}

}
