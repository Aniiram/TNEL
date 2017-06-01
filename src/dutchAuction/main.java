package dutchAuction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class main {
	
	public static void main(String[] args){
		// Auction args: boolean quickAuction, int rounds, int maxBid, int numDays
		Auction auction = new Auction(false, 4, 10, 3);
		auction.launchJade();	
		
		//int numGoodsTotal, double discParameter, int[] insProfit
		auction.loadBuyers(2, 1.0d, new double[] {10.0d,6.0d});
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// String fileName, int numDaysToAnalize
		auction.loadSeller("test.txt", 1);
        
	}
}
