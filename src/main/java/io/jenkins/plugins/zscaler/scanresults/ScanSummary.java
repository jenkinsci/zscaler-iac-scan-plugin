package io.jenkins.plugins.zscaler.scanresults;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanSummary {

    @JsonProperty("scan_path")
    private String fileOrFolder;

    private String status;

    @JsonProperty("status_description")
    private String statusDescription;

    @JsonProperty("scanned_at")
    private String scannedAt;

    @JsonProperty("template_type")
    private List<String> iacType;

    @JsonProperty("total_policies")
    private int totalPolicies;

    @JsonProperty("passed_findings")
    private int passedFindings;

    @JsonProperty("failed_findings")
    private ScanResultStats failedFindings;

    @JsonProperty("skipped_findings")
    private ScanResultStats skippedFindings;

    @JsonProperty("failed_policies")
    private ScanResultStats failedPolicies;

    @JsonProperty("skipped_policies")
    private ScanResultStats skippedPolicies;

    public String getFileOrFolder() {
        return fileOrFolder;
    }

    public void setFileOrFolder(String fileOrFolder) {
        this.fileOrFolder = fileOrFolder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getScannedAt() {
        return scannedAt;
    }

    public void setScannedAt(String scannedAt) {
        this.scannedAt = scannedAt;
    }

    public List<String> getIacType() {
        return iacType;
    }

    public void setIacType(List<String> iacType) {
        this.iacType = iacType;
    }

    public int getTotalPolicies() {
        return totalPolicies;
    }

    public void setTotalPolicies(int totalPolicies) {
        this.totalPolicies = totalPolicies;
    }

    public int getPassedFindings() {
        return passedFindings;
    }

    public void setPassedFindings(int passedFindings) {
        this.passedFindings = passedFindings;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public ScanResultStats getFailedFindings() {
        return failedFindings;
    }

    public void setFailedFindings(ScanResultStats failedFindings) {
        this.failedFindings = failedFindings;
    }

    public ScanResultStats getSkippedFindings() {
        return skippedFindings;
    }

    public void setSkippedFindings(ScanResultStats skippedFindings) {
        this.skippedFindings = skippedFindings;
    }

    public ScanResultStats getFailedPolicies() {
        return failedPolicies;
    }

    public void setFailedPolicies(ScanResultStats failedPolicies) {
        this.failedPolicies = failedPolicies;
    }

    public ScanResultStats getSkippedPolicies() {
        return skippedPolicies;
    }

    public void setSkippedPolicies(ScanResultStats skippedPolicies) {
        this.skippedPolicies = skippedPolicies;
    }

    @Override
    public String toString() {
        return "ScanSummary{" +
                "fileOrFolder='" + fileOrFolder + '\'' +
                ", status='" + status + '\'' +
                ", statusDescription='" + statusDescription + '\'' +
                ", scannedAt='" + scannedAt + '\'' +
                ", iacType=" + iacType +
                ", totalPolicies=" + totalPolicies +
                ", passedFindings=" + passedFindings +
                ", failedFindings=" + failedFindings +
                ", skippedFindings=" + skippedFindings +
                ", failedPolicies=" + failedPolicies +
                ", skippedPolicies=" + skippedPolicies +
                '}';
    }
}
