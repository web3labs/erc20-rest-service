package io.blk.erc20;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for our ERC-20 contract API.
 */
@Api("ERC-20 token standard API")
@RestController
public class ERC20Controller {

    private final ERC20Service ERC20Service;

    @Autowired
    public ERC20Controller(ERC20Service ERC20Service) {
        this.ERC20Service = ERC20Service;
    }

    @ApiOperation("Application configuration")
    @RequestMapping(value = "/config", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    NodeConfiguration config() {
        return ERC20Service.getConfig();
    }

    @ApiOperation(
            value = "Deploy new ERC-20 token",
            notes = "Returns hex encoded contract address")
    @ApiImplicitParam(name = "privateFor",
            value = "Comma separated list of public keys of enclave nodes that transaction is "
                    + "private for",
            paramType = "header",
            dataType = "string")
    @RequestMapping(value = "/deploy", method = RequestMethod.POST)
    String deploy(
            HttpServletRequest request,
            @RequestBody ContractSpecification contractSpecification) {

        return ERC20Service.deploy(
                extractPrivateFor(request),
                contractSpecification.getInitialAmount(),
                contractSpecification.getTokenName(),
                contractSpecification.getDecimalUnits(),
                contractSpecification.getTokenSymbol());
    }

    @ApiOperation("Get token name")
    @RequestMapping(value = "/{contractAddress}/name", method = RequestMethod.GET)
    String name(@PathVariable String contractAddress) {
        return ERC20Service.name(contractAddress);
    }

    @ApiOperation(
            value = "Approve transfers by a specific address up to the provided total quantity",
            notes = "Returns hex encoded transaction hash")
    @ApiImplicitParam(name = "privateFor",
            value = "Comma separated list of public keys of enclave nodes that transaction is "
                    + "private for",
            paramType = "header",
            dataType = "string")
    @RequestMapping(value = "/{contractAddress}/approve", method = RequestMethod.POST)
    String approve(
            HttpServletRequest request,
            @PathVariable String contractAddress,
            @RequestBody ApproveRequest approveRequest) {
        return ERC20Service.approve(
                extractPrivateFor(request),
                contractAddress,
                approveRequest.getSpender(),
                approveRequest.getValue());
    }

    @ApiOperation("Get total supply of tokens")
    @RequestMapping(value = "/{contractAddress}/totalSupply", method = RequestMethod.GET)
    long totalSupply(@PathVariable String contractAddress) {
        return ERC20Service.totalSupply(contractAddress);
    }

    @ApiOperation(
            value = "Transfer tokens between addresses (must already be approved)",
            notes = "Returns hex encoded transaction hash")
    @ApiImplicitParam(name = "privateFor",
            value = "Comma separated list of public keys of enclave nodes that transaction is "
                    + "private for",
            paramType = "header",
            dataType = "string")
    @RequestMapping(value = "/{contractAddress}/transferFrom", method = RequestMethod.POST)
    String transferFrom(
            HttpServletRequest request,
            @PathVariable String contractAddress,
            @RequestBody TransferFromRequest transferFromRequest) {
        return ERC20Service.transferFrom(
                extractPrivateFor(request),
                contractAddress,
                transferFromRequest.getFrom(),
                transferFromRequest.getTo(),
                transferFromRequest.getValue());
    }

    @ApiOperation("Get decimal precision of tokens")
    @RequestMapping(value = "/{contractAddress}/decimals", method = RequestMethod.GET)
    long decimals(@PathVariable String contractAddress) {
        return ERC20Service.decimals(contractAddress);
    }

    @ApiOperation("Get contract version")
    @RequestMapping(value = "/{contractAddress}/version", method = RequestMethod.GET)
    String version(@PathVariable String contractAddress) {
        return ERC20Service.version(contractAddress);
    }

    @ApiOperation("Get token balance for address")
    @RequestMapping(value = "/{contractAddress}/balanceOf/{ownerAddress}", method = RequestMethod.GET)
    long balanceOf(
            @PathVariable String contractAddress,
            @PathVariable String ownerAddress) {
        return ERC20Service.balanceOf(contractAddress, ownerAddress);
    }

    @ApiOperation("Get token symbol")
    @RequestMapping(value = "/{contractAddress}/symbol", method = RequestMethod.GET)
    String symbol(@PathVariable String contractAddress) {
        return ERC20Service.symbol(contractAddress);
    }

    @ApiOperation(
            value = "Transfer tokens you own to another address",
            notes = "Returns hex encoded transaction hash")
    @ApiImplicitParam(name = "privateFor",
            value = "Comma separated list of public keys of enclave nodes that transaction is "
                    + "private for",
            paramType = "header",
            dataType = "string")
    @RequestMapping(value = "/{contractAddress}/transfer", method = RequestMethod.POST)
    String transfer(
            HttpServletRequest request,
            @PathVariable String contractAddress,
            @RequestBody TransferRequest transferRequest) {
        return ERC20Service.transfer(
                extractPrivateFor(request),
                contractAddress,
                transferRequest.getTo(),
                transferRequest.getValue());
    }

    @ApiOperation(
            value = "Approve transfers by a specific contract address up to the provided total "
                    + "quantity, and notify that contract address of the approval",
            notes = "Returns hex encoded transaction hash")
    @ApiImplicitParam(name = "privateFor",
            value = "Comma separated list of public keys of enclave nodes that transaction is "
                    + "private for",
            paramType = "header",
            dataType = "string")
    @RequestMapping(value = "/{contractAddress}/approveAndCall", method = RequestMethod.POST)
    String approveAndCall(
            HttpServletRequest request,
            @PathVariable String contractAddress,
            @RequestBody ApproveAndCallRequest approveAndCallRequest) {
        return ERC20Service.approveAndCall(
                extractPrivateFor(request),
                contractAddress,
                approveAndCallRequest.getSpender(),
                approveAndCallRequest.getValue(),
                approveAndCallRequest.getExtraData());
    }

    @ApiOperation("Get quantity of tokens you can transfer on another token holder's behalf")
    @RequestMapping(value = "/{contractAddress}/allowance", method = RequestMethod.GET)
    long allowance(
            @PathVariable String contractAddress,
            @RequestParam String ownerAddress,
            @RequestParam String spenderAddress) {
        return ERC20Service.allowance(
                contractAddress, ownerAddress, spenderAddress);
    }

    private static List<String> extractPrivateFor(HttpServletRequest request) {
        String privateFor = request.getHeader("privateFor");
        if (privateFor == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(privateFor.split(","));
        }
    }

    @Data
    static class ContractSpecification {
        private final long initialAmount;
        private final String tokenName;
        private final long decimalUnits;
        private final String tokenSymbol;
    }

    @Data
    static class ApproveRequest {
        private final String spender;
        private final long value;
    }

    @Data
    static class TransferFromRequest {
        private final String from;
        private final String to;
        private final long value;
    }

    @Data
    static class TransferRequest {
        private final String to;
        private final long value;
    }

    @Data
    static class ApproveAndCallRequest {
        private final String spender;
        private final long value;
        private final String extraData;
    }

    @Data
    static class AllowanceRequest {
        private final String ownerAddress;
        private final String spenderAddress;
    }
}
