/*
 * Copyright 2016-2017 Steinar Bang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations
 * under the License.
 */
package no.priv.bang.ukelonn.impl;

import static no.priv.bang.ukelonn.impl.CommonDatabaseMethods.*;

import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button.ClickEvent;

public class UserFallbackView extends AbstractView {
    private static final long serialVersionUID = 1388525490129647161L;
    private Account account;

    // Datamodel for the UI (updates to these will be transferred to the GUI listeners).
    private ObjectProperty<String> greetingProperty = new ObjectProperty<String>("Ukelønn for ????");;
    ObjectProperty<Double> balance = new ObjectProperty<Double>(0.0);
    BeanItemContainer<TransactionType> paymentTypesContainer = new BeanItemContainer<TransactionType>(TransactionType.class);
    ObjectProperty<Double> newJobAmount = new ObjectProperty<Double>(0.0);
    BeanItemContainer<Transaction> recentJobs = new BeanItemContainer<Transaction>(Transaction.class);
    BeanItemContainer<Transaction> recentPayments = new BeanItemContainer<Transaction>(Transaction.class);

    public UserFallbackView(VaadinRequest request) {
        // Display the greeting
        VerticalLayout content = new VerticalLayout();
        Component greeting = new Label(greetingProperty);
        greeting.setStyleName("h1");
        content.addComponent(greeting);

        FormLayout balanceLayout = new FormLayout();
        // Display the current balance
        TextField balanceDisplay = new TextField("Til gode:");
        balanceDisplay.setPropertyDataSource(balance);
        balanceDisplay.addStyleName("inline-label");
        balanceLayout.addComponent(balanceDisplay);
        content.addComponent(balanceLayout);

        Accordion accordion = new Accordion();

        FormLayout balanceAndNewJobTab = new FormLayout();
        Map<Integer, TransactionType> transactionTypes = getTransactionTypesFromUkelonnDatabase(getClass());
        List<TransactionType> paymentTypes = getJobTypesFromTransactionTypes(transactionTypes.values());
        paymentTypesContainer.addAll(paymentTypes);
        ComboBox jobtypeSelector = new ComboBox("Velg jobb", paymentTypesContainer);
        jobtypeSelector.setItemCaptionMode(ItemCaptionMode.PROPERTY);
        jobtypeSelector.setItemCaptionPropertyId("transactionTypeName");
        jobtypeSelector.setNullSelectionAllowed(true);
        balanceAndNewJobTab.addComponent(jobtypeSelector);
        TextField newAmountDisplay = new TextField(newJobAmount);
        newAmountDisplay.setReadOnly(true);
        balanceAndNewJobTab.addComponent(newAmountDisplay);
        jobtypeSelector.addValueChangeListener(new ValueChangeListener() {
                private static final long serialVersionUID = 3145027593224884343L;
                @Override
                public void valueChange(ValueChangeEvent event) {
                    if (jobtypeSelector.getValue() == null) {
                        newJobAmount.setValue(0.0);
                    } else {
                        newJobAmount.setValue(((TransactionType) jobtypeSelector.getValue()).getTransactionAmount());
                    }
                }
            });

        // Updatable containers
        recentJobs.addAll(getJobsFromAccount(account, getClass()));
        Table lastJobsTable = createTransactionTable("Jobbtype", recentJobs, true);
        lastJobsTable.setImmediate(true);
        recentPayments.addAll(getPaymentsFromAccount(account, getClass()));
        Class<?> classForLogMessage = getClass();

        // Have a clickable button
        balanceAndNewJobTab.addComponent(new Button("Registrer jobb",
                                                    new Button.ClickListener() {
                                                        private static final long serialVersionUID = 2723190031041985566L;

                                                        @Override
                                                        public void buttonClick(ClickEvent e) {
                                                            TransactionType jobType = (TransactionType) jobtypeSelector.getValue();
                                                            if (jobType != null) {
                                                                registerNewJobInDatabase(classForLogMessage, account, jobType.getId(), jobType.getTransactionAmount());
                                                                balance.setValue(account.getBalance());
                                                                jobtypeSelector.setValue(null);
                                                                recentJobs.removeAllItems();
                                                                recentJobs.addAll(getJobsFromAccount(account, classForLogMessage));
                                                            }
                                                        }
                                                    }));
        accordion.addTab(balanceAndNewJobTab, "Registrere jobb");

        VerticalLayout lastJobsTab = new VerticalLayout();
        lastJobsTab.addComponent(lastJobsTable);
        accordion.addTab(lastJobsTab, "Siste jobber");

        VerticalLayout lastPaymentsTab = new VerticalLayout();
        Table lastPaymentsTable = createTransactionTable("Type utbetaling", recentPayments, false);
        lastPaymentsTab.addComponent(lastPaymentsTable);
        accordion.addTab(lastPaymentsTab, "Siste utbetalinger");

        content.addComponent(accordion);

        HorizontalLayout links = createLinksToBrowserVersionAndLogout(request, "mobile", "Mobilversjon");
        content.addComponent(links);

        addComponent(content);
    }

    @Override
    public void enter(ViewChangeEvent event) {
        String currentUser = (String) SecurityUtils.getSubject().getPrincipal();
        account = getAccountInfoFromDatabase(getClass(), currentUser);

        greetingProperty.setValue("Ukelønn for " + account.getFirstName());
        balance.setValue(account.getBalance());
        recentJobs.removeAllItems();
        recentJobs.addAll(getJobsFromAccount(account, getClass()));
        recentPayments.removeAllItems();
        recentPayments.addAll(getPaymentsFromAccount(account, getClass()));
    }
}