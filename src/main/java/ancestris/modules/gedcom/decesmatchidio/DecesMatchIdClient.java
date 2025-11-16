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

import ancestris.util.swing.DialogManager;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public class DecesMatchIdClient {
    private static final Logger LOG = Logger.getLogger(DecesMatchIdClient.class.getName());

    public static List<DecesMatchId> lookup(DecesMatchId decesMatchIdSearch) {

        final String apiUrl = "https://deces.matchid.io/deces/api/v1/search";
        List<DecesMatchId> response = new ArrayList<DecesMatchId>();

        final JSONObject requestBody = new JSONObject();
        requestBody.put("firstName", decesMatchIdSearch.getFirstName());
        requestBody.put("lastName", decesMatchIdSearch.getLastName());
        requestBody.put("legalName", decesMatchIdSearch.getLegalName());
        requestBody.put("sex", decesMatchIdSearch.getSex());
        requestBody.put("birthDate", decesMatchIdSearch.getBirthDate());
        requestBody.put("birthCity", decesMatchIdSearch.getBirthCity());
        requestBody.put("birthLocationCode", decesMatchIdSearch.getBirthLocationCode());
        requestBody.put("birthPostalCode", decesMatchIdSearch.getBirthPostalCode());
        requestBody.put("birthDepartment", decesMatchIdSearch.getBirthDepartment());
        requestBody.put("birthCountry", decesMatchIdSearch.getBirthCountry());
        requestBody.put("deathDate", decesMatchIdSearch.getDeathDate());
        requestBody.put("deathCity", decesMatchIdSearch.getDeathCity());
        requestBody.put("deathLocationCode", decesMatchIdSearch.getDeathLocationCode());
        requestBody.put("deathPostalCode", decesMatchIdSearch.getDeathPostalCode());
        requestBody.put("deathDepartment", decesMatchIdSearch.getDeathDepartment());
        requestBody.put("deathCountry", decesMatchIdSearch.getDeathCountry());
        requestBody.put("lastSeenAliveDate", decesMatchIdSearch.getLastSeenAliveDate());
        requestBody.put("source", decesMatchIdSearch.getSource());
        requestBody.put("fuzzy", decesMatchIdSearch.isFuzzy());

        String json = "aborted";
        final HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(apiUrl))
            .header("Content-Type", "application/json").header("Accept", "application/json");
        final String apiKey = DecesMatchIdOptions.getInstance().getApiKey();
        if (apiKey != null && apiKey.length() > 0) {
            requestBuilder.header("Authorization", "Bearer " + apiKey);
        }
        final HttpRequest request = requestBuilder
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString())).build();
        DecesMatchIdClient.LOG.log(Level.INFO,
                NbBundle.getMessage((Class) DecesMatchIdClient.class, "DecesMatchIdClient.extractService.msg.calling"));

        try {
            final HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            json = httpResponse.body();
        } catch (final IOException | InterruptedException ex) {
            DecesMatchIdClient.LOG.log(Level.SEVERE, NbBundle.getMessage((Class) DecesMatchIdClient.class,
                    "DecesMatchIdClient.extractService.msg.malfunction"));
            Exceptions.printStackTrace((Throwable) ex);
            DecesMatchIdClient.LOG.log(Level.SEVERE, json);

            final String title = NbBundle.getMessage((Class) DecesMatchIdClient.class,
                    "DecesMatchIdClient.extractService.title");
            final String msg = NbBundle.getMessage((Class) DecesMatchIdClient.class,
                    "DecesMatchIdClient.extractService.msg.malfunction");
            DialogManager.createError(title, msg).show();
        }

        if (json == null || json.equals("aborted")) {
            DecesMatchIdClient.LOG.log(Level.INFO, NbBundle.getMessage((Class) DecesMatchIdClient.class,
                    "DecesMatchIdClient.extractService.msg.aborted"));
            final String title = NbBundle.getMessage((Class) DecesMatchIdClient.class,
                    "DecesMatchIdClient.extractService.title");
            final String msg = NbBundle.getMessage((Class) DecesMatchIdClient.class,
                    "DecesMatchIdClient.extractService.msg.malfunction");
            DialogManager.createError(title, msg).show();
            return response;
        }

        JSONObject jsonRoot = new JSONObject(json);
        JSONArray persons = null;
        try {
            persons = jsonRoot.getJSONObject("response").getJSONArray("persons");
        } catch (final JSONException ex) {
            DecesMatchIdClient.LOG.log(Level.INFO, NbBundle.getMessage((Class) DecesMatchIdClient.class,
                    "DecesMatchIdClient.extractService.msg.aborted"));
            final String title = NbBundle.getMessage((Class) DecesMatchIdClient.class,
                    "DecesMatchIdClient.extractService.title");
            final String msg = NbBundle.getMessage((Class) DecesMatchIdClient.class,
                    "DecesMatchIdClient.extractService.msg.malfunction");
            DialogManager.createError(title, msg + " " + ex.toString() + " " + ((JSONObject) jsonRoot).toString())
                    .show();
            return response;
        }
        for (Object person : persons) {
            try {
                DecesMatchId decesMatchId = internalize((JSONObject) person);
                response.add(decesMatchId);
            } catch (final JSONException ex) {
                DecesMatchIdClient.LOG.log(Level.SEVERE, NbBundle.getMessage((Class) DecesMatchIdClient.class,
                        "DecesMatchIdClient.extractService.msg.malfunction"));
                Exceptions.printStackTrace((Throwable) ex);
                DecesMatchIdClient.LOG.log(Level.SEVERE, json);

                final String title = NbBundle.getMessage((Class) DecesMatchIdClient.class,
                        "DecesMatchIdClient.extractService.title");
                final String msg = NbBundle.getMessage((Class) DecesMatchIdClient.class,
                        "DecesMatchIdClient.extractService.msg.malfunction");
                DialogManager.createError(title, msg + " " + ex.toString() + " " + ((JSONObject) person).toString())
                        .show();
            }
        }

        return response;
    }

    private static String parseStringOrArrayOfString(final JSONObject json, final String field,
            final boolean onlyLastString) {
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

    private static DecesMatchId internalize(final JSONObject person) {
        DecesMatchId decesMatchId = new DecesMatchId();
        final JSONObject name = person.getJSONObject("name");
        decesMatchId.setFirstName(parseStringOrArrayOfString(name, "first", false));
        decesMatchId.setLastName(parseStringOrArrayOfString(name, "last", false));
        decesMatchId.setLegalName(parseStringOrArrayOfString(name, "legal", false));
        switch (person.getString("sex")) {
            case "M": {
                decesMatchId.setSex(Sex.M);
                break;
            }
            case "F": {
                decesMatchId.setSex(Sex.F);
                break;
            }
            default: {
                decesMatchId.setSex(Sex.H);
                break;
            }
        }
        final JSONObject birth = person.getJSONObject("birth");
        final String d1 = birth.getString("date");
        decesMatchId.setBirthDate(d1.substring(0, 4) + "-" + d1.substring(4, 6) + "-" + d1.substring(6, 8));
        final JSONObject birthLocation = birth.getJSONObject("location");
        decesMatchId.setBirthCity(parseStringOrArrayOfString(birthLocation, "city", true));
        if (birthLocation.has("code")) {
            decesMatchId.setBirthLocationCode(birthLocation.getString("code"));
            decesMatchId.setBirthPostalCode(birthLocation.getString("code"));
        }
        if (birthLocation.has("departmentCode")) {
            decesMatchId.setBirthDepartment(birthLocation.getString("departmentCode"));
        }
        decesMatchId.setBirthCountry(birthLocation.getString("country"));
        if (birthLocation.has("latitude")) {
            decesMatchId.setBirthLatitude(birthLocation.getDouble("latitude"));
        }
        if (birthLocation.has("longitude")) {
            decesMatchId.setBirthLongitude(birthLocation.getDouble("longitude"));
        }
        final JSONObject death = person.getJSONObject("death");
        final String d2 = death.getString("date");
        decesMatchId.setDeathDate(d2.substring(0, 4) + "-" + d2.substring(4, 6) + "-" + d2.substring(6, 8));
        decesMatchId.setDeathCertificateId(death.getString("certificateId"));
        JSONObject deathLocation = death.getJSONObject("location");
        decesMatchId.setDeathCity(parseStringOrArrayOfString(deathLocation, "city", true));
        if (deathLocation.has("code")) {
            decesMatchId.setDeathLocationCode(deathLocation.getString("code"));
            decesMatchId.setDeathPostalCode(deathLocation.getString("code"));
        }
        if (deathLocation.has("departmentCode")) {
            decesMatchId.setDeathDepartment(deathLocation.getString("departmentCode"));
        }
        decesMatchId.setDeathCountry(deathLocation.getString("country"));
        if (deathLocation.has("latitude")) {
            decesMatchId.setDeathLatitude(deathLocation.getDouble("latitude"));
        }
        if (deathLocation.has("longitude")) {
            decesMatchId.setDeathLongitude(deathLocation.getDouble("longitude"));
        }
        decesMatchId.setSource(person.getString("source"));
        decesMatchId.setSourceLine(person.getInt("sourceLine"));
        return decesMatchId;
    }

}
