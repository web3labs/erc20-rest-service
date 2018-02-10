package io.blk.erc20;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import io.blk.erc20.generated.HumanStandardToken;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.quorum.Quorum;
import org.web3j.quorum.tx.ClientTransactionManager;
import org.web3j.tx.TransactionManager;

import static org.web3j.tx.Contract.GAS_LIMIT;
import static org.web3j.tx.ManagedTransaction.GAS_PRICE;

/**
 * Our smart contract service.
 */
@Service
public class ContractService {

    private final Quorum quorum;

    private final NodeConfiguration nodeConfiguration;

    @Autowired
    public ContractService(Quorum quorum, NodeConfiguration nodeConfiguration) {
        this.quorum = quorum;
        this.nodeConfiguration = nodeConfiguration;
    }

    public NodeConfiguration getConfig() {
        return nodeConfiguration;
    }

    public String deploy(
            List<String> privateFor, BigInteger initialAmount, String tokenName, BigInteger decimalUnits,
            String tokenSymbol) throws Exception {
        try {
            TransactionManager transactionManager = new ClientTransactionManager(
                    quorum, nodeConfiguration.getFromAddress(), privateFor);
            HumanStandardToken humanStandardToken = HumanStandardToken.deploy(
                    quorum, transactionManager, GAS_PRICE, GAS_LIMIT,
                    initialAmount, tokenName, decimalUnits,
                    tokenSymbol).send();
            return humanStandardToken.getContractAddress();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public String name(String contractAddress) throws Exception {
        HumanStandardToken humanStandardToken = load(contractAddress);
        try {
            return extractValue(humanStandardToken.name().send());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public TransactionResponse<ApprovalEventResponse> approve(
            List<String> privateFor, String contractAddress, String spender, BigInteger value) throws Exception {
        HumanStandardToken humanStandardToken = load(contractAddress, privateFor);
        try {
            TransactionReceipt transactionReceipt = humanStandardToken
                    .approve(spender, value).send();
            return processApprovalEventResponse(humanStandardToken, transactionReceipt);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public long totalSupply(String contractAddress) throws Exception {
        HumanStandardToken humanStandardToken = load(contractAddress);
        try {
            return extractLongValue(humanStandardToken.totalSupply().send());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public TransactionResponse<TransferEventResponse> transferFrom(
            List<String> privateFor, String contractAddress, String from, String to, BigInteger value) throws Exception {
        HumanStandardToken humanStandardToken = load(contractAddress, privateFor);
        try {
            TransactionReceipt transactionReceipt = humanStandardToken
                    .transferFrom(from, to, value).send();
            return processTransferEventsResponse(humanStandardToken, transactionReceipt);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public long decimals(String contractAddress) throws Exception {
        HumanStandardToken humanStandardToken = load(contractAddress);
        try {
            return extractLongValue(humanStandardToken.decimals().send());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public String version(String contractAddress) throws Exception {
        HumanStandardToken humanStandardToken = load(contractAddress);
        try {
            return extractValue(humanStandardToken.version().send());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public long balanceOf(String contractAddress, String ownerAddress) throws Exception {
        HumanStandardToken humanStandardToken = load(contractAddress);
        try {
            return extractLongValue(humanStandardToken.balanceOf(ownerAddress).send());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public String symbol(String contractAddress) throws Exception {
        HumanStandardToken humanStandardToken = load(contractAddress);
        try {
            return extractValue(humanStandardToken.symbol().send());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public TransactionResponse<TransferEventResponse> transfer(
            List<String> privateFor, String contractAddress, String to, BigInteger value) throws Exception {
        HumanStandardToken humanStandardToken = load(contractAddress, privateFor);
        try {
            TransactionReceipt transactionReceipt = humanStandardToken
                    .transfer(to, value).send();
            return processTransferEventsResponse(humanStandardToken, transactionReceipt);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public TransactionResponse<ApprovalEventResponse> approveAndCall(
            List<String> privateFor, String contractAddress, String spender, BigInteger value,
            String extraData) throws Exception {
        HumanStandardToken humanStandardToken = load(contractAddress, privateFor);
        try {
            TransactionReceipt transactionReceipt = humanStandardToken
                    .approveAndCall(
                            spender, value,
                            extraData.getBytes())
                    .send();
            return processApprovalEventResponse(humanStandardToken, transactionReceipt);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public long allowance(String contractAddress, String ownerAddress, String spenderAddress) throws Exception {
        HumanStandardToken humanStandardToken = load(contractAddress);
        try {
            return extractLongValue(humanStandardToken.allowance(
                    ownerAddress, spenderAddress)
                    .send());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private HumanStandardToken load(String contractAddress, List<String> privateFor) {
        TransactionManager transactionManager = new ClientTransactionManager(
                quorum, nodeConfiguration.getFromAddress(), privateFor);
        return HumanStandardToken.load(
                contractAddress, quorum, transactionManager, GAS_PRICE, GAS_LIMIT);
    }

    private HumanStandardToken load(String contractAddress) {
        TransactionManager transactionManager = new ClientTransactionManager(
                quorum, nodeConfiguration.getFromAddress(), Collections.emptyList());
        return HumanStandardToken.load(
                contractAddress, quorum, transactionManager, GAS_PRICE, GAS_LIMIT);
    }

    private <T> T extractValue(T value) {
        if (value != null) {
            return value;
        } else {
            throw new RuntimeException("Empty value returned by call");
        }
    }

    private long extractLongValue(BigInteger value) {
        return extractValue(value).longValueExact();
    }

    private TransactionResponse<ApprovalEventResponse>
            processApprovalEventResponse(
            HumanStandardToken humanStandardToken,
            TransactionReceipt transactionReceipt) {

        return processEventResponse(
                humanStandardToken.getApprovalEvents(transactionReceipt),
                transactionReceipt,
                ApprovalEventResponse::new);
    }

    private TransactionResponse<TransferEventResponse>
            processTransferEventsResponse(
            HumanStandardToken humanStandardToken,
            TransactionReceipt transactionReceipt) {

        return processEventResponse(
                humanStandardToken.getTransferEvents(transactionReceipt),
                transactionReceipt,
                TransferEventResponse::new);
    }

    private <T, R> TransactionResponse<R> processEventResponse(
            List<T> eventResponses, TransactionReceipt transactionReceipt, Function<T, R> map) {
        if (!eventResponses.isEmpty()) {
            return new TransactionResponse<>(
                    transactionReceipt.getTransactionHash(),
                    map.apply(eventResponses.get(0)));
        } else {
            return new TransactionResponse<>(
                    transactionReceipt.getTransactionHash());
        }
    }

    @Getter
    @Setter
    public static class TransferEventResponse {
        private String from;
        private String to;
        private long value;

        public TransferEventResponse() { }

        public TransferEventResponse(
                HumanStandardToken.TransferEventResponse transferEventResponse) {
            this.from = transferEventResponse._from;
            this.to = transferEventResponse._to;
            this.value = transferEventResponse._value.longValueExact();
        }
    }

    @Getter
    @Setter
    public static class ApprovalEventResponse {
        private String owner;
        private String spender;
        private long value;

        public ApprovalEventResponse() { }

        public ApprovalEventResponse(
                HumanStandardToken.ApprovalEventResponse approvalEventResponse) {
            this.owner = approvalEventResponse._owner;
            this.spender = approvalEventResponse._spender;
            this.value = approvalEventResponse._value.longValueExact();
        }
    }
}
