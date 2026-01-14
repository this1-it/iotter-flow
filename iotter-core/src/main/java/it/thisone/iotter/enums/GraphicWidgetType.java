package it.thisone.iotter.enums;

import it.thisone.iotter.common.Internationalizable;

public enum GraphicWidgetType  implements Internationalizable {
	CUSTOM(10),
	WEBPAGE(9),
	EMBEDDED(8),
	HISTOGRAM(7),
	LAST_MEASURE_TABLE(5),
	LABEL(4),
	TABLE(3),
	LAST_MEASURE(2),
	WIND_ROSE(1),
	MULTI_TRACE(0);

	/*
	LINEAR(1),
	SPECTROGRAM(2),
	HISTOGRAM_SPECTRA(3),
	FFT(4),
	PROBABILITY_DISTRIBUTION(5),
	EVENTS_AUDIO_VIDEO(6),
	CHART_RADAR(7),
	GEO_MAP(8),
	INTERVAL_INDICATOR(9),
	GAUGE(10),
	DIRECTIONAL(11) ,
	WIND_ROSE(12) ,
	UNKNOWN(99);
*/
	@SuppressWarnings("unused")
	private int value;

	@Override
	public String getI18nKey() {
        return "enum.graphwidgettype." + name().toLowerCase();        
    }

	private GraphicWidgetType(int value) {
		this.value = value;
	}

}
