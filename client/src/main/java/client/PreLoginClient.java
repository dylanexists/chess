package client;

import server.ResponseException;
import server.ServerFacade;

import java.util.Scanner;

public class PreLoginClient {
    private boolean active;

    public PreLoginClient() throws ResponseException {
        active = false;
    }

    public void run() {
        active = true;
        System.out.println("♕ Welcome to 240 chess. Type Help to get started. ♕");

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (active) {
            printPrompt();
            String line = scanner.nextLine();
        }
    }

    private void printPrompt() {
        System.out.print("\n[LOGGED_OUT] >>> ");
    }

}
