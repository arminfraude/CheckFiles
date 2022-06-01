package test;

import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IOTest {
	
	private static final Logger LOGGER = LogManager.getLogger(IOTest.class);

    public static void main(String[] args) {
    	IOTest instance = new IOTest();
        instance.start();
    }

    private void start() {
        Scanner sc = new Scanner(System.in);
        String input;

        while (!(input = sc.nextLine()).equals("exit")) {
            processInput(input);
        }

        sc.close();
    }

    private void processInput(String input) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //Thread.sleep(2000);
                    System.out.println("input: " + input);
             //   } catch (InterruptedException e) {
             //       e.printStackTrace();
                }
                finally {}
            }
        });
        t.start();
    }

}