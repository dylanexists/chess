package client;

public record PostLoginResult(String cmdResult, ClientRepl.ClientState nextState, Integer gameID) {}
