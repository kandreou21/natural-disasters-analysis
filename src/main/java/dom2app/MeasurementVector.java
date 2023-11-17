package dom2app;

import java.util.List;

import org.apache.commons.math3.util.Pair;

public class MeasurementVector implements IMeasurementVector {
	private String countryName;
	private String indicatorString;
	private List<Pair<Integer, Integer>> measurements;
	private String descriptiveStats;
	private String regressionResult;
	
	public MeasurementVector() {}
	
	public MeasurementVector(String countryName, String indicatorString, List<Pair<Integer, Integer>> measurements) {
		this.countryName = countryName;
		this.indicatorString = indicatorString;
		this.measurements = measurements;
	}
	
	@Override
	public String getCountryName() {
		return countryName;
	}

	@Override
	public String getIndicatorString() {
		return indicatorString;
	}

	@Override
	public List<Pair<Integer, Integer>> getMeasurements() {
		return measurements;
	}

	@Override
	public String getDescriptiveStatsAsString() {
		return descriptiveStats;
	}

	@Override
	public String getRegressionResultAsString() {
		return regressionResult;
	}
	
	public void setRegressionResult(String regressionResult) {
		this.regressionResult = regressionResult;
	}

	public void setDescriptiveStats(String descriptiveStats) {
		this.descriptiveStats = descriptiveStats;
	}

	@Override
	public String toString() {
		return "MeasurementVector [countryName=" + countryName + ", indicatorString=" + indicatorString
				+ ", measurements=" + measurements + ", descriptiveStats=" + descriptiveStats + ", regressionResult="
				+ regressionResult + "]";
	}
}
