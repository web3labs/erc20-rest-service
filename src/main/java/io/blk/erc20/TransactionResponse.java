package io.blk.erc20;

import lombok.Getter;
import lombok.Setter;

/**
 * TransactionResponse wrapper.
 */
@Getter
@Setter
public class TransactionResponse<T> {

    private String transactionHash;
    private T event;

    TransactionResponse() { }

    public TransactionResponse(String transactionHash) {
        this(transactionHash, null);
    }

    public TransactionResponse(String transactionHash, T event) {
        this.transactionHash = transactionHash;
        this.event = event;
    }
}
