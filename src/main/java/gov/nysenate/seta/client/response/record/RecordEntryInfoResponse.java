package gov.nysenate.seta.client.response.record;

import gov.nysenate.seta.client.view.AccrualsView;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "RecordEntryInfo")
public class RecordEntryInfoResponse
{
    @XmlElement
    protected boolean status = false;

    @XmlElement
    protected AccrualsView accruals;

    public RecordEntryInfoResponse() {
        this.accruals = new AccrualsView();
    }

    public boolean isStatus() {
        return status;
    }

    public AccrualsView getAccruals() {
        return accruals;
    }
}