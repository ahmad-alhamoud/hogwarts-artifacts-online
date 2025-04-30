package com.ahmad.hogwartsartifactsonline.wizard;

import com.ahmad.hogwartsartifactsonline.client.rediscache.RedisCacheClient;
import com.ahmad.hogwartsartifactsonline.system.StatusCode;
import com.ahmad.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import com.ahmad.hogwartsartifactsonline.wizard.dto.WizardDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(WizardController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "dev")
class WizardControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    WizardService wizardService;

    @MockitoBean
    RedisCacheClient redisCacheClient;

    List<WizardDto> wizardDtoList;
    List<Wizard> wizards;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @BeforeEach
    void setUp() {


        wizardDtoList = new ArrayList<>();

        WizardDto wizardDto1 = new WizardDto(1, "Gandalf the Grey", 1);
        WizardDto wizardDto2 = new WizardDto(2, "Merlin Ambrosius", 2);
        WizardDto wizardDto3 = new WizardDto(3, "Albus Dumbledore", 3);
        WizardDto wizardDto4 = new WizardDto(4, "Saruman the White", 4);
        WizardDto wizardDto5 = new WizardDto(5, "Morgana Le Fay", 5);

        wizardDtoList.add(wizardDto1);
        wizardDtoList.add(wizardDto2);
        wizardDtoList.add(wizardDto3);
        wizardDtoList.add(wizardDto4);
        wizardDtoList.add(wizardDto5);

    }


    @Test
    void testFindAllWizard() throws Exception {
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


        given(wizardService.findall()).willReturn(wizards);

        mvc.perform(
                        get(baseUrl + "/wizards")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Wizard Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(wizards.size())))
                .andExpect(jsonPath("$.data.[0].id").value(wizards.get(0).getId()))
                .andExpect(jsonPath("$.data.[0].name").value(wizards.get(0).getName()))
                .andExpect(jsonPath("$.data.[1].id").value(wizards.get(1).getId()))
                .andExpect(jsonPath("$.data.[1].name").value(wizards.get(1).getName()));


    }

    @Test
    void testFindWizardByIdSuccess() throws Exception {

        Wizard wizard = new Wizard();
        wizard.setId(1);
        wizard.setName("Gandalf the Grey");
        given(wizardService.findById(1)).willReturn(wizard);


        mvc.perform(
                        get(baseUrl + "/wizards/1")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find Wizard Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Gandalf the Grey"));

    }

    @Test
    void TestFindWizardByIdNotFound() throws Exception {

        given(wizardService.findById(Mockito.any(Integer.class))).willThrow(new ObjectNotFoundException("wizard", 2));

        mvc.perform(
                        get(baseUrl + "/wizards/2")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find wizard With Id 2 :("))
                .andExpect(jsonPath("$.data").isEmpty());

    }

    @Test
    void testDeleteWizardByIdSuccess() throws Exception {

        doNothing().when(wizardService).deleteWizardById(2);

        mvc.perform(
                        delete(baseUrl + "/wizards/2")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Wizard Success"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteWizardByIdNotFound() throws Exception {

        doThrow(new ObjectNotFoundException("wizard", 5)).when(wizardService).deleteWizardById(5);

        mvc.perform(
                        delete(baseUrl + "/wizards/5")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find wizard With Id 5 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testUpdateWizardByIdSuccess() throws Exception {

        WizardDto wizardDto = new WizardDto(null, "Gandalf the Grey", 10);

        Wizard wizard = new Wizard();
        wizard.setId(1);
        wizard.setName("Gandalf the Grey");


        String json = objectMapper.writeValueAsString(wizardDto);

        given(wizardService.update(eq(1), Mockito.any(Wizard.class))).willReturn(wizard);


        mvc.perform(
                        put(baseUrl + "/wizards/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update Wizard Success"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value(wizardDto.name()));


    }


    @Test
    void testUpdateWizardNotFound() throws Exception {

        WizardDto wizardDto = new WizardDto(1, "Gandalf the Grey", 10);

        given(wizardService.update(eq(1), Mockito.any(Wizard.class)))
                .willThrow(new ObjectNotFoundException("wizard", 1));

        String json = objectMapper.writeValueAsString(wizardDto);

        mvc.perform(
                        put(baseUrl + "/wizards/1")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find wizard With Id 1 :("))
                .andExpect(jsonPath("$.data").isEmpty());

    }

    @Test
    void testAddWizardSuccess() throws Exception {

        WizardDto wizardDto = new WizardDto(null, "Gandalf the Grey", 10);

        String json = objectMapper.writeValueAsString(wizardDto);

        Wizard wizard = new Wizard();
        wizard.setId(4);
        wizard.setName("Gandalf the Grey");

        given(wizardService.add(Mockito.any(Wizard.class))).willReturn(wizard);


        mvc.perform(
                        post(baseUrl + "/wizards")
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Wizard Success"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.name").value(wizardDto.name()));

    }

    @Test
    void testAssignArtifactSuccess() throws Exception {

        doNothing().when(wizardService).assignArtifact(1, "100");

        mvc.perform(
                        put(baseUrl + "/wizards/1/artifacts/100")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Artifact Assignment Success"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testAssignArtifactErrorWithNonExistentWizardId() throws Exception {

        doThrow(new ObjectNotFoundException("wizard", 1)).when(wizardService).assignArtifact(1, "125");

        mvc.perform(
                        put(baseUrl + "/wizards/1/artifacts/125")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find wizard With Id 1 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testAssignArtifactErrorWithNonExistentArtifactId() throws Exception {

        doThrow(new ObjectNotFoundException("artifact", "125")).when(wizardService)
                .assignArtifact(5, "125");

        mvc.perform(
                        put(baseUrl + "/wizards/5/artifacts/125")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find artifact With Id 125 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}