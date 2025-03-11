package com.ahmad.hogwartsartifactsonline.wizard;


import com.ahmad.hogwartsartifactsonline.artifact.Artifact;
import com.ahmad.hogwartsartifactsonline.artifact.ArtifactRepository;
import com.ahmad.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class WizardServiceTest {

    @Mock
    WizardRepository wizardRepository;

    @Mock
    ArtifactRepository artifactRepository;

    @InjectMocks
    WizardService wizardService;

    List<Wizard> wizards;

    @BeforeEach
    void setUp() {

        wizards = new ArrayList<>();

        Wizard wizard1 = new Wizard();
        wizard1.setId(1);
        wizard1.setName("Gandalf the Grey");
        wizards.add(wizard1);

        Wizard wizard2 = new Wizard();
        wizard2.setId(2);
        wizard2.setName("Merlin Ambrosius");
        wizards.add(wizard2);

        Wizard wizard3 = new Wizard();
        wizard3.setId(3);
        wizard3.setName("Albus Dumbledore");
        wizards.add(wizard3);

        Wizard wizard4 = new Wizard();
        wizard4.setId(4);
        wizard4.setName("Saruman the White");
        wizards.add(wizard4);

        Wizard wizard5 = new Wizard();
        wizard5.setId(5);
        wizard5.setName("Morgana Le Fay");
        wizards.add(wizard5);
    }

    @Test
    void testFindAllSuccess() {

        given(wizardRepository.findAll()).willReturn(wizards);

        List<Wizard> wizardList = wizardService.findall();

        assertThat(wizardList.size()).isEqualTo(wizards.size());


        verify(wizardRepository, times(1)).findAll();

    }

    @Test
    void testFindByIdSuccess() {
        Wizard wizard = new Wizard();
        wizard.setId(1);
        wizard.setName("Gandalf the Grey");

        given(wizardRepository.findById(1)).willReturn(Optional.of(wizard));

        Wizard foundWizard = wizardService.findById(1);

        assertThat(foundWizard.getId()).isEqualTo(wizard.getId());
        assertThat(foundWizard.getName()).isEqualTo(wizard.getName());

        verify(wizardRepository, times(1)).findById(1);
    }


    @Test
    void testFindByIdNotFound() {

        given(wizardRepository.findById(Mockito.any(Integer.class))).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> {
            wizardService.findById(1);
        });

        assertThat(thrown).isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find wizard With Id 1 :(");

        verify(wizardRepository, times(1)).findById(1);

    }

    @Test
    void testDeleteByIdSuccess() {

        Wizard wizard = new Wizard();
        wizard.setId(1);
        wizard.setName("Gandalf the Grey");

        given(wizardRepository.findById(1)).willReturn(Optional.of(wizard));
        doNothing().when(wizardRepository).deleteById(1);

        wizardService.deleteWizardById(1);


        verify(wizardRepository, times(1)).findById(1);
        verify(wizardRepository, times(1)).deleteById(1);

    }

    @Test
    void testDeleteNotFound() {

        given(wizardRepository.findById(2)).willReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> wizardService.deleteWizardById(2));

        verify(wizardRepository, times(1)).findById(2);
    }

    @Test
    void testUpdateSuccess() {

        Wizard oldWizard = new Wizard();
        oldWizard.setId(10);
        oldWizard.setName("Harry Dresden");

        Wizard updatedWizard = new Wizard();
        updatedWizard.setId(10);
        updatedWizard.setName("Harry");

        given(wizardRepository.findById(10)).willReturn(Optional.of(oldWizard));
        given(wizardRepository.save(oldWizard)).willReturn(oldWizard);


        Wizard savedWizard = wizardService.update(10, updatedWizard);

        assertThat(savedWizard.getId()).isEqualTo(updatedWizard.getId());
        assertThat(savedWizard.getName()).isEqualTo(updatedWizard.getName());

        verify(wizardRepository, times(1)).findById(10);
        verify(wizardRepository, times(1)).save(oldWizard);


    }

    @Test
    void testUpdateNotFound() {

        Wizard updatedWizard = new Wizard();
        updatedWizard.setId(10);
        updatedWizard.setName("Harry");

        given(wizardRepository.findById(Mockito.any(Integer.class))).willReturn(Optional.empty());


        assertThrows(ObjectNotFoundException.class, () -> wizardService.update(1, updatedWizard));


        verify(wizardRepository, times(1)).findById(1);
    }


    @Test
    void testSaveSuccess() {

        Wizard wizard = new Wizard();
        wizard.setName("Gandalf the Grey");

        given(wizardRepository.save(wizard)).willReturn(wizard);

        Wizard newWizard = wizardService.add(wizard);

        assertThat(newWizard.getName()).isEqualTo(wizard.getName());

        verify(wizardRepository, times(1)).save(wizard);

    }

    @Test
    void testAssignArtifactSuccess() {

        Artifact a = new Artifact();
        a.setId("1250808601744904192");
        a.setName("Invisibility Cloak");
        a.setDescription("An invisibility cloak is used to make the wearer invisible.");
        a.setImageUrl("ImageUrl");

        Wizard w2 = new Wizard();
        w2.setId(2);
        w2.setName("Harry Potter");
        w2.addArtifact(a);

        Wizard w3 = new Wizard();
        w3.setId(3);
        w3.setName("Neville Longbottom");

        given(artifactRepository.findById("1250808601744904192")).willReturn(Optional.of(a));
        given(wizardRepository.findById(3)).willReturn(Optional.of(w3));

        wizardService.assignArtifact(3, "1250808601744904192");

        assertThat(a.getOwner().getId()).isEqualTo(3);
        assertThat(w3.getArtifacts().contains(a));
    }


    @Test
    void testAssignArtifactErrorWithNonExistentWizardId() {

        Artifact a = new Artifact();
        a.setId("1250808601744904192");
        a.setName("Invisibility Cloak");
        a.setDescription("An invisibility cloak is used to make the wearer invisible.");
        a.setImageUrl("ImageUrl");

        Wizard w2 = new Wizard();
        w2.setId(2);
        w2.setName("Harry Potter");
        w2.addArtifact(a);


        given(artifactRepository.findById("1250808601744904192")).willReturn(Optional.of(a));
        given(wizardRepository.findById(3)).willReturn(Optional.empty());

        Throwable thrown = assertThrows(ObjectNotFoundException.class, () -> {
            wizardService.assignArtifact(3, "1250808601744904192");
        });

        assertThat(thrown)
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find wizard With Id 3 :(");

        assertThat(a.getOwner().getId()).isEqualTo(2);
    }

    @Test
    void testAssignArtifactErrorWithNonExistentArtifactId() {


        given(artifactRepository.findById(Mockito.any(String.class))).willReturn(Optional.empty());

        Throwable thrown = assertThrows(ObjectNotFoundException.class, () -> {
            wizardService.assignArtifact(3, "123");
        });

        assertThat(thrown)
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Could not find artifact With Id 123 :(");

    }

}