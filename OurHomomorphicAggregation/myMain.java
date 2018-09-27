import java.io.IOException;
import java.math.BigInteger;

import messages.ECC_Parameters;
import messages.PublicInfo;
import messages.RegBack;
import messages.RegMessage;
import messages.RepMessage;

public class myMain {

	private static long sl;
	private static long el;
	private static Out out;
	private static KGC kgc;
	private static Agg agg;
	private static Server server;
	private static SmartMeter[] sm;

	public static void main(String args[]) throws IOException {
		
		out = new Out("ourtime2019-9-10.time");

		kgc = new KGC();
		ECC_Parameters ps = kgc.publishECC_Parameters();

		server = new Server(ps);
		serverRegistration();

		agg = new Agg(ps);
		aggregatorRegistration();

		int maxMeterArrarySize = PublicParams.ARRAY_OF_METERS_NUM[PublicParams.ARRAY_OF_METERS_NUM.length - 1];
		sm = new SmartMeter[maxMeterArrarySize];
		meterIntitaliaztion();
		
		
		
		BigInteger g1 = new BigInteger("3855139508706856672900314918351400545731889998548698553336784320140515898052654625980687443322148896500165963730351113197702111413523485186651123223205985140965910964257864578800966830954778330394282920083291598689097573459203215049693840043914673522770509430202127404454153010506442567763084230757700803582486135313407450647784585318913713674416046186958730985285633659109603900987514836877357145845841076053942734952590919087614661526590102198113231693385939374895463273435715288234091796037657598999349098385110101756174191882205688604541924375806820008125456620349143196959608593603236075697381747521683263268697");
		BigInteger g2 = new BigInteger("2");
		
		BigInteger ns =new BigInteger("3989600052935332863260476301776288660283688182255820750466291013303614329762938843281596653646604299461685046973845629700727896613896458228846439221411608201825658916261801729004474496865593422522813207159967791327333681859796944664766263188719228614148023129386427514506075480338124912663710993656364909654894957077166858965115747261915640362916582995014406030968072300247140477172467986114047529850043441600941749183506689833753056688930571406039553935243701512340514482632985646285008096593443822194768946250161993060855274672930056296891491984756203340190817965772526809606444006290578420374015272354077060704649");

		int [] data = new int[1000];
		for(int i = 0 ;i< data.length; i ++){
			data[i] = Utils.randomInt();
		}
		
		BigInteger r1 = BigInteger.ONE;
		BigInteger t = BigInteger.ONE;
		sl = System.currentTimeMillis();
		for(int i = 0 ;i< data.length; i ++){
			t = g1.modPow(BigInteger.valueOf(data[i]), ns);
			r1 = r1.multiply(t).mod(ns);
		}
		el = System.currentTimeMillis();
		System.out.println("time is " + (el-sl));
		
		
		BigInteger r2 = BigInteger.ONE;
		t = BigInteger.ONE;
		sl = System.currentTimeMillis();
		for(int i = 0 ;i< data.length; i ++){
			t = g2.modPow(BigInteger.valueOf(data[i]), ns);
			r2 = r2.multiply(t).mod(ns);
		}
		el = System.currentTimeMillis();
		System.out.println("time is " + (el-sl));
		//		normalReportingPhase();
//		multipleDataReportingPhase();
//		multipleVarianceReportingPhase();
//		oneWayVarianceAnalysis();
		
		out.close();
//		Runtime.getRuntime().exec("shutdown -s");
	}

	/**
	 * simulate the normal reporting phase meters reporting only one data to the
	 * server analysis. one one smart meter reporting messages to the aggregator
	 * 
	 * @throws IOException
	 */
	private static void normalReportingPhase() throws IOException {

		PublicInfo pi = server.intializEncryptionVector();
		setMeterPublicInfo(pi);

		for (int meters : PublicParams.ARRAY_OF_METERS_NUM) {
			PublicParams.METERS_NUM = meters;

			int count = 0;
			long totalReportTime = 0;

			while (count < PublicParams.EXPERIMENT_REPEART_TIMES) {
				totalReportTime += oneTimeMeterRepTime(1);
				agg.clearReportMessage();
				count++;
			}

			printAndWrite("reporting time when meters number : " + meters);
			printAndWriteData(totalReportTime);
			printAndWrite("");
		}
		
//			PublicParams.METERS_NUM = 40;
//
//			int count = 0;
//			long totalReportTime = 0;
//
//			while (count < PublicParams.EXPERIMENT_REPEART_TIMES) {
//				totalReportTime += oneTimeMeterRepTime(1);
//				agg.clearReportMessage();
//				count++;
//			}
//
//			printAndWrite("reporting time when meters number : " + PublicParams.METERS_NUM);
//			printAndWriteData(totalReportTime);
//			printAndWrite("");
		
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
			PublicInfo pubInfo = server.intializEncryptionVector();
			setMeterPublicInfo(pubInfo);

			int count = 0;
			long runningTime = 0;
			while (count < PublicParams.EXPERIMENT_REPEART_TIMES) {
				runningTime = runningTime + oneTimeMeterRepTime(type);
				agg.clearReportMessage();
				count++;
			}
			printAndWrite("reporting time when data tyeps : " + type);
			printAndWriteData(runningTime);
		}
		printAndWrite("");
	}

	/**
	 * simulate the normal reporting phase meters reporting only one data to the
	 * server analysis. one one smart meter reporting messages to the aggregator
	 * 
	 * @throws IOException
	 */
	private static void multipleVarianceReportingPhase() throws IOException {
		PublicParams.METERS_NUM = PublicParams.ARRAY_OF_METERS_NUM[1];

		PublicParams.REPORTING_DATA_FOR_VARIANCE = true;
		for (int type : PublicParams.ARRAY_OF_REPORTING_DATA_TYPES) {

			PublicParams.NUMBER_OF_REPORTING_DATA_TYPE = type;
			PublicInfo pubInfo = server.intializEncryptionVector();
			setMeterPublicInfo(pubInfo);

			int count = 0;
			long runningTime = 0;
			while (count < PublicParams.EXPERIMENT_REPEART_TIMES) {
				runningTime = runningTime + repTime(type);
				agg.clearReportMessage();
				count++;
			}
			printAndWrite("data tyeps : " + type);
			printAndWriteData(runningTime);
		}
		PublicParams.REPORTING_DATA_FOR_VARIANCE = false;
		printAndWrite("");
	}

	/**
	 * simulate the reporting for the Server to conduct an one way variance
	 * analysis. one one smart meter reporting messages to the aggregator
	 * 
	 * @throws IOException
	 */
	private static void oneWayVarianceAnalysis() throws IOException {
		PublicParams.METERS_NUM = 1;
		int reportingDataType = 1;
		PublicParams.NUMBER_OF_REPORTING_DATA_TYPE = reportingDataType;

		PublicParams.REPORTING_DATA_FOR_VARIANCE = true;
		PublicInfo pubInfo = server.intializEncryptionVector();
		setMeterPublicInfo(pubInfo);

		printAndWrite("ANOVA REPORTING ");
		for (int reportingMessageTotalNumber : PublicParams.ANOVA_REPORTING_MESSAGE_NUMBER) {

			PublicParams.ANOVA_SINGLE_GROUP_SIZE = reportingMessageTotalNumber / PublicParams.ANOVA_GROUP_NUMBER;
			PublicParams.ANOVA_GROUP_SIZE = reportingMessageTotalNumber;

			int count = 0;
			double runningTime = 0;
			while (count < PublicParams.EXPERIMENT_REPEART_TIMES) {
				runningTime = runningTime + reportingTimeOfANOVA(reportingMessageTotalNumber, reportingDataType);
				count++;
			}
			printAndWrite("Messages : " + reportingMessageTotalNumber);
			printAndWriteData(runningTime / count);
		}
		PublicParams.REPORTING_DATA_FOR_VARIANCE = false;
	}

	private static void printAndWriteData(double totalTime) {
		System.out.println(totalTime / 1000000);
		out.println( totalTime / 1000000);
	}

	private static void printAndWrite(String outStr) {
		System.out.println(outStr);
		out.println(outStr);
	}

	private static void meterIntitaliaztion() throws IOException {
		RegMessage reg;
		RegBack back;
		ECC_Parameters ps = kgc.publishECC_Parameters();

		for (int i = 0; i < sm.length; i++) {
			sm[i] = new SmartMeter(ps);
			reg = sm[i].genRegMesssage();
			back = kgc.getRegMessage(reg);
			sm[i].getRegBack(back);
		}
	}

	private static void setMeterPublicInfo(PublicInfo pi) throws IOException {
		for (int i = 0; i < sm.length; i++) {
			sm[i].setPublicInfo(pi);
			sm[i].setAggId(agg.getId());
		}
		agg.setNsquare(pi.getNsquare());
	}

	private static void aggregatorRegistration() throws IOException {
		RegMessage reg = agg.genRegMesssage();
		RegBack back = kgc.getRegMessage(reg);
		agg.getRegBack(back);
		agg.setServerId(server.getId());
	}

	private static void serverRegistration() throws IOException {
		RegMessage reg = server.genRegMesssage();
		RegBack back = kgc.getRegMessage(reg);
		server.getRegBack(back);
	}

	private static long meterRegTime() throws IOException {
		sl = System.nanoTime();
		for (int i = 0; i < PublicParams.METERS_NUM; i++) {
			RegMessage reg = sm[i].genRegMesssage();
			RegBack back = kgc.getRegMessage(reg);
			sm[i].getRegBack(back);
		}
		el = System.nanoTime();
		return (el - sl);
	}

	private static long repTime(int type) throws IOException {
		sl = System.nanoTime();
		for (int i = 0; i < PublicParams.METERS_NUM; i++) {
			RepMessage repMessage = sm[i].genRepMessage(type);
			RepMessage repAgg = agg.getRepMessage(repMessage);
			server.getRepMessage(repAgg);
		}
		el = System.nanoTime();
		return (el - sl);
	}

	//111
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

	private static double oneTimeMeterRepTimeAggregator(int count) throws IOException {
		double total = 0;
		for (int i = 0; i < PublicParams.METERS_NUM; i++) {
			RepMessage repMessage = sm[i].genSingleRepMessage(count);
			sl = System.nanoTime();
			RepMessage repAgg = agg.getRepMessage(repMessage);
			el = System.nanoTime();
			server.getRepMessage(repAgg);
			total = total + (el - sl);
		}
		return total;
	}

	private static double oneTimeMeterRepTimeServer(int count) throws IOException {
		double total = 0;
		for (int i = 0; i < PublicParams.METERS_NUM; i++) {
			RepMessage repMessage = sm[i].genSingleRepMessage(count);
			RepMessage repAgg = agg.getRepMessage(repMessage);
			sl = System.nanoTime();
			server.getRepMessage(repAgg);
			el = System.nanoTime();
			total = total + (el - sl);
		}
		return total;
	}

	private static long reportingTimeOfANOVA(int reportingMessageTotalNumber, int reportingDataType)
			throws IOException {
		sl = System.nanoTime();
		RepMessage[] repAgg = null;

		// from smart meter to aggregator
		for (int i = 0; i < reportingMessageTotalNumber; i++) {
			RepMessage repMessage = sm[0].genRepMessage(reportingDataType);
			repAgg = agg.getRepMessageForANOVA(repMessage);
		}
		// from aggregator to server
		server.getRepMessageForANOVA(repAgg);
		el = System.nanoTime();
		agg.clearReportMessage();
		return (el - sl);
	}

}
