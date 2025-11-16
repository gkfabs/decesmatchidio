/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2014 Ancestris
 * 
 * Author: Fabien Carrion
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.decesmatchidio;

import genj.util.AncestrisPreferences;
import genj.util.Registry;

/**
 *
 * @author fabien
 */
public class DecesMatchIdOptions {

    // XXX: preference path must be defined in core options namespace
    private static AncestrisPreferences decesMatchIdOptions;

    private DecesMatchIdOptions() {
        decesMatchIdOptions = Registry.get(DecesMatchIdOptions.class);
    }

    public static DecesMatchIdOptions getInstance() {
        return OptionsHolder.INSTANCE;
    }

    private static class OptionsHolder {

        private static final DecesMatchIdOptions INSTANCE = new DecesMatchIdOptions();
    }

    public String getApiKey() {
        return decesMatchIdOptions.get("apikey", "");
    }

    public void setApiKey(String apiKey) {
        decesMatchIdOptions.put("apikey", apiKey);
    }

}
