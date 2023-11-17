package dom2app;

import java.util.List;

import org.apache.commons.math3.util.Pair;

public class IMeasurementVectorFactory {

	public enum MeasurementVectorTypeEnum{DEFAULT};
	
	public IMeasurementVector createMeasurementVector(MeasurementVectorTypeEnum measurementVectorType, 
			String countryName, String indicatorString, List<Pair<Integer, Integer>> measurements) {
		if (measurementVectorType == MeasurementVectorTypeEnum.DEFAULT)
			return new MeasurementVector(countryName, indicatorString, measurements); 
		return null;
	}
}
