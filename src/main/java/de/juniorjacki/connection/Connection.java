package de.juniorjacki.connection;

import de.juniorjacki.gui.LoadingScreen;
import de.juniorjacki.python.PythonController;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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

    private final boolean printProtocolLog = true;

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

            LoadingScreen frame = LoadingScreen.createAndShow("Hub Verbindung: " + deviceName);
            try {
                CompletableFuture<Boolean> waitingSequence = new CompletableFuture<Boolean>();
                try {
                    frame.setStatusText("Verbinde");
                    startingSequence(waitingSequence,frame);
                    if (waitingSequence.get(20000,TimeUnit.MILLISECONDS)) {
                        startListener();
                        readyForRequest.set(true);
                        startSender();
                        return true;
                    }
                } catch (Exception ignored) {
                    if (printProtocolLog) System.out.println("time capped init failure");
                }
                shutdown();
                return false;

            } finally {
                try {
                    Thread.sleep(2000);
                } catch (Exception ignored) {}
                frame.terminateAllSubThreads();
                frame.dispose(); // Schließe das Fenster
            }

        } else JOptionPane.showMessageDialog(null,"Could not use Python Env","Error",JOptionPane.ERROR_MESSAGE);
        return false;
    }

    private void startingSequence(CompletableFuture<Boolean> success, LoadingScreen hubLoadingScreen) {
        try {
            String initiationLine = reader.get().readLine();
            if (Objects.equals(initiationLine, "crdy")) {
                hubLoadingScreen.setStatusText("Verbindung hergestellt");
                System.out.println("Verbindung hergestellt");

                Thread.sleep(2000);
                writer.get().write("ini::"+"\n");
                writer.get().flush();


                hubLoadingScreen.setStatusText("Sende Init Kommando");
                if (printProtocolLog) System.out.println("Send initiation Command");

                Thread.sleep(1000);
                writer.get().write("dat::"+"\n");
                writer.get().flush();

                if (printProtocolLog) System.out.println("Send response refresh Command");
                Thread.sleep(1000);

                String response = reader.get().readLine();
                if (printProtocolLog) System.out.println(response);
                if (response.equals("ret:ins:")) { // Hub Confirms functionality

                    hubLoadingScreen.setStatusText("Verbindung erfolgreich");
                    success.complete(true);
                }
            } else {
                hubLoadingScreen.showErrorCross();
                hubLoadingScreen.setStatusText("Hub wurde nicht gefunden");
                System.out.println("Verbindung nicht möglich: " + initiationLine);
            }
            success.complete(false);
        } catch (Exception exc) {
            exc.printStackTrace();

        }

    }


    int maxResponseLogSize = 2000;
    Queue<String> responseLog =  new LinkedList<>();
    Queue<String> handleLog = new LinkedList<>(); // Used by sender to receive and Handle requests and responses

    private void addToResponseLog(String response) {
        responseLog.add(response);
        handleLog.add(response);
        if (responseLog.size() > maxResponseLogSize) responseLog.remove();
        if (response.contains("ret:")) {
            response = response.replace("ret:","");
            String[] finalLineSplit = response.split(":");
            String finalResponse = response;
            queuedResults.get().stream().filter(queuedResult -> queuedResult.identifier().equals(finalLineSplit[0])).toList().forEach(queuedResult -> {
                queuedResults.get().remove(queuedResult);
                queuedResult.waiter.complete(finalResponse.replace(finalLineSplit[0] + ":", ""));
            });
        }

    }

    public Thread listenerthread = new Thread(() -> {
        try {
            if (printProtocolLog) System.out.println("Listenner Thread started");
            String line;
            while (isAlive() && (line = reader.get().readLine()) != null) {
                if (printProtocolLog) System.out.println("HUB: " + line);
                if (line.equals("cnf") || line.equals("hwd")) {
                    readyForRequest.set(false);
                    disconnected();
                }
                addToResponseLog(line);
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


    public void disconnect() {
        try {
            writer.get().write("bye::"+"\n");
            writer.get().flush();
            Thread.sleep(500);
        }catch (Exception ignored) {}
    }

    private void disconnected() {
        System.out.println("Hub Disconnected");
        shutdown();
    }


    public String receivedResponse(String identifier) {
        List<String> response = handleLog.stream()
                .filter(log -> {
                   if (log.contains("ret:") && log.split(":").length > 1) {
                       return log.split(":")[1].equals(identifier);
                   }
                   return false;
                }).toList();
       if (!response.isEmpty())
       {
           response.forEach(log -> handleLog.remove(log));
           return response.get(0);
       }
       return null;
    }


    private void startSender() {
        new Thread(() -> {
            try {
                while (isAlive() ) {
                   if (readyForRequest.get()) {
                        QueuedRequest request = queuedRequests.get().poll() ;
                        if (request != null) {
                            try {
                                // Clear Old Responses assigned to identifier
                                receivedResponse(request.identifier());

                                // Send Query
                                writer.get().write(request.query+"\n");
                                writer.get().flush();
                                if (printProtocolLog) System.out.println("Sending: " + request.query);
                                Thread.sleep(300);
                                String response = receivedResponse(request.query);
                                if (response == null) {
                                    if (printProtocolLog) System.out.println("Did not receive a request Response Sending Additional Data request");
                                    long startTime = System.currentTimeMillis();
                                    while ((System.currentTimeMillis() - startTime) < 20000) {
                                        Thread.sleep(500);
                                        writer.get().write("dat::"+"\n");
                                        writer.get().flush();
                                        if (printProtocolLog) System.out.println("Sending: " + "dat::");
                                        response = receivedResponse(request.identifier());
                                        if (response != null) {
                                            break;
                                        }
                                        if (printProtocolLog) System.out.println("Hub did not respond to Cmd after "+ (System.currentTimeMillis()-startTime) + " ms");
                                    }
                                    if (response == null) {
                                        if (printProtocolLog) System.out.println("Hub did not respond to Cmd after "+ (System.currentTimeMillis()-startTime) + " ms");
                                        if (printProtocolLog) System.out.println("Hub seems to be Disconnected");
                                        disconnected();
                                    }
                                }
                                if (printProtocolLog) System.out.println("Received Command Response: " +response);
                                request.waiter().complete(true);
                            } catch (Exception e) {
                                System.out.println("ERROR");
                                e.printStackTrace();
                                request.waiter().complete(false);
                            }
                        }
                    }
                    Thread.sleep(200);
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
        onShutdown.run();
        connectionProcess.get().destroy();
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
    public boolean sendCommand(String commandID, String... args) {
        try {
            CompletableFuture<Boolean> executionWaiter = new CompletableFuture<>();
            queuedRequests.get().add(new QueuedRequest(commandID,getQuery(commandID, args), executionWaiter,false));
            return executionWaiter.get();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Optional<String> sendRequest(String requestID) {
        return sendRequest(requestID,null);
    }

    public Optional<String> sendRequest(String requestID,String... args) {
        try {
            CompletableFuture<String> resultWaiter = new CompletableFuture<>();
            QueuedResult queuedResult = new QueuedResult(requestID, resultWaiter);
            queuedResults.get().add(queuedResult);

            CompletableFuture<Boolean> executionWaiter = new CompletableFuture<>();
            QueuedRequest queuedRequest = new QueuedRequest(requestID,getQuery(requestID, args), executionWaiter,true);
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

    private static String getQuery(String requestID, String[] args) {
        String query = requestID;
        if (args != null && args.length > 0) {
            StringBuilder requestBuilder = new StringBuilder();
            for (String s : args) {
                requestBuilder.append(":").append(s);
            }
            query += requestBuilder.toString();
        } else {
            query += ":";
        }
        query += ":";
        return query;
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