package client;

public record InGameResult(String cmdResult, ClientRepl.ClientState nextState) {}
