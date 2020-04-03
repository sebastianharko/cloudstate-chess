package com.harko.chess;

import com.harko.chess.pb.ChessGameGrpc;
import io.cloudstate.javasupport.CloudState;

public final class Main {
  public static final void main(String[] args) throws Exception {
    new CloudState()
        .registerEventSourcedEntity(
            ChessBoardEntity.class,
            ChessGameGrpc.javaDescriptor(),
                Pb.getDescriptor()
        )
        .start()
        .toCompletableFuture()
        .get();
  }
}
