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
		
		Thread.currentThread().notifyAll();
	}
	
	public synchronized void garerVoiture() {
		Voiture v;
		try {
			if (this.estPlein()) {
				System.out.println("Le parking est plein.");
				Thread.currentThread().wait();
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
	
	
	public static void main(String[] args) {

	}

	public void run() {
		
	}

}
