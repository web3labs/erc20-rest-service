package io.blk.erc20;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import io.blk.erc20.generated.HumanStandardToken;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.quorum.Quorum;
import org.web3j.quorum.tx.ClientTransactionManager;
import org.web3j.tx.TransactionManager;

import static org.web3j.tx.Contract.GAS_LIMIT;
import static org.web3j.tx.ManagedTransaction.GAS_PRICE;

/**
 * Our contract service.
 */
@Service
public class ERC20Service {

    private final Quorum quorum;

    private final NodeConfiguration nodeConfiguration;

    @Autowired
    public ERC20Service(Quorum quorum, NodeConfiguration nodeConfiguration) {
        this.quorum = quorum;
        this.nodeConfiguration = nodeConfiguration;
    }

    public NodeConfiguration getConfig() {
        return nodeConfiguration;
    }

    public String deploy(
            List<String> privateFor, long initialAmount, String tokenName, long decimalUnits,
            String tokenSymbol) {
        try {
            TransactionManager transactionManager = new ClientTransactionManager(
                    quorum, nodeConfiguration.getFromAddress(), privateFor);
            HumanStandardToken humanStandardToken = HumanStandardToken.deploy(
                    quorum, transactionManager, GAS_PRICE, GAS_LIMIT, BigInteger.ZERO,
                    new Uint256(initialAmount), new Utf8String(tokenName), new Uint8(decimalUnits),
                    new Utf8String(tokenSymbol)).get();
            return humanStandardToken.getContractAddress();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public String name(String contractAddress) {
        HumanStandardToken humanStandardToken = load(contractAddress);
        try {
            return humanStandardToken.name().get().getValue();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public String approve(
            List<String> privateFor, String contractAddress, String spender, long value) {
        HumanStandardToken humanStandardToken = load(contractAddress, privateFor);
        try {
            TransactionReceipt transactionReceipt = humanStandardToken
                    .approve(new Address(spender), new Uint256(value)).get();
            return transactionReceipt.getTransactionHash();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public long totalSupply(String contractAddress) {
        HumanStandardToken humanStandardToken = load(contractAddress);
        try {
            return humanStandardToken.totalSupply().get().getValue().longValueExact();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public String transferFrom(
            List<String> privateFor, String contractAddress, String from, String to, long value) {
        HumanStandardToken humanStandardToken = load(contractAddress, privateFor);
        try {
            TransactionReceipt transactionReceipt = humanStandardToken
                    .transferFrom(new Address(from), new Address(to), new Uint256(value)).get();
            return transactionReceipt.getTransactionHash();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public long decimals(String contractAddress) {
        HumanStandardToken humanStandardToken = load(contractAddress);
        try {
            return humanStandardToken.decimals().get().getValue().longValueExact();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public String version(String contractAddress) {
        HumanStandardToken humanStandardToken = load(contractAddress);
        try {
            return humanStandardToken.version().get().getValue();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public long balanceOf(String contractAddress, String ownerAddress) {
        HumanStandardToken humanStandardToken = load(contractAddress);
        try {
            return humanStandardToken.balanceOf(new Address(ownerAddress))
                    .get().getValue().longValueExact();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public String symbol(String contractAddress) {
        HumanStandardToken humanStandardToken = load(contractAddress);
        try {
            return humanStandardToken.symbol().get().getValue();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public String transfer(
            List<String> privateFor, String contractAddress, String to, long value) {
        HumanStandardToken humanStandardToken = load(contractAddress, privateFor);
        try {
            TransactionReceipt transactionReceipt = humanStandardToken
                    .transfer(new Address(to), new Uint256(value)).get();
            return transactionReceipt.getTransactionHash();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public String approveAndCall(
            List<String> privateFor, String contractAddress, String spender, long value,
            String extraData) {
        HumanStandardToken humanStandardToken = load(contractAddress, privateFor);
        try {
            TransactionReceipt transactionReceipt = humanStandardToken
                    .approveAndCall(
                            new Address(spender), new Uint256(value),
                            new DynamicBytes(extraData.getBytes()))
                    .get();
            return transactionReceipt.getTransactionHash();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public long allowance(String contractAddress, String ownerAddress, String spenderAddress) {
        HumanStandardToken humanStandardToken = load(contractAddress);
        try {
            return humanStandardToken.allowance(
                    new Address(ownerAddress), new Address(spenderAddress))
                    .get().getValue().longValueExact();
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
}
