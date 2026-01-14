package it.thisone.iotter.rest.model;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;


@ApiModel(value="IotParameter",description="Measurement Parameter")
public class ParamConfiguration implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("oid")
	public String getOid() {
		return oid;
	}

	@JsonProperty("sn")
	public String getSerial() {
		return serial;
	}


	@JsonProperty("id")
	public String getId() {
		return id;
	}


	@JsonProperty("unit")
	public int[] getUnit() {
		return unit;
	}

	@JsonProperty("scale")
	public float[] getScale() {
		return scale;
	}

	@JsonProperty("offset")
	public float[] getOffset() {
		return offset;
	}

	@JsonProperty("format")
	public String[] getFormat() {
		return format;
	}

	@JsonProperty("label")
	public String getLabel() {
		return label;
	}

	@JsonProperty("sublabel")
	public String getSublabel() {
		return sublabel;
	}

	@JsonProperty("hide_id")
	public int getHide_id() {
		return hide_id;
	}


	@JsonProperty("desc")
	public String getDesc() {
		return desc;
	}


	@JsonProperty("qual")
	public int getQual() {
		return qual;
	}


	@JsonProperty("sens")
	public int getSens() {
		return sens;
	}
	
	public ParamConfiguration() {
	}
	
	@JsonIgnore
	private String oid;

	/* MANDATORY = FALSE se si tratta di strumento singolo
	 * Serial number dello strumento a cui appartiene il canale/parametro di misura. 
	 * Questo valore deve essere impostato nel caso di strumento composto da vari nodi 
	 * per indicare il nodo al quale il canale/parametro appartiene.
	 */
	@JsonIgnore
	private String serial;
	
	/*
	 * Numero del canale/parametro
	 */
	@JsonIgnore
	private String id;

	
	/*
	 * Elenco delle possibili unità di misura per le quali è possibile offrire 
	 * una visualizzazione dei dati. I valori rappresentano dei codici la cui associazione 
	 * con la rappresentazione a stringa (es. C°, g, Kg, etc.) 
	 * sarà imputabile da parte del supervisore della piattaforma 
	 * con un sezione ad hoc della interfaccia grafica. 
	 * Il simbolo della unità di misura verrà rappresentato sugli assi dei grafici 
	 * ai quali il canale/parametro appartiene.
	 */
	@JsonIgnore
	private int[] unit;
	

	/*
	 * Contiene eventuali fattori di scale relativi all'unità di misura. 
	 * La chiave quindi conterrà il valore per il quale moltiplicare 
	 * il dato di misura comunicato dallo strumento e salvato a data base. 
	 * La logica è tale che se l'utente scegli l'unità di misura, 
	 * per esempio, in posizione 3 dell'array della chiave unit 
	 * la piattaforma utilizzerà il valore in posizione 3 
	 * dell'array scale come fattore di scala
	 */
	@JsonIgnore
	private float[] scale;


	/*
	 * L'attuale struttura è un array di float che rappresentano il fattore di conversione 
	 * tra la scale di default, identificata dal primo elemento dell'array presente nella chiave unit, 
	 * con la quale sono comunicati i dati e la scala scelta dall'utente tra le possibili scale contenute 
	 * nel vettore. La proposta prevede di aggiungere anche un offset utile per alcuni tipi di conversioni 
	 * (es. Celsius/Fahrenheit). 
	 * in modo da applicare la trasformazione y= scale*x + offset.
	 */
	@JsonIgnore
	private float[] offset;
	
	/*
	 * la chiave rappresenta un array di stringhe utili per la formattazione del risultato della 
	 * trasformazione applicata con scale. Le stringhe sono del tipo: "N.M" dove N rappresenta 
	 * il numero di cifre primo della virgola e M il numero di cifre di risoluzione. 
	 * Nel caso di overflow del risultato si adottano le procedure di visualizzazione descritte nel seguito.
	 */
	@JsonIgnore
	private String[] format;
	


	/*
	 * Stringa da associare al parametro. 
	 * Questa stringa verrà visualizzata sia nella legenda dei grafici 
	 * che contengono la visualizzazione del parametro che durante 
	 * la scelta del parametro di uno strumento nella composizione di un grafico.
	 */
	@JsonIgnore
	private String label;


	/*
	 * stringa da associare al parametro. 
	 * Questa stringa è simile alla precedente ma se presente verrà rappresentata 
	 * come pedice di ciò che è riportati nella chiave label.
	 */
	@JsonIgnore
	private String sublabel;

	/*
	 * con questa chiave non obbligatoria si permette di visualizzare o meno 
	 * l'identificativo associato al parametro durante la configurazione dei grafici. 
	 * Se la chiave non è presente o il suo valore è false comporta che all'utente venga visualizzato, 
	 * come stringa identificativa del parametro, la sequenza "id label sublabel". 
	 * Nel caso di chiave a true all'utente si visualozzerà solo "label sublabel"
	 */
	@JsonIgnore
	private int hide_id;

	
	/*
	 * stringa contenete una descrizione del parametro. 
	 * Potrà essere visualizzata in aggiunta, ad esempio come tool-tip, 
	 * in vari punti dell'interfaccia grafica.
	 */
	@JsonIgnore
	private String desc;

	
	/*
	 * qualificatore di una misura
	 */
	@JsonIgnore
	private int qual;

	/*
	 * tipo sensore
	 * Feature #247 Introduzione dei codici SENSOR per i parametri dello strumento
	 */
	@JsonIgnore
	private int sens;
	
	


	public void setOid(@JsonProperty("oid") String oid) {
		this.oid = oid;
	}


	public void setSerial(@JsonProperty("sn") String serial) {
		this.serial = serial;
	}


	public void setId(@JsonProperty("id") String id) {
		this.id = id;
	}

	public void setUnit(@JsonProperty("unit") int[] unit) {
		this.unit = unit;
	}


	public void setScale(@JsonProperty("scale") float[] scale) {
		this.scale = scale;
	}


	public void setOffset(@JsonProperty("offset") float[] offset) {
		this.offset = offset;
	}


	public void setFormat(@JsonProperty("format") String[] format) {
		this.format = format;
	}


	public void setLabel(@JsonProperty("label") String label) {
		this.label = label;
	}


	public void setSublabel(@JsonProperty("sublabel") String sublabel) {
		this.sublabel = sublabel;
	}


	public void setHide_id(@JsonProperty("hide_id") int hide_id) {
		this.hide_id = hide_id;
	}


	@JsonIgnore
	public void setDesc(String desc) {
		this.desc = desc;
	}


	@JsonIgnore
	public void setQual(int qual) {
		this.qual = qual;
	}


	@JsonIgnore
	public void setSens(int sens) {
		this.sens = sens;
	}

}