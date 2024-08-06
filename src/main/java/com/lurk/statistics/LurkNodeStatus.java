package com.lurk.statistics;

import java.time.Duration;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * Instance of node status information parsed from HTTP
 * body. This status is expected to be requested during
 * health checking process. 
*/
public class LurkNodeStatus {

    private static final Logger log = LoggerFactory.getLogger(LurkNodeStatus.class);

    @JsonProperty("uptime_secs")
    private long uptimeSecs;

    @JsonProperty("started_utc_ts")
    private Date startedUtcDate;

    public Duration getNodeUptime() {
        return Duration.ofSeconds(uptimeSecs);
    }

    public Date getNodeStartedUtcDate() {
        return startedUtcDate;
    }

    public static LurkNodeStatus from(String jsonString) {
        try {
            return new ObjectMapper().readValue(jsonString, LurkNodeStatus.class);
        } catch (JsonProcessingException e) {
            log.error("Exception thrown while parsing node status JSON from HTTP body", e);
            return null;
        }
    }

    @Override
    public String toString() {
        return "{ uptime=%s, started='%s' }".formatted(getNodeUptime(), getNodeStartedUtcDate());
    }
}
