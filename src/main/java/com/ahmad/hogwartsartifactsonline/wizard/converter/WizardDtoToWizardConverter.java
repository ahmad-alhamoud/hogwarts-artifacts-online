package com.ahmad.hogwartsartifactsonline.wizard.converter;

import com.ahmad.hogwartsartifactsonline.wizard.Wizard;
import com.ahmad.hogwartsartifactsonline.wizard.dto.WizardDto;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WizardDtoToWizardConverter implements Converter<WizardDto, Wizard> {

    @Override
    public Wizard convert(WizardDto source) {
        Wizard wizard =  new Wizard();
        wizard.setId(wizard.getId());
        wizard.setName(source.name());

        return wizard;
    }
}
