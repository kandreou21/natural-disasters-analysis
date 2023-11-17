package dom2app;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.util.Pair;

public class SingleMeasureRequest implements ISingleMeasureRequest {
	private String requestName;
	private String requestFilter;
	private boolean answeredFlag;
	private IMeasurementVector answer;
	
	public SingleMeasureRequest() {}
	
	public SingleMeasureRequest(String requestName, String requestFilter, boolean answeredFlag, IMeasurementVector answer) {
		this.requestName = requestName;
		this.requestFilter = requestFilter;
		this.answeredFlag = answeredFlag;
		this.answer = answer;
	}

	@Override
	public String getRequestName() {
		return requestName;
	}

	@Override
	public String getRequestFilter() {
		return requestFilter;
	}

	@Override
	public boolean isAnsweredFlag() {
		return answeredFlag;
	}

	@Override
	public IMeasurementVector getAnswer() {
		return answer;
	}
	
	@Override
	public String getDescriptiveStatsString() {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for(Pair<Integer, Integer> xyPair: answer.getMeasurements()) {
			int value = xyPair.getSecond();
			stats.addValue(value);
		}
		
		long count = stats.getN();
		double minD = stats.getMin();
		double gMean = stats.getGeometricMean();
		double mean = stats.getMean();
		double medianD = stats.getPercentile(50);
		double maxD = stats.getMax();
		double kurtosis = stats.getKurtosis();
		double stdev = stats.getStandardDeviation();
		double sumD = stats.getSum();
		
		String result =  "DescriptiveStatsResult [count=" + count + ", min=" + minD 
				+ ", gMean=" + gMean + ", mean=" + mean + ", median=" + medianD + ", max=" + maxD 
				+ ", kurtosis=" + kurtosis + ", stdev=" + stdev + ", sum=" + sumD + "]";
		MeasurementVector answer = (MeasurementVector)this.answer;
		answer.setDescriptiveStats(result);
		return result;
	}
		
	@Override
	public String getRegressionResultString() {
		SimpleRegression regression = new SimpleRegression();
		for(Pair<Integer, Integer> xyPair: answer.getMeasurements()) {
			int year = xyPair.getFirst();
			int value = xyPair.getSecond();
			regression.addData(year, value);
		}
		
		double intercept = regression.getIntercept();
		double slope = regression.getSlope();
		double slopeError = regression.getSlopeStdErr();
		String tendency = getLabel(slope);
		
		String result = "RegressionResult [intercept=" + intercept + ", slope=" + slope 
				+ ", slopeError=" + slopeError + ", " + tendency + "]";
		MeasurementVector answer = (MeasurementVector)this.answer;
		answer.setRegressionResult(result);
		return result;
	}
	
	public String getLabel(double slope) {
		if (Double.isNaN(slope))
			return "Tendency Undefined";
		else if(slope > 0.1)
			return "Increased Tendency";
		else if(slope < -0.1)
			return "Decreased Tendency";
		return "Tendency stable";
	}

	@Override
	public String toString() {
		return "SingleMeasureRequest [requestName=" + requestName + ", requestFilter=" + requestFilter
				+ ", answeredFlag=" + answeredFlag + ", answer=" + answer + "]";
	}
}
