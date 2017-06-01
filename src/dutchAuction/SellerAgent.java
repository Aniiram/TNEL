package dutchAuction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class SellerAgent extends Agent {
	private Seller seller; 
	DFAgentDescription[] buyers;
	boolean quickAuction;
	PrintWriter pw;
	
	public SellerAgent(Seller seller, boolean quickAuction){
		this.seller = seller;
		this.quickAuction = quickAuction;
		
		//Prepare File to write
		File file = new File(seller.getFileName());
		try {
			this.pw = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 
	
	protected void setup(){
		//Encontrar agentes do tipo buyer
		DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd  = new ServiceDescription();
        sd.setType("Buyer");
        dfd.addServices(sd); 
        
        try {
			buyers = DFService.search(this, dfd);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        if(quickAuction){
        	addBehaviour(new CyclicBehaviour(this){
            	ACLMessage msg;
            	int numAnswers = 0;
            	int[] bids;
            	Random r = new Random();
            	
            	@Override
    			public void onStart() {
            		//initiate bid
            		bids = new int[buyers.length];
            		
    				//send message inform beginning of round
            		msg = new ACLMessage(ACLMessage.INFORM);
            		msg.setContent("BEGIN: Round " + seller.getRound() + " Day " + seller.getDay());
            		
            		System.out.println("\n[SellerAgent] " + msg.getContent());
            		
            		//Print on File
            		if(seller.isToPrint()){            			
                		pw.println("\n[SellerAgent] " + msg.getContent());
                		pw.flush();
            		}
            		
            		//add recipients to the message
            		for (int i = 0; i < buyers.length; i++){
            			msg.addReceiver(buyers[i].getName());
            		}
            		
            		//send message
            		send(msg);
            		
            		super.onStart();
            	}
            	
    			@Override
    			public void action() {
    				msg = receive();
    				
    				if (msg != null){
    					
    					switch(msg.getPerformative()){
    					case ACLMessage.PROPOSE:
    						numAnswers++;
    						readMessage(msg);
    							
							if(numAnswers >= buyers.length ){
								numAnswers = 0;
    							int winner = checkWinner();
    							
    							System.out.println("[SellerAgent] Winner: " + buyers[winner].getName().getLocalName());
    							System.out.println("[SellerAgent] Winner Bid: " + bids[winner]);
    							
    							//Print on File
    		            		if(seller.isToPrint()){   
	    							pw.println("[SellerAgent] Winner: " + buyers[winner].getName().getLocalName());
	    							pw.println("[SellerAgent] Winner Bid: " + bids[winner]);
	    							pw.println(" ");
	    							pw.flush();
    		            		}
    		            		
    							ACLMessage win = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
    							win.addReceiver(buyers[winner].getName());
    							
    							int loseReceivers = 0;
    							ACLMessage lose = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
    							for (int i = 0; i < buyers.length; i++){
    								if (i != winner){
        		            			lose.addReceiver(buyers[i].getName());
        		            			loseReceivers++;
    								}
    		            		}
    							
    							send(win);
    							if(loseReceivers > 0){
    								send(lose);
    							}
    							
    						}
    						
    						break;
    						
    					case ACLMessage.CONFIRM:
    						numAnswers++;
    						
    						if(numAnswers >= buyers.length){
    							switch(seller.decRound()){
    							case 0:
    	    						onStart();
    	    						break;
    	    					case 1:
    	    						onStart();
    	    						break;
    	    					case 2:
    	    						pw.close();
    	    						seller.getAuction().killAgents();
    	    						break;
    	    					}
    							numAnswers = 0;
    						}
    						
    						break;
    					}
    						
    				}else{
    					block();
    				}	
    			}
    			
    			public void readMessage(ACLMessage msg){
    				//System.out.println("[SellerAgent] ReadMessage " + msg.getContent());
    				
    				for (int i = 0; i < buyers.length; i++){
        				//System.out.println("[SellerAgent] ReadMessage " + buyers[i].getName());
        				//System.out.println("[SellerAgent] ReadMessage " + msg.getSender());
    					
        				if(buyers[i].getName().equals(msg.getSender())){
    						System.out.println("[SellerAgent] Buyer: " + buyers[i].getName().getLocalName() + " Bid:" + msg.getContent());
    						
    						//Print on File
    	            		if(seller.isToPrint()){   
	    						pw.println("[SellerAgent] Buyer: " + buyers[i].getName().getLocalName() + " Bid:" + msg.getContent());
	    						pw.flush();
    	            		}
    	            		
    						bids[i] = Integer.parseInt(msg.getContent());
    						
    						//System.out.println("[SelletAgent] Buyer: " + i + " Check Bid:" + bids[i]);
    						return;
    					}
    				}
    			}
    			
    			public int checkWinner(){
            		List<Integer> winner = new ArrayList<>();
            		winner.add(0);
            		
    				for(int i = 1; i < buyers.length; i++){
    					if(bids[winner.get(0)] < bids[i]){
    						winner = new ArrayList<>();
    						winner.add(i);
    						
    					}else if(bids[winner.get(0)] == bids[i]){
    						winner.add(i);
    					}
    				}
    				
    				return winner.get(r.nextInt(winner.size()));
    			}
            });
        	
        }else{
        	addBehaviour(new CyclicBehaviour(this){
            	ACLMessage msg;
            	int numAnswers = 0;
            	int bid;
            	boolean[] ans;
            	Random r = new Random();
            	
            	@Override
    			public void onStart() {
            		//initialize variables
            		bid = seller.getMaxBid();
            		ans = new boolean[buyers.length];
            		
    				//send message inform beginning of round
            		msg = new ACLMessage(ACLMessage.INFORM);
            		msg.setContent("BEGIN: Round " + seller.getRound() + " Day " + seller.getDay());
            		
            		System.out.println("\n[SellerAgent] " + msg.getContent());
            		
            		//Print on File
            		if(seller.isToPrint()){            			
                		pw.println("\n[SellerAgent] " + msg.getContent());
                		pw.flush();
            		}
            		
            		//add recipients to the message
            		for (int i = 0; i < buyers.length; i++){
            			msg.addReceiver(buyers[i].getName());
            		}
            		
            		//send message
            		send(msg);
            		
            		super.onStart();
            	}
            	
            	@Override
    			public void action() {
    				msg = receive();
    				
    				if (msg != null){
    					//System.out.println("[SellerAgent] MSG received: " + msg.getPerformative());
    					
    					switch(msg.getPerformative()){
    					case ACLMessage.INFORM:
    						numAnswers++;
    							
							if(numAnswers >= buyers.length ){
								numAnswers = 0;
					
    							ACLMessage propose = new ACLMessage(ACLMessage.PROPOSE);
    							propose.setContent(Integer.toString(bid));
    							
    							//add recipients to the message
    		            		for (int i = 0; i < buyers.length; i++){
    		            			propose.addReceiver(buyers[i].getName());
    		            		}
    							send(propose);
    						}
    						
    						break;
    						
    					case ACLMessage.ACCEPT_PROPOSAL:
    						numAnswers++;
    						readMessage(msg, true);
							
							if(numAnswers >= buyers.length ){
								numAnswers = 0;
								seeAnswers();
    						}
    						break;
    						
    					case ACLMessage.REJECT_PROPOSAL:
    						numAnswers++;
    						readMessage(msg, false);
    						
							if(numAnswers >= buyers.length ){
								numAnswers = 0;
								seeAnswers();
    						}
    						break;	
    						
    					case ACLMessage.CONFIRM:
    						numAnswers++;
    						
    						if(numAnswers >= buyers.length){
    							numAnswers = 0;
    							bid = 0;
    							nextStep();
    						}
    						
    						break;
    					}
    						
    				}else{
    					block();
    				}	
    			}
            	
            	public void readMessage(ACLMessage msg, boolean win){    				
    				for (int i = 0; i < buyers.length; i++){
        				
        				if(buyers[i].getName().equals(msg.getSender())){
    						System.out.println("[SellerAgent] Buyer: " + buyers[i].getName().getLocalName() + " Accept:" + win + " Bid: " + bid);
    						
    						//Print on File
    	            		if(seller.isToPrint()){   
	    						pw.println("[SellerAgent] Buyer: " + buyers[i].getName().getLocalName() + " Accept:" + win + " Bid: " + bid);
	    						pw.flush();
    	            		}
    	            		
    						ans[i] = win;
    						
    						return;
    					}
    				}
    			}
            	
            	public void seeAnswers(){
            		List<Integer> winners = new ArrayList<>();
            		
            		for (int i = 0; i < buyers.length; i++){
            			if(ans[i]){
            				winners.add(i);
            			}
            		}
            		
            		if(winners.size() > 0){
            			int theWinner = winners.get(r.nextInt(winners.size()));
            			
            			ACLMessage win = new ACLMessage(ACLMessage.AGREE);
						win.addReceiver(buyers[theWinner].getName());
						
						int loseReceivers = 0;
						ACLMessage lose = new ACLMessage(ACLMessage.CANCEL);
						for (int i = 0; i < buyers.length; i++){
							if (i != theWinner){
		            			lose.addReceiver(buyers[i].getName());
		            			loseReceivers++;
							}
	            		}
						
						//Print on File
	            		if(seller.isToPrint()){   
							pw.println("[SellerAgent] Winner: " + buyers[theWinner].getName().getLocalName());
							pw.println("[SellerAgent] Winner Bid: " + bid);
							pw.println(" ");
							pw.flush();
	            		}
						
						send(win);
						if(loseReceivers > 0){
							send(lose);
						}
						bid = 0;
						
            		}else{
            			if(bid >= 0){
            				bid = bid -1;
            				ACLMessage propose = new ACLMessage(ACLMessage.PROPOSE);
    						propose.setContent(Integer.toString(bid));
    						
    						//add recipients to the message
    	            		for (int i = 0; i < buyers.length; i++){
    	            			propose.addReceiver(buyers[i].getName());
    	            		}
    						send(propose);
            			}else{
            				bid = 0;
            				nextStep();
            			}
            		}      
            	}
            	
            	public void nextStep(){
            		switch(seller.decRound()){
					case 0:
						onStart();
						break;
					case 1:
						onStart();
						break;
					case 2:
						pw.close();
						seller.getAuction().killAgents();
						break;
					}
            	}
            });
        }
        
        
	}
}
