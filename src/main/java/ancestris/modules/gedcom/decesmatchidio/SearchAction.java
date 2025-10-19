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

import ancestris.core.actions.AbstractAncestrisContextAction;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Entity;
import genj.gedcom.Indi;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import java.awt.event.ActionEvent;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Logger;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;

public class SearchAction extends AbstractAncestrisContextAction {
    private static final Logger LOG = Logger.getLogger(SearchAction.class.getName());
    private Entity entity;
    private DecesMatchId decesMatchId;
    private SearchPanel searchPanel = null;

    public SearchAction() {
        this.setIconBase("ancestris/modules/gedcom/decesmatchidio/search.png");
        this.setText(NbBundle.getMessage((Class) SearchAction.class, "CTL_SearchAction"));
        this.setTip(NbBundle.getMessage((Class) SearchAction.class, "CTL_SearchAction.tip"));
    }

    protected final void contextChanged() {
        this.setEnabled(this.entity != null);
    }

    public void resultChanged(final LookupEvent ev) {
        this.entity = null;
        this.decesMatchId = null;
        final Collection<? extends Property> props = this.lkpInfo.allInstances();
        if (!props.isEmpty()) {
            final Property prop = (Property) props.iterator().next();
            if (prop.getEntity() instanceof Indi) {
                this.entity = prop.getEntity();
            } else if (this.entity == null) {
                this.entity = prop.getGedcom().getFirstEntity("INDI");
            }
            this.decesMatchId = new DecesMatchId((Indi) this.entity);
            if (searchPanel != null) {
                searchPanel.set(this.decesMatchId);
            }
        }
        super.resultChanged(ev);
    }

    protected void actionPerformedImpl(final ActionEvent event) {
        if (this.entity == null || this.entity.getGedcom() == null) {
            return;
        }

        if (searchPanel == null) {
            searchPanel = new SearchPanel();
        }
        searchPanel.set(this.decesMatchId);
        DialogManager.ADialog dialog = DialogManager.create(
                NbBundle.getMessage((Class) SearchAction.class, "SearchPanel.dialogManager.title"), searchPanel);
        if (dialog.show() == DialogManager.ADialog.OK_OPTION) {
            DecesMatchId decesMatchIdFound = searchPanel.getSelected();
            if (decesMatchIdFound == null) {
                return;
            }
            Indi indi = (Indi) this.entity;
            indi.setName(decesMatchIdFound.getFirstName(), decesMatchIdFound.getLastName());
            switch (decesMatchIdFound.getSex().toString()) {
            case "M": {
                    indi.setSex(1);
                    break;
            }
            case "F": {
                    indi.setSex(2);
                    break;
            }
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            Property birth = indi.getProperty(new TagPath("INDI:BIRT"));
            if (birth == null) {
                birth = indi.addProperty("INDI:DEAT", "");
            }
            String birthDate = decesMatchIdFound.getBirthLocalDate().format(formatter);
            String birthPlace = ", " + decesMatchIdFound.getBirthCity() + ", , " + decesMatchIdFound.getBirthDepartment() + ", , " + decesMatchIdFound.getBirthCountry();
            if (birth.getProperty("DATE") != null) {
                birth.getProperty("DATE").setValue(birthDate);
            } else {
                birth.addProperty("DATE", birthDate);
            }
            if (birth.getProperty("PLAC") != null) {
                birth.getProperty("PLAC").setValue(birthPlace);
            } else {
                birth.addProperty("PLAC", birthPlace);
            }
            Property death = indi.getProperty(new TagPath("INDI:DEAT"));
            if (death == null) {
                death = indi.addProperty("INDI:DEAT", "");
            }
            String deathDate = decesMatchIdFound.getDeathLocalDate().format(formatter);
            String deathPlace = ", " + decesMatchIdFound.getDeathCity() + ", , " + decesMatchIdFound.getDeathDepartment() + ", , " + decesMatchIdFound.getDeathCountry();
            if (death.getProperty("DATE") != null) {
                death.getProperty("DATE").setValue(deathDate);
            } else {
                death.addProperty("DATE", deathDate);
            }
            if (death.getProperty("PLAC") != null) {
                death.getProperty("PLAC").setValue(deathPlace);
            } else {
                death.addProperty("PLAC", deathPlace);
            }
            final String note = "Certificate Id: " + decesMatchIdFound.getDeathCertificateId() + "\nSource: " + decesMatchIdFound.getSource() + "\nSource Line: " + decesMatchIdFound.getSourceLine();
            if (death.getProperty("NOTE") != null) {
                Property currentNote = death.getProperty("NOTE");
                currentNote.setValue(currentNote.getValue() + "\n" + note);
            } else {
                death.addProperty("NOTE", note);
            }
        }
    }
}
