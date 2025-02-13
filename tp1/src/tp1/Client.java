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
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

	private static final String CHEMIN_CLIENT = "./src/tp1/FichiersClient/";
	private static final String CHEMIN_SERVEUR = "./src/tp1/FichiersServeur/";

	private String hote;
	private int port;
	private Socket cliSocket;
	private DataInputStream entree;
	private DataOutputStream sortie;
	
	public Client(String h, int p) {
		hote = h;
		port = p;
	}
		
	private void initierConnexion() {
		try {
			this.cliSocket = new Socket(hote, port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.err.println("C : L'hote n'est pas connu");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("C : Impossible de récupérer la socket");
			System.exit(0);
		}
		
		try {
			this.entree = new DataInputStream(cliSocket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("C : Impossible de récupérer l'entrée");
			System.exit(0);
		}
		
		try {
			this.sortie = new DataOutputStream(cliSocket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("C : Impossible de récupérer la sortie");
			System.exit(0);
		}
		
		System.out.println("C : Connexion au serveur réussie");
		
	}
	public void seDeconnecter() {
		//On ferme les flux d'entrée et de sortie
		try {
			this.entree.close();
		} catch (IOException e) {
			System.out.println("C : impossible de fermer l'entrée");
			e.printStackTrace();
			System.exit(0);
		}
		try {
			this.sortie.close();
		} catch (IOException e) {
			System.out.println("C : impossible de fermer la sortie");
			e.printStackTrace();
			System.exit(0);
		}
		
		try {
			this.cliSocket.close();
		} catch (IOException e) {
			System.out.println("C : impossible de fermer la socket");
			e.printStackTrace();
			System.exit(0);
		}
		
		System.out.println("C : Fermeture des fluxs et de la socket");
	}
	
	public void lireManuel() {
		String manuel = null;
		
		try {
			manuel = this.entree.readUTF();
		} catch (IOException e) {
			System.err.println("C : ERREUR lors de la lecture du manuel");
			e.printStackTrace();
			System.exit(0);
		}
		
		System.out.println("C : Récupération du manuel");
		System.out.println(manuel);
	}
	
	public void uploader(String nomFichier) {
		String cheminRecu = null;
		int tailleFichier = 0;
		boolean ok_reponse = false;
		//Envoi du nom du fichier avec PUT
		try {
			this.sortie.writeUTF("PUT:"+nomFichier);
		} catch (IOException e) {
			System.err.println("C : ERREUR lors de l'envoie du nom du fichier");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("C : envoie du nom du fichier réussi");
		
		
		//Récupération d'un entier, 1 ça baigne, on envoie le nombre de fichier, 0 quitte
		try {
			ok_reponse = this.entree.readBoolean();
		} catch (IOException e) {
			System.out.println("C : ERREUR lors de la lecture dans le flux");
			e.printStackTrace();
			System.exit(0);
		}
		
		if (!(ok_reponse)) {
			//Le client se déconnecte = on termine la fonction et on fait le seDéconnecter juste après l'appel de la fonction
			return;
		}
		
		System.out.println("C : On ouvre le fichier pour envoyer sa taille : "+nomFichier);
		//Ouverture du fichier pour l'envoie de sa taille
		File fichier = new File(nomFichier);
		if (!(fichier.exists())) {
			System.err.println("C : ERREUR le fichier n'existe pas");
			this.seDeconnecter();
			System.exit(0);
		}
		else if (!(fichier.canRead())) {
			System.err.println("C : ERREUR le fichier ne possède pas le droit en écriture");
			this.seDeconnecter();
			System.exit(0);
		}
		else if (!(fichier.isFile())) {
			System.err.println("C : ERREUR le fichier n'est pas un fichier");
			this.seDeconnecter();
			System.exit(0);
		}
		
		tailleFichier = (int)(fichier.length());
		System.out.println("C : Taille du fichier : "+tailleFichier);
		
		System.out.println("C : On envoie la taille du fichier : "+tailleFichier);
		//Envoie de la taille du fichier
		try {
			this.sortie.writeInt(tailleFichier);
		} catch (IOException e) {
			System.out.println("C : ERREUR lors de l'ecriture dans le flux");
			e.printStackTrace();
			System.exit(0);
		}
		
		System.out.println("C : On attends la confirmation");
		//Lecture de la confirmation et demande du contenu du fichier
		try {
			ok_reponse = this.entree.readBoolean();
		} catch (IOException e) {
			System.out.println("C : ERREUR lors de la lecture dans le flux");
			e.printStackTrace();
			System.exit(0);
		}
		
		if (!(ok_reponse)) {
			return;
		}
		
		
		//Envoi progressif des octets du serveur
		System.out.println("C : Ouverture du fichier à envoyer");
		//Ouverture du fichier
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fichier);
		} catch (FileNotFoundException e) {
			System.out.println("C : ERREUR lors de l'ouverture du fichier");
			e.printStackTrace();
			System.exit(0);
		}
		BufferedInputStream bis = new BufferedInputStream(fis);
		
		//écriture dans le flux de sortie
		System.out.println("C : Envoi des octets du fichier");
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
		
		
		//Fermeture
		System.out.println("C : On ferme les flux");
		try {
			fis.close();
		} catch (IOException e) {
			System.out.println("C : ERREUR lors de la fermeture des Streams");
			e.printStackTrace();
			System.exit(0);
		}
		
		
		
		//Attente de la réception du nom de la copie du fichier sur le serveur
		try {
			cheminRecu = this.entree.readUTF();
		} catch (IOException e) {
			System.err.println("C : ERREUR lors de la réception du chemin");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("C : réception du chemin réussi : "+cheminRecu);
		
		//Se déconnecte après l'appel de cette fonction dans le main
		
		
	}
	
	public void downloader(String nomFichierServeur) {
		int nbOctets = 0;
		int i;
		byte b = 0;
		String reponse = null;
		//Envoie de la commande GET
		try {
			System.out.println("C : Envoie de la commande GET:"+nomFichierServeur);
			this.sortie.writeUTF("GET:"+nomFichierServeur);
		} catch (IOException e) {
			System.err.println("C : ERREUR lors de l'écriture dans le flux de sortie");
			e.printStackTrace();
			System.exit(0);
		}
		
		//Réception de la réponse du serveur
		try {
			reponse = this.entree.readUTF();
		} catch (IOException e) {
			System.err.println("C : ERREUR lors de la lecture dans le flux de sortie");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("C : "+reponse);
		
		if (reponse.split(":")[0].compareTo("ERREUR") == 0) {
			System.out.println("C : déconnexion");
			this.seDeconnecter();
		}
		else {
			
			//Réception du nombre d'octets à télécharger
			System.out.println("C : Lecture du nombre d'octets");
			try {
				nbOctets = this.entree.readInt();
				System.out.println("C : Nombre d'octets : "+nbOctets);
			} catch (IOException e) {
				System.err.println("C : ERREUR lors de la lecture dans le flux");
				e.printStackTrace();
				System.exit(0);
			}
			
			//Ouverture du fichier
			FileOutputStream fos = null;
			try {
				String[] tab = nomFichierServeur.split("/");
				fos = new FileOutputStream(CHEMIN_CLIENT+tab[tab.length-1]);
			} catch (FileNotFoundException e) {
				System.out.println("C : ERREUR lors de l'ouverture du fichier");
				e.printStackTrace();
				System.exit(0);
			}
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			
			//Réception du serveur des octets du fichier
			System.out.println("C : Réception des octets du fichier");
			for (i = 0; i < nbOctets; i++) {
				try {
					b = this.entree.readByte();
				} catch (IOException e) {
					System.err.println("C : ERREUR lors de la lecture dans le flux");
					e.printStackTrace();
					System.exit(0);
				}
				
				try {
					bos.write(b);
					bos.flush();
				} catch (IOException e) {
					System.err.println("C : ERREUR lors de l'écriture dans le flux");
					e.printStackTrace();
					System.exit(0);
				}
			}
			
			System.out.println("C : Réception du fichier ok, déconnexion");
			try {
				bos.close();
				fos.close();
			} catch (IOException e) {
				System.out.println("C : ERREUR lors de la fermeture des flux");
				e.printStackTrace();
			}
			
			this.seDeconnecter();

		}
	}
	
	public static void main(String[] args) {
		Client c = new Client("127.0.0.1", 2121);
		c.initierConnexion();
		c.lireManuel();
		//c.uploader(CHEMIN_CLIENT+"toucan.jpg");
		c.downloader(CHEMIN_SERVEUR+"tigre.jpg");
		c.seDeconnecter();
		
	}

}
