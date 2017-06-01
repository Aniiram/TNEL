package dutchAuction;

import jade.core.Agent;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Participant {
	protected String name;
	protected AgentController agentContr;
	
	public String getName(){return name;}
	
	protected void createAgent(ContainerController container, Agent agent) throws StaleProxyException{
			this.agentContr = container.acceptNewAgent(this.name, agent);
			this.agentContr.start();
	}
	
	protected Agent createAgent(ContainerController container, BuyerLearn part, boolean quickAuction) throws StaleProxyException{
		Agent agent = new BuyerAgent(part, quickAuction);
		createAgent(container, agent);
		System.out.println("[Participant] BuyerLearn created...");
		
		return agent;
	}
	
	protected Agent createAgent(ContainerController container, DummyBuyer part, boolean quickAuction) throws StaleProxyException{
		Agent agent = new BuyerAgent(part, quickAuction);
		createAgent(container, agent);
		System.out.println("[Participant] DummyBuyer created...");
		
		return agent;
	}
	
	protected Agent createAgent(ContainerController container, Seller part, boolean quickAuction) throws StaleProxyException{
		Agent agent = new SellerAgent(part, quickAuction);
		createAgent(container, agent);
		System.out.println("[Participant] Seller created...");
		
		return agent;
	}

	public String doResponse(String msg) {return null;}
	public Boolean accept(int bid) {return null;}
	public void win(){}
	public void lose(){}
}
