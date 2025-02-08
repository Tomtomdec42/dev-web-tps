package tp1;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
	public void gererUpload(String nomFichierClient) {
		int nbOctets = -1;
		int i;
		byte b = 0;
		String nomFichierCopie = null;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		
		System.out.println("On attends");
		//Envoie de la demande du nombre d'octets (1 car ça baigne)
		try {
			this.sortie.writeBoolean(true);
		} catch (IOException e) {
			System.out.println("S : ERREUR lors de l'écriture dans le flux");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("S : On demande le nombre d'octets");
		
		//Attente du nombre d'octets du fichier (-1 si ya un problème et qu'on doit déco le client)
		try {
			nbOctets = this.entree.readInt();
		} catch (IOException e) {
			System.out.println("S : ERREUR lors de la lecture dans le flux");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("S : Nombre d'octets reçus : "+nbOctets);
		
		//Envoie confirmation et demande du contenu du fichier (envoie de true)
		try {
			this.sortie.writeBoolean(true);
		} catch (IOException e) {
			System.out.println("S : ERREUR lors de l'écriture dans le flux");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("S : On demande le contenu du fichier");
		
		//Construire le nom du fichier
		String tab[] = nomFichierClient.split("/");
		nomFichierCopie = CHEMIN_SERVEUR+tab[tab.length-1];
		
		System.out.println("S : On construit le nom de la copie et on lit le fichier");
		//Ouvrir le fichier résultat de la copie
		
		try {
			fos = new FileOutputStream(nomFichierCopie);
		} catch (FileNotFoundException e) {
			System.out.println("S : ERREUR lors de l'ouverture du fichier");
			e.printStackTrace();
			System.exit(0);
		}
		bos = new BufferedOutputStream(fos);
		
		//Réception des octets du fichier et écriture progressive
		for (i = 0; i < nbOctets; i++) {
			try {
				b = this.entree.readByte();
			} catch (IOException e) {
				System.out.println("S : ERREUR lors de la lecture dans le flux");
				e.printStackTrace();
				System.exit(0);
			}
			
			try {
				bos.write(b);
				bos.flush();
			} catch (IOException e) {
				System.out.println("S : ERREUR lors de l'écriture dans le fichier");
				e.printStackTrace();
				System.exit(0);
			}
		}
		System.out.println(i);
		try {
			bos.close();
			fos.close();
		} catch (IOException e) {
			System.out.println("S : ERREUR lors de la fermeture");
			e.printStackTrace();
		}
		
		System.out.println("S : On a copié le fichier, on envoie le nom du fichier au client");
		//Une fois tous les octets récupérés, envoi du nom du fichier au client
		try {
			this.sortie.writeUTF(nomFichierCopie);
		} catch (IOException e) {
			System.out.println("S : ERREUR lors de l'écriture dans le flux");
			e.printStackTrace();
			System.exit(0);;
		}
		
		System.out.println("S : On déconnecte le client");
		this.deconnecteClient();

	}
	
	public void lireCommande() {
		String commande = null;
		String tokens[] = null;
		try {
			commande = this.entree.readUTF();
		} catch (IOException e) {
			System.out.println("S : ERREUR lors de la lecture de la commande");
			e.printStackTrace();
		}
		
		tokens = commande.split(":");
		if (tokens[0].equals("PUT")) {
			System.out.println("S : Commande PUT reçue : "+commande);
			gererUpload(tokens[1]);
		}
		else {
			this.deconnecteClient();
			//Envoie au client qu'il doit se déconnecter
		}
	}
	public void ecouter() {
		for (;;) {
			
			//On accepte la connexion du client
			try {
				this.cliSocket = this.servSocket.accept();
				this.ouvrirFlux();
				this.envoyerManuel();
				this.lireCommande();
			} catch (IOException e) {
				System.out.println("S : ERREUR impossible d'accepter la demande de connexion");
				e.printStackTrace();
				System.exit(0);
			}
			
			
			
		}
	}
	
	public static void main(String[] args) {
		Serveur s = new Serveur();
		
		s.ecouter();
		
	}

}
