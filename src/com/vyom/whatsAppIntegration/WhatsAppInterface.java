package com.vyom.whatsAppIntegration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


public class WhatsAppInterface  {

	public static boolean init=false;
	public static String browserLocation="C://tools//chromedriver.exe";
	public static WebDriver driver =null;
	public static WebDriverWait wait; 
	WebDriverWait driverWait;
	public static Actions actions ;
	public static HashMap<String, String> map_UserNo_LastMsg = new HashMap<String, String>();
	public static HashMap<String, Date> lastReply = new HashMap<>();
	public static HashMap<String, Integer> map_UserState = new HashMap<>();
	public static Thread t1= null;
	

	/*public String call() throws Exception {
		Thread.sleep(1000);
		//return the thread name executing this callable task
		return Thread.currentThread().getName();
	}*/
	
	public String sendReqOnChatBot(String userNo, String userReply) {
		 
		String output="";
		 String jsonToString="";
		try {

				URL url = new URL("http://localhost:8080/chatBotSprHbm/user/chatBotReq.htm");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");
				
				String input="{"
						+"\"contactNo\": \""+userNo+"\""
						+ ","
						+"\"requestMsg\": \""+userReply+"\""
						+ "}";
				//"{\"contactNo\": \"123456\",\"requestMsg\": \"hello\"}";
				//String input = "{\"contactNo\":123456,\"requestMsg\":\"hi\"}";
				System.out.println("input :"+input);
				
				OutputStream os = conn.getOutputStream();
				os.write(input.getBytes());
				os.flush();

				if (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
					throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
				}

				BufferedReader br = new BufferedReader(new InputStreamReader(
						(conn.getInputStream())));

				String json="";
				
				while ((output = br.readLine()) != null) {
					System.out.println("Output from pizza webservice: "+output);
					json=json+output;
				}
				json = json.toString();
				
				ObjectMapper mapper1 = new ObjectMapper();
			    JsonNode actualObj = mapper1.readTree(json);
			    String jsonNode1 = actualObj.get("message").toString();
			   
			    System.out.println("Message value: "+jsonNode1.toString().replace("\"", ""));
			   // string = string.replace("\"", "");
			    
			   jsonToString=jsonNode1.toString().replace("\"", "");
			   System.out.println("jsonToString :"+jsonToString);
				conn.disconnect();

			  } catch (MalformedURLException e) {

				e.printStackTrace();

			  } catch (IOException e) {

				e.printStackTrace();

			 }
		return jsonToString;
		
	}
	public static void main(String[] args) throws Exception {

		WhatsAppInterface obj=new WhatsAppInterface();
		obj.cleanBrowser();
		ChatBotLogger.LogGenerator();
		do
		{
			obj.init();
			actions = new Actions(driver);
		}while(driver == null);
		obj.DisplayUser();


	}
	//Cleaning browser
	private static void cleanBrowser() {
		try
		{ 
			Process p = Runtime.getRuntime().exec("Taskkill /f /im chromedriver.exe");
			p = Runtime.getRuntime().exec("Taskkill /f /im chrome.exe");
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	//Initializing browser
	private static void init()
	{
		ChatBotLogger.logger.info("Startig INIT ");
		//Driver Type and Location	
		driver = DriverUtility.getDriver(browserLocation);				
	}
	public static WebDriver getDriver(String CHROME_LOCATION)
	{
		WebDriver driver;
		System.setProperty("webdriver.chrome.driver", CHROME_LOCATION);			
		HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
		chromePrefs.put("profile.default_content_settings.popups", 0);
		chromePrefs.put("chrome.switches", "--start-maximized");
		ChromeOptions options = new ChromeOptions();
		options.setExperimentalOption("prefs", chromePrefs);
		DesiredCapabilities cap = DesiredCapabilities.chrome();
		cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		cap.setCapability(ChromeOptions.CAPABILITY, options);
		driver = new ChromeDriver(cap);
		return driver;
	}
	public String removeSpecialCharacter(String temp)
	{
		try 
		{
			return temp.replaceAll("\\P{Print}", "").trim();
		} catch (Exception e) {
			return temp;
		}
	}
	public	boolean processMessage(String key) 
	{
		Date d1 = lastReply.get(key);
		if (d1 == null)
			return true;
		Date d2 = new Date();
		long seconds = (d2.getTime() - d1.getTime()) / 1000;
		if (seconds > 1) {
			return true;
		}
		return false;
	}

	public static int count = 1;
	public String UserNameNo = null;
	
	public void DisplayUser() throws Exception
	{    //wait
		wait = new WebDriverWait(driver, 60);
		if(init==false)	/***** Initialize Web.Whatsapp.com *****/
		{
			driver.get("https://web.whatsapp.com/");
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("side")));
			init=true;
		}
		/***** switch to first frame in the frameset. *****/
		driver.switchTo().defaultContent();
		WebElement sidepanel=driver.findElement(By.id("side"));
		WebElement chatContactPanel=driver.findElement(By.id("pane-side"));
		List<WebElement> listOfChatingUsers = chatContactPanel.findElements(By.className("_2wP_Y"));	
		ChatBotLogger.logger.info("Number of users in Queue : "+listOfChatingUsers.size());
		//check for current message using green bubble
		boolean hasNewMsg = false;
		int i = 0;
		for(WebElement currentUserNo:listOfChatingUsers)
		{
			i++;
			try
			{
				//current user chats
				String userNo=currentUserNo.findElement(By.className("_25Ooe")).findElement(By.tagName("span")).getAttribute("title");
				/*hasNewMsg = driver.findElements(By.xpath("//*[@id='pane-side']/div/div/div/div["+ i +"]/div/div/div[2]/div[2]/div[2]/span[1]/div/span")).size() > 0;
				System.out.println("Is " + userNo + " has new msg : " +hasNewMsg);
				if(!hasNewMsg)
					continue;*/
				currentUserNo.click();
				actions.moveToElement(currentUserNo).click().perform();
				//String userNo = currentUserNo.findElement(By.className("_25Ooe")).findElement(By.tagName("span")).getAttribute("title");
				//chat Title (Customer No/Name)	
				ChatBotLogger.logger.info("chat Title (Customer No/Name): " + userNo);									//chat Title (Customer No/Name)			
				actions.moveToElement(currentUserNo).click().perform();
				String customerLastMsg = "";		
				customerLastMsg =currentUserNo.findElement(By.className("_1AwDx")).findElement(By.className("_itDl")).findElement(By.className("_2_LEW")).getAttribute("title");
				//removing special character by calling removeSpecialCharacter
				customerLastMsg=removeSpecialCharacter(customerLastMsg);
				ChatBotLogger.logger.info("customerLastMsg try: " + customerLastMsg);
				System.out.println("customerLastMsg try: " + customerLastMsg);
				System.out.println("map_UserNo_LastMsg.get(userNo) : " +map_UserNo_LastMsg.get(userNo));
				 if(map_UserNo_LastMsg.get(userNo)!=null &&map_UserNo_LastMsg.get(userNo).equals(customerLastMsg))
				 {
					 System.out.println("continue...");
						continue;
				 }
				/***** if map_UserNo_LastMsg not having current user then add****/				
				if(!map_UserNo_LastMsg.containsKey(userNo))
				{
					map_UserNo_LastMsg.put(userNo, customerLastMsg);
				}
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("_1AwDx")));


				if (processMessage(userNo))
				{
					if (customerLastMsg.equals("‪Messages you text to this chat and calls are secured with end-to-end encryption.")) 									
						continue;
					String replay = messageReply(userNo, customerLastMsg);
					map_UserNo_LastMsg.put(userNo, replay);
/*
					String reqMsg = new String(customerLastMsg);
					String fileName = new String(userNo);
					new Thread(new Runnable() {
						@Override
						public void run()
						{
							try {
								loadFile(fileName+".txt", fileName+" : "+ reqMsg);
							} catch (IOException | InterruptedException e) {
								e.printStackTrace();
							}
						}
					}).start();*/
					
				} 	
			}
			catch(Exception e){
				e.printStackTrace();				
				break;
			}
			ChatBotLogger.logger.info("-----------------------------------------------");

		}
		Thread.sleep(3000);
		DisplayUser();
	}
	
	//loading file
	public void loadFile(String fileName, String ReqMsg) throws IOException, InterruptedException
	{
		File file=new File(fileName);
		System.out.println("fileName : "+fileName);
		Thread.sleep(5000);

		if(!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream fos =new FileOutputStream(file);    
		byte b[] = ReqMsg.getBytes();
		fos.write(b);
		fos.close();  

		
		
		
	}
	public String messageReply(String userNo, String userReplay) throws Exception {
		String reply = "";
		userNo = userNo.replace("+91", "").replace(" ", "");
		Pattern intermediateIdPattern=Pattern.compile("(\\d{12})");
		intermediateIdPattern.matcher(userReplay);
		Pattern.compile("(([A-D]{1}|[a-d]{1})[1-4]{1})|(([A-D]{1}|[a-d]{1})[1-4]{1}[$]{1}[0-9]{1,7})|(([A-D]{1}|[a-d]{1})[1-4]{1}[@]{1}[0-9]{1,5})|(([A-D]{1}|[a-d]{1})[1-4]{1}[$]{1}[0-9]{1,7}[@]{1}[0-9]{1,5})|(([A-D]{1}|[a-d]{1})[1-4]{1}[@]{1}[0-9]{1,5}[$]{1}[0-9]{1,7})");
		userNo.split(" ");
		if(!map_UserState.containsKey(userNo))		//check userNo is already register in userState map or not
			map_UserState.put(userNo, 1);			//if not then set userState 1 (fresh chat) in userState map

//		ReadChatBotMenuXML readChatBotMenuXML = new ReadChatBotMenuXML();
		//int userState=map_UserState.get(userNo);
//		reply = reply + RequestDispatcher.requestProcess(userNo, userReplay);
		reply = reply + sendReqOnChatBot(userNo, userReplay);
		sendMessage(userNo, reply);	
		lastReply.put(userNo, new Date());
		archiveContact();
		Thread.sleep(2000);
		return removeSpecialCharacter(reply);
	}

	public void sendMessage(String contactNo, String reply)
	{
		/***** switch to first frame in the frameset. *****/
		driver.switchTo().defaultContent();		

		/***** wait until switch frame to chatPanel  *****/
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("main")));

		/***** get element of chatPanel  *****/
		WebElement chatPanel = (WebElement) driver.findElement(By.id("main"));	

		/***** get element of inputPanel  *****/
		WebElement inputPanel = (WebElement) chatPanel.findElement(By.className("_3oju3"));		

		/***** get element of inputTextBox  *****/
		WebElement inputTextBox = (WebElement) inputPanel.findElement(By.className("_2bXVy"));	

		/*****  wait until locate pointer  *****/
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"main\"]/footer/div[1]/div[2]/div/div[2]")));	

		/*****  get element of pointer  *****/
		WebElement inputText = (WebElement) inputTextBox.findElement(By.xpath("//*[@id=\"main\"]/footer/div[1]/div[2]/div/div[2]"));

		/***** Click in textBox  *****/
		inputText.click();

		/***** Clear textBox  *****/
		inputText.clear();	

		/***** Write message in textBox  *****/
		inputText.sendKeys(reply.replaceAll("\n", Keys.chord(Keys.SHIFT, Keys.ENTER)));			

		/***** get element of send button  *****/
		WebElement sendButton = (WebElement) inputPanel.findElements(By.tagName("button")).get(1);
		/***** Click on send button  *****/
		sendButton.click();																		
		map_UserNo_LastMsg.put(contactNo, reply);	/***** update last sent message in map  *****/

	}
	public void archiveContact()
	{
		WebElement archi = driver.findElement(By.xpath("//*[@id='pane-side']/div/div/div/div/div/div/div/div[2]"));

		actions.moveToElement(archi).click().perform();
		archi.click();
		actions.contextClick(archi).build().perform();
		wait.until(ExpectedConditions.visibilityOfElementLocated((By.className("_3s1D4"))));
		WebElement arch = driver.findElement(By.className("_3s1D4"));
		List<WebElement> archive = arch.findElements(By.tagName("li"));
		archive.get(0).click();
	}




}



