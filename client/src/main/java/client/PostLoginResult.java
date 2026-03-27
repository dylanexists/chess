package client;

import chess.ChessGame;

public record PostLoginResult(String cmdResult, ClientRepl.ClientState nextState, Integer gameID, ChessGame.TeamColor playerColor) {}
