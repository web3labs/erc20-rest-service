package io.blk.erc20;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Node configuration bean.
 */
@Data
@ConfigurationProperties
@Component
public class NodeConfiguration {

    private String nodeEndpoint;
    private String fromAddress;
}
