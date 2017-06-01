package dutchAuction;

import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Seller extends Participant{
	protected int rounds, days, roundNow = 1, dayNow = 1, numDaysAnalised, maxBid;
	private Auction auction;
	private String fileName;
	
	public Seller(ContainerController contContr, Auction auction, boolean quickAuction, int rounds, int days, int numDaysAnalised, int maxBid, String fileName){
		this.name = "Seller";
		this.rounds = rounds;
		this.days = days;
		this.auction = auction;
		this.fileName = fileName;
		this.numDaysAnalised = numDaysAnalised;
		this.maxBid = maxBid;
		
		try {
			this.createAgent(contContr, this, quickAuction);
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getFileName(){return fileName;}
	public Auction getAuction(){return auction;}
	public int getRound(){return roundNow;}
	public int getDay(){return dayNow;}
	public int getMaxBid(){return maxBid;}
	public int decRound(){
		if(roundNow >= rounds){
			if(dayNow >= days){
				return 2;
			}
			dayNow++;
			roundNow = 1;
			return 1;
		}
		roundNow++;
		return 0;
	}
	public boolean isToPrint(){
		if ((days - dayNow) < numDaysAnalised){
			return true;
		}
		return false;
	}
}
