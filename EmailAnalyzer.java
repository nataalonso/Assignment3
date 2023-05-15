package for_assignment3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class EmailAnalyzer {
    private Map<String, List<String>> adjacencyList; // Stores friendship graph
    private List<String> connectors; // Stores connectors in the graph
    private Map<String, Integer> sentCountMap; // Tracks number of sent emails for each person
    private Map<String, Integer> receivedCountMap; // Tracks number of received emails for each person
    private Map<String, List<String>> teamMap; // Stores team information for each person

    public EmailAnalyzer() {
        adjacencyList = new HashMap<>();
        connectors = new ArrayList<>();
        sentCountMap = new HashMap<>();
        receivedCountMap = new HashMap<>();
        teamMap = new HashMap<>();
    }

    public void addEdge(String person1, String person2) {
    	
        // Add an edge between two people in the friendship graph
        addNeighbor(person1, person2);
        addNeighbor(person2, person1);
    }

    private void addNeighbor(String person, String neighbor) {
        List<String> neighbors = adjacencyList.getOrDefault(person, new ArrayList<>());
        if (!neighbors.contains(neighbor)) {
            neighbors.add(neighbor);
        }
        adjacencyList.put(person, neighbors);
    }

    public List<String> getFriends(String person) {
        // Get the friends of a given person in the friendship graph
        return adjacencyList.getOrDefault(person, new ArrayList<>());
    }

    public boolean areFriends(String person1, String person2) {
        // Check if two people are friends in the friendship graph
        List<String> friends = adjacencyList.getOrDefault(person1, new ArrayList<>());
        return friends.contains(person2);
    }

    public void identifyConnectors() {
        connectors.clear();
        // Initialize data structures for the depth-first search (DFS)
        Map<String, Boolean> visited = new HashMap<>(); // Tracks visited nodes
        Map<String, Integer> dfsNum = new HashMap<>(); // Assigns DFS numbers to nodes
        Map<String, Integer> back = new HashMap<>(); // Tracks the lowest DFS number reachable from a node
        
        // Mark all nodes as not visited initially
        for (String person : adjacencyList.keySet()) {
            visited.put(person, false);
        }
        
        
        // Perform DFS on each unvisited node
        for (String person : adjacencyList.keySet()) {
            if (!visited.get(person)) {
                dfs(person, person, visited, dfsNum, back); // Start DFS from the current person
            }
        }
    }
    
    private void dfs(String start, String person, Map<String, Boolean> visited,
    	
                     Map<String, Integer> dfsNum, Map<String, Integer> back) {
        visited.put(person, true);  // Mark the current person as visited
        int dfsNumber = dfsNum.getOrDefault(person, 0) + 1; // Assign a new DFS number
        dfsNum.put(person, dfsNumber); // Update the DFS number for the current person
        back.put(person, dfsNumber); // Initialize the lowest reachable DFS number as the current DFS number

        List<String> neighbors = adjacencyList.get(person);
        if (neighbors != null) {
            for (String neighbor : neighbors) {
                if (!visited.get(neighbor)) {
                    dfs(start, neighbor, visited, dfsNum, back);
                    
                    // Update the lowest reachable DFS number of the current person
                    back.put(person, Math.min(back.get(person), back.get(neighbor)));
                    
                    // Check if the current person is a connector
                    if ((dfsNum.get(person) <= back.get(neighbor)) && !person.equals(start)) {
                        connectors.add(person); // Add the current person to the list of connectors
                    }
                } else {
                	// Update the lowest reachable DFS number of the current person
                    back.put(person, Math.min(back.get(person), dfsNum.get(neighbor)));
                }
            }
        }
    }
    private static class Mail {
        private String sender;
        private List<String> recipients;
       
        
        public Mail(String sender, List<String> recipients) {
            this.sender = sender;
            this.recipients = recipients;
        }

        public String getSender() {
            return sender; // Returns address of sender
        }

        public List<String> getRecipients() {
            return recipients; // Returns list of email addresses of recipients
        }
        
        
    }

    public void processMail(Mail mail) {
        String sender = mail.getSender();
        List<String> recipients = mail.getRecipients();

        // Update sent count
        if (sentCountMap.containsKey(sender)) {
            sentCountMap.put(sender, sentCountMap.get(sender) + 1);
        } else {
            sentCountMap.put(sender, 1);
        }

        // Update received
       
        for (String recipient : recipients) {
            if (receivedCountMap.containsKey(recipient)) {
                receivedCountMap.put(recipient, receivedCountMap.get(recipient) + 1);
            } else {
                receivedCountMap.put(recipient, 1);
            }
        }

        // Update team map
        for (String recipient : recipients) {
            List<String> team = teamMap.getOrDefault(sender, new ArrayList<>());
            if (!team.contains(recipient)) {
                team.add(recipient);
            }
            teamMap.put(sender, team);

            List<String> recipientTeam = teamMap.getOrDefault(recipient, new ArrayList<>());
            if (!recipientTeam.contains(sender)) {
                recipientTeam.add(sender);
            }
            teamMap.put(recipient, recipientTeam);
        }
    }

    public void printConnectors(String filePath) {
        try {
            if (filePath != null) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
                for (String connector : connectors) {
                    writer.write(connector);
                    writer.newLine();
                }
                writer.close();
            }

            for (String connector : connectors) {
                System.out.println(connector);
            }
        } catch (IOException e) {
            System.out.println("Error writing connectors to file.");
            e.printStackTrace();
        }
    }

    public void printPersonDetails(String emailAddress) {
        if (!adjacencyList.containsKey(emailAddress)) {
            System.out.println("Email address (" + emailAddress + ") not found in the dataset.");
            return;
        }

        int sentCount = sentCountMap.getOrDefault(emailAddress, 0);
        int receivedCount = receivedCountMap.getOrDefault(emailAddress, 0);
        List<String> team = teamMap.getOrDefault(emailAddress, new ArrayList<>());

        System.out.println(emailAddress + " has sent messages to " + sentCount + " others");
        System.out.println(emailAddress + " has received messages from " + receivedCount + " others");
        System.out.println(emailAddress + " is in a team with " + team.size() + " individuals");
    }

    public static void main(String[] args) {
        String connectorsFilePath = args.length > 1 ? args[1] : null;

        // Create a new instance of the EmailAnalyzer
        EmailAnalyzer analyzer = new EmailAnalyzer();

        // Read and process the dataset, populate the analyzer's data structures
        String datasetPath = args.length > 0 ? args[0] : "dataset.txt";
        try (Scanner fileScanner = new Scanner(new File(datasetPath))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] tokens = line.split(",");

                String sender = tokens[0];
                List<String> recipients = Arrays.asList(tokens).subList(1, tokens.length);

                // Create a new Mail object and process it
                Mail mail = new Mail(sender, recipients);
                analyzer.processMail(mail);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Dataset file not found.");
        }

        // Identify connectors
        analyzer.identifyConnectors();

        // Print connectors
        analyzer.printConnectors(connectorsFilePath);

        // Interactive user input for person details
        Scanner scanner = new Scanner(System.in);
        String emailAddress;

        while (true) {
            System.out.print("Email address of the individual (or EXIT to quit): ");
            emailAddress = scanner.nextLine();

            if (emailAddress.equalsIgnoreCase("EXIT")) {
                break;
            }

            analyzer.printPersonDetails(emailAddress);
        }

        scanner.close();
    }

}

