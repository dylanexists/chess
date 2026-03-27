package ui;

import chess.ChessGame;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static ui.EscapeSequences.*;

public class DrawnChessBoard {

    ChessGame game;

    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final int SQUARE_SIZE_IN_PADDED_CHARS = 3;
    private static final boolean REVERSED = false;
    public static final String TINY = "\u2006"; // without this things dont line up

    public DrawnChessBoard(ChessGame game) {
        this.game = game;
    }

    public static void main(String[] args) {
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);

        out.print(ERASE_SCREEN);
        drawHeader(out);
        drawLine(out);
        //drawBorderLine(out, "1");
        //drawBorderLine(out, null);

        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private static void drawLine(PrintStream out) {
        drawBorderLine(out, " 8 ");
        out.print(SET_BG_COLOR_DARK_GREEN);
        drawBorderLine(out, BLACK_PAWN);
        out.print(SET_BG_COLOR_LIGHT_GREY);
        drawBorderLine(out, EMPTY);
        out.print(SET_BG_COLOR_DARK_GREEN);
        drawBorderLine(out, EMPTY);
        out.print(SET_BG_COLOR_LIGHT_GREY);
        drawBorderLine(out, EMPTY);
        out.print(SET_BG_COLOR_DARK_GREEN);
        drawBorderLine(out, EMPTY);
        out.print(SET_BG_COLOR_LIGHT_GREY);
        drawBorderLine(out, EMPTY);
        out.print(SET_BG_COLOR_DARK_GREEN);
        drawBorderLine(out, EMPTY);
        out.print(SET_BG_COLOR_LIGHT_GREY);
        drawBorderLine(out, EMPTY);
        out.print(SET_BG_COLOR_BLUE);
        drawBorderLine(out, " 8  " + TINY);
    }

    private static void drawBorderLine(PrintStream out, String number) {
        out.print(SET_TEXT_COLOR_BLACK);
        out.print(number);

    }





    public static void drawHeader(PrintStream out) {
        out.print(SET_TEXT_COLOR_BLACK);
        out.print(SET_BG_COLOR_BLUE);

        out.print(EMPTY);

        String[] headers = {"a", TINY + "b", TINY + "c", "d", TINY + "e", TINY + "f", "g", "h"}; //weird additional spacing to account for em-space being weird
        if (!REVERSED) {
            for (int boardCol = 0; boardCol < BOARD_SIZE_IN_SQUARES; boardCol++) {
                drawBigHeader(out, headers[boardCol]);
            }
        } else {
            for (int boardCol = BOARD_SIZE_IN_SQUARES; boardCol > 0; boardCol--) {
                drawBigHeader(out, headers[boardCol - 1]);
            }
        }
        out.print(EMPTY);
        headerNewline(out);


    }

    private static void headerNewline(PrintStream out) {
        out.print(RESET_BG_COLOR);
        out.println();
        out.print(SET_BG_COLOR_BLUE);
    }

    private static void drawBigHeader(PrintStream out, String headerText) {

        //out.print(EMPTY.repeat(prefixLength));
        out.print(TINY + TINY + headerText + "  ");
        //out.print(EMPTY.repeat(suffixLength));
    }

}
