package dutchAuction;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class BuyerAgent extends Agent{
	private Participant buyer;
	boolean quickAuction;
	
	public BuyerAgent(Participant buyer, boolean quickAuction){
		this.buyer = buyer;
		this.quickAuction = quickAuction;
	}
	
	protected void setup() {
		//registar agente na DF
		DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(BuyerAgent.class.getName());
        sd.setType("Buyer");
        dfd.addServices(sd);
        
        try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        if(quickAuction){
        	addBehaviour(new CyclicBehaviour() {
        		ACLMessage answer, msg;
        		
    			@Override
    			public void action() {
    				msg = receive();
    				if(msg != null){
    					System.out.println("[BuyerAgent] MSG received: " + msg.getContent());
    					//System.out.println("[BuyerAgent] Sender: " + msg.getSender());
    					
    					switch(msg.getPerformative()){
    						case ACLMessage.INFORM:
    							answer = new ACLMessage(ACLMessage.PROPOSE);
    	    					answer.setContent(buyer.doResponse(msg.getContent()));	
    	    					
    							break;
    						case ACLMessage.ACCEPT_PROPOSAL:
    							buyer.win();
    							answer = new ACLMessage(ACLMessage.CONFIRM);
    	    					
    							break;
    						case ACLMessage.REJECT_PROPOSAL:
    							buyer.lose();
    							answer = new ACLMessage(ACLMessage.CONFIRM);
    	    					
    							break;
    					}
    					answer.addReceiver(msg.getSender());
    					send(answer);
    					
    				}else{
    					block();
    				}		
    			}
            });
        }else{
        	addBehaviour(new CyclicBehaviour() {
        		ACLMessage answer, msg;
        		
    			@Override
    			public void action() {
    				msg = receive();
    				if(msg != null){
    					//System.out.println("[BuyerAgent] MSG received: " + msg.getContent());
    					//System.out.println("[BuyerAgent] Sender: " + msg.getSender());
    					
    					switch(msg.getPerformative()){
    						case ACLMessage.INFORM:
    							answer = new ACLMessage(ACLMessage.INFORM);
    	    					buyer.doResponse(msg.getContent());	
    	    					
    							break;
    							
    						case ACLMessage.PROPOSE:
    							if(buyer.accept(Integer.parseInt(msg.getContent()))){
    								answer = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
    							}else{
    								answer = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
    							}    							
    	    					
    							break;
    						
    						case ACLMessage.AGREE:
    							buyer.win();
    							answer = new ACLMessage(ACLMessage.CONFIRM);
    	    					
    							break;
    						case ACLMessage.CANCEL:
    							buyer.lose();
    							answer = new ACLMessage(ACLMessage.CONFIRM);
    	    					
    							break;
    					}
    					answer.addReceiver(msg.getSender());
    					send(answer);
    					
    				}else{
    					block();
    				}		
    			}
            });
        }
	}
}
