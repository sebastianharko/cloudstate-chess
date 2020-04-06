package com.harko.chess;

import static org.junit.Assert.assertTrue;

import io.cloudstate.javasupport.impl.eventsourced.EventSourcedImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

public class Tst {

    @Test
    public void isGreaterTest() {
        assertTrue("Num 1 is greater than Num 2", 2 > 1);
    }

    @Test
    public void canInitializeBoard() {
        ChessBoardEntity entity = new ChessBoardEntity(UUID.randomUUID().toString());
        assertTrue(entity.blackPlayerSideCode == null);
        assertTrue(entity.whiteSidePassCode == null);
        assertTrue(entity.getTurn().equals("white"));
    }

    @Test
    public void canHandleMoveMade() {
        ChessBoardEntity entity = new ChessBoardEntity(UUID.randomUUID().toString());
        entity.handleMoveMade(Pb.MoveMade.newBuilder().setSource("A2").setTarget("A3").build());
        assertTrue(entity.getTurn().equals("black"));
        assertTrue(!entity.board.isDraw());
        assertTrue(!entity.board.isMated());
        assertTrue(!entity.board.isStaleMate());
    }

    @Test
    public void canHandleUserEnteredGame() {
        String boardId = UUID.randomUUID().toString();
        ChessBoardEntity entity = new ChessBoardEntity(boardId);
        entity.handleUserEnteredGame(Pb.UserEnteredGame.newBuilder().setPassCode("cloud").setSide("white").build());
        assertTrue(entity.blackPlayerSideCode == null);
        assertTrue(entity.whiteSidePassCode.equals("cloud"));
        entity.handleUserEnteredGame(Pb.UserEnteredGame.newBuilder().setPassCode("cloud").setSide("black").build());
        assertTrue(entity.blackPlayerSideCode.equals("cloud"));
        assertTrue(entity.whiteSidePassCode.equals("cloud"));
    }
}


