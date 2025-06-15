package de.juniorjacki.connection;

import de.juniorjacki.python.PythonController;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Connection {
    private String serviceUUID;
    private String deviceName;
    private Runnable onShutdown;
    private AtomicReference<Process> connectionProcess = new AtomicReference<>();
    private AtomicReference<BufferedReader> reader = new AtomicReference<>();
    private AtomicReference<BufferedWriter> writer = new AtomicReference<>();
    private AtomicBoolean readyForRequest = new AtomicBoolean(false);
    private AtomicReference<Queue<QueuedRequest>> queuedRequests = new AtomicReference<>();
    private AtomicReference<List<QueuedResult>> queuedResults = new AtomicReference<>();

    private record QueuedRequest(String identifier,String query,CompletableFuture<Boolean> waiter,boolean resendOnError) {}
    private record QueuedResult(String identifier,CompletableFuture<String> waiter) {}
    public record DeviceServiceData(int voltage, int ampere){};

    private final long timeoutTime = 10000; // [Milliseconds] If a Process takes longer than this time it will be skipped
    private final long requestTimeoutTime = 400;

    public Connection(String deviceName, String serviceUUID,Runnable onShutdown) {
        this.deviceName = deviceName;
        this.serviceUUID = serviceUUID != null ? serviceUUID : "c5f50002-8280-46da-89f4-6d8051e4aeef";
        this.onShutdown = onShutdown;
        queuedRequests.set(new LinkedList<>());
        queuedResults.set(new LinkedList<>());
    }

    public boolean init() {
        Optional<PythonController.Env> optionalEnv = PythonController.getEnv();
        if (optionalEnv.isPresent()) {
            PythonController.Env env = optionalEnv.get();
            ProcessBuilder pb = new ProcessBuilder(env.pythonExe().getAbsolutePath(), env.scriptFile().getAbsolutePath(),deviceName,serviceUUID);
            pb.directory(env.workingDir());
            pb.redirectErrorStream(true);
            try {
                connectionProcess.set(pb.start());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,e.getStackTrace(),"Could not start BLE Python Process",JOptionPane.ERROR_MESSAGE);
                return false;
            }
            writer.set(new BufferedWriter(new OutputStreamWriter(connectionProcess.get().getOutputStream())));
            reader.set(new BufferedReader(new InputStreamReader(connectionProcess.get().getInputStream())));

            CompletableFuture<Boolean> waitingSequence = new CompletableFuture<Boolean>();
            startingSequence(waitingSequence);
            try {
                if (waitingSequence.get(20000,TimeUnit.MILLISECONDS)) {
                    startListener();
                    readyForRequest.set(true);
                    startSender();
                    return true;
                }
            } catch (Exception ignored) {}
            shutdown();
            return false;
        } else JOptionPane.showMessageDialog(null,"Could not use Python Env","Error",JOptionPane.ERROR_MESSAGE);
        return false;
    }

    private void startingSequence(CompletableFuture<Boolean> success) {
        try {
            if (!waitForResult(reader.get(),5000).isPresent()) {
                success.complete(false);
                return;
            }
            Thread.sleep(2000);
            writer.get().write("ini"+"\n");
            writer.get().flush();
            Thread.sleep(1000);
            writer.get().write("ini"+"\n");
            writer.get().flush();
            Thread.sleep(1000);
            waitForResult(reader.get(),5000).ifPresentOrElse(result -> {
                System.out.println("Received Something: " + result);
                success.complete(result.equals("ret:ins:"));
            },() -> {
                System.out.println("Did not receive anything!");
                success.complete(false);
            });

        } catch (Exception exc) {
            exc.printStackTrace();

        }

    }

    private Optional<String> waitForResult(BufferedReader reader,long timeout) throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < timeout) {
            if (reader.ready()) {
                String line = reader.readLine();
                System.out.println("ente "+line);
                if (line != null) {
                    return Optional.ofNullable(line);
                }
            }

            Thread.sleep(20); // Small sleep to avoid busy-waiting

        }
        return Optional.empty();
    }


    public Thread listenerthread = new Thread(() -> {
        try {
            String line;
            while (isAlive() && (line = reader.get().readLine()) != null) {
                System.out.println("HUB: " + line);
                if (line.equals("cnf") || line.equals("hwd")) {
                    readyForRequest.set(false);
                    disconnected();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally { // RESTART if Needed
            if (isAlive()) startListener();
        }
    });

    private void startListener() {
        listenerthread.start();
    }

    private void interuptListerner() {
        listenerthread.interrupt();
    }


    public void disconnect() {
        sendCommand("bye");
    }

    private void disconnected() {
        System.out.println("Hub Disconnected");
        shutdown();
    }


    private List<String> waitForResults(BufferedReader reader,long time) throws IOException, InterruptedException {
        List<String> result = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < time) {
            System.out.println("hi");
            if (reader.ready()) {
                System.out.println("want to read");
                String line = reader.readLine();
                System.out.println("read");
                if (line != null) {
                    result.add(line);
                }
            }
            Thread.sleep(50); // Small sleep to avoid busy-waiting
            System.out.println("BUSY");
        }
        return result;
    }

    private List<String> waitForResults(BufferedReader reader,String keyCode,long time) throws IOException, InterruptedException {
        List<String> result = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) > time) {
            if (reader.ready()) {
                String line = reader.readLine();
                if (line != null) {
                    result.add(line);
                    if (line.contains(keyCode)) {break;}
                }
            }
            Thread.sleep(20); // Small sleep to avoid busy-waiting
            System.out.println("BUSY W");
        }
        return result;
    }

    private void startSender() {
        new Thread(() -> {
            try {
                while (isAlive() ) {
                   if (readyForRequest.get()) {
                        QueuedRequest request = queuedRequests.get().poll() ;
                        if (request != null) {
                            interuptListerner();
                            try {
                                writer.get().write(request.query+"\n");
                                writer.get().flush();
                                System.out.println("Sending: " + request.query);
                                List<String> waitForResult = waitForResults(reader.get(),250);
                                System.out.println("d");
                                Optional<String> result = waitForResult.stream().filter(s -> s.contains(request.identifier)).findFirst();
                                System.out.println("b");
                                if (!result.isPresent()) {
                                    System.out.println("e");
                                    writer.get().write("dat");
                                    writer.get().flush();
                                    System.out.println("Sending Additional Data request!");
                                    waitForResult = waitForResults(reader.get(),request.identifier,timeoutTime);
                                    result = waitForResult.stream().filter(s -> s.contains(request.identifier)).findFirst();
                                }
                                System.out.println("a");
                                if (result.isPresent()) {
                                    System.out.println("f");
                                    waitForResult.stream().filter(s -> s.contains("ret")).forEach(s -> {
                                        s = s.replace("ret:","");
                                        String[] finalLineSplit = s.split(":");
                                        String finalS = s;
                                        queuedResults.get().stream().filter(queuedResult -> queuedResult.identifier().equals(finalLineSplit[0])).toList().forEach(queuedResult -> {
                                            queuedResults.get().remove(queuedResult);
                                            queuedResult.waiter.complete(finalS.replace(finalLineSplit[0] + ":", ""));
                                        });
                                    });
                                } else {
                                    System.out.println("Something went wrong");
                                }
                                System.out.println("c");
                                request.waiter().complete(true);
                            } catch (Exception e) {
                                System.out.println("ERROR");
                                e.printStackTrace();
                                request.waiter().complete(false);
                            }
                            startListener();
                            System.out.println("Restarted thread");
                        }
                    } else System.out.println("No Commands");
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally { // RESTART if Needed
                if (isAlive()) startSender();
            }
        }).start();
    }

    private QueuedResult setResultHook(String identifier) {
        QueuedResult queuedResult = new QueuedResult(identifier,new CompletableFuture<>());
        queuedResults.get().add(queuedResult);
        return queuedResult;
    }

    private void deleteResultHook(QueuedResult queuedResult) {
        queuedResults.get().remove(queuedResult);
    }

    public void shutdown() {
        connectionProcess.get().destroy();
        onShutdown.run();
    }

    private boolean isAlive() {
        if (!connectionProcess.get().isAlive()) {
            readyForRequest.set(false);
            disconnected();
            return false;
        }
        return true;
    }

    public boolean sendCommand(String commandID) {
        return sendCommand(commandID,null);
    }

    /**
     * Queues New Requests for Hub
     * @param commandID Query
     * @param args Arguments
     * @return True if Success else False
     */
    public boolean sendCommand(String commandID, String[] args) {
        try {
            String query = commandID;
            if (args != null && args.length > 0) {
                StringBuilder requestBuilder = new StringBuilder();
                for (String s : args) {
                    requestBuilder.append(":").append(s);
                }
                query += requestBuilder.toString();
            }
            CompletableFuture<Boolean> executionWaiter = new CompletableFuture<>();
            queuedRequests.get().add(new QueuedRequest(commandID,query, executionWaiter,false));
            return executionWaiter.get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Optional<String> sendRequest(String requestID) {
        return sendRequest(requestID,null);
    }

    public Optional<String> sendRequest(String requestID,String[] args) {
        try {
            String query = requestID;
            if (args != null && args.length > 0) {
                StringBuilder requestBuilder = new StringBuilder();
                for (String s : args) {
                    requestBuilder.append(":").append(s);
                }
                query += requestBuilder.toString();
            }
            CompletableFuture<String> resultWaiter = new CompletableFuture<>();
            QueuedResult queuedResult = new QueuedResult(requestID, resultWaiter);
            queuedResults.get().add(queuedResult);

            CompletableFuture<Boolean> executionWaiter = new CompletableFuture<>();
            QueuedRequest queuedRequest = new QueuedRequest(requestID,query, executionWaiter,true);
            queuedRequests.get().add(queuedRequest);
            if (executionWaiter.get(timeoutTime, TimeUnit.MILLISECONDS)) {
                if (resultWaiter.get(timeoutTime, TimeUnit.MILLISECONDS) != null) {
                    return Optional.of(resultWaiter.get());
                }
            } else {
                queuedRequests.get().remove(queuedRequest);
                queuedResults.get().remove(queuedResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }



    public Optional<DeviceServiceData> getDeviceData() {
        try {
            Optional<String> result = sendRequest("rdd");
            if (result.isPresent()) { // RSD = Request Device Data
                String[] results = result.get().split(":");
                return Optional.of(new DeviceServiceData(Integer.parseInt(results[0]), Integer.parseInt(results[1])));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }



    
}