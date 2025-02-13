package tp1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
		LocalDateTime date = java.time.LocalDateTime.now();
		String sdate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_H:m:s"));
		System.out.println(nomFichierClient);
		String tab[] = nomFichierClient.split("[.]"); //c'est une expression régulière donc le . veut dire nimporte quoi donc il reste plus rien : donc mettre [] pour dire explicitement le .
		nomFichierCopie = CHEMIN_SERVEUR+sdate+"."+tab[tab.length-1];

		
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
	public void gererDownload(String nomFichierServeur) {
		//Pour l'instant le nom contient le chemin d'accès, à voir si on le laisse
		File fichier = new File(CHEMIN_SERVEUR+nomFichierServeur);
		if (fichier.exists() && fichier.isDirectory() == false && fichier.canRead()) {
			System.out.println("S : Ok j'envoie le nombre d'octets du fichier");
			try {
				this.sortie.writeUTF("OK: J'envoie le nombre d'octets du fichier");
			} catch (IOException e) {
				System.out.println("S : ERREUR lors de l'écriture dans le flux");
				e.printStackTrace();
				System.exit(0);;			}
		}
		else {
			System.err.println("S : ERREUR Le fichier demandé n'existe pas");
			try {
				this.sortie.writeUTF("ERREUR:Le fichier demandé n'existe pas");
			} catch (IOException e) {
				System.out.println("S : ERREUR lors de l'écriture dans le flux");
				e.printStackTrace();
				System.exit(0);;
			}
			
		}
		//Envoie du nombre d'octets du fichier
		try {
			this.sortie.writeInt((int)fichier.length());
		} catch (IOException e) {
			System.out.println("S : ERREUR lors de l'écriture dans le flux");
			e.printStackTrace();
			System.exit(0);;
		}
		
		FileInputStream fis = null;
		//ouverture des flux du fichier
		try {
			fis = new FileInputStream(CHEMIN_SERVEUR+nomFichierServeur);
		} catch (FileNotFoundException e) {
			System.out.println("S : ERREUR lors de l'ouverture du fichier");
			e.printStackTrace();
			System.exit(0);
		}
		BufferedInputStream bis = new BufferedInputStream(fis);
		
		//Envoie des octets du fichier
		System.out.println("Envoie des octets du fichier");
		byte[] tab = new byte[1024];
		int n;
		try {
			while ((n = bis.read(tab)) > 0) {
				this.sortie.write(tab, 0, n);
			}
		}
		catch (IOException e) {
			System.out.println("C : ERREUR lors de la lecture et écriture dans les fichiers");
			e.printStackTrace();
			System.exit(0);
		}
		
		try {
			bis.close();
			fis.close();
		} catch (IOException e) {
			System.out.println("C : ERREUR lors de la fermeture des Streams");
			e.printStackTrace();
			System.exit(0);
		}
		
	}
	
	public void gererList(String extension) {
		File dossier = new File(CHEMIN_SERVEUR);
		File listeFichiers[] = dossier.listFiles();
		boolean all = extension.compareTo("all") == 0;
		int i;
		
		System.out.println("S : On envoie les fichier se terminant par "+extension);
		for (i = 0; i < listeFichiers.length; i++) {
			if (all || listeFichiers[i].getName().endsWith(extension)) {
				try {
					this.sortie.writeUTF(listeFichiers[i].getName());
				} catch (IOException e) {
					System.out.println("C : ERREUR lors de l'écriture dans le fichier");
					e.printStackTrace();
					System.exit(0);
				}
			}
		}
		
		//Envoie de fin
		try {
			this.sortie.writeUTF("fin");
		} catch (IOException e) {
			System.out.println("C : ERREUR lors de l'écriture dans le fichier");
			e.printStackTrace();
			System.exit(0);
		}
		
		
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
		else if (tokens[0].compareTo("GET") == 0) {
			System.out.println("S : Commande GET reçue : "+commande);
			gererDownload(tokens[1]);
		}
		else if (tokens[0].compareTo("LIST") == 0) {
			System.out.println("S : Commande LIST reçue : "+commande);
			gererList(tokens[1]);
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
