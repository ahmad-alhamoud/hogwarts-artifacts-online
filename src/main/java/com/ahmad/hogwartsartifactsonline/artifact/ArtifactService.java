package com.ahmad.hogwartsartifactsonline.artifact;

import com.ahmad.hogwartsartifactsonline.artifact.dto.ArtifactDto;
import com.ahmad.hogwartsartifactsonline.artifact.utils.IdWorker;
import com.ahmad.hogwartsartifactsonline.client.ai.chat.ChatClient;
import com.ahmad.hogwartsartifactsonline.client.ai.chat.dto.ChatRequest;
import com.ahmad.hogwartsartifactsonline.client.ai.chat.dto.ChatResponse;
import com.ahmad.hogwartsartifactsonline.client.ai.chat.dto.Message;
import com.ahmad.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ArtifactService {

    private final ArtifactRepository artifactRepository;
    private final IdWorker idWorker;
    private final ChatClient chatClient;

    public ArtifactService(ArtifactRepository artifactRepository, IdWorker idWorker, ChatClient chatClient) {
        this.artifactRepository = artifactRepository;
        this.idWorker = idWorker;
        this.chatClient = chatClient;
    }

    @Observed(name = "artifact", contextualName = "findByIdService")
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

    public String summarize(List<ArtifactDto> artifactDtos) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonArray = objectMapper.writeValueAsString(artifactDtos);

        // Prepare the messages for summarizing.
        List<Message> messages = List.of(
                new Message("system", "Your task is to generate a short summary of a given JSON array in at most 100 words. The summary must include the number of artifacts, each artifact's description, and the ownership information. Don't mention that the summary is from a given JSON array."),
                new Message("user", jsonArray)
        );

        ChatRequest chatRequest = new ChatRequest("gpt-4", messages);

        ChatResponse chatResponse = this.chatClient.generate(chatRequest); // Tell chatClient to generate a text summary based on the given chatRequest.

        // Retrieve the AI-generated text and return to the controller.
        return chatResponse.choices().get(0).message().content();
    }

}
