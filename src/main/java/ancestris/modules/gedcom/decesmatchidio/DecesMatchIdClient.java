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
        final HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl))
                .header("Content-Type", "application/json").header("Accept", "application/json")
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
            return response;
        }

        JSONObject jsonRoot = new JSONObject(json);
        JSONArray persons = jsonRoot.getJSONObject("response").getJSONArray("persons");
        for (Object person : persons) {
            try {
                DecesMatchId decesMatchId = new DecesMatchId((JSONObject) person);
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
                DialogManager.createError(title, msg + " " + ex.toString() + " " + ((JSONObject) person).toString()).show();
            }
        }

        return response;
    }
}
