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
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genj.gedcom.UnitOfWork;
import java.awt.event.ActionEvent;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;

public class SearchAction extends AbstractAncestrisContextAction {
    private static final Logger LOG = Logger.getLogger(SearchAction.class.getName());
    private Entity entity;
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
        final Collection<? extends Property> props = this.lkpInfo.allInstances();
        if (!props.isEmpty()) {
            final Property prop = (Property) props.iterator().next();
            if (prop.getEntity() instanceof Indi) {
                this.entity = prop.getEntity();
            } else if (this.entity == null) {
                this.entity = prop.getGedcom().getFirstEntity("INDI");
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
        searchPanel.set(new DecesMatchId((Indi) this.entity));
        DialogManager.ADialog dialog = DialogManager.create(
                NbBundle.getMessage((Class) SearchAction.class, "SearchPanel.dialogManager.title"), searchPanel);
        if (dialog.show() == DialogManager.ADialog.OK_OPTION) {
            try {
                this.entity.getGedcom().doUnitOfWork(new UnitOfWork() {
                            @Override
                            public void perform(Gedcom gedcom) throws GedcomException {
                                final DecesMatchId decesMatchIdFound = searchPanel.getSelected();
                                if (decesMatchIdFound == null) {
                                    return;
                                }
                                final Indi indi = (Indi) entity;
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
                                final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
                                final String birthPlace = ", " + decesMatchIdFound.getBirthCity() + ", , "
                                    + decesMatchIdFound.getBirthDepartment() + ", , " + decesMatchIdFound.getBirthCountry();
                                indi.setValue(new TagPath("INDI:BIRT:DATE"), decesMatchIdFound.getBirthLocalDate().format(formatter));
                                indi.setValue(new TagPath("INDI:BIRT:PLAC"), birthPlace);
                                final String deathPlace = ", " + decesMatchIdFound.getDeathCity() + ", , "
                                    + decesMatchIdFound.getDeathDepartment() + ", , " + decesMatchIdFound.getDeathCountry();
                                indi.setValue(new TagPath("INDI:DEAT:DATE"), decesMatchIdFound.getDeathLocalDate().format(formatter));
                                indi.setValue(new TagPath("INDI:DEAT:PLAC"), deathPlace);
                                final String description = "DecesMatchId: certificate: " + decesMatchIdFound.getDeathCertificateId()
                                    + ", source: " + decesMatchIdFound.getSource() + ", source line: "
                                    + decesMatchIdFound.getSourceLine();
                                Property death = indi.getProperty(new TagPath("INDI:DEAT"));
                                if (death == null) {
                                    death = indi.setValue(new TagPath("INDI:DEAT"), "");
                                }
                                death.addSimpleProperty("SOUR", description, -1);
                            }
                        });
                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
                    }
        }
    }
}
