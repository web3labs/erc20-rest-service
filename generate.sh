#!/usr/bin/env bash

cd src/main/resources/solidity/contract/ && \
    solc --bin --abi --optimize --overwrite HumanStandardToken.sol -o build/ && \
    web3j solidity generate \
        build/HumanStandardToken.bin \
        build/HumanStandardToken.abi \
        -p io.blk.erc20.generated \
        -o ../../../java/
