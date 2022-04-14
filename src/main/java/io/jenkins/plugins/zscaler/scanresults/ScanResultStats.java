package io.jenkins.plugins.zscaler.scanresults;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanResultStats {
    private int total;

    @JsonProperty("severity_counts")
    private SeverityCounts policyCounts;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public SeverityCounts getPolicyCounts() {
        return policyCounts;
    }

    public void setPolicyCounts(SeverityCounts policyCounts) {
        this.policyCounts = policyCounts;
    }

}
