package tp3;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class Parking implements Runnable{

	private int capacite;
	private double tarifHoraire;
	private double chiffreAffaire;
	private volatile HashMap<Integer, Voiture> voituresGarees;
	private volatile LinkedList<Voiture> fileAttente;
	private Random r = new Random();
	
	public Parking(int c, double th) {
		this.chiffreAffaire = 0;
		this.capacite = c;
		this.tarifHoraire = th;
		
		this.voituresGarees = new HashMap<Integer, Voiture>();
		this.fileAttente = new LinkedList<Voiture>();
		
		System.out.println("Parking de "+capacite+"de tarif horaire "+this.tarifHoraire+" créé.");
	}
	
	public synchronized boolean estPlein() {
		return (this.capacite == this.voituresGarees.size());
	}
	public synchronized boolean existeVoitureEnAttente() {
		//System.out.println(this.fileAttente.isEmpty());
		return (!(this.fileAttente.isEmpty()));
	}
	
	public synchronized void arriverVoiture(Voiture v) {
		this.fileAttente.add(v);
	}
	public synchronized void departVoitureImpatience(Voiture v) {
		this.fileAttente.remove(v);
	}
	
	public synchronized void departVoiture(Voiture v) {
		this.voituresGarees.remove(v.getNumPlace());
		System.out.println("La voiture "+v.getImmatriculation()+" libère la place "+v.getNumPlace());
		System.out.println("Duree de stationnement : "+v.getDureeStationnement()+", gain : "+this.tarifHoraire*v.getDureeStationnement()/1000);
		
		this.chiffreAffaire += this.tarifHoraire*v.getDureeStationnement()/1000;
		
		notifyAll();
	}
	
	public synchronized void garerVoiture() {
		Voiture v;
		try {
			if (this.estPlein()) {
				System.out.println("Le parking est plein.");
				wait();
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			System.exit(1);
		}
		
		//Sort la première voiture de la file d'attente
		v = this.fileAttente.removeFirst();
		
		//On choisi le numéro de la place
		v.setNumPlace(1+this.voituresGarees.size());
	
		//On la place
		this.voituresGarees.put(v.getNumPlace(), v);
		System.out.println("La voiture "+v.getImmatriculation()+" est garée en place "+v.getNumPlace());
		v.setStatut(Voiture.GAREE);
	}



	public void run() {
		while (!(Thread.currentThread().isInterrupted())) {
			Thread.interrupted();
			//System.out.println("On attends ");
			if (this.existeVoitureEnAttente()) {
				System.out.println("On gare la voiture");
				this.garerVoiture();
			}
		}
		System.out.println("Le chiffre d'affaires total réalisé est de "+this.chiffreAffaire);
		
	}
	
	
	public static void main(String[] args) {
		Parking p = new Parking(2, 10);
			
		// temps de stationnement, patience
		Voiture v1 = new Voiture("V1", 2000, 1000, p);
		Voiture v2 = new Voiture("V2", 2000, 1000, p);
		Voiture v3 = new Voiture("V3", 1000, 5000, p);

		Thread tp = new Thread(p);
		Thread tv1 = new Thread(v1);
		Thread tv2 = new Thread(v2);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Thread tv3 = new Thread(v3);
		
		tp.start();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		tv1.start();
		tv2.start();
		tv3.start();
		
	
	}
}
