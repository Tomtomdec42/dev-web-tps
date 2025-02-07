package tp1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

	private static final String CHEMIN_CLIENT = "./src/tp1/FichiersClient/";
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
		System.exit(0);
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
		long tailleFichier = 0;
		//Envoi du nom du fichier
		try {
			this.sortie.writeUTF("PUT:"+nomFichier);
		} catch (IOException e) {
			System.err.println("C : ERREUR lors de l'envoie du nom du fichier");
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("C : envoie du nom du fichier réussi");
		//Récupération du nom de la copie
		try {
			cheminRecu = this.entree.readUTF();
		} catch (IOException e) {
			System.err.println("C : ERREUR lors de la réception du chemin");
			e.printStackTrace();
			System.exit(0);
		}
		
		System.out.println("C : réception du chemin réussi : "+cheminRecu);
		
		File fichier = new File(cheminRecu);
		if (!(fichier.exists())) {
			System.err.println("C : ERREUR le fichier n'existe pas");
			this.seDeconnecter();
		}
		else if (!(fichier.canRead())) {
			System.err.println("C : ERREUR le fichier ne possède pas le droit en écriture");
			this.seDeconnecter();
		}
		else if (!(fichier.isFile())) {
			System.err.println("C : ERREUR le fichier n'est pas un fichier");
			this.seDeconnecter();
		}
		
		tailleFichier = fichier.length();
		System.out.println("C : Fichier reçu : taille "+tailleFichier);
		
	}
	
	public static void main(String[] args) {
		Client c = new Client("127.0.0.1", 2121);
		c.initierConnexion();
		c.lireManuel();
		c.seDeconnecter();
		
	}
	//TODO voir si le git marche avec la config d'eclispe sur la fac

}
