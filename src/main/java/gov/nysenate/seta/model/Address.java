package gov.nysenate.seta.model;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Basic model for a street address
 */
public class Address
{
    protected String addr1 = "";
    protected String addr2 = "";
    protected String city = "";
    protected String state = "";
    protected String zip5 = "";
    protected String zip4 = "";

    public Address() {}

    public Address(String addr1) {
        this(addr1, "","","","","");
    }

    public Address(String addr1, String city, String state, String postal) {
        this.setAddr1(addr1);
        this.setCity(city);
        this.setState(state);
        this.setPostal(postal);
    }

    public Address(String addr1, String addr2, String city, String state, String zip5, String zip4) {
        this.setAddr1(addr1);
        this.setAddr2(addr2);
        this.setCity(city);
        this.setState(state);
        this.setZip5(zip5);
        this.setZip4(zip4);
    }

    public boolean isParsed() {
        return !(addr2.trim().isEmpty() && city.trim().isEmpty() &&
                state.trim().isEmpty() && zip5.trim().isEmpty());
    }

    public boolean isEmpty() {
        return (addr1.trim().isEmpty() && !isParsed());
    }

    @Override
    public String toString() {
        if (isParsed()) {
            return ((!addr1.equals("") ? addr1 : "") + (!addr2.equals("") ? " " + addr2 + "" : "")
                    + (!addr1.equals("") || !addr2.equals("") ? "," : "")
                    + (!city.equals("") ? " " + city + "," : "") + ( !state.equals("") ? " " + state : "")
                    + (!zip5.equals("") ? " " + zip5 : "") + ( !zip4.equals("")  ? "-"+zip4 : "")).trim();
        }
        else {
            return addr1;
        }
    }

    /**
     * Normalization applied:
     * - Remove the dash within the building number
     * @return String
     */
    public String toNormalizedString() {
        return toString().replaceFirst("^(\\d+)(-)(\\d+)","$1$3");
    }

    public String getAddr1() {
        return addr1;
    }

    public void setAddr1(String addr1) {
        if (addr1 != null){
            this.addr1 = addr1;
        }
    }

    public String getAddr2() {
        return addr2;
    }

    public void setAddr2(String addr2) {
        if (addr2 != null){
            this.addr2 = addr2;
        }
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        if (city != null) {
            this.city = city;
        }
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        if (state != null){
            this.state = state;
        }
    }

    public String getZip5() {
        return this.zip5;
    }

    public void setZip5(String zip5) {
        if (zip5 != null && !zip5.isEmpty() && !zip5.equalsIgnoreCase("null")){
            this.zip5 = StringUtils.leftPad(zip5, 5, "0");
        }
    }

    public String getZip4() {
        return this.zip4;
    }

    public void setZip4(String zip4) {
        if (zip4 != null && !zip4.isEmpty() && !zip5.equalsIgnoreCase("null")){
            this.zip4 = StringUtils.leftPad(zip4, 4, "0");
        }
    }

    /** Stores 12345-1234 style postal codes into zip5 and zip4 parts */
    public void setPostal(String postal) {
        if (postal != null) {
            ArrayList<String> zipParts = new ArrayList<>(Arrays.asList(postal.split("-")));
            this.setZip5((zipParts.size() > 0) ? zipParts.get(0).trim() : "");
            this.setZip4((zipParts.size() > 1) ? zipParts.get(1).trim() : "");
        }
    }
}