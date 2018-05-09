package com.vyom.whatsAppIntegration;

import java.io.Serializable;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ChatBotLogger implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static Logger logger = null;
	
	public ChatBotLogger() {
		// TODO Auto-generated constructor stub
		LogGenerator();
	}
	
	public static void LogGenerator(){
		logger = Logger.getLogger("MyLog");
		FileHandler fh;  
		try {  
			fh = new FileHandler("logs/chatBotLogFile.log");  
			logger.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();  
			fh.setFormatter(formatter);  
			// the following statement is used to log any messages  
			logger.info("\n===================	chatBot Log	===================\n");  
		} catch (Exception e) {e.printStackTrace();}  
		
	}
	
}
