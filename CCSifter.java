import java.util.regex.*;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.*;
import java.nio.charset.*;
import java.io.*;


public class CCSifter {



    public CCSifter(){
	String fileContents = "";
	String absolutePath = Paths.get("").toAbsolutePath().toString();

	Path pathToDump = Paths.get(absolutePath, "memorydump.dmp");
	Charset charset = Charset.forName("ISO-8859-1");

	List<String> track1L = new ArrayList<>();
	List<String> track2L = new ArrayList<>();
	List<CardInfo> cards = new ArrayList<>();
	try{
	    List<String> allLines = Files.readAllLines(pathToDump, charset);

	    for(String line : allLines){
		fileContents = fileContents + line;
	    }
	}catch(IOException ie){
	    System.out.println("ERROR: "+ie.getMessage());
	}
	
	
	
	Pattern track1P = Pattern.compile("\\%[B][\\d]{13,19}\\^[a-zA-Z]{2,26}/[a-zA-Z]{2,26}\\^[\\d]*\\?");
	Matcher track1M = track1P.matcher(fileContents);

	while(track1M.find()){
	    track1L.add(track1M.group());
	}

	Pattern track2P = Pattern.compile("\\;[\\d]{13,19}\\=[\\d]*\\?");
	Matcher track2M = track2P.matcher(fileContents);

	while(track2M.find()){
	    track2L.add(track2M.group());
	}
	String cardNum;
	String cardholder;
	String expDate;
	String pin;
	String cvv;
	String serviceCode;

	
	for(String track1 : track1L){
	    
	    try{
		cardNum = track1.split("\\^")[0].substring(2);
		cardNum = cardNum.substring(0, 4) + " " + cardNum.substring(4, 8) + " " + cardNum.substring(8,12) + " " + cardNum.substring(12);
		cardholder = track1.split("\\^")[1].replace("/", " ");
		expDate = track1.split("\\^")[2].substring(0, 4);
		expDate = expDate.substring(2) + "/" + expDate.substring(0, 2);
		serviceCode = track1.split("\\^")[2].substring(4, 7);
	    }catch(StringIndexOutOfBoundsException sioobe){
		continue;
	    }
	    cards.add(new CardInfo(cardNum, cardholder, expDate, serviceCode));
	}
		
	for(String track2 : track2L){
	    try{
		cardNum = track2.split("\\=")[0].substring(1);
		cardNum = cardNum.substring(0, 4) + " " + cardNum.substring(4, 8) + " " + cardNum.substring(8,12) + " " + cardNum.substring(12);
		expDate = track2.split("\\=")[1].substring(0, 4);
		expDate = expDate.substring(2) + "/" + expDate.substring(0, 2);
		serviceCode = track2.split("\\=")[1].substring(4, 7);
		pin = track2.split("\\=")[1].substring(7, 11);
		cvv = track2.split("\\=")[1].substring(11, 14);
	    }catch(StringIndexOutOfBoundsException sioobe){
		continue;
	    }

	    for(CardInfo card : cards){
		if(card.addTrack2(cardNum, expDate, serviceCode, pin, cvv))
		    break;
	    }
	}

	int counter = 1;
	System.out.println();
	for(CardInfo card : cards){
	    if(card.pin != ""){
		System.out.println("<The info for card number " + counter + " is>");
		card.display();
		counter++;
	    }
	}
    }
    public static void main(String[] args){
	System.out.println("A program by Joe Whitney and Sam Shissler");
	CCSifter f1 = new CCSifter();
    }

    public class CardInfo{
	String cardNum;
	String cardholder;
	String expDate;
	String pin="";
	String cvv;
	String serviceCode;
	
	public CardInfo(String cardNum, String cardholder, String expDate, String serviceCode){
	    this.cardNum = cardNum;
	    this.cardholder = cardholder;
	    this.expDate = expDate;
	    this.serviceCode = serviceCode;
	}

	public boolean addTrack2(String cardNum, String expDate, String serviceCode, String pin, String cvv){	    
	    if(this.pin == "" && this.cardNum.equals(cardNum) && this.expDate.equals(expDate) && this.serviceCode.equals(serviceCode)){
		this.pin = pin;
		this.cvv = cvv;
		return true;
	    }
	    return false;
	}

	public void display(){
	    System.out.println("Cardholder Name:\t" + cardholder);
	    System.out.println("Card Number:\t\t" + cardNum);
	    System.out.println("Experation Date:\t" + expDate);
	    System.out.println("Encrypted PIN:\t\t" + pin);
	    System.out.println("CVV Number:\t\t" + cvv);
	    System.out.println();
	}
    }
}
