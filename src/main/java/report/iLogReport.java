package report;

public interface iLogReport {

	void logTestMessage(String message);
	
	void logReportWithStatus(boolean status, String message);
	
	void logReportWithStatusAndException(boolean status, String message, Error e);
} 
