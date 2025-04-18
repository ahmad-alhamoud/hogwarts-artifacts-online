package com.ahmad.hogwartsartifactsonline.artifact;

import com.ahmad.hogwartsartifactsonline.artifact.utils.IdWorker;
import com.ahmad.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import io.micrometer.observation.annotation.Observed;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ArtifactService {

    private final ArtifactRepository artifactRepository;
    private final IdWorker idWorker;

    public ArtifactService(ArtifactRepository artifactRepository, IdWorker idWorker) {
        this.artifactRepository = artifactRepository;
        this.idWorker = idWorker;
    }
    
    @Observed(name = "artifact",contextualName = "findByIdService")
    public Artifact findArtifactById(String artifactId) {
        return artifactRepository.findById(artifactId)
                .orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));
    }

    public List<Artifact> findAll() {
        return artifactRepository.findAll();
    }

    public Artifact save(Artifact newArtifact) {
        newArtifact.setId(idWorker.nextId() + "");
        return artifactRepository.save(newArtifact);
    }

    public Artifact update(String artifactId, Artifact update) {
        Artifact oldArtifact = artifactRepository.findById(artifactId)
                .orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));

        oldArtifact.setName(update.getName());
        oldArtifact.setDescription(update.getDescription());
        oldArtifact.setImageUrl(update.getImageUrl());

        return artifactRepository.save(oldArtifact);
    }

    public void delete(String artifactId) {

        Artifact artifact = artifactRepository.findById(artifactId)
                .orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));

        artifactRepository.deleteById(artifactId);
    }

}
