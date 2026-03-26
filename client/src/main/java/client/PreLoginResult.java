package client;

public record PreLoginResult(String cmdResult, ClientRepl.ClientState nextState, String authToken) {}
