package engine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import dom2app.IMeasurementVector;
import dom2app.IMeasurementVectorFactory;
import dom2app.ISingleMeasureRequest;
import dom2app.ISingleMeasureRequestFactory;

public class MainController implements IMainController {
	private List<IMeasurementVector> measurementVectors;
	private Set<String> allRequestNames;
	private Map<String, ISingleMeasureRequest> requests;
	
	public MainController() {
		this.measurementVectors = new ArrayList<IMeasurementVector>();
		this.allRequestNames = new HashSet<String>();
		this.requests = new HashMap<String, ISingleMeasureRequest>();
	}
	
	@Override
	public List<IMeasurementVector> load(String fileName, String delimiter) throws FileNotFoundException, IOException {
		try {  
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			System.out.println("Stats for File: " + fileName);
			String[] firstLine = br.readLine().split(delimiter);
			int startingYear = Integer.parseInt(firstLine[5]);
			String line;
			String lineValues[];
			int numberOfLines = 0;
			while ((line = br.readLine()) != null) {
				numberOfLines++;
				List<Pair<Integer, Integer>> measurements = new ArrayList<Pair<Integer, Integer>>();
				int year = startingYear;
				lineValues = line.split(delimiter);
				int numOfDisasters = 0;
				for (int i = 5; i < firstLine.length; i++) {
					if (i >= lineValues.length) {
						measurements.add(new Pair<>(year, 0));
					} else {
						if (lineValues[i].equals("")) {
							numOfDisasters = 0;
						} else {
							numOfDisasters = Integer.parseInt(lineValues[i]);
						}
						measurements.add(new Pair<>(year, numOfDisasters));
					}
					year++;
				}
				IMeasurementVectorFactory factory = new IMeasurementVectorFactory();
				IMeasurementVector measurementVector = factory.createMeasurementVector(IMeasurementVectorFactory.MeasurementVectorTypeEnum.DEFAULT, 
						lineValues[1], lineValues[4], measurements);
				measurementVectors.add(measurementVector);
			} 
 			System.out.println("Rows Read: " + numberOfLines);
			br.close();
		} catch (IOException e) {  
			e.printStackTrace();
		}
		return measurementVectors;
	}

	@Override
	public ISingleMeasureRequest findSingleCountryIndicator(String requestName, String countryName,
			String indicatorString) throws IllegalArgumentException { 
		ISingleMeasureRequestFactory factory = new ISingleMeasureRequestFactory();
		allRequestNames.add(requestName);
		for (IMeasurementVector measurementVector : measurementVectors) {
			if (measurementVector.getCountryName().equals(countryName) 
					&& measurementVector.getIndicatorString().equals(indicatorString)) {
				ISingleMeasureRequest request = factory.createSingleMeasureRequest(ISingleMeasureRequestFactory.SingleMeasureRequestTypeEnum.DEFAULT, 
						requestName, countryName + " " + indicatorString, true, measurementVector);
				requests.put(requestName, request);
				return request;
			}
		}
		return factory.createSingleMeasureRequest(ISingleMeasureRequestFactory.SingleMeasureRequestTypeEnum.DEFAULT, requestName, countryName + " " + indicatorString, false, null);
	}

	@Override
	public ISingleMeasureRequest findSingleCountryIndicatorYearRange(String requestName, String countryName,
			String indicatorString, int startYear, int endYear) throws IllegalArgumentException {
		if (startYear > endYear)
			throw new IllegalArgumentException("Start Year was greater than end year!");
		allRequestNames.add(requestName);
		ISingleMeasureRequestFactory requestFactory = new ISingleMeasureRequestFactory();
		IMeasurementVectorFactory measureFactory = new IMeasurementVectorFactory();
		
		for (IMeasurementVector measurementVector : measurementVectors) {
			if (measurementVector.getCountryName().equals(countryName) 
					&& measurementVector.getIndicatorString().equals(indicatorString)) {
				List<Pair<Integer, Integer>> measurements = new ArrayList<Pair<Integer, Integer>>();
				for (Pair<Integer, Integer> pair : measurementVector.getMeasurements()) {
					if (pair.getKey() >= startYear && pair.getKey() <= endYear) {
						measurements.add(pair);
					}
				}
				IMeasurementVector answer = measureFactory.createMeasurementVector(IMeasurementVectorFactory.MeasurementVectorTypeEnum.DEFAULT, 
						countryName, indicatorString, measurements);
				ISingleMeasureRequest request = requestFactory.createSingleMeasureRequest(ISingleMeasureRequestFactory.SingleMeasureRequestTypeEnum.DEFAULT, 
						requestName, countryName + " " + indicatorString, true, answer);
				requests.put(requestName, request);
				return request;
			}		
		}
		return requestFactory.createSingleMeasureRequest(ISingleMeasureRequestFactory.SingleMeasureRequestTypeEnum.DEFAULT, requestName, countryName + " " + indicatorString, false, null); 
	}

	@Override
	public Set<String> getAllRequestNames() {
		return allRequestNames;
	}

	@Override
	public ISingleMeasureRequest getRequestByName(String requestName) {
		return requests.get(requestName);
	}

	@Override
	public ISingleMeasureRequest getRegression(String requestName) {
		ISingleMeasureRequest request = requests.get(requestName);
		if (request == null || request.getAnswer().getMeasurements() == null) {
			return null;
		}
		request.getRegressionResultString();
		return request;
	}

	@Override
	public ISingleMeasureRequest getDescriptiveStats(String requestName) {
		ISingleMeasureRequest request = requests.get(requestName);
		if (request == null || request.getAnswer().getMeasurements() == null) {
			return null;
		}
		request.getDescriptiveStatsString();
		return request;
	}

	@Override
	public int reportToFile(String outputFilePath, String requestName, String reportType) throws IOException {
		IMeasurementVector measureVector = requests.get(requestName).getAnswer();
		int linesCounter = 0;
		try {
			PrintWriter outputStream = new PrintWriter(new FileOutputStream(outputFilePath));
			if (reportType.equals("text")) {
				linesCounter = reportTXT(requestName, measureVector, outputStream);
			} else if (reportType.equals("md")) {
				linesCounter = reportMD(requestName, measureVector, outputStream); 
			} else if (reportType.equals("html")){
				linesCounter = reportHTML(requestName, measureVector, outputStream);
			} 
			outputStream.close();
			return linesCounter;
		} catch (FileNotFoundException e) {
			System.out.println("Problem opening files.");
			System.exit(0);
		}
		return -1;
	}

	public int reportTXT(String requestName, IMeasurementVector measureVector, PrintWriter outputStream) {
		int linesCounter = 0;
		outputStream.println(requestName);
		outputStream.println("Country ~ " + measureVector.getCountryName() 
							+ " Indicator: " + measureVector.getIndicatorString());
		outputStream.println("Year\tValue");
		for (Pair<Integer, Integer> pair : measureVector.getMeasurements()) {
			outputStream.println(pair.getKey() + "\t" + pair.getValue());
			linesCounter++;
		}
		outputStream.println(measureVector.getDescriptiveStatsAsString());
		outputStream.println(measureVector.getRegressionResultAsString());
		linesCounter += 5;
		return linesCounter;
	}
	
	public int reportMD(String requestName, IMeasurementVector measureVector, PrintWriter outputStream) {
		int linesCounter = 0;
		outputStream.println("**" + requestName + "**");
		outputStream.println("\n_Country ~ " + measureVector.getCountryName() 
							+ " Indicator: " + measureVector.getIndicatorString() + "_");
		outputStream.println("\n|*Year*|*Value*|");
		outputStream.println("|----|----|");
		for (Pair<Integer, Integer> pair : measureVector.getMeasurements()) {
			outputStream.println("|" + pair.getKey() + " |" + pair.getValue() + "|");
			linesCounter++;
		}
		outputStream.println("\n" + measureVector.getDescriptiveStatsAsString());
		outputStream.println("\n" + measureVector.getRegressionResultAsString());
		linesCounter += 6;
		return linesCounter;
	}
	
	public int reportHTML(String requestName, IMeasurementVector measureVector, PrintWriter outputStream) {
		int linesCounter = 0;
		outputStream.println("<!doctype html>\n" + 
				"<html>\n" + 
				"<head>\n" + 
				"<meta http-equiv=\"Content-Type\" content\"text/html; charset=windows-1253\">\n" + 
				"<title>Natural Disaster Data</title>\n" + 
				"</head>\n" + 
				"<body>\n\n");
		outputStream.println("<p><b>" + requestName + "</b></p>");
		outputStream.println("<p><i>" + "Country ~ " + measureVector.getCountryName() + 
				" Indicator: " + measureVector.getIndicatorString() + "</i></p>");
		outputStream.println("<table>\n" + "<tr>");
		outputStream.println("<td>Year</td>\t<td>Value</td>\t</tr>\n");
		for (Pair<Integer, Integer> pair : measureVector.getMeasurements()) {
			linesCounter++;
			outputStream.println("<tr>\n" + "<td>" + pair.getKey() + "</td>\t" + 
			"<td>" + pair.getValue() + "</td>\n</tr>");
		}
		outputStream.println("</table><p>" + measureVector.getDescriptiveStatsAsString() + "<p>" + 
				measureVector.getRegressionResultAsString() + "</body>");
		outputStream.println("</html>");
		linesCounter += 5;
		return linesCounter;
	}
}