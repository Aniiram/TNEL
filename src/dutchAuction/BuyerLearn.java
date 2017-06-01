package dutchAuction;

import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class BuyerLearn extends Participant{
	// [] first means numGoods bought {0,1}
	// [] second means round {1,2,3,4,5,6,7,8}
	// [] third means bid {0,1,2,3,4,5,6,7,8,9,10}
	double[][][] attractions;
	double forgParam = 0.1d;
	int numGoodsTotal;
	int numRounds;
	int maxBid;
	double discParameter;
	double[] instProfit;
	int numGoods;
	int numDays;
	int nowDay = 0;
	int nowRound;
	int bid = 0;
	
	
	public BuyerLearn(String name, ContainerController container, boolean quickAuction, int numGoodsTotal, int rounds, int maxBid, double discParameter, double[] instProfit, int numDays){
		this.attractions = new double[numGoodsTotal][rounds][maxBid+1];
		//update attractions
		for(int i = 0; i < numGoodsTotal; i++){
			for(int j = 0; j < rounds; j++){
				for(int k = 0; k <= maxBid; k++){
					this.attractions[i][j][k] = 1.0d;
				}
			}
		}
		
		this.numGoodsTotal = numGoodsTotal;
		this.numRounds = rounds;
		this.maxBid = maxBid;
		this.discParameter = discParameter;
		this.instProfit = instProfit;
		this.numGoods = 0;
		this.numDays = numDays;
		this.name = name;
		
		try {
			this.createAgent(container, this, quickAuction);
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private double probBidX(int auction, int bid, int day){
		double res = 0, temp = 0;
		for(int i = 0; i <= maxBid; i++){ 
			if(i == bid){
				temp = Math.exp(timeVariant() * attractions[numGoods][auction][i]);
			}
			res = res + Math.exp(timeVariant() * attractions[numGoods][auction][i]);	
		}
		return temp/res;
	}
	
	private double nextBid(int auction){
		double soma = 0, temp = 0;
		double[] attr = new double[maxBid+1];
		
		for(int i = 0; i <= maxBid; i++){
			attr[i] = Math.exp(timeVariant() * attractions[numGoods][auction][i]);
			System.out.println("[BuyerLearn] 1stPart buyer " + i + ": " + String.valueOf(attr[i]));
			soma = soma + attr[i];	
		}
		
		//System.out.println("[BuyerLearn] 2stPart: " + String.valueOf(soma));
		
		int res = 0;
		temp = attr[res]/soma; 
		System.out.println("[BuyerLearn] total buyer 0: " + String.valueOf(temp));
		
		for(int i = 1; i <= maxBid; i++){
			System.out.println("[BuyerLearn] total buyer " + i + ": " + String.valueOf(attr[i]/soma));
			if(temp < (attr[i]/soma)){
				res = i;
			}
			//System.out.println("[BuyerLearn] Winner Bid: "+ res);
		}
		//System.out.println("[BuyerLearn] Winner Bid: "+ res);
		
		if(numGoods < numGoodsTotal && res == 0){
			res = res + 1;
		}
		
		return res;
	}
	
	private double attraction(int auction, int bidPrice, boolean win){
		//System.out.println("\n[BuyerLearn] Attraction: round- " + auction + " bid: " + bidPrice + " win: " + win);
		//System.out.println("[BuyerLearn] attractions value: " + attractions[numGoods][auction][bidPrice]);
		double res = (1.0d-forgParam) * attractions[numGoods][auction][bidPrice] 
				+ forgParam * rewardTerm (auction+1, win);
		return res;
	}
	
	private double rewardTerm(int nextState, boolean win){
		double maxAttract = maxAttract(nextState);
		double profit = 0;
		if(win){
			profit = instProfit[numGoods];
		}
		return profit + 
				discParameter * maxAttract;
	}
	
	private double maxAttract(int nextState){
		double res = 0;
		
		if(numGoods + 1 < numGoodsTotal){
			if(nextState < numRounds){
				for(int i = 0; i <= maxBid; i++){
					if(res < attractions[numGoods+1][nextState][i]){
						res = attractions[numGoods+1][nextState][i];
					}
				}
			}
		}
		
		if(nextState < numRounds){
			for(int i = 0; i <= maxBid; i++){
				if(res < attractions[numGoods][nextState][i]){
					res = attractions[numGoods][nextState][i];
				}
			}
		}
		return res;
	}
	
	private double timeVariant() {
		double temp = (nowDay * 1.0d) / (numDays * 1.0d);
		temp = temp*temp*temp;
		
		//System.out.println("[BuyerLearn] Result: " + temp + " today: " + nowDay + " total days: " + numDays);
		
		return temp;
	}
	
	@Override
	public Boolean accept(int bid){
		System.out.println("[BuyerAgent] Propose bid: " + bid + " Agent bid:" + this.bid);
		
		if(bid == this.bid){
			return true;
		}
		return false;
	} 
	
	@Override
	public String doResponse(String msg) {
		String[] words = msg.split(" ");
		nowDay = Integer.parseInt(words[4]);
		nowRound = Integer.parseInt(words[2]) - 1;
		
		if(nowRound == 0){
			numGoods = 0;
		}
		System.out.println("[BuyerLearn] Auction: " + nowRound + " Day: " + nowDay + " Goods: " + numGoods);
		
		if(numGoods < 2){
			
			double nBid = nextBid(nowRound);
			bid = (int) Math.floor(nBid);
			
			if(nBid-bid >= 0.5){
				bid++;
			}
		}else{
			bid = 0;
		}
		

		return Integer.toString(bid);
	}
	
	@Override
	public void win(){
		if(numGoods < 2){
			for(int i = 0; i < maxBid ; i++){
				if(i == bid){
					attractions[numGoods][nowRound][i] = attraction(nowRound, bid, true);
					//System.out.println("[BuyerLearn] win true: " + attractions[numGoods][nowRound][i]);
				}else{
					attractions[numGoods][nowRound][i] = attraction(nowRound, i, false);
					//System.out.println("[BuyerLearn] win false: " + attractions[numGoods][nowRound][i]);
				}
			}
			
			numGoods++;
		}		
	}
	
	@Override
	public void lose(){
		if(numGoods < 2){
			for(int i = 0; i < maxBid ; i++){				
				attractions[numGoods][nowRound][i] = attraction(nowRound, i, false);
				//System.out.println("[BuyerLearn] lose: " + attractions[numGoods][nowRound][i]);
			}
		}
	}
}
