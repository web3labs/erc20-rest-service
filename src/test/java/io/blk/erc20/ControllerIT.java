package io.blk.erc20;

import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static junit.framework.TestCase.assertNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ControllerIT {

    // key2
    private static final String OTHER_ACCOUNT = "ca843569e3427144cead5e4d5999a3d0ccf92b8e";
    // Transaction manager 2
    private static final String PRIVATE_FOR = "QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=";

    @Autowired
    private NodeConfiguration nodeConfiguration;

    @Autowired
    private TestRestTemplate restTemplate;

//    @Before
//    public void setup() {
//        nodeConfiguration.setNodeEndpoint("");
//    }

    @Test
    public void testConfig() {
        ResponseEntity<NodeConfiguration> responseEntity =
                this.restTemplate.getForEntity("/config", NodeConfiguration.class);
        verifyHttpStatus(responseEntity);
        assertNotNull(responseEntity.getBody());
    }

    @Test
    public void testDeploy() {

    }

    @Test
    public void testLifeCycle() {
        Controller.ContractSpecification contractSpecification =
                new Controller.ContractSpecification(
                        BigInteger.valueOf(1000000), "Quorum Token", BigInteger.valueOf(6), "QT");

        String contractAddress = deploy(contractSpecification);

        verifyName(contractAddress, contractSpecification.getTokenName());
        verifySymbol(contractAddress, contractSpecification.getTokenSymbol());
        verifyDecimals(contractAddress, contractSpecification.getDecimalUnits());
        verifyVersion(contractAddress, "H0.1");

        verifyTotalSupply(contractAddress, contractSpecification.getInitialAmount());
        verifyBalanceOf(contractAddress,
                nodeConfiguration.getFromAddress(), contractSpecification.getInitialAmount());

        Controller.ApproveRequest approveRequest = new Controller.ApproveRequest(
                OTHER_ACCOUNT, BigInteger.valueOf(10000));
//        verifyApproveTx(contractAddress, approveRequest);

//        verifyAllowance(
//                contractAddress, nodeConfiguration.getFromAddress(), OTHER_ACCOUNT,
//                approveRequest.getValue());

        Controller.TransferRequest transferRequest = new Controller.TransferRequest(
                OTHER_ACCOUNT, BigInteger.valueOf(10000));
        verifyTransferTx(contractAddress, transferRequest);
        verifyBalanceOf(
                contractAddress,
                transferRequest.getTo(),
                transferRequest.getValue());
        verifyBalanceOf(
                contractAddress,
                nodeConfiguration.getFromAddress(),
                contractSpecification.getInitialAmount().subtract(transferRequest.getValue()));

        // Needs to be performed by another account, hence this will fail
        Controller.TransferFromRequest transferFromRequest =
                new Controller.TransferFromRequest(
                        nodeConfiguration.getFromAddress(), OTHER_ACCOUNT, BigInteger.valueOf(1000));
        verifyTransferFromTxFailure(contractAddress, transferFromRequest);
        // Therefore our balance remains the same
        verifyBalanceOf(
                contractAddress,
                transferFromRequest.getFrom(),
                contractSpecification.getInitialAmount().subtract(transferRequest.getValue()));
    }

    private String deploy(
            Controller.ContractSpecification contractSpecification) {

        ResponseEntity<String> responseEntity =
                this.restTemplate.postForEntity(
                        "/deploy", buildEntity(contractSpecification), String.class);
        verifyHttpStatus(responseEntity);

        String contractAddress = responseEntity.getBody();
        assertFalse(contractAddress.isEmpty());
        return contractAddress;
    }

    private void verifyName(String contractAddress, String name) {
        ResponseEntity<String> responseEntity =
                this.restTemplate.getForEntity(
                        "/" + contractAddress + "" + "/name", String.class);
        verifyHttpStatus(responseEntity);
        assertThat(responseEntity.getBody(), is(name));
    }

    private void verifyTotalSupply(String contractAddress, BigInteger totalSupply) {
        ResponseEntity<BigInteger> responseEntity =
                this.restTemplate.getForEntity(
                        "/" + contractAddress + "" + "/totalSupply", BigInteger.class);
        verifyHttpStatus(responseEntity);
        assertThat(responseEntity.getBody(), is(totalSupply));
    }

    private void verifyDecimals(String contractAddress, BigInteger decimals) {
        ResponseEntity<BigInteger> responseEntity =
                this.restTemplate.getForEntity(
                        "/" + contractAddress + "" + "/decimals", BigInteger.class);
        verifyHttpStatus(responseEntity);
        assertThat(responseEntity.getBody(), is(decimals));
    }

    private void verifyVersion(String contractAddress, String version) {
        ResponseEntity<String> responseEntity =
                this.restTemplate.getForEntity(
                        "/" + contractAddress + "" + "/version", String.class);
        verifyHttpStatus(responseEntity);
        assertThat(responseEntity.getBody(), is(version));
    }

    private void verifyBalanceOf(String contractAddress, String ownerAddress, BigInteger balance) {
        ResponseEntity<BigInteger> responseEntity =
                this.restTemplate.getForEntity(
                        "/" + contractAddress + "" + "/balanceOf/" + ownerAddress,
                        BigInteger.class);
        verifyHttpStatus(responseEntity);
        assertThat(responseEntity.getBody(), is(balance));
    }

    private void verifySymbol(String contractAddress, String symbol) {
        ResponseEntity<String> responseEntity =
                this.restTemplate.getForEntity(
                        "/" + contractAddress + "" + "/symbol", String.class);
        verifyHttpStatus(responseEntity);
        assertThat(responseEntity.getBody(), is(symbol));
    }

    private void verifyAllowance(
            String contractAddress,
            String ownerAddress,
            String spenderAddress,
            BigInteger expected) {
        ResponseEntity<BigInteger> responseEntity =
                this.restTemplate.getForEntity(
                        "/" + contractAddress
                                + "/allowance?"
                                + "ownerAddress={ownerAddress}"
                                + "&spenderAddress={spenderAddress}",
                        BigInteger.class,
                        ownerAddress,
                        spenderAddress);
        verifyHttpStatus(responseEntity);
        assertThat(responseEntity.getBody(), is(expected));
    }

    private void verifyTransferFromTxFailure(
            String contractAddress, Controller.TransferFromRequest transferFromRequest) {
        ResponseEntity<TransactionResponse> responseEntity =
                this.restTemplate.postForEntity(
                        "/" + contractAddress + "/transferFrom",
                        buildEntity(transferFromRequest),
                        TransactionResponse.class);
        verifyPostResponseFailure(responseEntity);
    }

    private void verifyApproveTx(
            String contractAddress, Controller.ApproveRequest approveRequest) {
        ResponseEntity<TransactionResponse> responseEntity =
                this.restTemplate.postForEntity(
                        "/" + contractAddress + "/approve",
                        buildEntity(approveRequest),
                        TransactionResponse.class);
        verifyPostResponse(responseEntity);
    }

    private void verifyTransferTx(
            String contractAddress, Controller.TransferRequest transferRequest) {
        ResponseEntity<TransactionResponse> responseEntity =
                this.restTemplate.postForEntity(
                        "/" + contractAddress + "/transfer",
                        buildEntity(transferRequest),
                        TransactionResponse.class);
        verifyPostResponse(responseEntity);
    }

    private <T> HttpEntity<T> buildEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("privateFor", PRIVATE_FOR);

        return new HttpEntity<>(body, headers);
    }

    private <T> void verifyHttpStatus(ResponseEntity<T> responseEntity) {
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
    }

    private void verifyPostResponse(ResponseEntity<TransactionResponse> responseEntity) {
        verifyPostResponseBody(responseEntity);
        assertNotNull(responseEntity.getBody().getEvent());
    }

    private void verifyPostResponseFailure(ResponseEntity<TransactionResponse> responseEntity) {
        verifyHttpStatus(responseEntity);
        assertNull(responseEntity.getBody().getEvent());
    }

    private void verifyPostResponseBody(ResponseEntity<TransactionResponse> responseEntity) {
        verifyHttpStatus(responseEntity);
        TransactionResponse body = responseEntity.getBody();
        assertNotNull(body);
        String transactionHash = body.getTransactionHash();
        assertTrue(transactionHash.startsWith("0x"));
        assertThat(transactionHash.length(), is(66));
    }
}
