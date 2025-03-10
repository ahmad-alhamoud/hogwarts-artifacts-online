package com.ahmad.hogwartsartifactsonline.wizard;

import com.ahmad.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class WizardService {

    private final WizardRepository wizardRepository;

    public WizardService(WizardRepository wizardRepository) {
        this.wizardRepository = wizardRepository;
    }

    public List<Wizard> findall() {
        return wizardRepository.findAll();
    }


    public Wizard add(Wizard wizard) {
        return wizardRepository.save(wizard);
    }

    public Wizard findById(Integer wizardId) {
        return wizardRepository.findById(wizardId)
                .orElseThrow(() -> new ObjectNotFoundException("wizard",wizardId));
    }

    public Wizard update(Integer wizardId, Wizard update) {
        Wizard wizard = wizardRepository.findById(wizardId)
                .orElseThrow(() -> new ObjectNotFoundException("wizard",wizardId));

        wizard.setName(update.getName());

        return wizardRepository.save(wizard);
    }

    public void deleteWizardById(Integer wizardId) {
       Wizard wizardToBeDeleted =  wizardRepository.findById(wizardId).orElseThrow(() -> new ObjectNotFoundException("wizard",wizardId));;

       wizardToBeDeleted.removeAllArtifacts();

        wizardRepository.deleteById(wizardId);
    }

}
