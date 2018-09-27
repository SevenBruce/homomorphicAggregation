import java.io.IOException;
import messages.PublicInfo;
import messages.RegBack;
import messages.RegMessage;
import messages.RepAgg;
import messages.RepMessage;

public class myMain {

	private static long sl;
	private static long el;
	static Out out;
	static PKG pkg;
	static Server server;
	static Agg agg;
	static SmartMeter[] sm;

	public static void main(String args[]) throws IOException {

		out = new Out("time.time");
		
		pkg = new PKG();
		server = new Server();
		
		PublicInfo pubInfo = pkg.publishPublicInfo(server.getIdentity());
		server.getPublicInfo(pubInfo);
		agg = new Agg(pubInfo);
		regAggregator();
		regServer();
		
		int maxMeterArrarySize = PublicParams.ARRAY_OF_METERS_NUM[PublicParams.ARRAY_OF_METERS_NUM.length - 1];
		meterIntitaliaztion(pubInfo, maxMeterArrarySize);
		
		normalReportingPhase();
		out.close();
//		Runtime.getRuntime().exec("shutdown -s");
	}

	/**
	 * simulate the normal reporting phase meters reporting only one data to the
	 * server analysis. one one smart meter reporting messages to the aggregator
	 * @throws IOException 
	 */
	private static void normalReportingPhase() throws IOException {
		for (int meters : PublicParams.ARRAY_OF_METERS_NUM) {
			PublicParams.METERS_NUM = meters;

			int count = 0;
			long totalReportTime = 0;
			while (count < PublicParams.EXPERIMENT_REPEART_TIMES) {
				totalReportTime += oneTimeMeterRepTime();
				agg.clearReportMessage();
				count++;
			}

			printAndWrite("report time when meters number is : " + meters);
			printAndWriteData(totalReportTime);
			printAndWrite("");
		}
	}


	private static void printAndWriteData(double totalTime) {
		System.out.println(totalTime / 1000000);
		out.println(totalTime / 1000000);
	}

	private static void printAndWrite(String outStr) {
		System.out.println(outStr);
		out.println(outStr);
	}

	private static void meterIntitaliaztion(PublicInfo pubInfo, int meterSize) throws IOException {
		sm = new SmartMeter[meterSize];
		for (int i = 0; i < meterSize; i++) {
			sm[i] = new SmartMeter(pubInfo);
			RegMessage reg = sm[i].genRegMesssage();
			RegBack back = pkg.getRegMessage(reg);
			sm[i].genRegBack(back);
		}
	}

	private static void regAggregator() throws IOException {
		RegMessage reg = agg.genRegMesssage();
		RegBack back = pkg.getRegMessage(reg);
		agg.getRegBack(back);
	}
	
	private static void regServer() throws IOException {
		RegMessage reg = server.genRegMesssage();
		RegBack back = pkg.getRegMessage(reg);
		server.getRegBack(back);
	}

	private static long oneTimeMeterRepTime() throws IOException {
		sl = System.nanoTime();
		for (int i = 0; i < PublicParams.METERS_NUM; i++) {
			RepMessage repMessage = sm[i].genSingleRepMessage();
			RepAgg repAgg = agg.getRepMessage(repMessage);
			server.getRepMessage(repAgg);
		}
		el = System.nanoTime();
		return (el - sl);
	}
	

}
