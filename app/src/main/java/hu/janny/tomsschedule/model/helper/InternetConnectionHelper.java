package hu.janny.tomsschedule.model.helper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class InternetConnectionHelper {

    /**
     * Checks if the device has Internet connection at the moment.
     * @return has reliable Internet connection
     */
    public static boolean hasInternetConnection() {

        // Starts a new thread for the internet checking
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Boolean> future = executor.submit(() -> {
            try {
                int timeoutMs = 1500;
                Socket sock = new Socket();
                SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);

                sock.connect(sockaddr, timeoutMs);
                sock.close();

                return true;
            } catch (IOException e) {
                return false;
            }
        });

        // Checks future progress
        while(!future.isDone()) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                return false;
            }
        }

        // Retrieves future return data
        Boolean result = null;
        try {
            result = future.get();
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }

        boolean canceled = future.cancel(true);
        executor.shutdown();

        return result;
    }
}
