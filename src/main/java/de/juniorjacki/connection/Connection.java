import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.List;
import java.util.Optional;

public class Connection {
    private Process connectionProcess;
    private BufferedReader reader;
    private BufferedWriter writer;

    public Connection(String hubName,String blSeviceUUID) {
        
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        
    }

    record hubServiceData(int voltage,int ampere){};

    public Optional<hubServiceData> getServiceData() {
        writer.write("rsd");


    } 

    public Optional<String> waitForRequest(String requestIdentifier) {
        long endTime = System.currentTimeMillis() + 5000;
        while (endTime >= System.currentTimeMillis()) {
            List<String> foundRequests = reader.lines().filter(s -> s.contains(requestIdentifier)).collect(String::new);
            for (String request : foundRequests) {
                String timestamp = request.split(":")[0];
                
            }
        }
    }

    
}