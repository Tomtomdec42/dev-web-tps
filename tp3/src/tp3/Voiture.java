package tp3;

public class Voiture implements Runnable{

	public static int GAREE = 1;
	public static int EN_ATTENTE = 0;
	public static int TEMPS_TERMINE = 2;
	public static int IMPATIENT = 3;
	
	private String immatriculation;
	private long tempsDebutAttente;
	private int dureeStationnement;
	private int patience;
	private Parking parking;
	private volatile int statut;
	private volatile int numPlace;
	
	public Voiture(String i, int ds, int p, Parking pk) {
		this.immatriculation = i;
		this.dureeStationnement = ds;
		this.patience = p;
		this.parking = pk;
		this.numPlace = -1;
		this.statut = -1;
	}
	
	public String getImmatriculation() {
		return this.immatriculation;
	}
	public int getDureeStationnement() {
		return this.dureeStationnement;
	}
	public void setStatut(int s) {
		this.statut = s;
	}
	int getNumPlace() {
		return this.numPlace;
	}
	void setNumPlace(int np) {
		this.numPlace = np;
	}
	
	
	
	public void arriveeParking() {
		this.parking.arriverVoiture(this);
		this.setStatut(0);
		System.out.println("La voiture "+this.getImmatriculation()+" est dans la file d'attente");
		this.tempsDebutAttente = (int)(System.currentTimeMillis());
	}
	public void departParkingImpatience() {
		this.parking.departVoitureImpatience(null);
		this.statut = Voiture.IMPATIENT;

		Thread.interrupted();
		
	}
	public void departParking() {
		System.out.println("ok je me barre");
		this.parking.departVoiture(this);
		this.setNumPlace(-1);
		this.setStatut(Voiture.TEMPS_TERMINE);
		
	}
	public void run() {
		try {
			long dureeAttente;
			this.arriveeParking();
			while (this.statut == Voiture.EN_ATTENTE) {
				dureeAttente = (int)((System.currentTimeMillis())-this.tempsDebutAttente);
				if (dureeAttente > this.patience) {
					System.out.println("La voiture "+this.getImmatriculation()+" part du parking impatient");
					this.departParkingImpatience();
				}
			}
		
			if (this.statut == Voiture.GAREE) {
				//System.out.println("ON attends ");
				Thread.sleep(this.dureeStationnement);
				//La voiture quitte le parking
				this.departParking();
				
				
			}
			System.out.println("La voiture "+this.getImmatriculation()+" s'en vas, raison "+this.statut+".");
			Thread.currentThread().interrupt();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
