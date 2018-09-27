import java.io.IOException;
import messages.EccParameters;
import messages.GeneratorInfo;
import messages.RegBack;
import messages.RegMessage;
import messages.RepMessage;

public class myMain {

	private static long sl;
	private static long el;
	static Out out;
	static Server server;
	static Agg agg;
	static SmartMeter[] sm;

	public static void main(String args[]) throws IOException {

		out = new Out("time2018_9-13.time");
		
		server = new Server();
		EccParameters ecc = server.publishEccParameters();
		
		agg = new Agg(ecc);
		regAggregator();
		
		int maxMeterArrarySize = PublicParams.ARRAY_OF_METERS_NUM[PublicParams.ARRAY_OF_METERS_NUM.length - 1];
		meterIntitaliaztion(ecc, maxMeterArrarySize);

		normalReportingPhase();
		multipleDataReportingPhase();

		out.close();
		Runtime.getRuntime().exec("shutdown -s");
	}

	/**
	 * simulate the normal reporting phase meters reporting only one data to the
	 * server analysis. one one smart meter reporting messages to the aggregator
	 * 
	 * @throws IOException
	 */
	private static void normalReportingPhase() throws IOException {
		
		GeneratorInfo gen = server.initializeGenerators();
		setGeneratorInfo(gen);
		
		for (int meters : PublicParams.ARRAY_OF_METERS_NUM) {
			
			PublicParams.METERS_NUM = meters;
			int count = 0;
			long totalReportTime = 0;
			
			while (count < PublicParams.EXPERIMENT_REPEART_TIMES) {
				meterRegAtAggregator();
				totalReportTime = totalReportTime + oneTimeMeterRepTime(1);
				agg.clear();
				count++;
			}

			printAndWrite("report time when meters number is : " + meters);
			printAndWriteData(totalReportTime);
			printAndWrite("");
		}
	}

	private static void setGeneratorInfo(GeneratorInfo gen) {
		// TODO Auto-generated method stub
		int maxMeterArrarySize = PublicParams.ARRAY_OF_METERS_NUM[PublicParams.ARRAY_OF_METERS_NUM.length - 1];
		for (int i = 0; i < maxMeterArrarySize; i++) {
			sm[i].setGenerators(gen);;
		}
		agg.setNsquare(gen.getNsquare());
	}

	/**
	 * simulate the normal reporting phase meters reporting only one data to the
	 * server analysis. one one smart meter reporting messages to the aggregator
	 * 
	 * @throws IOException
	 */
	private static void multipleDataReportingPhase() throws IOException {
		PublicParams.METERS_NUM = PublicParams.ARRAY_OF_METERS_NUM[1];
		
		for (int type : PublicParams.ARRAY_OF_REPORTING_DATA_TYPES) {
			PublicParams.NUMBER_OF_REPORTING_DATA_TYPE = type;

			GeneratorInfo gen = server.initializeGenerators();
			setGeneratorInfo(gen);

			int count = 0;
			long runningTime = 0;
			while (count < PublicParams.EXPERIMENT_REPEART_TIMES) {
				meterRegAtAggregator();
				runningTime = runningTime + oneTimeMeterRepTime(type);
				agg.clearReportMessages();
				count++;
			}
			
			printAndWrite("report time when data tyeps is : " + type);
			printAndWriteData(runningTime);
		}
		printAndWrite("");
	}

	private static void printAndWriteData(double totalTime) {
		System.out.println( totalTime / 1000000);
		out.println(totalTime / 1000000);
	}

	private static void printAndWrite(String outStr) {
		System.out.println(outStr);
		out.println(outStr);
	}

	private static void meterIntitaliaztion(EccParameters ecc, int meterSize) throws IOException {
		sm = new SmartMeter[meterSize];
		for (int i = 0; i < meterSize; i++) {
			sm[i] = new SmartMeter(ecc);
		}
		meterRegAtAggregator();
	}

	private static void regAggregator() {
		RegMessage reg = agg.genRegMesssage();
		server.getRegMessage(reg);
	}

	private static void meterRegAtAggregator() {
		for (int i = 0; i < PublicParams.METERS_NUM; i++) {
			RegMessage reg = sm[i].genRegMesssage();
			agg.getRegMessage(reg);
		}
	}

	private static long oneTimeMeterRepTime(int count) throws IOException {
		sl = System.nanoTime();
		for (int i = 0; i < PublicParams.METERS_NUM; i++) {
			RepMessage repMessage = sm[i].genSingleRepMessage(count);
			RepMessage repAgg = agg.getRepMessage(repMessage);
			server.getRepMessage(repAgg);
		}
		el = System.nanoTime();
		return (el - sl);
	}

}
