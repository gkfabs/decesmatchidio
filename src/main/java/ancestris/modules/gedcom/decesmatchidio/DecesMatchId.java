/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Fabien Carrion.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.decesmatchidio;

import genj.gedcom.Indi;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyPlace;
import genj.gedcom.time.PointInTime;
import java.io.Serializable;
import java.time.LocalDate;

import org.json.JSONArray;
import org.json.JSONObject;

public class DecesMatchId implements Serializable {

    private String firstName;
    private String lastName;
    private String legalName;
    private Sex sex;

    private String birthDate;
    private String birthCity;
    private String birthLocationCode;
    private String birthPostalCode;
    private String birthDepartment;
    private String birthCountry;
    private Double birthLatitude;
    private Double birthLongitude;

    private String deathDate;
    private String deathCertificateId;
    private String deathCity;
    private String deathLocationCode;
    private String deathPostalCode;
    private String deathDepartment;
    private String deathCountry;
    private Double deathLatitude;
    private Double deathLongitude;

    private String lastSeenAliveDate;
    private String source;
    private Integer sourceLine;
    private Boolean fuzzy;

    public DecesMatchId(final Indi indi) {
        if (indi.getFirstNames() != null) {
            this.firstName = String.join(", ", (CharSequence[]) indi.getFirstNames());
        }
        if (indi.getLastNames() != null) {
            this.lastName = String.join(", ", (CharSequence[]) indi.getLastNames());
        }
        switch (indi.getSex()) {
            case 1: {
                this.sex = Sex.M;
                break;
            }
            case 2: {
                this.sex = Sex.F;
                break;
            }
            default: {
                this.sex = Sex.H;
                break;
            }
        }
        if (indi.getBirthDate() != null) {
            final PropertyDate date = indi.getBirthDate();
            if (date.getStart() != null) {
                final PointInTime pit = date.getStart();
                final int year = pit.getYear();
                this.birthDate = "01/01/" + year + "-31/12/" + year;
            }
        }
        if (indi.getBirthPlaceOption() != null) {
            final PropertyPlace place = indi.getBirthPlaceOption();
            this.birthCity = place.getCity();
            this.birthLocationCode = place.getNumericalJurisdictions();
            this.birthCountry = place.getCountry();
        }
        if (indi.getDeathDate() != null) {
            final PropertyDate date = indi.getDeathDate();
            if (date.getStart() != null) {
                final PointInTime pit = date.getStart();
                final int year = pit.getYear();
                this.deathDate = "01/01/" + year + "-31/12/" + year;
            }
        }
        if (indi.getDeathPlaceOption() != null) {
            final PropertyPlace place = indi.getDeathPlaceOption();
            this.deathCity = place.getCity();
            this.deathLocationCode = place.getNumericalJurisdictions();
            this.deathCountry = place.getCountry();
        }
        this.fuzzy = true;
    }

    private String parseStringOrArrayOfString(JSONObject json, String field, boolean onlyLastString) {
        if (!json.has(field))
            return null;
        if (json.get(field) instanceof String)
            return json.getString(field);
        JSONArray jsonArray = json.getJSONArray(field);
        if (onlyLastString) {
            return jsonArray.getString(jsonArray.length() - 1);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < jsonArray.length(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(jsonArray.getString(i));
        }
        return sb.toString();
    }

    public DecesMatchId(final JSONObject person) {
        final JSONObject name = person.getJSONObject("name");
        this.firstName = parseStringOrArrayOfString(name, "first", false);
        this.lastName = parseStringOrArrayOfString(name, "last", false);
        this.legalName = parseStringOrArrayOfString(name, "legal", false);
        switch (person.getString("sex")) {
            case "M": {
                this.sex = Sex.M;
                break;
            }
            case "F": {
                this.sex = Sex.F;
                break;
            }
            default: {
                this.sex = Sex.H;
                break;
            }
        }
        final JSONObject birth = person.getJSONObject("birth");
        final String d1 = birth.getString("date");
        this.birthDate =  d1.substring(0, 4) + "-" + d1.substring(4, 6) + "-" + d1.substring(6, 8);
        final JSONObject birthLocation = birth.getJSONObject("location");
        this.birthCity = parseStringOrArrayOfString(birthLocation, "city", true);
        if (birthLocation.has("code")) {
            this.birthLocationCode = birthLocation.getString("code");
            this.birthPostalCode = birthLocation.getString("code");
        }
        if (birthLocation.has("departmentCode")) {
            this.birthDepartment = birthLocation.getString("departmentCode");
        }
        this.birthCountry = birthLocation.getString("country");
        if (birthLocation.has("latitude")) {
            this.birthLatitude = birthLocation.getDouble("latitude");
        }
        if (birthLocation.has("longitude")) {
            this.birthLongitude = birthLocation.getDouble("longitude");
        }
        final JSONObject death = person.getJSONObject("death");
        final String d2 = death.getString("date");
        this.deathDate = d2.substring(0, 4) + "-" + d2.substring(4, 6) + "-" + d2.substring(6, 8);
        this.deathCertificateId = death.getString("certificateId");
        JSONObject deathLocation = death.getJSONObject("location");
        this.deathCity = parseStringOrArrayOfString(deathLocation, "city", true);
        if (deathLocation.has("code")) {
            this.deathLocationCode = deathLocation.getString("code");
            this.deathPostalCode = deathLocation.getString("code");
        }
        if (deathLocation.has("departmentCode")) {
            this.deathDepartment = deathLocation.getString("departmentCode");
        }
        this.deathCountry = deathLocation.getString("country");
        if (deathLocation.has("latitude")) {
            this.deathLatitude = deathLocation.getDouble("latitude");
        }
        if (deathLocation.has("longitude")) {
            this.deathLongitude = deathLocation.getDouble("longitude");
        }
        this.source = person.getString("source");
        this.sourceLine = person.getInt("sourceLine");
        this.fuzzy = true;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getLegalName() {
        return this.legalName;
    }

    public void setLegalName(final String legalName) {
        this.legalName = legalName;
    }

    public Sex getSex() {
        return this.sex;
    }

    public void setSex(final Sex sex) {
        this.sex = sex;
    }

    public String getBirthDate() {
        return this.birthDate;
    }

    public LocalDate getBirthLocalDate() {
        LocalDate d = null;
        try {
            d = LocalDate.parse(this.birthDate);
        } catch (Exception e) {}
        return d;
    }

    public void setBirthDate(final String birthDate) {
        this.birthDate = birthDate;
    }

    public String getBirthCity() {
        return this.birthCity;
    }

    public void setBirthCity(final String birthCity) {
        this.birthCity = birthCity;
    }

    public String getBirthLocationCode() {
        return this.birthLocationCode;
    }

    public void setBirthLocationCode(final String birthLocationCode) {
        this.birthLocationCode = birthLocationCode;
    }

    public String getBirthPostalCode() {
        return this.birthPostalCode;
    }

    public void setBirthPostalCode(final String birthPostalCode) {
        this.birthPostalCode = birthPostalCode;
    }

    public String getBirthDepartment() {
        return this.birthDepartment;
    }

    public void setBirthDepartment(final String birthDepartment) {
        this.birthDepartment = birthDepartment;
    }

    public String getBirthCountry() {
        return this.birthCountry;
    }

    public void setBirthCountry(final String birthCountry) {
        this.birthCountry = birthCountry;
    }

    public Double getBirthLatitude() {
        return this.birthLatitude;
    }

    public void setBirthLatitude(final Double birthLatitude) {
        this.birthLatitude = birthLatitude;
    }

    public Double getBirthLongitude() {
        return this.birthLongitude;
    }

    public void setBirthLongitude(final Double birthLongitude) {
        this.birthLongitude = birthLongitude;
    }

    public String getDeathDate() {
        return this.deathDate;
    }

    public LocalDate getDeathLocalDate() {
        LocalDate d = null;
        try {
            d = LocalDate.parse(this.deathDate);
        } catch (Exception e) {}
        return d;
    }

    public void setDeathDate(final String deathDate) {
        this.deathDate = deathDate;
    }

    public String getDeathCertificateId() {
        return this.deathCertificateId;
    }

    public void setDeathCertificateId(final String deathCertificateId) {
        this.deathCertificateId = deathCertificateId;
    }

    public String getDeathCity() {
        return this.deathCity;
    }

    public void setDeathCity(final String deathCity) {
        this.deathCity = deathCity;
    }

    public String getDeathLocationCode() {
        return this.deathLocationCode;
    }

    public void setDeathLocationCode(final String deathLocationCode) {
        this.deathLocationCode = deathLocationCode;
    }

    public String getDeathPostalCode() {
        return this.deathPostalCode;
    }

    public void setDeathPostalCode(final String deathPostalCode) {
        this.deathPostalCode = deathPostalCode;
    }

    public String getDeathDepartment() {
        return this.deathDepartment;
    }

    public void setDeathDepartment(final String deathDepartment) {
        this.deathDepartment = deathDepartment;
    }

    public String getDeathCountry() {
        return this.deathCountry;
    }

    public void setDeathCountry(final String deathCountry) {
        this.deathCountry = deathCountry;
    }

    public Double getDeathLatitude() {
        return this.deathLatitude;
    }

    public void setDeathLatitude(final Double deathLatitude) {
        this.deathLatitude = deathLatitude;
    }

    public Double getDeathLongitude() {
        return this.deathLongitude;
    }

    public void setDeathLongitude(final Double deathLongitude) {
        this.deathLongitude = deathLongitude;
    }

    public String getLastSeenAliveDate() {
        return this.lastSeenAliveDate;
    }

    public void setLastSeenAliveDate(final String lastSeenAliveDate) {
        this.lastSeenAliveDate = lastSeenAliveDate;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public Integer getSourceLine() {
        return this.sourceLine;
    }

    public void setSourceLine(final Integer sourceLine) {
        this.sourceLine = sourceLine;
    }

    public Boolean isFuzzy() {
        return this.fuzzy;
    }

    public void setFuzzy(final Boolean fuzzy) {
        this.fuzzy = fuzzy;
    }

    public String toString() {
        return "DecesMatchId(" + firstName + ", " + lastName + ")";
    }
}
