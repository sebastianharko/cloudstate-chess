package com.harko.chess;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import io.cloudstate.javasupport.EntityId;
import io.cloudstate.javasupport.eventsourced.*;

import java.util.UUID;

/**
 * An event sourced entity.
 */
@EventSourcedEntity
public class ChessBoardEntity {
    private final String entityId;

    private String whiteSidePassCode = null;
    private String blackPlayerSideCode = null;

    private Board board = new Board();

    public ChessBoardEntity(@EntityId String entityId) {
        this.entityId = entityId;
    }

    private String getBoardAsFen() {
        return board.getFen();
    }

    private String getTurn() {
        return board.getSideToMove().value().toLowerCase();
    }

    @Snapshot
    public Pb.FullBoard snapshot() {
        return Pb.FullBoard.newBuilder().setFen(getBoardAsFen())
                .setBlackPassCode(blackPlayerSideCode)
                .setWhitePassCode(whiteSidePassCode)
                .build();
    }

    @SnapshotHandler
    public void handleSnapshot(Pb.FullBoard board) {
        this.board.clear();
        this.board.loadFromFen(board.getFen());
        this.blackPlayerSideCode = board.getBlackPassCode();
        this.whiteSidePassCode = board.getWhitePassCode();
    }

    private boolean isValidPassCode(String passCode) {
        if (passCode.equals(blackPlayerSideCode)) {
            return getTurn().equals("black");
        } else if (passCode.equals(whiteSidePassCode)) {
            return getTurn().equals("white");
        } else {
            return false;
        }
    }

    private boolean isValidMove(String source, String target) {
        return board.isMoveLegal(new Move(Square.fromValue(source), Square.fromValue(target)), true);
    }

    @EventHandler
    public void handleMoveMade(Pb.MoveMade moveMade) {
        this.board.doMove(new Move(Square.fromValue(moveMade.getSource()), Square.fromValue(moveMade.getTarget())));
    }

    @EventHandler
    public void handleUserEnteredGame(Pb.UserEnteredGame userEnteredGame) {
        if (userEnteredGame.getSide().equals("white")) {
            this.whiteSidePassCode = userEnteredGame.getPassCode();
        } else if (userEnteredGame.getSide().equals("black")) {
            this.blackPlayerSideCode = userEnteredGame.getPassCode();
        }
    }

    @CommandHandler
    public Pb.EnterGameResult cEnterGame(Pb.EnterGame command, CommandContext ctx) {
        if (whiteSidePassCode == null) {
            whiteSidePassCode = UUID.randomUUID().toString();
            ctx.emit(Pb.UserEnteredGame.newBuilder()
                    .setPassCode(whiteSidePassCode)
                    .setSide("white").build());
            return Pb.EnterGameResult.newBuilder()
                    .setSide("white")
                    .setCanEnterGame(true)
                    .setPassCode(whiteSidePassCode).build();
        } else if (blackPlayerSideCode == null) {
            blackPlayerSideCode = UUID.randomUUID().toString();
            ctx.emit(Pb.UserEnteredGame.newBuilder()
                    .setSide("black")
                    .setPassCode(blackPlayerSideCode).build());
            return Pb.EnterGameResult.newBuilder()
                    .setSide("black")
                    .setCanEnterGame(true)
                    .setPassCode(blackPlayerSideCode).build();
        } else {
            return Pb.EnterGameResult.newBuilder()
                    .setCanEnterGame(false)
                    .setSide("observer")
                    .setPassCode("none")
                    .build();
        }
    }

    @CommandHandler
    public Pb.MakeMoveResult cMakeMove(Pb.MakeMove command, CommandContext ctx) {
        if (isValidPassCode(command.getPassCode())) {
            if (isValidMove(command.getSource(), command.getTarget())) {
                ctx.emit(Pb.MoveMade.newBuilder().setSource(command.getSource()).setTarget(command.getTarget()).build());
                return Pb.MakeMoveResult.newBuilder().setValid(true).build(); // move is valid
            } else {
                return Pb.MakeMoveResult.newBuilder().setValid(false).build(); // move is not valid
            }
        } else {
            return Pb.MakeMoveResult.newBuilder().setValid(false).build(); // invalid pass code or not your turn
        }
    }

    @CommandHandler
    public Pb.BoardView cGetBoard(CommandContext ctx) {
        return Pb.BoardView.newBuilder()
                .setFen(getBoardAsFen())
                .build();
    }


}
