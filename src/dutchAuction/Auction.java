package dutchAuction;

import java.util.ArrayList;
import java.util.List;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Auction {
	AgentContainer mainContainer;
	private static List<Participant> buyers = new ArrayList<>();
	private Seller seller;
	private int numRounds;
	private int maxBid;
	private int numDays;
	private boolean quickAuction;
	
	public Auction(boolean quickAuction, int rounds, int maxBid, int numDays){
		this.quickAuction = quickAuction;
		this.numRounds = rounds;
		this.maxBid = maxBid;
		this.numDays = numDays;
	}
	
	public List<Participant> getBuyers(){
		return buyers;
	}
	
	public Participant getBuyerByAgentName(String name){
		for (int i = 0; i < getBuyers().size(); i++){
            if (getBuyers().get(i).getName().equals(name)){
                return getBuyers().get(i);
            }
		}
        return null;
	}
	
	public void launchJade() {
		// Get a hold on JADE runtime
		Runtime rt = Runtime.instance();
		
		// Exit the JVM when there are no more containers around
		rt.setCloseVM(true);
		System.out.print("runtime created\n");
		
		// Create a default profile
		Profile profile = new ProfileImpl(null, 1200, null);
		System.out.print("profile created\n");

		System.out.println("Launching a whole in-process platform..."+profile);
		this.mainContainer = rt.createMainContainer(profile);
	}
	
	public void loadBuyers(int numGoodsTotal, double discParameter, double[] insProfit){
		System.out.println("[Auction] load buyers...");
		buyers.add(new DummyBuyer("dummy",mainContainer,quickAuction, maxBid));
		//buyers.add(new DummyBuyer("dummy2",mainContainer,quickAuction, maxBid));
		
		//Learn Agents: String name, ContainerController container, boolean quickAuction, 
		//				int numGoodsTotal, int rounds, int maxBid, 
		//				int discParameter, int[] instProfit, int numDays
		buyers.add(new BuyerLearn("learn", mainContainer, quickAuction, 
									numGoodsTotal, numRounds, maxBid,
									discParameter, insProfit, numDays));
	}
	
	public void loadSeller(String fileName, int numDaysToAnalize){
		System.out.println("[Auction] load seller...");
		seller = new Seller(mainContainer, this, quickAuction, numRounds, numDays, numDaysToAnalize, maxBid, fileName);
	}
	
	public void killAgents(){
		try {
			System.out.println("\n\n[Auction] Terminating...");
			mainContainer.kill();
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
