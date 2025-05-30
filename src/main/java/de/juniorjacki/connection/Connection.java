package de.juniorjacki.connection;

import de.juniorjacki.python.PythonController;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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
        this.serviceUUID = serviceUUID;
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
            startListener();
            startSender();
            return true;

        } else JOptionPane.showMessageDialog(null,"Could not use Python Env","Error",JOptionPane.ERROR_MESSAGE);
        return false;
    }



    private void startListener() {
        new Thread(() -> {
            try {
                String line;
                boolean initiated = false;
                while (isAlive() && (line = reader.get().readLine()) != null) {
                    System.out.println("HUB: " + line);

                    if (line.contains("rdy") && initiated) {
                        readyForRequest.set(true);
                        line = line.replace("rdy:", "");
                    }

                    if (line.contains("ins")) {
                        initiated = true;
                        readyForRequest.set(true);
                       // startSender();
                    }

                    if (line.equals("crdy") && !initiated) {
                        System.out.println("Start The Program Now");
                        JOptionPane.showConfirmDialog(null,"Hast du das Programm auf dem Hub gestartet?","Bitte bestätige",JOptionPane.YES_NO_OPTION);
                        writer.get().write("ini\n");
                        writer.get().flush();
                        System.out.println("Send: ini");
                    }

                    if (line.equals("cnf") || line.equals("hwd")) {
                        readyForRequest.set(false);
                        disconnected();
                    }


                    AtomicReference<String[]> finalLineSplit = new AtomicReference<>(line.split(":"));
                    AtomicReference<String> finalLine = new AtomicReference<>(line);
                    queuedResults.get().stream().filter(queuedResult -> queuedResult.identifier().equals(finalLineSplit.get()[0])).toList().forEach(queuedResult -> {
                        queuedResults.get().remove(queuedResult);
                        queuedResult.waiter.complete(finalLine.get().replace(finalLineSplit.get()[0]+":",""));
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally { // RESTART if Needed
                if (isAlive()) startListener();
            }
        }).start();
    }

    private void disconnected() {
        System.out.println("Hub Disconnected");
        connectionProcess.get().destroy();
        onShutdown.run();
    }

    private void startSender() {
        new Thread(() -> {
            try {
                while (isAlive() ) {
                   if (readyForRequest.get()) {
                        QueuedRequest request = queuedRequests.get().poll() ;
                        if (request != null) {
                            readyForRequest.set(false);
                            try {
                                QueuedResult checkResult = setResultHook(request.identifier);

                                writer.get().write(request.query+"\n");
                                writer.get().flush();


                                try {
                                    checkResult.waiter.get(requestTimeoutTime, TimeUnit.MILLISECONDS);
                                } catch (Exception ignored) {

                                    if (request.resendOnError) {
                                        writer.get().write(request.query+"\n");
                                        writer.get().flush();
                                        System.out.println("ReSend: " + request.query);
                                    } else {
                                        readyForRequest.set(true);
                                    }
                                }

                                deleteResultHook(checkResult);
                                request.waiter().complete(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                                request.waiter().complete(false);
                            }
                        }
                    }
                    Thread.sleep(40);
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
    }

    private boolean isAlive() {
        if (!connectionProcess.get().isAlive()) {
            readyForRequest.set(false);
            onShutdown.run(); // Shutdown Callback
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