package io.blk.erc20;

import java.util.Arrays;

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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ERC20ControllerIT {

    // key2
    private static final String OTHER_ACCOUNT = "ca843569e3427144cead5e4d5999a3d0ccf92b8e";
    private static final String PRIVATE_FOR = "QfeDAys9MPDs2XHExtc84jKGHxZg/aj52DTh0vtA3Xc=";

    @Autowired
    private NodeConfiguration nodeConfiguration;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testConfig() {
        ResponseEntity<NodeConfiguration> responseEntity =
                this.restTemplate.getForEntity("/config", NodeConfiguration.class);
        verify(responseEntity);
        assertNotNull(responseEntity.getBody());
    }

    @Test
    public void testLifeCycle() {
        ERC20Controller.ContractSpecification contractSpecification =
                new ERC20Controller.ContractSpecification(
                        1_000_000L, "Quorum Token", 6, "QT");

        String contractAddress = deploy(contractSpecification);

        verifyName(contractAddress, contractSpecification.getTokenName());
        verifySymbol(contractAddress, contractSpecification.getTokenSymbol());
        verifyDecimals(contractAddress, contractSpecification.getDecimalUnits());
        verifyVersion(contractAddress, "H0.1");

        verifyTotalSupply(contractAddress, contractSpecification.getInitialAmount());
        verifyBalanceOf(contractAddress,
                nodeConfiguration.getFromAddress(), contractSpecification.getInitialAmount());

        ERC20Controller.ApproveRequest approveRequest = new ERC20Controller.ApproveRequest(
                OTHER_ACCOUNT, 10_000);
        verifyApprove(contractAddress, approveRequest);

        verifyAllowance(
                contractAddress, nodeConfiguration.getFromAddress(), OTHER_ACCOUNT,
                approveRequest.getValue());

        ERC20Controller.TransferRequest transferRequest = new ERC20Controller.TransferRequest(
                OTHER_ACCOUNT, 10_000);
        verifyTransfer(contractAddress, transferRequest);
        verifyBalanceOf(
                contractAddress,
                transferRequest.getTo(),
                transferRequest.getValue());
        verifyBalanceOf(
                contractAddress,
                nodeConfiguration.getFromAddress(),
                contractSpecification.getInitialAmount() - transferRequest.getValue());


        // Needs to be performed by another account
//        ERC20Controller.TransferFromRequest transferFromRequest =
//                new ERC20Controller.TransferFromRequest(
//                        nodeConfiguration.getFromAddress(), OTHER_ACCOUNT, 1000);
//        verifyTransferFrom(contractAddress, transferFromRequest);
//        verifyBalanceOf(
//                contractAddress,
//                transferFromRequest.getFrom(),
//                contractSpecification.getInitialAmount() - transferFromRequest.getValue());
    }

    private String deploy(
            ERC20Controller.ContractSpecification contractSpecification) {

        ResponseEntity<String> responseEntity =
                this.restTemplate.postForEntity(
                        "/deploy", buildEntity(contractSpecification), String.class);
        verify(responseEntity);

        String contractAddress = responseEntity.getBody();
        assertFalse(contractAddress.isEmpty());
        return contractAddress;
    }

    private void verifyName(String contractAddress, String name) {
        ResponseEntity<String> responseEntity =
                this.restTemplate.getForEntity(
                        "/" + contractAddress + "" + "/name", String.class);
        verify(responseEntity);
        assertThat(responseEntity.getBody(), is(name));
    }

    private void verifyTotalSupply(String contractAddress, long totalSupply) {
        ResponseEntity<Long> responseEntity =
                this.restTemplate.getForEntity(
                        "/" + contractAddress + "" + "/totalSupply", long.class);
        verify(responseEntity);
        assertThat(responseEntity.getBody(), is(totalSupply));
    }

    private void verifyDecimals(String contractAddress, long decimals) {
        ResponseEntity<Long> responseEntity =
                this.restTemplate.getForEntity(
                        "/" + contractAddress + "" + "/decimals", long.class);
        verify(responseEntity);
        assertThat(responseEntity.getBody(), is(decimals));
    }

    private void verifyVersion(String contractAddress, String version) {
        ResponseEntity<String> responseEntity =
                this.restTemplate.getForEntity(
                        "/" + contractAddress + "" + "/version", String.class);
        verify(responseEntity);
        assertThat(responseEntity.getBody(), is(version));
    }

    private void verifyBalanceOf(String contractAddress, String ownerAddress, long balance) {
        ResponseEntity<Long> responseEntity =
                this.restTemplate.getForEntity(
                        "/" + contractAddress + "" + "/balanceOf/" + ownerAddress,
                        long.class);
        verify(responseEntity);
        assertThat(responseEntity.getBody(), is(balance));
    }

    private void verifySymbol(String contractAddress, String symbol) {
        ResponseEntity<String> responseEntity =
                this.restTemplate.getForEntity(
                        "/" + contractAddress + "" + "/symbol", String.class);
        verify(responseEntity);
        assertThat(responseEntity.getBody(), is(symbol));
    }

    private void verifyAllowance(
            String contractAddress,
            String ownerAddress,
            String spenderAddress,
            long expected) {
        ResponseEntity<Long> responseEntity =
                this.restTemplate.getForEntity(
                        "/" + contractAddress
                                + "/allowance?"
                                + "ownerAddress={ownerAddress}"
                                + "&spenderAddress={spenderAddress}",
                        long.class,
                        ownerAddress,
                        spenderAddress);
        verify(responseEntity);
        assertThat(responseEntity.getBody(), is(expected));
    }

    private void verifyTransferFrom(
            String contractAddress, ERC20Controller.TransferFromRequest transferFromRequest) {
        ResponseEntity<String> responseEntity =
                this.restTemplate.postForEntity(
                        "/" + contractAddress + "/transferFrom",
                        buildEntity(transferFromRequest),
                        String.class);
        verify(responseEntity);
        assertNotNull(responseEntity.getBody());
    }

    private void verifyApprove(
            String contractAddress, ERC20Controller.ApproveRequest approveRequest) {
        ResponseEntity<String> responseEntity =
                this.restTemplate.postForEntity(
                        "/" + contractAddress + "/approve",
                        buildEntity(approveRequest),
                        String.class);
        verify(responseEntity);
        assertNotNull(responseEntity.getBody());
    }

    private void verifyTransfer(
            String contractAddress, ERC20Controller.TransferRequest transferRequest) {
        ResponseEntity<String> responseEntity =
                this.restTemplate.postForEntity(
                        "/" + contractAddress + "/transfer",
                        buildEntity(transferRequest),
                        String.class);
        verify(responseEntity);
        assertNotNull(responseEntity.getBody());
    }

    private <T> HttpEntity<T> buildEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("privateFor", PRIVATE_FOR);

        return new HttpEntity<T>(body, headers);
    }

    private <T> void verify(ResponseEntity<T> responseEntity) {
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
    }
}
