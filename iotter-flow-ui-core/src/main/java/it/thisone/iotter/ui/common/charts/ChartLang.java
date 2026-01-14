package it.thisone.iotter.ui.common.charts;

import com.vaadin.addon.charts.model.Lang;

/**
 * Bug #280 [HighCharts] Some internationalization issues
 */

public class ChartLang extends Lang {

	private static final long serialVersionUID = 1L;

	private String contextButtonTitle; // Chart context menu
	private String downloadJPEG; // Download JPEG image
	private String downloadPDF; // Download PDF document
	private String downloadPNG; // Download PNG image
	private String downloadSVG; // Download SVG vector image
	private String printChart; // Print chart
	private String noData ; // No Data To Display

	private String loading ; // Loading
	
	public String getContextButtonTitle() {
		return contextButtonTitle;
	}
	public void setContextButtonTitle(String contextButtonTitle) {
		this.contextButtonTitle = contextButtonTitle;
	}
	public String getDownloadJPEG() {
		return downloadJPEG;
	}
	public void setDownloadJPEG(String downloadJPEG) {
		this.downloadJPEG = downloadJPEG;
	}
	public String getDownloadPDF() {
		return downloadPDF;
	}
	public void setDownloadPDF(String downloadPDF) {
		this.downloadPDF = downloadPDF;
	}
	public String getDownloadPNG() {
		return downloadPNG;
	}
	public void setDownloadPNG(String downloadPNG) {
		this.downloadPNG = downloadPNG;
	}
	public String getDownloadSVG() {
		return downloadSVG;
	}
	public void setDownloadSVG(String downloadSVG) {
		this.downloadSVG = downloadSVG;
	}
	public String getPrintChart() {
		return printChart;
	}
	public void setPrintChart(String printChart) {
		this.printChart = printChart;
	}
	public String getNoData() {
		return noData;
	}
	public void setNoData(String noData) {
		this.noData = noData;
	}
	public String getLoading() {
		return loading;
	}
	public void setLoading(String loading) {
		this.loading = loading;
	}
	
}
