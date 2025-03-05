package com.ahmad.hogwartsartifactsonline.artifact.converter;

import com.ahmad.hogwartsartifactsonline.artifact.Artifact;
import com.ahmad.hogwartsartifactsonline.artifact.dto.ArtifactDto;
import com.ahmad.hogwartsartifactsonline.wizard.converter.WizardToWizardDtoConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ArtifactToArtifactDtoConverter implements Converter<Artifact, ArtifactDto> {

    private final WizardToWizardDtoConverter wizardToWizardDtoConverter;

    public ArtifactToArtifactDtoConverter(WizardToWizardDtoConverter wizardDtoConverter) {
        this.wizardToWizardDtoConverter = wizardDtoConverter;
    }

    @Override
    public ArtifactDto convert(Artifact source) {
        ArtifactDto artifactDto = new ArtifactDto(
                source.getId(),
                source.getName(),
                source.getDescription(),
                source.getImageUrl(),
                source.getOwner() != null
                        ? this.wizardToWizardDtoConverter.convert(source.getOwner()) : null
        );
        return artifactDto;
    }
}
