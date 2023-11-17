package dom2app;

public class ISingleMeasureRequestFactory {
	
	public enum SingleMeasureRequestTypeEnum{DEFAULT};
	
	public ISingleMeasureRequest createSingleMeasureRequest(SingleMeasureRequestTypeEnum singleMeasureRequestType, 
			String requestName, String requestFilter, boolean answeredFlag, IMeasurementVector answer) {
		if (singleMeasureRequestType == SingleMeasureRequestTypeEnum.DEFAULT)
			return new SingleMeasureRequest(requestName, requestFilter, answeredFlag, answer); 
		return null;
	}

}
