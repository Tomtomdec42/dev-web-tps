package tp1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {
	
	private static final int PORT = 2121;
	private static final String CHEMIN_SERVEUR = "./src/tp1/FichiersServeur/";
	private ServerSocket servSocket;
	private Socket cliSocket;
	private DataInputStream entree;
	private DataOutputStream sortie;
	

	public Serveur() {
		try {
			this.servSocket = new ServerSocket(Serveur.PORT);

			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("S : ERREUR impossible de lancer le serveur");
			System.exit(0);
		}
		System.out.println("S : Lancement du serveur réussi");

	}
	private void ouvrirFlux() {
		//Ouverture du flux d'entrée
		try {
			this.entree = new DataInputStream(cliSocket.getInputStream());
		} catch (IOException e) {
			System.out.println("S : impossible de récupérer l'entrée");
			e.printStackTrace();
			System.exit(0);
		}
		
		//Ouverture du flux de sortie
		try {
			this.sortie = new DataOutputStream(cliSocket.getOutputStream());
		} catch (IOException e) {
			System.out.println("S : impossible de récupérer la sortie");
			e.printStackTrace();
			System.exit(0);
		}
		
		System.out.println("S : Récupération des flux réussis");
	}
	public void envoyerManuel() {
		String manuel = "manuel";

		try {
			this.sortie.writeUTF(manuel);
		} catch (IOException e) {
			System.out.println("S : ERREUR lors de l'envoie du manuel");
			e.printStackTrace();
		}
		
		
		
		System.out.println("S : Envoie du manuel réussi");
	}
	public void deconnecteClient() {
		try {
			this.entree.close();
			this.entree = null;
		} catch (IOException e) {
			System.out.println("S : ERREUR lors dela fermeture du flux d'entrée");
			e.printStackTrace();
			System.exit(0);
		}
		try {
			this.sortie.close();
			this.sortie = null;
		} catch (IOException e) {
			System.out.println("S : ERREUR lors dela fermeture du flux de sortie");
			e.printStackTrace();
			System.exit(0);
		}
		try {
			this.cliSocket.close();
			this.cliSocket = null;
		} catch (IOException e) {
			System.out.println("S : ERREUR lors dela fermeture de la socket du client");
			e.printStackTrace();
			System.exit(0);
		}
		
		System.out.println("S : Client déconnecté");
	}
	public void lireCommande() {
		String commande = null;
		String tokens[] = null;
		try {
			commande = this.entree.readUTF();
		} catch (IOException e) {
			System.out.println("S : ERREUR lors de la lecture de la commande");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		tokens = commande.split(":");
		if (tokens[0].equals("PUT")) {
			
		}
		else {
			
		}
	}
	
	
	public static void main(String[] args) {
		Serveur s = new Serveur();
		
		for (;;) {
			
			//On accepte la connexion du client
			try {
				s.cliSocket = s.servSocket.accept();
				s.ouvrirFlux();
				s.envoyerManuel();
			} catch (IOException e) {
				System.out.println("S : ERREUR impossible d'accepter la demande de connexion");
				e.printStackTrace();
				System.exit(0);
			}
			
			
			
		}
		
	}

}
