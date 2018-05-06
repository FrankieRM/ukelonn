/*
 * Copyright 2016-2018 Steinar Bang
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

import com.vaadin.data.ValidationResult;
import com.vaadin.data.ValueContext;
import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.ui.PasswordField;

public class PasswordCompareValidator extends AbstractValidator<String> {
    private static final long serialVersionUID = 2610490969282733208L;
    PasswordField otherPassword;
    private String errorMessage;

    public PasswordCompareValidator(String errorMessage, PasswordField otherPassword) {
        super(errorMessage);
        this.errorMessage = errorMessage;
        this.otherPassword = otherPassword;
    }

    @Override
    public ValidationResult apply(String value, ValueContext context) {
        String otherPasswordValue = otherPassword.getValue();
        if (!otherPasswordValue.equals(value)){
            return ValidationResult.error(errorMessage);
        }

        return ValidationResult.ok();
    }

}
