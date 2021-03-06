import java.lang.Thread;
import java.lang.Process;
import java.lang.Runtime;


public class RunThread extends Thread {


	private String filePath;
	private String fileName;

	public RunThread(String filePath,String fileName){

		this.filePath = filePath;
		this.fileName = fileName;

	}

    public void run() {

    	try{
	        System.out.println("Running java program");

					System.out.println("executing Runthread");
			Runtime runtime = Runtime.getRuntime();

			Process classpath = runtime.exec("java -classpath " + filePath);

			classpath.waitFor();

			Process compile = runtime.exec("javac " + filePath + "\\*.java");

			System.out.println("compiling");

			compile.waitFor();

			System.out.println("done compiling");
			
			Process run = runtime.exec("java " + fileName);

			System.out.println("running");

			run.getInputStream().close();

			run.getOutputStream().close();

			run.getErrorStream().close();

			run.waitFor();
		}

		catch(Exception e){

			e.printStackTrace();
		}

    }

}
