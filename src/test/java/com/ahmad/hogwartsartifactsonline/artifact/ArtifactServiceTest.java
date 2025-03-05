package com.ahmad.hogwartsartifactsonline.artifact;

import com.ahmad.hogwartsartifactsonline.artifact.utils.IdWorker;
import com.ahmad.hogwartsartifactsonline.wizard.Wizard;
import org.junit.jupiter.api.AfterEach;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ArtifactServiceTest {

    @Mock
    ArtifactRepository artifactRepository;

    @Mock
    IdWorker idWorker;

    @InjectMocks
    ArtifactService artifactService;

    List<Artifact> artifacts;

    @BeforeEach
    void setUp() {
        Artifact a1 = new Artifact();
        a1.setId("1250808601744904191");
        a1.setName("Deluminator");
        a1.setDescription("A Deluminator is a device invented by Albus Dumbledore that resembles a cigarette lighter. It is used to remove or absorb (as well as return) the light from any light source to provide cover to the user.");
        a1.setImageUrl("imageUrl");

        Artifact a2 = new Artifact();
        a2.setId("1250808601744904192");
        a2.setName("Invisibility Cloak");
        a2.setDescription("An invisibility cloak is used to make the wearer invisible.");
        a2.setImageUrl("imageUrl");

        this.artifacts = new ArrayList<>();
        this.artifacts.add(a1);
        this.artifacts.add(a2);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFindByIdSuccess() {

        Artifact a = new Artifact();
        a.setId("12345");
        a.setName("Invisibility Cloak");
        a.setDescription("des");
        a.setImageUrl("ImageUrl");

        Wizard w = new Wizard();

        w.setId(2);
        w.setName("Harry Potter");
        a.setOwner(w);

        given(artifactRepository.findById("12345")).willReturn(Optional.of(a));

        Artifact returnedArtifact = artifactService.findArtifactById("12345");

        assertThat(returnedArtifact.getId()).isEqualTo(a.getId());
        assertThat(returnedArtifact.getName()).isEqualTo(a.getName());
        assertThat(returnedArtifact.getDescription()).isEqualTo(a.getDescription());
        assertThat(returnedArtifact.getImageUrl()).isEqualTo(a.getImageUrl());
        assertThat(returnedArtifact.getOwner()).isEqualTo(a.getOwner());
        verify(artifactRepository, times(1)).findById("12345");

    }

    @Test
    void testFindByIdNotFound() {
        // Given
        given(artifactRepository.findById(Mockito.any(String.class))).willReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> {
            Artifact returnedArtifact = artifactService.findArtifactById("12345");
        });

        assertThat(thrown)
                .isInstanceOf(ArtifactNotFoundException.class)
                .hasMessage("Could not found artifact with Id 12345 :(");


        verify(artifactRepository, times(1)).findById("12345");
    }

    @Test
    void testFindAllSuccess() {
        // Given

        given(artifactRepository.findAll()).willReturn(artifacts);

        // When
        List<Artifact> actualArtifacts = artifactService.findAll();

        // Then
        assertThat(actualArtifacts.size()).isEqualTo(artifacts.size());

        verify(artifactRepository, times(1)).findAll();

    }


    @Test
    void testSaveSuccess() {
        Artifact newArtifact = new Artifact();
        newArtifact.setName("Artifact 3");
        newArtifact.setDescription("Description");
        newArtifact.setImageUrl("imageUrl");

        given(idWorker.nextId()).willReturn(123456L);
        given(artifactRepository.save(newArtifact)).willReturn(newArtifact);

        Artifact savedArtifact = artifactService.save(newArtifact);

        assertThat(savedArtifact.getId()).isEqualTo("123456");
        assertThat(savedArtifact.getName()).isEqualTo(newArtifact.getName());
        assertThat(savedArtifact.getDescription()).isEqualTo(newArtifact.getDescription());
        assertThat(savedArtifact.getImageUrl()).isEqualTo(newArtifact.getImageUrl());

        verify(artifactRepository, times(1)).save(newArtifact);
    }

    @Test
    void testUpdateSuccess() {
        Artifact oldArtifact = new Artifact();
        oldArtifact.setId("1234");
        oldArtifact.setName("Artifact 3");
        oldArtifact.setDescription("Description");
        oldArtifact.setImageUrl("imageUrl");

        Artifact update = new Artifact();
        update.setId("1234");
        update.setName("Artifact 3");
        update.setDescription(" new Description");
        update.setImageUrl("imageUrl");

        given(artifactRepository.findById("1234")).willReturn(Optional.of(oldArtifact));
        given(artifactRepository.save(oldArtifact)).willReturn(oldArtifact);

        Artifact updatedArtifact = artifactService.update("1234", update);

        assertThat(updatedArtifact.getId()).isEqualTo(update.getId());
        assertThat(updatedArtifact.getDescription()).isEqualTo(update.getDescription());

        verify(artifactRepository, times(1)).findById(oldArtifact.getId());
        verify(artifactRepository, times(1)).save(oldArtifact);


    }

    @Test
    void testUpdateNotFound() {


        Artifact update = new Artifact();
        update.setId("1234");
        update.setName("Artifact 3");
        update.setDescription(" new Description");
        update.setImageUrl("imageUrl");

        given(artifactRepository.findById(Mockito.any(String.class))).willReturn(Optional.empty());


        assertThrows(ArtifactNotFoundException.class, () -> {
            artifactService.update("1234", update);
        });

        verify(artifactRepository, times(1)).findById("1234");
    }

    @Test
    void testDeleteSuccess() {

        Artifact artifact = new Artifact();
        artifact.setId("1");
        artifact.setName("Artifact 3");
        artifact.setDescription("Description");
        artifact.setImageUrl("imageUrl");

        given(artifactRepository.findById("1")).willReturn(Optional.of(artifact));
        doNothing().when(artifactRepository).deleteById("1");

        artifactService.delete("1");


        verify(artifactRepository, times(1)).deleteById("1");

    }

    @Test
    void testDeleteNotFound() {

        given(artifactRepository.findById(Mockito.any(String.class))).willReturn(Optional.empty());

        assertThrows(ArtifactNotFoundException.class, () -> artifactService.delete("2"));


        verify(artifactRepository,times(1)).findById("2");


    }

}