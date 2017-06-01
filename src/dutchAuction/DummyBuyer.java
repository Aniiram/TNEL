package dutchAuction;

import java.util.Random;

import jade.core.Agent;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class DummyBuyer extends Participant{
	Random r = new Random();
	int maxBid, bid;
	boolean win = false;
	
	public DummyBuyer(String name, ContainerController container, boolean quickAuction, int maxBid){
		this.name = name;
		this.maxBid = maxBid;
		try {
			this.createAgent(container, this, quickAuction);
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public Boolean accept(int bid){
		if(bid == this.bid){
			return true;
		}
		return false;
	}  
	
	@Override
	public String doResponse(String msg) {
		String res = Integer.toString(bid);
		if(!win){
			bid = r.nextInt(maxBid);
			res = Integer.toString(bid);
		}
		return res;
	}
	
	@Override
	public void win(){
		win = true;
	}
	
	@Override
	public void lose(){
		win = false;
	}
}
